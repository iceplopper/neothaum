package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.aspects.ObjectAspects;
import dev.thaumvibe.crafting.CrucibleRecipe;
import dev.thaumvibe.fx.FX;
import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.research.ResearchManager;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Port of thaumcraft.common.tiles.TileCrucible. Water is a plain 0-1000 mB counter. */
public class CrucibleBlockEntity extends TVBlockEntity {
	public static final int CAPACITY = 1000;
	public short heat;
	public AspectList aspects = new AspectList();
	public int water;
	public int bellows = -1;
	private long counter = -100L;

	public CrucibleBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.CRUCIBLE, pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.heat = (short) input.getShortOr("Heat", (short) 0);
		this.water = input.getIntOr("Water", 0);
		this.aspects = input.read("Aspects", AspectList.CODEC).orElseGet(AspectList::new);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putShort("Heat", this.heat);
		output.putInt("Water", this.water);
		output.store("Aspects", AspectList.CODEC, this.aspects);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity be) {
		be.updateEntity();
	}

	private boolean isHeatSource(BlockState below) {
		return below.getFluidState().is(FluidTags.LAVA) || below.is(BlockTags.FIRE) || below.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)
			|| below.is(TVBlocks.NITOR_LIGHT) || below.is(BlockTags.CAMPFIRES) && below.getValue(net.minecraft.world.level.block.CampfireBlock.LIT);
	}

	public void updateEntity() {
		this.counter++;
		int prevheat = this.heat;
		if (!this.level.isClientSide()) {
			if (this.bellows < 0 || this.counter % 20 == 0) {
				// TileCrucible.getBellows: bellows blowing in from the sides
				this.bellows = BellowsBlockEntity.getBellows(this.level, this.worldPosition, net.minecraft.core.Direction.Plane.HORIZONTAL.stream()
					.toArray(net.minecraft.core.Direction[]::new));
			}
			if (this.water <= 0) {
				if (this.heat > 0) {
					this.heat--;
				}
			} else if (isHeatSource(this.level.getBlockState(this.worldPosition.below()))) {
				if (this.heat < 200) {
					this.heat = (short) (this.heat + 1 + this.bellows * 2);
					if (prevheat < 151 && this.heat >= 151) {
						this.markForUpdate();
					}
				}
			} else if (this.heat > 0) {
				this.heat--;
				if (this.heat == 149) {
					this.markForUpdate();
				}
			}

			if (this.tagAmount() > 100 && this.counter % 5L == 0L) {
				this.takeRandomFromSource();
				this.spill();
			}

			if (this.counter > 100L && this.heat > 150) {
				this.counter = 0L;
				if (this.tagAmount() > 0) {
					var rand = this.level.getRandom();
					Aspect[] all = this.aspects.getAspects();
					Aspect a = all[rand.nextInt(all.length)];
					if (a.isPrimal()) {
						a = all[rand.nextInt(all.length)];
					}
					this.water = Math.max(0, this.water - 2);
					this.aspects.remove(a, 1);
					if (!a.isPrimal()) {
						this.aspects.add(rand.nextBoolean() ? a.getComponents()[0] : a.getComponents()[1], 1);
					} else {
						this.spill();
					}
				}
				this.markForUpdate();
			}
		} else if (this.water > 0) {
			this.drawEffects();
		}
		if (this.level.isClientSide() && prevheat < 151 && this.heat >= 151) {
			this.heat++;
		}
	}

	private void drawEffects() {
		var rand = this.level.getRandom();
		int x0 = this.worldPosition.getX(), y0 = this.worldPosition.getY(), z0 = this.worldPosition.getZ();
		if (this.heat > 150) {
			FX.wisp(this.level, x0 + 0.2F + rand.nextFloat() * 0.6F, y0 + this.getFluidHeight(), z0 + 0.2F + rand.nextFloat() * 0.6F, 0.25F, 0.8F, 0.8F,
				0.9F, false, true, -0.002F);
		}
		if (rand.nextInt(6) == 0 && this.aspects.size() > 0) {
			int color = this.aspects.getAspects()[rand.nextInt(this.aspects.size())].getColor();
			int x = 5 + rand.nextInt(22);
			int y = 5 + rand.nextInt(22);
			FX.crucibleBubble(this.level, x0 + x / 32.0F + 0.015625F, y0 + 0.05F + this.getFluidHeight(), z0 + y / 32.0F + 0.015625F,
				(color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);
		}
	}

	/** Flux goo/gas arrive with the taint phase; until then spilled vis simply dissipates with a puff of smoke. */
	public void spill() {
		if (this.level instanceof ServerLevel sl && this.level.getRandom().nextInt(4) == 0) {
			sl.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.1,
				this.worldPosition.getZ() + 0.5, 3, 0.2, 0.1, 0.2, 0.0);
		}
	}

	public void spillRemnants() {
		if (this.water > 0 || this.aspects.visSize() > 0) {
			this.water = 0;
			for (int a = 0; a < this.aspects.visSize() / 2; a++) {
				this.spill();
			}
			this.aspects = new AspectList();
			this.markForUpdate();
			TVNetwork.sendCrucibleBoil(this.level, this.worldPosition, 5);
		}
	}

	public void ejectItem(ItemStack items) {
		boolean first = true;
		var rand = this.level.getRandom();
		do {
			ItemStack spitout = items.copyWithCount(Math.min(items.getCount(), items.getMaxStackSize()));
			items.shrink(spitout.getCount());
			ItemEntity e = new ItemEntity(this.level, this.worldPosition.getX() + 0.5F, this.worldPosition.getY() + 0.71F, this.worldPosition.getZ() + 0.5F,
				spitout);
			e.setDeltaMovement(first ? 0.0 : (rand.nextFloat() - rand.nextFloat()) * 0.01F, 0.1F, first ? 0.0 : (rand.nextFloat() - rand.nextFloat()) * 0.01F);
			e.addTag("thaumvibe_crucible_output");
			e.setPickUpDelay(10);
			this.level.addFreshEntity(e);
			first = false;
		} while (items.getCount() > 0);
	}

	public static boolean isCrucibleOutput(Entity e) {
		return e.entityTags().contains("thaumvibe_crucible_output");
	}

	public CrucibleRecipe findMatchingCrucibleRecipe(Player player, ItemStack lastDrop) {
		if (!(this.level instanceof ServerLevel sl)) {
			return null;
		}
		int highest = 0;
		CrucibleRecipe out = null;
		for (RecipeHolder<?> h : sl.getServer().getRecipeManager().getRecipes()) {
			if (h.value() instanceof CrucibleRecipe r && player != null && ResearchManager.isResearchComplete(player, r.research()) && r.matches(this.aspects, lastDrop)) {
				int result = r.aspects().size();
				if (result > highest) {
					highest = result;
					out = r;
				}
			}
		}
		return out;
	}

	public void attemptSmelt(ItemEntity entity) {
		boolean bubble = false;
		boolean event = false;
		ItemStack item = entity.getItem();
		Player p = null;
		if (entity.getOwner() instanceof Player owner) {
			p = owner;
		}
		int stacksize = item.getCount();
		var rand = this.level.getRandom();
		for (int a = 0; a < item.getCount(); a++) {
			CrucibleRecipe rc = findMatchingCrucibleRecipe(p, item);
			if (rc != null && this.water > 0) {
				ItemStack out = rc.result().create();
				if (p != null) {
					out.onCraftedBy(p, out.getCount());
				}
				this.aspects = rc.removeMatching(this.aspects);
				this.water = Math.max(0, this.water - 50);
				this.ejectItem(out);
				event = true;
				stacksize--;
				this.counter = -250L;
			} else {
				AspectList ot = ObjectAspects.getAspects(item.copyWithCount(1));
				if (ot == null || ot.size() == 0) {
					entity.setDeltaMovement((rand.nextFloat() - rand.nextFloat()) * 0.2F, 0.35F, (rand.nextFloat() - rand.nextFloat()) * 0.2F);
					this.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F,
						(rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F);
					return;
				}
				for (Aspect tag : ot.getAspects()) {
					this.aspects.add(tag, ot.getAmount(tag));
				}
				bubble = true;
				stacksize--;
				this.counter = -150L;
			}
			if (stacksize <= 0) {
				break;
			}
		}
		if (bubble) {
			this.level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), TVSounds.BUBBLE, SoundSource.BLOCKS, 0.2F, 1.0F + rand.nextFloat() * 0.4F);
			TVNetwork.sendCrucibleBoil(this.level, this.worldPosition, 1);
		}
		if (event) {
			TVNetwork.sendCrucibleBoil(this.level, this.worldPosition, 5);
		}
		if (stacksize <= 0) {
			entity.discard();
		} else {
			entity.setItem(item.copyWithCount(stacksize));
		}
		this.markForUpdate();
	}

	public int tagAmount() {
		return this.aspects.visSize();
	}

	public float getFluidHeight() {
		float base = 0.3F + 0.5F * ((float) this.water / CAPACITY);
		float out = base + this.tagAmount() / 100.0F * (1.0F - base);
		if (out > 1.0F) {
			out = 1.001F;
		}
		if (out == 1.0F) {
			out = 0.9999F;
		}
		return out;
	}

	public AspectList takeRandomFromSource() {
		AspectList output = new AspectList();
		if (this.aspects.size() > 0) {
			Aspect tag = this.aspects.getAspects()[this.level.getRandom().nextInt(this.aspects.size())];
			output.add(tag, 1);
			this.aspects.remove(tag, 1);
		}
		this.markForUpdate();
		return output;
	}
}
