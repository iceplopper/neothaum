package dev.neothaum.tiles.eldritch;

import dev.neothaum.blocks.TaintFibreBlock;
import dev.neothaum.entity.TVEntities;
import dev.neothaum.entity.champion.ChampionEvents;
import dev.neothaum.entity.monster.EldritchGolemEntity;
import dev.neothaum.entity.monster.EldritchWardenEntity;
import dev.neothaum.entity.monster.Taintacle;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.tiles.TVBlockEntity;
import dev.neothaum.world.TVBiomes;
import dev.neothaum.world.outer.Cell;
import dev.neothaum.world.outer.MazeHandler;
import dev.neothaum.world.outer.OuterBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileEldritchLock: once a Runed Tablet is fitted (count from 0), it pumps for five seconds, then wakes the boss room it guards: the next of
 * MapBossData's four bosses (eldritch golem, eldritch warden, crimson portal, a nest of champion taintacles) with its arena, and the
 * impassable wall in the doorway and the lock itself vanish.
 */
public class AncientLockBlockEntity extends TVBlockEntity {
	/** The pedestal of stairs around a boss's dais (stair metadata; -1 the glowing stone in the middle). */
	private static final int[][] PED = {{2, 2, 2}, {0, -1, 1}, {3, 3, 3}};
	public int count = -1;

	public AncientLockBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ANCIENT_LOCK, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, AncientLockBlockEntity lock) {
		if (lock.count == -1) {
			return;
		}
		lock.count++;
		if (lock.count % 5 == 0 && !level.isClientSide()) {
			level.playSound(null, pos, TVSounds.PUMP, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
		if (lock.count > 100 && level instanceof ServerLevel sl) {
			lock.doBossSpawn(sl);
		}
	}

	private void doBossSpawn(ServerLevel level) {
		level.playSound(null, this.worldPosition, TVSounds.ICE, SoundSource.BLOCKS, 1.0F, 1.0F);
		int cx = this.worldPosition.getX() >> 4;
		int cz = this.worldPosition.getZ() >> 4;
		int centerx = cx;
		int centerz = cz;
		int exit = 0;
		for (int a = -2; a <= 2; a++) {
			for (int b = -2; b <= 2; b++) {
				Cell c = MazeHandler.getCell(cx + a, cz + b);
				if (c != null && c.feature == 2) {
					centerx = cx + a;
					centerz = cz + b;
				}
				if (c != null && c.feature >= 2 && c.feature <= 5 && (c.north || c.south || c.east || c.west)) {
					exit = c.feature;
				}
			}
		}
		switch (MazeHandler.nextBoss(level.getRandom())) {
			case 0 -> this.spawnGolemBossRoom(level, centerx, centerz, exit);
			case 1 -> this.spawnWardenBossRoom(level, centerx, centerz, exit);
			case 2 -> this.spawnCultistBossRoom(level, centerx, centerz);
			default -> this.spawnTaintBossRoom(level, centerx, centerz);
		}
		// the doorway's invisible wall falls away
		for (BlockPos p : BlockPos.betweenClosed(this.worldPosition.offset(-2, -2, -2), this.worldPosition.offset(2, 2, 2))) {
			if (level.getBlockState(p).is(TVBlocks.IMPASSABLE)) {
				dev.neothaum.network.TVNetwork.sendBlockSparkle(level, p.immutable(), 4194368);
				level.removeBlock(p, false);
			}
		}
		level.removeBlock(this.worldPosition, false);
	}

	private void announce(ServerLevel level, String key) {
		for (var p : level.players()) {
			if (p.distanceToSqr(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()) < 300.0) {
				p.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
			}
		}
	}

	private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
		level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_ALL);
	}

	private static void genObelisk(ServerLevel level, int x, int y, int z) {
		set(level, x, y, z, OuterBlocks.obeliskBase());
		for (int i = 1; i <= 4; i++) {
			set(level, x, y + i, z, OuterBlocks.obeliskShaft());
		}
	}

	private static int lootMeta(RandomSource rand, float rare, float uncommon) {
		float rr = rand.nextFloat();
		return rr < rare ? 2 : rr < uncommon ? 1 : 0;
	}

	/** The boss on its dais faces the lock, and keeps to its room. */
	private <T extends Mob> T placeBoss(ServerLevel level, EntityType<T> type, double x, double y, double z, boolean finalize) {
		T boss = type.create(level, EntitySpawnReason.EVENT);
		if (boss == null) {
			return null;
		}
		double d0 = this.worldPosition.getX() - x;
		double d1 = this.worldPosition.getY() - (y + boss.getEyeHeight());
		double d2 = this.worldPosition.getZ() - z;
		double d3 = Math.sqrt(d0 * d0 + d2 * d2);
		float yaw = (float) (Math.atan2(d2, d0) * 180.0 / Math.PI) - 90.0F;
		float pitch = (float) -(Math.atan2(d1, d3) * 180.0 / Math.PI);
		boss.snapTo(x, y, z, yaw, pitch);
		if (finalize) {
			boss.finalizeSpawn(level, level.getCurrentDifficultyAt(boss.blockPosition()), EntitySpawnReason.EVENT, null);
		}
		return boss;
	}

	private void spawnWardenBossRoom(ServerLevel level, int cx, int cz, int exit) {
		this.announce(level, "tc.boss.warden");
		RandomSource rand = level.getRandom();
		int x = cx * 16 + 16;
		int y = MazeHandler.Y;
		int z = cz * 16 + 16;
		int x2 = x;
		int z2 = z;
		switch (exit) {
			case 2 -> {
				x2 = x + 8;
				z2 = z + 8;
			}
			case 3 -> {
				x2 = x - 8;
				z2 = z + 8;
			}
			case 4 -> {
				x2 = x + 8;
				z2 = z - 8;
			}
			case 5 -> {
				x2 = x - 8;
				z2 = z - 8;
			}
			default -> {
			}
		}
		genObelisk(level, x2, y + 4, z);
		genObelisk(level, x, y + 4, z2);
		set(level, x2, y + 2, z, TVBlocks.ELDRITCH_CAP.defaultBlockState());
		set(level, x, y + 2, z2, TVBlocks.ELDRITCH_CAP.defaultBlockState());
		for (int a = -1; a <= 1; a++) {
			for (int b = -1; b <= 1; b++) {
				if (a != 0 && b != 0 && rand.nextFloat() < 0.9F) {
					set(level, x2 + a, y + 2, z + b, OuterBlocks.urn(lootMeta(rand, 0.1F, 0.3F)));
				}
				if (a != 0 && b != 0 && rand.nextFloat() < 0.9F) {
					set(level, x + a, y + 2, z2 + b, OuterBlocks.urn(lootMeta(rand, 0.1F, 0.3F)));
				}
			}
		}
		for (int[] c : new int[][]{{-2, -2}, {-2, 2}, {2, 2}, {2, -2}}) {
			set(level, x + c[0], y + 3, z + c[1], TVBlocks.RUNED_STONE.defaultBlockState());
			set(level, x + c[0], y + 2, z + c[1], TVBlocks.ANCIENT_PEDESTAL.defaultBlockState());
		}
		this.pedestal(level, x2, y + 2, z2);
		EldritchWardenEntity boss = this.placeBoss(level, TVEntities.ELDRITCH_WARDEN, x2 + 0.5, y + 3, z2 + 0.5, true);
		if (boss != null) {
			boss.setHomeTo(new BlockPos(x, y + 2, z), 32);
			level.addFreshEntity(boss);
		}
	}

	private void pedestal(ServerLevel level, int x, int y, int z) {
		for (int a = 0; a < 3; a++) {
			for (int b = 0; b < 3; b++) {
				set(level, x - 1 + b, y, z - 1 + a,
					PED[a][b] < 0 ? TVBlocks.GLOWING_CRUSTED_STONE.defaultBlockState() : OuterBlocks.stairs(PED[a][b]));
			}
		}
	}

	private void spawnGolemBossRoom(ServerLevel level, int cx, int cz, int exit) {
		this.announce(level, "tc.boss.golem");
		RandomSource rand = level.getRandom();
		int x = cx * 16 + 16;
		int y = MazeHandler.Y;
		int z = cz * 16 + 16;
		int x2 = 0;
		int z2 = 0;
		switch (exit) {
			case 2 -> {
				x2 = 8;
				z2 = 8;
			}
			case 3 -> {
				x2 = -8;
				z2 = 8;
			}
			case 4 -> {
				x2 = 8;
				z2 = -8;
			}
			case 5 -> {
				x2 = -8;
				z2 = -8;
			}
			default -> {
			}
		}
		genObelisk(level, x + x2, y + 4, z + z2);
		genObelisk(level, x - x2, y + 4, z + z2);
		genObelisk(level, x + x2, y + 4, z - z2);
		set(level, x + x2, y + 2, z + z2, TVBlocks.ELDRITCH_CAP.defaultBlockState());
		set(level, x - x2, y + 2, z + z2, TVBlocks.ELDRITCH_CAP.defaultBlockState());
		set(level, x + x2, y + 2, z - z2, TVBlocks.ELDRITCH_CAP.defaultBlockState());
		this.pedestal(level, x, y + 2, z);
		for (int a = -10; a <= 10; a++) {
			for (int b = -10; b <= 10; b++) {
				if ((a < -2 && b < -2 || a > 2 && b > 2 || a < -2 && b > 2 || a > 2 && b < -2) && rand.nextFloat() < 0.15F
					&& level.isEmptyBlock(new BlockPos(x + a, y + 2, z + b))) {
					int md = lootMeta(rand, 0.05F, 0.2F);
					set(level, x + a, y + 2, z + b, rand.nextFloat() < 0.3F ? OuterBlocks.crate(md) : OuterBlocks.urn(md));
				}
			}
		}
		EldritchGolemEntity boss = this.placeBoss(level, TVEntities.ELDRITCH_GOLEM, x + 0.5, y + 3, z + 0.5, true);
		if (boss != null) {
			level.addFreshEntity(boss);
		}
	}

	private void spawnCultistBossRoom(ServerLevel level, int cx, int cz) {
		this.announce(level, "tc.boss.crimson");
		RandomSource rand = level.getRandom();
		int x = cx * 16 + 16;
		int y = MazeHandler.Y;
		int z = cz * 16 + 16;
		for (int a = -4; a <= 4; a++) {
			for (int b = -4; b <= 4; b++) {
				if ((Math.abs(a) != 2 && Math.abs(b) != 2 || !rand.nextBoolean()) && (Math.abs(a) != 3 && Math.abs(b) != 3 || !(rand.nextFloat() > 0.33F))
					&& (Math.abs(a) != 4 && Math.abs(b) != 4 || !(rand.nextFloat() > 0.25F))) {
					set(level, x + b, y + 1, z + a, TVBlocks.ANCIENT_DOORWAY.defaultBlockState());
				}
			}
		}
		for (int a = 0; a < 5; a++) {
			for (int b = 0; b < 5; b++) {
				if (a == 0 || a == 4 || b == 0 || b == 4) {
					int px = x - 8 + b * 4;
					int pz = z - 8 + a * 4;
					set(level, px, y + 2, pz, TVBlocks.ANCIENT_STONE.defaultBlockState());
					set(level, px, y + 3, pz, TVBlocks.GLYPHED_STONE.defaultBlockState());
					set(level, px, y + 4, pz, OuterBlocks.slab(false));
					set(level, px, y + 10, pz, TVBlocks.ANCIENT_STONE.defaultBlockState());
					set(level, px, y + 9, pz, TVBlocks.GLYPHED_STONE.defaultBlockState());
					set(level, px, y + 8, pz, OuterBlocks.slab(true));
				}
			}
		}
		var boss = TVEntities.CULTIST_PORTAL.create(level, EntitySpawnReason.EVENT);
		if (boss != null) {
			boss.snapTo(x + 0.5, y + 2, z + 0.5, 0.0F, 0.0F);
			level.addFreshEntity(boss);
		}
	}

	private void spawnTaintBossRoom(ServerLevel level, int cx, int cz) {
		this.announce(level, "tc.boss.taint");
		RandomSource rand = level.getRandom();
		int x = cx * 16 + 16;
		int y = MazeHandler.Y;
		int z = cz * 16 + 16;
		for (int a = -12; a <= 12; a++) {
			for (int b = -12; b <= 12; b++) {
				TVBiomes.setBiomeAt(level, x + b, z + a, TVBiomes.TAINTED_LAND);
				for (int c = 0; c < 9; c++) {
					BlockPos p = new BlockPos(x + b, y + 2 + c, z + a);
					if (level.isEmptyBlock(p) && adjacentToSolid(level, p) && rand.nextInt(3) != 0) {
						level.setBlock(p, TVBlocks.TAINT_FIBRES.defaultBlockState().setValue(TaintFibreBlock.TYPE, rand.nextInt(4) == 0 ? 1 : 0),
							Block.UPDATE_ALL);
					}
				}
				if (rand.nextFloat() < 0.15F) {
					set(level, x + b, y + 2, z + a, TVBlocks.CRUSTED_TAINT.defaultBlockState());
					if (rand.nextFloat() < 0.2F) {
						set(level, x + b, y + 3, z + a, TVBlocks.CRUSTED_TAINT.defaultBlockState());
					}
				}
				if ((Math.abs(a) != 4 && Math.abs(b) != 4 || !rand.nextBoolean()) && (Math.abs(a) < 5 && Math.abs(b) < 5 || !(rand.nextFloat() > 0.33F))
					&& (Math.abs(a) < 7 && Math.abs(b) < 7 || !(rand.nextFloat() > 0.25F))) {
					set(level, x + b, y + 1, z + a, TVBlocks.TAINTED_SOIL.defaultBlockState());
				}
			}
		}
		// five champion taintacles, giants among them (the middle one always on hard)
		boolean giant1 = level.getDifficulty() == Difficulty.HARD;
		this.taintacle(level, giant1, x + 0.5, y + 3, z + 0.5);
		boolean giant2 = rand.nextBoolean();
		this.taintacle(level, giant2, x + 3.5, y + 3, z + 3.5);
		this.taintacle(level, !giant2, x - 2.5, y + 3, z + 3.5);
		boolean giant4 = rand.nextBoolean();
		this.taintacle(level, giant4, x + 3.5, y + 3, z - 2.5);
		this.taintacle(level, !giant4, x - 2.5, y + 3, z - 2.5);
	}

	private void taintacle(ServerLevel level, boolean giant, double x, double y, double z) {
		Taintacle t = (giant ? TVEntities.TAINTACLE_GIANT : TVEntities.TAINTACLE).create(level, EntitySpawnReason.EVENT);
		if (t == null) {
			return;
		}
		t.snapTo(x, y, z, 0.0F, 0.0F);
		t.addTag(ChampionEvents.TAG_CHECKED);
		ChampionEvents.makeChampion(t, true);
		level.addFreshEntity(t);
	}

	private static boolean adjacentToSolid(ServerLevel level, BlockPos p) {
		for (Direction d : Direction.values()) {
			BlockPos n = p.relative(d);
			if (level.getBlockState(n).isFaceSturdy(level, n, d.getOpposite())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.count = input.getShortOr("count", (short) -1);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putShort("count", (short) this.count);
	}
}
