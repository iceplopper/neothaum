package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.registry.TVBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Port of TC4 TileManaPod:
 * Block entity for Mana Pods hanging from logs in magical biomes.
 * Cross-breeds aspects from adjacent mana pods upon reaching stage 3.
 */
public class ManaPodBlockEntity extends TVBlockEntity {
	private Aspect aspect = null;

	public ManaPodBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.MANA_POD, pos, state);
	}

	public Aspect getAspect() {
		return this.aspect;
	}

	public void setAspect(Aspect aspect) {
		this.aspect = aspect;
		this.markForUpdate();
	}

	/**
	 * Growth and cross-breeding logic ported 1:1 from TC4 TileManaPod.checkGrowth.
	 */
	public void checkGrowth(int newAge) {
		if (newAge > 2) {
			if (newAge == 3 && this.aspect == null && this.level != null) {
				AspectList al = new AspectList();
				if (this.aspect != null) {
					al.add(this.aspect, 1);
				}

				// Check 4 horizontal neighbors for adjacent pods to cross-breed with
				for (Direction d : Direction.Plane.HORIZONTAL) {
					BlockPos neighborPos = this.worldPosition.relative(d);
					if (this.level.getBlockEntity(neighborPos) instanceof ManaPodBlockEntity other && other.aspect != null) {
						al.add(other.aspect, 1);
					}
				}

				if (al.size() > 1) {
					Aspect[] aa = al.getAspects();
					List<Aspect> outlist = new ArrayList<>();

					for (int i = 0; i < aa.length; i++) {
						outlist.add(aa[i]);

						for (int j = 0; j < aa.length; j++) {
							if (i != j) {
								Aspect combo = Aspect.getCombination(aa[i], aa[j]);
								if (combo != null) {
									outlist.add(combo);
									outlist.add(combo);
								}
							}
						}
					}

					if (!outlist.isEmpty()) {
						this.aspect = outlist.get(this.level.getRandom().nextInt(outlist.size()));
						this.markForUpdate();
					}
				}

				if (al.size() >= 1 && this.aspect == null) {
					this.aspect = al.getAspectsSortedAmount()[0];
					this.markForUpdate();
				}
			}

			if (this.aspect == null && this.level != null) {
				if (this.level.getRandom().nextInt(8) == 0) {
					this.aspect = Aspect.PLANT; // Plant aspect
				} else {
					List<Aspect> primals = Aspect.getPrimalAspects();
					this.aspect = primals.get(this.level.getRandom().nextInt(primals.size()));
				}
				this.markForUpdate();
			}
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.aspect = input.read("Aspect", Aspect.CODEC).orElse(null);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (this.aspect != null) {
			output.store("Aspect", Aspect.CODEC, this.aspect);
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		if (this.aspect != null) {
			tag.putString("aspect", this.aspect.getTag());
		}
		return tag;
	}
}
