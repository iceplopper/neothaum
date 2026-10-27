package dev.neothaum.tiles;

import com.mojang.serialization.Codec;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.api.aspects.AspectContainer;
import dev.neothaum.api.aspects.AspectList;
import dev.neothaum.api.aspects.EssentiaTransport;
import dev.neothaum.blocks.MnemonicMatrixBlock;
import dev.neothaum.blocks.ThaumatoriumBlock;
import dev.neothaum.crafting.CrucibleRecipe;
import dev.neothaum.menu.ThaumatoriumMenu;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.research.ResearchManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Port of TileThaumatorium: automated crucible alchemy.
 * Draws liquid essentia from tubes, consumes catalysts from slot 0, and ejects products out the front.
 */
public class ThaumatoriumBlockEntity extends TVBlockEntity implements AspectContainer, EssentiaTransport, WorldlyContainer, MenuProvider {
	public record CrucibleEntry(Identifier id, CrucibleRecipe recipe) {}

	public ItemStack inputStack = ItemStack.EMPTY;
	public AspectList essentia = new AspectList();
	public List<Identifier> selectedRecipes = new ArrayList<>();
	public int maxRecipes = 1;
	public Direction facing = Direction.NORTH;
	public Aspect currentSuction = null;
	public int venting = 0;
	public int counter = 0;
	public boolean heated = false;

	public ThaumatoriumBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.THAUMATORIUM, pos, state);
	}

	public Direction getFacing() {
		if (this.level != null) {
			BlockState s = this.getBlockState();
			if (s.hasProperty(ThaumatoriumBlock.FACING)) {
				this.facing = s.getValue(ThaumatoriumBlock.FACING);
			}
		}
		return this.facing;
	}

	public void setFacing(Direction f) {
		this.facing = f;
		this.markForUpdate();
	}

	public boolean hasSelectedRecipe(Identifier id) {
		return this.selectedRecipes.contains(id);
	}

	public boolean toggleRecipe(Player player, int recipeIndex) {
		if (this.level == null) return false;
		List<CrucibleEntry> available = getAvailableRecipes(player);
		if (recipeIndex >= 0 && recipeIndex < available.size()) {
			Identifier id = available.get(recipeIndex).id();
			if (this.selectedRecipes.contains(id)) {
				this.selectedRecipes.remove(id);
			} else if (this.selectedRecipes.size() < this.maxRecipes) {
				this.selectedRecipes.add(id);
			}
			this.markForUpdate();
			return true;
		}
		return false;
	}

	public CrucibleRecipe getCrucibleRecipe(Identifier id) {
		if (this.level instanceof ServerLevel sl) {
			for (RecipeHolder<?> h : sl.getServer().getRecipeManager().getRecipes()) {
				if (h.id().identifier().equals(id) && h.value() instanceof CrucibleRecipe cr) {
					return cr;
				}
			}
		}
		return null;
	}

	public List<CrucibleEntry> getAvailableRecipes(Player player) {
		List<CrucibleEntry> list = new ArrayList<>();
		if (this.level instanceof ServerLevel sl) {
			for (RecipeHolder<?> h : sl.getServer().getRecipeManager().getRecipes()) {
				if (h.value() instanceof CrucibleRecipe r) {
					Identifier id = h.id().identifier();
					if (r.research().isEmpty() || ResearchManager.isResearchComplete(player, r.research())) {
						if (!this.inputStack.isEmpty() && r.catalyst().test(this.inputStack)) {
							list.add(new CrucibleEntry(id, r));
						} else if (this.selectedRecipes.contains(id)) {
							list.add(new CrucibleEntry(id, r));
						}
					}
				}
			}
		}
		return list;
	}

	public boolean isGettingPower() {
		if (this.level == null) return false;
		return this.level.hasNeighborSignal(this.worldPosition)
			|| this.level.hasNeighborSignal(this.worldPosition.below())
			|| this.level.hasNeighborSignal(this.worldPosition.above());
	}

	private static boolean isHeatSource(BlockState below) {
		return below.getFluidState().is(FluidTags.LAVA)
			|| below.is(BlockTags.FIRE)
			|| below.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK)
			|| below.is(TVBlocks.NITOR_LIGHT)
			|| (below.is(BlockTags.CAMPFIRES) && below.getValue(CampfireBlock.LIT));
	}

	private boolean checkHeat() {
		if (this.level == null) return false;
		return this.level.getBlockState(this.worldPosition.below()).is(TVBlocks.CRUCIBLE)
			&& isHeatSource(this.level.getBlockState(this.worldPosition.below(2)));
	}

	public void checkUpgrades() {
		if (this.level == null) return;
		int mr = 1;
		for (int yy = 0; yy <= 1; yy++) {
			BlockPos checkPos = this.worldPosition.above(yy);
			for (Direction dir : Direction.Plane.HORIZONTAL) {
				if (dir != this.getFacing()) {
					BlockState s = this.level.getBlockState(checkPos.relative(dir));
					if (s.is(TVBlocks.MNEMONIC_MATRIX) && s.getValue(MnemonicMatrixBlock.FACING) == dir.getOpposite()) {
						mr += 2;
					}
				}
			}
		}
		if (mr != this.maxRecipes) {
			this.maxRecipes = mr;
			while (this.selectedRecipes.size() > this.maxRecipes) {
				this.selectedRecipes.remove(this.selectedRecipes.size() - 1);
			}
			this.markForUpdate();
		}
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, ThaumatoriumBlockEntity be) {
		if (be.venting > 0) {
			be.venting--;
			Direction f = be.getFacing();
			double fx = 0.1 - level.getRandom().nextDouble() * 0.2;
			double fy = 0.1 - level.getRandom().nextDouble() * 0.2;
			double fz = 0.1 - level.getRandom().nextDouble() * 0.2;
			double px = pos.getX() + 0.5 + fx + f.getStepX() * 0.55;
			double py = pos.getY() + 0.5 + fy;
			double pz = pos.getZ() + 0.5 + fz + f.getStepZ() * 0.55;
			level.addParticle(ParticleTypes.CLOUD, px, py, pz, f.getStepX() * 0.08, 0.02, f.getStepZ() * 0.08);
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ThaumatoriumBlockEntity be) {
		if (be.counter == 0 || be.counter % 40 == 0) {
			be.heated = be.checkHeat();
			be.checkUpgrades();
		}
		be.counter++;

		if (be.heated && !be.isGettingPower() && be.counter % 5 == 0 && !be.selectedRecipes.isEmpty()) {
			if (be.inputStack.isEmpty()) {
				be.currentSuction = null;
				return;
			}

			// Find active recipe matching the catalyst
			CrucibleRecipe match = null;
			for (Identifier id : be.selectedRecipes) {
				CrucibleRecipe cr = be.getCrucibleRecipe(id);
				if (cr != null && cr.catalyst().test(be.inputStack)) {
					match = cr;
					break;
				}
			}

			if (match == null) {
				be.currentSuction = null;
				return;
			}

			// Check if essentia requirements are met
			boolean done = true;
			be.currentSuction = null;

			for (Aspect aspect : match.aspects().getAspectsSorted()) {
				if (be.essentia.getAmount(aspect) < match.aspects().getAmount(aspect)) {
					be.currentSuction = aspect;
					done = false;
					break;
				}
			}

			if (done) {
				be.completeRecipe(match);
			} else if (be.currentSuction != null) {
				be.fill();
			}
		}
	}

	private void completeRecipe(CrucibleRecipe recipe) {
		if (this.inputStack.isEmpty() || !recipe.matches(this.essentia, this.inputStack)) {
			return;
		}

		this.inputStack.shrink(1);
		this.essentia = new AspectList();
		this.currentSuction = null;

		ItemStack result = recipe.result().create();
		Direction f = this.getFacing();
		BlockPos outPos = this.worldPosition.relative(f);
		if (this.level.getBlockEntity(outPos) instanceof Container targetContainer) {
			result = HopperBlockEntity.addItem(null, targetContainer, result, f.getOpposite());
		}

		if (!result.isEmpty()) {
			ItemEntity entity = new ItemEntity(this.level,
				this.worldPosition.getX() + 0.5 + f.getStepX() * 0.66,
				this.worldPosition.getY() + 0.33,
				this.worldPosition.getZ() + 0.5 + f.getStepZ() * 0.66,
				result);
			entity.setDeltaMovement(0.075 * f.getStepX(), 0.025, 0.075 * f.getStepZ());
			this.level.addFreshEntity(entity);
		}

		this.level.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.25F,
			2.6F + (this.level.getRandom().nextFloat() - this.level.getRandom().nextFloat()) * 0.8F);
		this.venting = 7;
		this.markForUpdate();
	}

	void fill() {
		if (this.currentSuction == null || this.level == null) return;
		for (int y = 0; y <= 1; y++) {
			BlockPos checkPos = this.worldPosition.above(y);
			for (Direction dir : Direction.values()) {
				if (dir != this.getFacing() && dir != Direction.DOWN && (y != 0 || dir != Direction.UP)) {
					EssentiaTransport ic = EssentiaTransport.connectable(this.level, checkPos, dir);
					if (ic != null) {
						Direction opp = dir.getOpposite();
						if (ic.getEssentiaAmount(opp) > 0
							&& ic.getSuctionAmount(opp) < this.getSuctionAmount(dir)
							&& this.getSuctionAmount(dir) >= ic.getMinimumSuction()) {
							int ess = ic.takeEssentia(this.currentSuction, 1, opp);
							if (ess > 0) {
								this.addToContainer(this.currentSuction, ess);
								return;
							}
						}
					}
				}
			}
		}
	}

	// ------------------------------------------------------------------ NBT Serialization
	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.facing = Direction.byName(input.getStringOr("facing", Direction.NORTH.getSerializedName()));
		if (this.facing == null || !this.facing.getAxis().isHorizontal()) {
			this.facing = Direction.NORTH;
		}
		this.maxRecipes = input.getIntOr("maxrec", 1);
		this.venting = input.getIntOr("venting", 0);
		this.essentia = input.read("essentia", AspectList.CODEC).orElseGet(AspectList::new);
		this.selectedRecipes = new ArrayList<>(input.read("recipes", Codec.list(Identifier.CODEC)).orElseGet(List::of));
		this.inputStack = input.read("inputStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("facing", this.getFacing().getSerializedName());
		output.putInt("maxrec", this.maxRecipes);
		output.putInt("venting", this.venting);
		output.store("essentia", AspectList.CODEC, this.essentia);
		output.store("recipes", Codec.list(Identifier.CODEC), this.selectedRecipes);
		if (!this.inputStack.isEmpty()) {
			output.store("inputStack", ItemStack.OPTIONAL_CODEC, this.inputStack);
		}
	}

	// ------------------------------------------------------------------ AspectContainer
	@Override
	public AspectList getAspects() {
		return this.essentia;
	}

	@Override
	public void setAspects(AspectList aspects) {
		this.essentia = aspects;
		this.markForUpdate();
	}

	@Override
	public boolean doesContainerAccept(Aspect tag) {
		return true;
	}

	@Override
	public int addToContainer(Aspect tt, int am) {
		this.essentia.add(tt, am);
		this.markForUpdate();
		return 0;
	}

	@Override
	public boolean takeFromContainer(Aspect tt, int am) {
		if (this.essentia.getAmount(tt) >= am) {
			this.essentia.remove(tt, am);
			this.markForUpdate();
			return true;
		}
		return false;
	}

	@Override
	public boolean takeFromContainer(AspectList ot) {
		return false;
	}

	@Override
	public boolean doesContainerContain(AspectList ot) {
		return false;
	}

	@Override
	public boolean doesContainerContainAmount(Aspect tt, int am) {
		return this.essentia.getAmount(tt) >= am;
	}

	@Override
	public int containerContains(Aspect tt) {
		return this.essentia.getAmount(tt);
	}

	// ------------------------------------------------------------------ EssentiaTransport
	@Override
	public boolean isConnectable(Direction face) {
		return face != this.getFacing();
	}

	@Override
	public boolean canInputFrom(Direction face) {
		return face != this.getFacing();
	}

	@Override
	public boolean canOutputTo(Direction face) {
		return false;
	}

	@Override
	public void setSuction(Aspect aspect, int amount) {
		this.currentSuction = aspect;
	}

	@Override
	public Aspect getSuctionType(Direction face) {
		return this.currentSuction;
	}

	@Override
	public int getSuctionAmount(Direction face) {
		return this.currentSuction != null ? 128 : 0;
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
		if (this.canInputFrom(face)) {
			this.addToContainer(aspect, amount);
			return amount;
		}
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

	// ------------------------------------------------------------------ Container
	@Override
	public int getContainerSize() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return this.inputStack.isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return slot == 0 ? this.inputStack : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (slot == 0 && !this.inputStack.isEmpty()) {
			ItemStack split = this.inputStack.split(amount);
			if (this.inputStack.isEmpty()) {
				this.inputStack = ItemStack.EMPTY;
			}
			this.markForUpdate();
			return split;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		if (slot == 0) {
			ItemStack item = this.inputStack;
			this.inputStack = ItemStack.EMPTY;
			return item;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (slot == 0) {
			this.inputStack = stack;
			this.markForUpdate();
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.inputStack = ItemStack.EMPTY;
		this.markForUpdate();
	}

	// ------------------------------------------------------------------ WorldlyContainer
	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[]{0};
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return true;
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return true;
	}

	// ------------------------------------------------------------------ MenuProvider
	@Override
	public Component getDisplayName() {
		return Component.translatable("gui.thaumatorium");
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
		return new ThaumatoriumMenu(containerId, playerInventory, this.worldPosition);
	}
}
