package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.EssentiaTransport;
import dev.thaumvibe.blocks.AbstractLampBlock;
import dev.thaumvibe.blocks.LampOfFertilityBlock;
import dev.thaumvibe.registry.TVBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

/**
 * TileArcaneLampFertility: feeds on Victus essentia (Aspect.LIFE; holds up to 4 charges).
 * Every 300 ticks, breeds a pair of mature animals in a 7-block radius if that animal type count <= 7.
 */
public class ArcaneLampFertilityBlockEntity extends TVBlockEntity implements EssentiaTransport {
	private static final int DISTANCE = 7;
	public int charges = 0;
	private int count = 0;
	private int drawDelay = 0;

	public ArcaneLampFertilityBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.LAMP_OF_FERTILITY, pos, state);
	}

	public Direction getFacing() {
		BlockState state = this.getBlockState();
		return state.hasProperty(AbstractLampBlock.FACING) ? state.getValue(AbstractLampBlock.FACING) : Direction.DOWN;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ArcaneLampFertilityBlockEntity lamp) {
		if (level.isClientSide()) {
			return;
		}
		lamp.serverTick(level, pos, state);
	}

	private void serverTick(Level level, BlockPos pos, BlockState state) {
		Direction facing = this.getFacing();

		if (this.charges < 4 && this.drawEssentia(level, pos, facing)) {
			this.charges++;
			this.setChanged();
		}

		if (this.charges > 1 && ++this.count % 300 == 0) {
			this.updateAnimals(level, pos);
		}

		boolean isLit = this.charges > 0;
		if (state.hasProperty(LampOfFertilityBlock.LIT) && state.getValue(LampOfFertilityBlock.LIT) != isLit) {
			level.setBlock(pos, state.setValue(LampOfFertilityBlock.LIT, isLit), 3);
		}
	}

	private boolean drawEssentia(Level level, BlockPos pos, Direction facing) {
		if (++this.drawDelay % 5 != 0) {
			return false;
		}
		EssentiaTransport transport = EssentiaTransport.connectable(level, pos, facing);
		if (transport != null) {
			Direction opp = facing.getOpposite();
			if (!transport.canOutputTo(opp)) {
				return false;
			}
			if (transport.getSuctionFor(Aspect.LIFE, opp) < this.getSuctionAmount(facing)
				&& transport.takeEssentia(Aspect.LIFE, 1, opp) == 1) {
				return true;
			}
		}
		return false;
	}

	private void updateAnimals(Level level, BlockPos pos) {
		AABB area = new AABB(pos).inflate(DISTANCE);
		List<Animal> animals = level.getEntitiesOfClass(Animal.class, area);

		for (Animal first : animals) {
			if (first.getAge() == 0 && !first.isInLove() && first.canFallInLove()) {
				List<Animal> sameType = new ArrayList<>();
				for (Animal a : animals) {
					if (a.getType() == first.getType()) {
						sameType.add(a);
					}
				}

				if (sameType.size() <= 7) {
					Animal partner = null;
					for (Animal candidate : sameType) {
						if (candidate.getAge() == 0 && !candidate.isInLove() && candidate.canFallInLove()) {
							if (partner != null) {
								this.charges -= 2;
								candidate.setInLove(null);
								partner.setInLove(null);
								if (level instanceof ServerLevel sl) {
									sl.sendParticles(ParticleTypes.HEART, candidate.getX(), candidate.getY() + 0.5, candidate.getZ(), 4, 0.2,
										0.2, 0.2, 0.0);
									sl.sendParticles(ParticleTypes.HEART, partner.getX(), partner.getY() + 0.5, partner.getZ(), 4, 0.2,
										0.2, 0.2, 0.0);
								}
								this.setChanged();
								return;
							}
							partner = candidate;
						}
					}
				}
			}
		}
	}

	@Override
	public boolean isConnectable(Direction face) {
		return face == this.getFacing();
	}

	@Override
	public boolean canInputFrom(Direction face) {
		return face == this.getFacing();
	}

	@Override
	public boolean canOutputTo(Direction face) {
		return false;
	}

	@Override
	public void setSuction(Aspect aspect, int amount) {
	}

	@Override
	public Aspect getSuctionType(Direction face) {
		return Aspect.LIFE;
	}

	@Override
	public int getSuctionAmount(Direction face) {
		return face == this.getFacing() ? Math.max(0, 128 - this.charges * 10) : 0;
	}

	@Override
	public Aspect getEssentiaType(Direction face) {
		return null;
	}

	@Override
	public int getEssentiaAmount(Direction face) {
		return 0;
	}

	@Override
	public int takeEssentia(Aspect aspect, int amount, Direction face) {
		return 0;
	}

	@Override
	public int addEssentia(Aspect aspect, int amount, Direction face) {
		return 0;
	}

	@Override
	public int getMinimumSuction() {
		return 0;
	}

	@Override
	public boolean renderExtendedTube() {
		return false;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.charges = input.getIntOr("charges", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("charges", this.charges);
	}
}
