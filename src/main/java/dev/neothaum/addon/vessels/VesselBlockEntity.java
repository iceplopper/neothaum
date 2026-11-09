package dev.neothaum.addon.vessels;

import dev.neothaum.tiles.TVBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/** A vessel set down: what it holds, kept as the item kept it, until a wand lets it out. */
public class VesselBlockEntity extends TVBlockEntity {
	List<CompoundTag> creatures = new ArrayList<>();
	/** The creature shown turning inside (client), and which saved form it was made from. */
	private @Nullable Entity shown;
	private @Nullable CompoundTag shownFrom;

	public VesselBlockEntity(BlockPos pos, BlockState state) {
		super(Vessels.VESSEL_ENTITY, pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.creatures = new ArrayList<>(input.read("Creatures", CompoundTag.CODEC.listOf()).orElse(List.of()));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("Creatures", CompoundTag.CODEC.listOf(), this.creatures);
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		super.applyImplicitComponents(components);
		this.creatures = new ArrayList<>(components.getOrDefault(Vessels.BOUND, List.of()));
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder builder) {
		super.collectImplicitComponents(builder);
		if (!this.creatures.isEmpty()) {
			builder.set(Vessels.BOUND, List.copyOf(this.creatures));
		}
	}

	public boolean isEmpty() {
		return this.creatures.isEmpty();
	}

	/**
	 * Lets out all it holds, on top of the vessel, as they went in (each with a new identity, so a copied vessel can't make twins of one
	 * creature). A hostile one is slowed for three seconds, time to step back. The vessel stays, empty, to be taken up.
	 */
	public int release(ServerLevel level) {
		int out = 0;
		BlockPos at = this.worldPosition.above();
		for (CompoundTag saved : this.creatures) {
			CompoundTag tag = saved.copy();
			tag.remove("UUID");
			Entity e = EntityType.loadEntityRecursive(tag, level, new EntitySpawnRequest(EntitySpawnReason.TRIGGERED, true), entity -> {
				entity.snapTo(at.getX() + 0.5 + (level.getRandom().nextFloat() - 0.5) * 0.4, at.getY(),
					at.getZ() + 0.5 + (level.getRandom().nextFloat() - 0.5) * 0.4, entity.getYRot(), 0.0F);
				return entity;
			});
			if (e != null && level.addFreshEntity(e)) {
				out++;
				if (e instanceof Enemy && e instanceof LivingEntity living) {
					living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 3));
				}
				level.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH, e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ(), 16,
					0.3, e.getBbHeight() / 3, 0.3, 0.02);
			}
		}
		if (out > 0) {
			level.playSound(null, this.worldPosition, dev.neothaum.registry.TVSounds.JAR, SoundSource.BLOCKS, 0.8F, 0.7F);
		}
		this.creatures.clear();
		this.markForUpdate();
		return out;
	}

	/** The first creature, made (not added to the world) to be drawn inside the vessel; null if empty. Client side. */
	public @Nullable Entity shown(Level level) {
		CompoundTag first = this.creatures.isEmpty() ? null : this.creatures.get(0);
		if (first == null) {
			this.shown = null;
			this.shownFrom = null;
			return null;
		}
		if (this.shown == null || this.shownFrom != first) {
			this.shownFrom = first;
			CompoundTag tag = first.copy();
			tag.remove("UUID");
			try (var reporter = new ProblemReporter.ScopedCollector(dev.neothaum.NeoThaum.LOGGER)) {
				this.shown = EntityType.loadEntityRecursive(
					net.minecraft.world.level.storage.TagValueInput.create(reporter, level.registryAccess(), tag), level,
					new EntitySpawnRequest(EntitySpawnReason.LOAD, true), EntityProcessor.NOP);
			}
		}
		return this.shown;
	}
}
