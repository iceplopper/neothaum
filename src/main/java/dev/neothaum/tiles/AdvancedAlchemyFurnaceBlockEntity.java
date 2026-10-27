package dev.neothaum.tiles;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.visnet.VisNetHandler;
import dev.neothaum.aspects.ObjectAspects;
import dev.neothaum.blocks.AdvancedAlchemyFurnaceBlock;
import dev.neothaum.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Port of TC4 TileAlchemyFurnaceAdvanced:
 * The core controller of the multiblock.
 * Draws Ignis (heat), Perditio (breakdown power 1), and Aqua (cooling/stabilizing power 2) centivis from the aura grid.
 * Smelts items dropped into the top funnel into pure essentia with 100% efficiency and zero flux slurry.
 */
public class AdvancedAlchemyFurnaceBlockEntity extends TVBlockEntity {
	public AspectList aspects = new AspectList();
	public int vis = 0;
	public final int maxVis = 500;
	public int heat = 0;
	public int power1 = 0;
	public int power2 = 0;
	public final int maxPower = 500;
	public boolean destroy = false;
	public int count = 0;
	public int processed = 0;

	public AdvancedAlchemyFurnaceBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ADVANCED_ALCHEMY_FURNACE, pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, AdvancedAlchemyFurnaceBlockEntity furnace) {
		furnace.count++;
		if (furnace.destroy) {
			AdvancedAlchemyFurnaceBlock.restoreBlocks(level, pos);
			return;
		}

		if (furnace.processed > 0) {
			furnace.processed--;
		}

		if (furnace.count % 5 == 0) {
			int pt = furnace.heat;
			if (furnace.heat > 0) {
				furnace.heat--;
			}

			if (furnace.heat <= furnace.maxPower) {
				furnace.heat += VisNetHandler.drainVis(level, pos, Aspect.FIRE, 50);
			}
			if (furnace.power1 <= furnace.maxPower) {
				furnace.power1 += VisNetHandler.drainVis(level, pos, Aspect.ENTROPY, 50);
			}
			if (furnace.power2 <= furnace.maxPower) {
				furnace.power2 += VisNetHandler.drainVis(level, pos, Aspect.WATER, 50);
			}

			if (pt / 50 != furnace.heat / 50) {
				furnace.markForUpdate();
			}
		}
	}

	public boolean canSmelt(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		AspectList al = ObjectAspects.getAspects(stack);
		if (al == null || al.size() == 0) {
			return false;
		}
		int vs = al.visSize();
		return vs + this.aspects.visSize() <= this.maxVis;
	}

	public boolean process(ItemStack stack) {
		if (this.processed == 0 && this.canSmelt(stack)) {
			AspectList al = ObjectAspects.getAspects(stack);
			if (al == null) {
				return false;
			}
			int aa = al.visSize();
			if (aa * 2 <= this.heat && aa <= this.power1 && aa <= this.power2) {
				this.heat -= aa * 2;
				this.power1 -= aa;
				this.power2 -= aa;
				this.processed = (int) (5.0F + Math.max(0.0F, (1.0F - (float) this.heat / (float) this.maxPower) * 100.0F));
				for (Aspect a : al.getAspects()) {
					this.aspects.add(a, al.getAmount(a));
				}
				this.vis = this.aspects.visSize();
				stack.shrink(1);
				this.markForUpdate();
				return true;
			}
		}
		return false;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.aspects = input.read("Aspects", AspectList.CODEC).orElseGet(AspectList::new);
		this.vis = this.aspects.visSize();
		this.heat = input.getIntOr("heat", 0);
		this.power1 = input.getIntOr("power1", 0);
		this.power2 = input.getIntOr("power2", 0);
		this.processed = input.getIntOr("processed", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("Aspects", AspectList.CODEC, this.aspects);
		output.putInt("heat", this.heat);
		output.putInt("power1", this.power1);
		output.putInt("power2", this.power2);
		output.putInt("processed", this.processed);
	}
}
