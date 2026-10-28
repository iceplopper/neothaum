package dev.neothaum.tiles.eldritch;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.entity.TVEntities;
import dev.neothaum.entity.monster.CrimsonClericEntity;
import dev.neothaum.entity.monster.CrimsonKnightEntity;
import dev.neothaum.entity.monster.CultistEntity;
import dev.neothaum.entity.monster.EldritchGuardianEntity;
import dev.neothaum.items.WandItem;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.NodeBlockEntity;
import dev.neothaum.tiles.TVBlockEntity;
import dev.neothaum.api.nodes.NodeType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
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
			// BlockEldritch: the third and fourth eyes wake the guardians
			if (this.eyes >= 2) {
				this.spawner = true;
				this.spawnType = 1;
			}
			this.eyes++;
			// TileEldritchAltar.checkForMaze: start laying out the maze beyond while the eyes go in
			this.checkForMaze();
			if (!player.isCreative()) {
				stack.shrink(1);
			}
			if (this.level != null) {
				this.level.playSound(null, this.worldPosition, TVSounds.CRYSTAL, SoundSource.BLOCKS, 0.2F, 1.0F);
			}
			this.markForUpdate();
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	/**
	 * WandManager.createOculus: with all four eyes in and a sinister node floating just above, a wand strike paying 100 of every primal
	 * opens the Eldritch Portal in the node's place.
	 */
	public InteractionResult activateWand(Player player, ItemStack stack) {
		if (this.eyes != 4 || this.open || !(this.level instanceof ServerLevel sl)) {
			return InteractionResult.PASS;
		}
		BlockPos nodePos = this.worldPosition.above();
		if (!(sl.getBlockEntity(nodePos) instanceof NodeBlockEntity node) || node.getNodeType() != NodeType.DARK) {
			return InteractionResult.PASS;
		}
		if (!this.checkForMaze()) {
			return InteractionResult.PASS;
		}
		WandItem wand = (WandItem) stack.getItem();
		AspectList cost = new AspectList().add(Aspect.AIR, 100).add(Aspect.FIRE, 100).add(Aspect.EARTH, 100).add(Aspect.WATER, 100)
			.add(Aspect.ORDER, 100).add(Aspect.ENTROPY, 100);
		if (!wand.consumeAllVisCrafting(stack, player, cost, true)) {
			return InteractionResult.PASS;
		}
		sl.playSound(null, this.worldPosition, TVSounds.WAND, SoundSource.BLOCKS, 1.0F, 1.0F);
		this.open = true;
		sl.removeBlock(nodePos, false);
		sl.setBlockAndUpdate(nodePos, TVBlocks.ELDRITCH_PORTAL.defaultBlockState());
		this.markForUpdate();
		return InteractionResult.SUCCESS;
	}

	/** Whether a maze is ready beyond this altar; if none reaches here, one is started (and is ready a moment later). */
	public boolean checkForMaze() {
		return this.level == null || this.level.isClientSide()
			|| dev.neothaum.world.outer.MazeHandler.checkForMaze(this.worldPosition.getX() >> 4, this.worldPosition.getZ() >> 4, this.level.getRandom());
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

	/** TileEldritchAltar.spawnClerics: four ritualist clerics at the corners, leashed within 8; done once at least three appear. */
	private void spawnClerics() {
		if (!(this.level instanceof ServerLevel sl)) {
			return;
		}
		int success = 0;
		for (int[] off : new int[][]{{-2, -2}, {-2, 2}, {2, -2}, {2, 2}}) {
			BlockPos cp = this.worldPosition.offset(off[0], 0, off[1]);
			CrimsonClericEntity cleric = TVEntities.CULTIST_CLERIC.create(sl, EntitySpawnReason.EVENT);
			if (cleric == null || !sl.getBlockState(cp.below()).isFaceSturdy(sl, cp.below(), net.minecraft.core.Direction.UP)) {
				continue;
			}
			cleric.snapTo(cp.getX() + 0.5, cp.getY(), cp.getZ() + 0.5, 0, 0);
			if (sl.noCollision(cleric) && !sl.containsAnyLiquid(cleric.getBoundingBox())) {
				cleric.setHomeTo(this.worldPosition, 8);
				cleric.finalizeSpawn(sl, sl.getCurrentDifficultyAt(cp), EntitySpawnReason.EVENT, null);
				cleric.spawnAnim();
				if (sl.addFreshEntity(cleric)) {
					success++;
					cleric.setRitualist(true);
				}
			}
		}
		if (success > 2) {
			this.spawnedClerics = true;
			this.setChanged();
		}
	}

	/** TileEldritchAltar.spawnGuards: while a cleric lives within 24, a cultist knight joins as long as there are fewer than 8 cultists. */
	private void spawnGuards() {
		if (!(this.level instanceof ServerLevel sl)) {
			return;
		}
		AABB area = new AABB(this.worldPosition).inflate(24.0, 16.0, 24.0);
		if (sl.getEntitiesOfClass(CrimsonClericEntity.class, area).isEmpty()) {
			this.spawner = false;
			return;
		}
		if (sl.getEntitiesOfClass(CultistEntity.class, area).size() < 8) {
			CrimsonKnightEntity knight = TVEntities.CULTIST_KNIGHT.create(sl, EntitySpawnReason.EVENT);
			BlockPos at = this.defenderSpot(sl);
			if (knight != null && at != null) {
				knight.snapTo(at.getX(), at.getY(), at.getZ(), 0, 0);
				if (sl.noCollision(knight) && !sl.containsAnyLiquid(knight.getBoundingBox())) {
					knight.finalizeSpawn(sl, sl.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT, null);
					knight.spawnAnim();
					knight.setHomeTo(this.worldPosition, 16);
					sl.addFreshEntity(knight);
				}
			}
		}
	}

	/** TileEldritchAltar.spawnGuardian: every two seconds an eldritch guardian, leashed within 16, wherever one can stand. */
	private void spawnGuardians() {
		if (!(this.level instanceof ServerLevel sl)) {
			return;
		}
		EldritchGuardianEntity guardian = TVEntities.ELDRITCH_GUARDIAN.create(sl, EntitySpawnReason.EVENT);
		BlockPos at = this.defenderSpot(sl);
		if (guardian != null && at != null) {
			guardian.snapTo(at.getX(), at.getY(), at.getZ(), 0, 0);
			if (guardian.checkSpawnObstruction(sl)) {
				guardian.finalizeSpawn(sl, sl.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT, null);
				guardian.spawnAnim();
				guardian.setHomeTo(this.worldPosition, 16);
				sl.addFreshEntity(guardian);
			}
		}
	}

	/** 4 to 10 blocks out on each axis (or none), 0 to 3 up or down, standing on something solid; null if not. */
	private BlockPos defenderSpot(ServerLevel sl) {
		var rand = sl.getRandom();
		int x = this.worldPosition.getX() + net.minecraft.util.Mth.randomBetweenInclusive(rand, 4, 10) * net.minecraft.util.Mth.randomBetweenInclusive(rand, -1, 1);
		int y = this.worldPosition.getY() + net.minecraft.util.Mth.randomBetweenInclusive(rand, 0, 3) * net.minecraft.util.Mth.randomBetweenInclusive(rand, -1, 1);
		int z = this.worldPosition.getZ() + net.minecraft.util.Mth.randomBetweenInclusive(rand, 4, 10) * net.minecraft.util.Mth.randomBetweenInclusive(rand, -1, 1);
		BlockPos at = new BlockPos(x, y, z);
		return sl.getBlockState(at.below()).isFaceSturdy(sl, at.below(), net.minecraft.core.Direction.UP) ? at : null;
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
