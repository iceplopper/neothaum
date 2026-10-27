package dev.neothaum.tiles.eldritch;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.entity.TVEntities;
import dev.neothaum.entity.monster.CrimsonClericEntity;
import dev.neothaum.entity.monster.CrimsonKnightEntity;
import dev.neothaum.entity.monster.EldritchGuardianEntity;
import dev.neothaum.items.WandItem;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.TVBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * 1:1 Port of TileEldritchAltar.
 * Accepts 4 Eldritch Eyes, spawns cultist/guardian defenders, and unlocks the Eldritch Portal via wand strike.
 */
public class EldritchAltarBlockEntity extends TVBlockEntity {
	private byte eyes = 0;
	private boolean open = false;
	private boolean spawner = false;
	private byte spawnType = 0; // 0 = cultists, 1 = guardians
	private boolean spawnedClerics = false;
	private int counter = 0;

	public EldritchAltarBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ELDRITCH_ALTAR, pos, state);
	}

	public byte getEyes() {
		return this.eyes;
	}

	public void setEyes(byte eyes) {
		this.eyes = eyes;
		this.markForUpdate();
	}

	public boolean isOpen() {
		return this.open;
	}

	public void setOpen(boolean open) {
		this.open = open;
		this.markForUpdate();
	}

	public boolean isSpawner() {
		return this.spawner;
	}

	public void setSpawner(boolean spawner) {
		this.spawner = spawner;
	}

	public byte getSpawnType() {
		return this.spawnType;
	}

	public void setSpawnType(byte spawnType) {
		this.spawnType = spawnType;
	}

	public InteractionResult insertEye(Player player, ItemStack stack) {
		if (this.eyes < 4) {
			this.eyes++;
			if (!player.isCreative()) {
				stack.shrink(1);
			}
			if (this.eyes >= 2 && !this.spawner) {
				this.spawner = true;
				this.spawnType = 1;
			}
			if (this.level != null) {
				this.level.playSound(null, this.worldPosition, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
			this.markForUpdate();
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public InteractionResult activateWand(Player player, ItemStack stack) {
		if (this.eyes == 4 && !this.open && this.level != null && !this.level.isClientSide()) {
			WandItem wand = (WandItem) stack.getItem();
			AspectList cost = new AspectList()
				.add(Aspect.AIR, 100)
				.add(Aspect.EARTH, 100)
				.add(Aspect.FIRE, 100)
				.add(Aspect.WATER, 100)
				.add(Aspect.ORDER, 100)
				.add(Aspect.ENTROPY, 100);

			if (player.isCreative() || wand.consumeAllVisCrafting(stack, player, cost, true)) {
				this.open = true;
				this.spawner = false;
				ServerLevel sl = (ServerLevel) this.level;
				sl.playSound(null, this.worldPosition, TVSounds.WAND, SoundSource.BLOCKS, 1.0F, 1.0F);

				// Lightning strike
				LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(sl, EntitySpawnReason.EVENT);
				if (bolt != null) {
					bolt.snapTo(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0, this.worldPosition.getZ() + 0.5, 0, 0);
					sl.addFreshEntity(bolt);
				}

				// Place Eldritch Portal directly above altar
				BlockPos portalPos = this.worldPosition.above();
				sl.setBlockAndUpdate(portalPos, TVBlocks.ELDRITCH_PORTAL.defaultBlockState());
				this.markForUpdate();
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, EldritchAltarBlockEntity altar) {
		if (level.isClientSide() || !altar.spawner) {
			return;
		}
		altar.counter++;
		if (altar.counter >= 80 && altar.counter % 40 == 0) {
			if (altar.spawnType == 0) {
				if (!altar.spawnedClerics) {
					altar.spawnClerics();
				} else {
					altar.spawnGuards();
				}
			} else if (altar.spawnType == 1) {
				altar.spawnGuardians();
			}
		}
	}

	private void spawnClerics() {
		if (!(this.level instanceof ServerLevel sl)) return;
		int[][] offsets = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
		int success = 0;
		for (int[] off : offsets) {
			BlockPos cp = this.worldPosition.offset(off[0], 0, off[1]);
			CrimsonClericEntity cleric = TVEntities.CULTIST_CLERIC.create(sl, EntitySpawnReason.EVENT);
			if (cleric != null) {
				cleric.snapTo(cp.getX() + 0.5, cp.getY(), cp.getZ() + 0.5, 0, 0);
				cleric.setRitualist(true);
				if (sl.addFreshEntity(cleric)) {
					success++;
				}
			}
		}
		if (success > 0) {
			this.spawnedClerics = true;
			this.markForUpdate();
		}
	}

	private void spawnGuards() {
		if (!(this.level instanceof ServerLevel sl)) return;
		AABB area = new AABB(this.worldPosition).inflate(24.0, 16.0, 24.0);
		List<CrimsonClericEntity> clerics = sl.getEntitiesOfClass(CrimsonClericEntity.class, area);
		if (clerics.isEmpty()) {
			this.spawner = false;
			return;
		}
		List<CrimsonKnightEntity> knights = sl.getEntitiesOfClass(CrimsonKnightEntity.class, area);
		if (knights.size() < 6) {
			CrimsonKnightEntity knight = TVEntities.CULTIST_KNIGHT.create(sl, EntitySpawnReason.EVENT);
			if (knight != null) {
				int rx = sl.getRandom().nextInt(12) - 6;
				int rz = sl.getRandom().nextInt(12) - 6;
				knight.snapTo(this.worldPosition.getX() + rx + 0.5, this.worldPosition.getY(), this.worldPosition.getZ() + rz + 0.5, sl.getRandom().nextFloat() * 360, 0);
				sl.addFreshEntity(knight);
			}
		}
	}

	private void spawnGuardians() {
		if (!(this.level instanceof ServerLevel sl)) return;
		AABB area = new AABB(this.worldPosition).inflate(24.0, 16.0, 24.0);
		List<EldritchGuardianEntity> guards = sl.getEntitiesOfClass(EldritchGuardianEntity.class, area);
		if (guards.size() < 3) {
			EldritchGuardianEntity guardian = TVEntities.ELDRITCH_GUARDIAN.create(sl, EntitySpawnReason.EVENT);
			if (guardian != null) {
				int rx = sl.getRandom().nextInt(10) - 5;
				int rz = sl.getRandom().nextInt(10) - 5;
				guardian.snapTo(this.worldPosition.getX() + rx + 0.5, this.worldPosition.getY(), this.worldPosition.getZ() + rz + 0.5, sl.getRandom().nextFloat() * 360, 0);
				sl.addFreshEntity(guardian);
			}
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.eyes = input.getByteOr("eyes", (byte) 0);
		this.open = input.getBooleanOr("open", false);
		this.spawner = input.getBooleanOr("spawner", false);
		this.spawnType = input.getByteOr("spawntype", (byte) 0);
		this.spawnedClerics = input.getBooleanOr("spawnedClerics", false);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putByte("eyes", this.eyes);
		output.putBoolean("open", this.open);
		output.putBoolean("spawner", this.spawner);
		output.putByte("spawntype", this.spawnType);
		output.putBoolean("spawnedClerics", this.spawnedClerics);
	}
}
