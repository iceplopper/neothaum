package dev.thaumvibe.entity.monster;

import dev.thaumvibe.blocks.TaintFibreBlock;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.world.TVBiomes;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * EntityTaintSheep: a tainted sheep that grazes like any other, except that what it eats turns to taint fibres and the land round it to
 * tainted land. Its wool shears off purple.
 */
public class TaintSheep extends TaintedBeast implements Shearable {
	private static final EntityDataAccessor<Boolean> SHEARED = SynchedEntityData.defineId(TaintSheep.class, EntityDataSerializers.BOOLEAN);
	private ConvertGrassGoal convertGrass;
	private int sheepTimer;

	public TaintSheep(EntityType<? extends TaintSheep> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.MOVEMENT_SPEED, 0.25)
			.add(Attributes.ARMOR, 1.0);
	}

	@Override
	protected boolean huntsAnimals() {
		return false;
	}

	@Override
	protected void registerGoals() {
		this.convertGrass = new ConvertGrassGoal();
		this.goalSelector.addGoal(2, this.convertGrass);
		super.registerGoals();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(SHEARED, false);
	}

	public boolean isSheared() {
		return this.entityData.get(SHEARED);
	}

	public void setSheared(boolean sheared) {
		this.entityData.set(SHEARED, sheared);
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		this.sheepTimer = this.convertGrass.timer;
		super.customServerAiStep(level);
	}

	@Override
	public void aiStep() {
		if (this.level().isClientSide()) {
			this.sheepTimer = Math.max(0, this.sheepTimer - 1);
		}
		super.aiStep();
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 10) {
			this.sheepTimer = 40;
		} else {
			super.handleEntityEvent(id);
		}
	}

	/** func_44003_c: how far the head has dipped to graze. */
	public float getHeadEatPositionScale(float a) {
		if (this.sheepTimer <= 0) {
			return 0.0F;
		}
		if (this.sheepTimer >= 4 && this.sheepTimer <= 36) {
			return 1.0F;
		}
		return this.sheepTimer < 4 ? (this.sheepTimer - a) / 4.0F : -(this.sheepTimer - 40 - a) / 4.0F;
	}

	/** func_44002_d: the head's nodding while it grazes. */
	public float getHeadEatAngleScale(float a) {
		if (this.sheepTimer > 4 && this.sheepTimer <= 36) {
			float f = (this.sheepTimer - 4 - a) / 32.0F;
			return (float) (Math.PI / 5) + 0.2199115F * Mth.sin(f * 28.7F);
		}
		return this.sheepTimer > 0 ? (float) (Math.PI / 5) : this.getXRot() * (float) (Math.PI / 180.0);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.is(Items.SHEARS)) {
			if (this.level() instanceof ServerLevel level && this.readyForShearing()) {
				this.shear(level, SoundSource.PLAYERS, stack);
				stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
				return InteractionResult.SUCCESS_SERVER;
			}
			return InteractionResult.CONSUME;
		}
		return super.mobInteract(player, hand);
	}

	@Override
	public boolean readyForShearing() {
		return this.isAlive() && !this.isSheared();
	}

	@Override
	public void shear(ServerLevel level, SoundSource source, ItemStack tool) {
		level.playSound(null, this, SoundEvents.SHEEP_SHEAR, source, 1.0F, 1.0F);
		this.setSheared(true);
		int i = 1 + this.random.nextInt(3);
		for (int j = 0; j < i; j++) {
			this.spawnAtLocation(level, new ItemStack(Blocks.WOOL.purple()), 1.0F);
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SHEEP_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.SHEEP_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SHEEP_AMBIENT;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(SoundEvents.SHEEP_STEP, 0.15F, 1.0F);
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		this.dropTaint(level, 3);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putBoolean("Sheared", this.isSheared());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setSheared(input.getBooleanOr("Sheared", false));
	}

	/** AIConvertGrass: graze, and leave taint where the grass was. */
	private class ConvertGrassGoal extends Goal {
		int timer;

		ConvertGrassGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
		}

		@Override
		public boolean canUse() {
			TaintSheep s = TaintSheep.this;
			if (s.getRandom().nextInt(250) != 0) {
				return false;
			}
			BlockPos p = s.blockPosition();
			return s.level().getBlockState(p).is(Blocks.SHORT_GRASS) || s.level().getBlockState(p.below()).is(Blocks.GRASS_BLOCK);
		}

		@Override
		public void start() {
			this.timer = 40;
			TaintSheep.this.level().broadcastEntityEvent(TaintSheep.this, (byte) 10);
			TaintSheep.this.getNavigation().stop();
		}

		@Override
		public void stop() {
			this.timer = 0;
		}

		@Override
		public boolean canContinueToUse() {
			return this.timer > 0;
		}

		@Override
		public void tick() {
			this.timer = Math.max(0, this.timer - 1);
			if (this.timer != 4 || !(TaintSheep.this.level() instanceof ServerLevel level)) {
				return;
			}
			BlockPos p = TaintSheep.this.blockPosition();
			TaintFibreBlock fibres = (TaintFibreBlock) TVBlocks.TAINT_FIBRES;
			if (level.getBlockState(p).is(Blocks.SHORT_GRASS)) {
				level.levelEvent(2001, p, Block.getId(Blocks.SHORT_GRASS.defaultBlockState()));
				TVBiomes.setBiomeAt(level, p.getX(), p.getZ(), TVBiomes.TAINTED_LAND);
				level.setBlock(p, fibres.ofType(level, p, 0), Block.UPDATE_ALL);
				TaintSheep.this.ate();
			} else if (level.getBlockState(p.below()).is(Blocks.GRASS_BLOCK)) {
				level.levelEvent(2001, p.below(), Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
				TVBiomes.setBiomeAt(level, p.getX(), p.getZ(), TVBiomes.TAINTED_LAND);
				level.setBlock(p, fibres.ofType(level, p, 0), Block.UPDATE_ALL);
				TaintSheep.this.ate();
			}
		}
	}
}
