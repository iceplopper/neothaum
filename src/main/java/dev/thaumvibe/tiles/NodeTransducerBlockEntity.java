package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.fx.FX;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * TileNodeConverter, the node transducer: hung over an aura node that sits on a node stabilizer, a redstone signal slowly drains the node and,
 * after 1000 ticks, turns it into an energized node. Powering it over an energized node turns it back, but without the stabilizer it explodes.
 */
public class NodeTransducerBlockEntity extends TVBlockEntity {
	public int count = -1;
	/** 0 idle, 1 energizing, 2 energized. */
	public int status;

	public NodeTransducerBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.NODE_TRANSDUCER, pos, state);
	}

	private boolean powered(BlockPos p) {
		return this.level.hasNeighborSignal(p);
	}

	private boolean hasStabilizer() {
		BlockPos s = this.worldPosition.below(2);
		return !this.powered(s) && this.level.getBlockEntity(s) instanceof NodeStabilizerBlockEntity;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, NodeTransducerBlockEntity be) {
		if (be.count == -1) {
			be.checkStatus();
		}
		BlockPos below = pos.below();
		if (be.status == 1 && !level.isClientSide() && be.count >= 1000 && level.getBlockEntity(below) instanceof NodeBlockEntity node) {
			AspectList base = node.getAspectsBase().copy();
			var type = node.getNodeType();
			var mod = node.getNodeModifier();
			level.setBlock(below, TVBlocks.ENERGIZED_AURA_NODE.defaultBlockState(), 3);
			if (level.getBlockEntity(below) instanceof EnergizedNodeBlockEntity en) {
				en.setNodeModifier(mod);
				en.setNodeType(type);
				en.setAuraBase(base);
				en.setupNode();
			}
			be.checkStatus();
			level.blockEvent(pos, state.getBlock(), 10, 10);
			be.markForUpdate();
		}
		if (be.status == 2 && !level.isClientSide() && be.count <= 50 && level.getBlockEntity(below) instanceof EnergizedNodeBlockEntity en) {
			AspectList base = en.getAuraBase().copy();
			var type = en.getNodeType();
			var mod = en.getNodeModifier();
			level.setBlock(below, TVBlocks.AURA_NODE.defaultBlockState(), 3);
			if (level.getBlockEntity(below) instanceof NodeBlockEntity node) {
				node.setNodeModifier(mod);
				node.setNodeType(type);
				node.setAspects(base);
				for (Aspect a : node.getAspects().getAspects()) {
					node.takeFromContainer(a, node.getAspects().getAmount(a));
				}
			}
			level.blockEvent(pos, state.getBlock(), 10, 10);
			be.markForUpdate();
			be.status = 0;
		}
		var r = level.getRandom();
		if (be.status != 0 && be.powered(pos)) {
			if (be.count < 1000) {
				be.count++;
				if (!level.isClientSide() && level.getBlockEntity(below) instanceof NodeBlockEntity nd) {
					Aspect[] as = nd.getAspects().getAspects();
					if (as.length > 0) {
						nd.takeFromContainer(as[r.nextInt(as.length)], 1);
						if (be.count % 5 == 0 || nd.getAspects().visSize() == 0) {
							nd.markForUpdate();
						}
					}
				}
				if (be.count > 50 && level.isClientSide()) {
					be.bolts(level, pos);
				}
			}
		} else if (be.count > 0) {
			be.count--;
			if (be.count > 50 && level.isClientSide()) {
				be.bolts(level, pos);
			}
		}
		if (be.count > 1000) {
			be.count = 1000;
		}
	}

	private void bolts(Level level, BlockPos pos) {
		var r = level.getRandom();
		if (r.nextBoolean()) {
			FX.nodeBolt(level, pos.getX() + 0.25F + r.nextFloat() * 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.25F + r.nextFloat() * 0.5F, pos.getX() + 0.5F,
				pos.getY() - 0.5F, pos.getZ() + 0.5F);
		}
		if (r.nextBoolean() && this.hasStabilizer()) {
			FX.nodeBolt(level, pos.getX() + 0.25F + r.nextFloat() * 0.5F, pos.getY() - 1.5F, pos.getZ() + 0.25F + r.nextFloat() * 0.5F,
				pos.getX() + 0.5F, pos.getY() - 0.5F, pos.getZ() + 0.5F);
		}
	}

	public void checkStatus() {
		if (this.count == -1) {
			this.count = 0;
		}
		BlockState below = this.level.getBlockState(this.worldPosition.below());
		boolean energized = below.is(TVBlocks.ENERGIZED_AURA_NODE);
		if (this.status != 2 || this.count <= 50 || this.hasStabilizer() && energized) {
			if (this.powered(this.worldPosition) && below.is(TVBlocks.AURA_NODE) && this.hasStabilizer()) {
				this.status = 1;
				this.markForUpdate();
			} else if (energized) {
				this.status = 2;
				this.count = 1000;
				this.markForUpdate();
			} else {
				this.status = 0;
			}
		} else {
			explodify(this.level, this.worldPosition.below());
			this.status = 0;
			this.count = 50;
			this.markForUpdate();
		}
	}

	/** BlockAiry.explodify: the node bursts, leaving flux goo below and flux gas above. */
	public static void explodify(Level level, BlockPos pos) {
		if (!(level instanceof ServerLevel sl)) {
			return;
		}
		sl.removeBlock(pos, false);
		sl.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0F, false, Level.ExplosionInteraction.BLOCK);
		var r = sl.getRandom();
		for (int a = 0; a < 50; a++) {
			BlockPos p = pos.offset(r.nextInt(8) - r.nextInt(8), r.nextInt(8) - r.nextInt(8), r.nextInt(8) - r.nextInt(8));
			if (sl.isEmptyBlock(p)) {
				var b = p.getY() < pos.getY() ? TVBlocks.FLUX_GOO : TVBlocks.FLUX_GAS;
				sl.setBlock(p, ((dev.thaumvibe.blocks.FluxFluidBlock) b).fullState(), 3);
			}
		}
	}

	@Override
	public boolean triggerEvent(int id, int param) {
		if (id == 10 && param == 10) {
			if (this.level != null && this.level.isClientSide()) {
				FX.burst(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() - 0.5, this.worldPosition.getZ() + 0.5, 1.0F);
				this.level.playLocalSound(this.worldPosition.below(), TVSounds.CRAFTFAIL, SoundSource.BLOCKS, 0.5F, 1.0F, false);
			}
			return true;
		}
		return super.triggerEvent(id, param);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.status = input.getIntOr("status", 0);
		this.count = input.getIntOr("count", -1);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("status", this.status);
		output.putInt("count", this.count);
	}
}
