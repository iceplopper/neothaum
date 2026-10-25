package dev.thaumvibe.tiles;

import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** TileJarBrain: draws in experience orbs from six blocks away and eats them, up to 2000 points; it watches you, and sighs. */
public class BrainJarBlockEntity extends TVBlockEntity {
	public float rota;
	public float rotb;
	/** field_40066_q, field_40059_f, field_40061_d: where it is looking and how it twitches. */
	private float targetRot;
	private float twitch;
	private float twitchDir;
	public int xp;
	public int xpMax = 2000;
	public int eatDelay;
	private long lastsigh = System.currentTimeMillis() + 1500L;

	public BrainJarBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.BRAIN_JAR, pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.xp = input.getIntOr("XP", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("XP", this.xp);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, BrainJarBlockEntity b) {
		b.xp = Math.min(b.xp, b.xpMax);
		Vec3 c = Vec3.atCenterOf(pos);
		ExperienceOrb orb = null;
		if (b.xp < b.xpMax) {
			orb = b.closestOrb();
			if (orb != null && b.eatDelay == 0) {
				double dx = (c.x - orb.getX()) / 7.0, dy = (c.y - orb.getY()) / 7.0, dz = (c.z - orb.getZ()) / 7.0;
				double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
				double pull = 1.0 - d;
				if (pull > 0.0) {
					pull *= pull;
					orb.setDeltaMovement(orb.getDeltaMovement().add(dx / d * pull * 0.15, dy / d * pull * 0.33, dz / d * pull * 0.15));
				}
			}
		}
		if (level.isClientSide()) {
			b.rotb = b.rota;
			var looking = orb != null ? orb : level.getNearestPlayer(c.x, c.y, c.z, 6.0, false);
			if (orb == null && looking != null && b.lastsigh < System.currentTimeMillis()) {
				level.playLocalSound(pos, TVSounds.BRAIN, SoundSource.BLOCKS, 0.15F, 0.8F + level.getRandom().nextFloat() * 0.4F, false);
				b.lastsigh = System.currentTimeMillis() + 5000L + level.getRandom().nextInt(25000);
			}
			if (looking != null) {
				b.targetRot = (float) Math.atan2(looking.getZ() - c.z, looking.getX() - c.x);
				b.twitch += 0.1F;
				if (b.twitch < 0.5F || level.getRandom().nextInt(40) == 0) {
					float old = b.twitchDir;
					do {
						b.twitchDir += level.getRandom().nextInt(4) - level.getRandom().nextInt(4);
					} while (old == b.twitchDir);
				}
			} else {
				b.targetRot += 0.01F;
			}
			b.rota = Mth.wrapDegrees(b.rota * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
			b.targetRot = Mth.wrapDegrees(b.targetRot * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
			float f = Mth.wrapDegrees((b.targetRot - b.rota) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
			b.rota += f * 0.04F;
		}
		if (b.eatDelay > 0) {
			b.eatDelay--;
		} else if (b.xp < b.xpMax && !level.isClientSide()) {
			List<ExperienceOrb> ents = level.getEntitiesOfClass(ExperienceOrb.class, new AABB(pos).inflate(0.1));
			if (!ents.isEmpty()) {
				for (ExperienceOrb eo : ents) {
					b.xp += eo.getValue();
					level.playSound(null, eo.blockPosition(), SoundEvents.GENERIC_EAT.value(), SoundSource.BLOCKS, 0.1F,
						(level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);
					eo.discard();
				}
				b.markForUpdate();
			}
		}
	}

	private ExperienceOrb closestOrb() {
		Vec3 c = Vec3.atCenterOf(this.worldPosition);
		ExperienceOrb best = null;
		double cdist = Double.MAX_VALUE;
		for (ExperienceOrb eo : this.level.getEntitiesOfClass(ExperienceOrb.class, new AABB(this.worldPosition).inflate(6.0))) {
			double d = eo.distanceToSqr(c);
			if (d < cdist) {
				best = eo;
				cdist = d;
			}
		}
		return best;
	}

	/** BlockJar.onBlockActivated: a click coaxes up to 64 experience back out, and stops it eating for two seconds. */
	public void release(Player player) {
		this.eatDelay = 40;
		if (this.level instanceof net.minecraft.server.level.ServerLevel sl) {
			int amount = sl.getRandom().nextInt(Math.min(this.xp + 1, 64));
			if (amount > 0) {
				this.xp -= amount;
				ExperienceOrb.award(sl, Vec3.atCenterOf(this.worldPosition), amount);
				this.markForUpdate();
			}
		}
	}

	/** breakBlock: everything it held spills out. */
	public void spill() {
		if (this.level instanceof net.minecraft.server.level.ServerLevel sl && this.xp > 0) {
			ExperienceOrb.award(sl, Vec3.atLowerCornerOf(this.worldPosition), this.xp);
			this.xp = 0;
		}
	}
}
