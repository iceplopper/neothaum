package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.menu.ResearchTableMenu;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVComponents;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.research.HexUtils;
import dev.thaumvibe.research.PlayerKnowledge;
import dev.thaumvibe.research.ResearchItem;
import dev.thaumvibe.research.ResearchManager;
import dev.thaumvibe.research.ResearchNoteData;
import dev.thaumvibe.research.ScanManager;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Port of thaumcraft.common.tiles.TileResearchTable: slot 0 scribing tools, slot 1 research notes. */
public class ResearchTableBlockEntity extends TVBlockEntity implements Container, ExtendedMenuProvider<BlockPos> {
	private final NonNullList<ItemStack> contents = NonNullList.withSize(2, ItemStack.EMPTY);
	public AspectList bonusAspects = new AspectList();
	int nextRecalc = 0;

	public ResearchTableBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.RESEARCH_TABLE, pos, state);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.contents.clear();
		ContainerHelper.loadAllItems(input, this.contents);
		this.nextRecalc = input.getIntOr("nextRecalc", 0);
		this.bonusAspects = input.read("bonusAspects", AspectList.CODEC).orElseGet(AspectList::new);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.contents);
		output.putInt("nextRecalc", this.nextRecalc);
		output.store("bonusAspects", AspectList.CODEC, this.bonusAspects);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ResearchTableBlockEntity be) {
		if (!level.isClientSide() && be.nextRecalc++ > 600) {
			be.nextRecalc = 0;
			be.recalculateBonus();
			be.markForUpdate();
		}
	}

	public ResearchNoteData getData() {
		ItemStack s = this.contents.get(1);
		return s.is(TVItems.RESEARCH_NOTES) ? s.get(TVComponents.RESEARCH_NOTE) : null;
	}

	public void placeAspect(int q, int r, Aspect aspect, Player player) {
		ResearchNoteData data = getData();
		ItemStack ink = this.contents.get(0);
		ItemStack notes = this.contents.get(1);
		if (!ResearchManager.consumeInkFromTable(ink, false)) {
			return;
		}
		if (data != null && !data.complete) {
			data = data.copy();
			boolean r1 = ResearchManager.isResearchComplete(player, "RESEARCHER1");
			boolean r2 = ResearchManager.isResearchComplete(player, "RESEARCHER2");
			HexUtils.Hex hex = new HexUtils.Hex(q, r);
			ResearchNoteData.HexEntry he;
			var rand = this.level.getRandom();
			if (aspect != null) {
				he = new ResearchNoteData.HexEntry(aspect, 2);
				PlayerKnowledge k = ResearchManager.knowledge(player);
				if (r2 && rand.nextFloat() < 0.1F) {
					this.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2F,
						0.9F + rand.nextFloat() * 0.2F);
				} else if (k.getAspectPool(aspect) <= 0) {
					this.bonusAspects.remove(aspect, 1);
				} else {
					k = k.copy();
					k.addAspectPool(aspect, -1);
					ResearchManager.setKnowledge(player, k);
				}
			} else {
				ResearchNoteData.HexEntry old = data.hexEntries.get(hex.toString());
				float f = rand.nextFloat();
				if (old != null && old.aspect() != null && (r1 && f < 0.25F || r2 && f < 0.5F)) {
					this.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.2F,
						0.9F + rand.nextFloat() * 0.2F);
					PlayerKnowledge k = ResearchManager.knowledge(player).copy();
					k.addAspectPool(old.aspect(), 1);
					ResearchManager.setKnowledge(player, k);
				}
				he = new ResearchNoteData.HexEntry(null, 0);
			}
			data.hexEntries.put(hex.toString(), he);
			data.hexes.put(hex.toString(), hex);
			notes.set(TVComponents.RESEARCH_NOTE, data);
			ResearchManager.consumeInkFromTable(ink, true);
			if (ResearchManager.checkResearchCompletion(notes, data, player)) {
				this.level.playSound(null, this.worldPosition, TVSounds.LEARN, SoundSource.BLOCKS, 1.0F, 1.0F);
			}
		}
		this.markForUpdate();
	}

	/** PacketAspectCombinationToServer: ab = take from the table's bonus aspects. */
	public void combine(Player player, Aspect aspect1, Aspect aspect2, boolean ab1, boolean ab2) {
		if (aspect1 == null || aspect2 == null || !(player instanceof ServerPlayer sp)) {
			return;
		}
		Aspect combo = ResearchManager.getCombinationResult(aspect1, aspect2);
		PlayerKnowledge k = ResearchManager.knowledge(player).copy();
		if ((k.getAspectPool(aspect1) > 0 || ab1) && (k.getAspectPool(aspect2) > 0 || ab2)) {
			if (k.getAspectPool(aspect1) > 0 || !ab1) {
				k.addAspectPool(aspect1, -1);
			} else {
				this.bonusAspects.remove(aspect1, 1);
			}
			if (k.getAspectPool(aspect2) > 0 || !ab2) {
				k.addAspectPool(aspect2, -1);
			} else {
				this.bonusAspects.remove(aspect2, 1);
			}
			ResearchManager.setKnowledge(player, k);
			if (combo != null) {
				ScanManager.checkAndSyncAspectKnowledge(sp, combo, 1);
			}
			this.markForUpdate();
		}
	}

	private void recalculateBonus() {
		var rand = this.level.getRandom();
		int y0 = this.worldPosition.getY();
		if (!this.level.isBrightOutside() && this.level.getMaxLocalRawBrightness(this.worldPosition.above()) < 4 && !this.level.canSeeSky(this.worldPosition.above())
			&& rand.nextInt(20) == 0) {
			this.bonusAspects.merge(Aspect.ENTROPY, 1);
		}
		int height = this.level.getMaxY() - this.level.getMinY();
		int rel = y0 - this.level.getMinY();
		if (rel > height * 0.5F && rand.nextInt(20) == 0) {
			this.bonusAspects.merge(Aspect.AIR, 1);
		}
		if (rel > height * 0.66F && rand.nextInt(20) == 0) {
			this.bonusAspects.merge(Aspect.AIR, 1);
		}
		if (rel > height * 0.75F && rand.nextInt(20) == 0) {
			this.bonusAspects.merge(Aspect.AIR, 1);
		}
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int x = -8; x <= 8; x++) {
			for (int z = -8; z <= 8; z++) {
				for (int y = -8; y <= 8; y++) {
					p.set(this.worldPosition.getX() + x, y0 + y, this.worldPosition.getZ() + z);
					if (this.level.isOutsideBuildHeight(p)) {
						continue;
					}
					BlockState bi = this.level.getBlockState(p);
					if (bi.is(TVBlocks.AIR_INFUSED_STONE)) {
						if (bonus(Aspect.AIR, 20)) return;
					} else if (bi.is(TVBlocks.AIR_CRYSTAL_CLUSTER)) {
						if (bonus(Aspect.AIR, 10)) return;
					} else if (bi.is(BlockTags.FIRE) || bi.getFluidState().is(FluidTags.LAVA) || bi.is(TVBlocks.FIRE_INFUSED_STONE)) {
						if (bonus(Aspect.FIRE, 20)) return;
					} else if (bi.is(TVBlocks.FIRE_CRYSTAL_CLUSTER)) {
						if (bonus(Aspect.FIRE, 10)) return;
					} else if (bi.is(BlockTags.DIRT) || bi.is(BlockTags.SAND)) {
						if (bonus(Aspect.EARTH, 20)) return;
					} else if (bi.is(TVBlocks.EARTH_INFUSED_STONE)) {
						if (bonus(Aspect.EARTH, 20)) return;
					} else if (bi.is(TVBlocks.EARTH_CRYSTAL_CLUSTER)) {
						if (bonus(Aspect.EARTH, 10)) return;
					} else if (bi.getFluidState().is(FluidTags.WATER)) {
						if (bonus(Aspect.WATER, 15)) return;
					} else if (bi.is(TVBlocks.WATER_INFUSED_STONE)) {
						if (bonus(Aspect.WATER, 20)) return;
					} else if (bi.is(TVBlocks.WATER_CRYSTAL_CLUSTER)) {
						if (bonus(Aspect.WATER, 10)) return;
					} else if (bi.is(Blocks.REDSTONE_WIRE) || bi.is(Blocks.REPEATER) || bi.is(Blocks.COMPARATOR) || bi.is(Blocks.PISTON)
						|| bi.is(Blocks.STICKY_PISTON) || bi.is(Blocks.LEVER)) {
						if (bonus(Aspect.ORDER, 20)) return;
					} else if (bi.is(TVBlocks.ORDER_INFUSED_STONE)) {
						if (bonus(Aspect.ORDER, 20)) return;
					} else if (bi.is(TVBlocks.ORDER_CRYSTAL_CLUSTER)) {
						if (bonus(Aspect.ORDER, 10)) return;
					} else if (bi.is(TVBlocks.ENTROPY_INFUSED_STONE)) {
						if (bonus(Aspect.ENTROPY, 20)) return;
					} else if (bi.is(TVBlocks.ENTROPY_CRYSTAL_CLUSTER)) {
						if (bonus(Aspect.ENTROPY, 10)) return;
					}
					if (bi.is(Blocks.BOOKSHELF) && rand.nextInt(300) == 0) {
						Aspect[] all = Aspect.aspects.values().toArray(new Aspect[0]);
						this.bonusAspects.merge(all[rand.nextInt(all.length)], 1);
						return;
					}
				}
			}
		}
	}

	private boolean bonus(Aspect a, int chance) {
		if (this.bonusAspects.getAmount(a) < 1 && this.level.getRandom().nextInt(chance) == 0) {
			this.bonusAspects.merge(a, 1);
			return true;
		}
		return false;
	}

	public void duplicate(Player player) {
		ResearchNoteData data = getData();
		ItemStack notes = this.contents.get(1);
		if (data == null || !data.complete) {
			return;
		}
		int paper = slotOf(player, Items.PAPER);
		int ink = slotOf(player, Items.INK_SAC);
		if (paper < 0 || ink < 0) {
			return;
		}
		ResearchItem rr = ResearchManager.getResearch(data.key);
		if (rr == null) {
			return;
		}
		PlayerKnowledge k = ResearchManager.knowledge(player).copy();
		for (Aspect aspect : rr.tags.getAspects()) {
			if (k.getAspectPool(aspect) < rr.tags.getAmount(aspect) + data.copies) {
				return;
			}
		}
		for (Aspect aspect : rr.tags.getAspects()) {
			k.addAspectPool(aspect, -(rr.tags.getAmount(aspect) + data.copies));
		}
		ResearchManager.setKnowledge(player, k);
		player.getInventory().getItem(paper).shrink(1);
		player.getInventory().getItem(ink).shrink(1);
		this.level.playSound(null, this.worldPosition, TVSounds.LEARN, SoundSource.BLOCKS, 1.0F, 1.0F);
		data = data.copy();
		data.copies++;
		notes.set(TVComponents.RESEARCH_NOTE, data);
		notes.grow(1);
		this.markForUpdate();
	}

	private static int slotOf(Player p, net.minecraft.world.item.Item item) {
		for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
			if (p.getInventory().getItem(i).is(item)) {
				return i;
			}
		}
		return -1;
	}

	// ------------------------------------------------------------ Container

	@Override
	public int getContainerSize() {
		return 2;
	}

	@Override
	public boolean isEmpty() {
		return this.contents.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.contents.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack s = ContainerHelper.removeItem(this.contents, slot, amount);
		this.markForUpdate();
		return s;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.contents, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.contents.set(slot, stack);
		this.markForUpdate();
	}

	@Override
	public void markForUpdate() {
		super.markForUpdate();
		if (this.level != null && !this.level.isClientSide()) {
			BlockState s = this.getBlockState();
			if (s.hasProperty(dev.thaumvibe.blocks.ResearchTableBlock.INK)) {
				BlockState n = s.setValue(dev.thaumvibe.blocks.ResearchTableBlock.INK, this.contents.get(0).is(TVItems.SCRIBING_TOOLS))
					.setValue(dev.thaumvibe.blocks.ResearchTableBlock.NOTES, this.contents.get(1).is(TVItems.RESEARCH_NOTES));
				if (n != s) {
					this.level.setBlock(this.worldPosition, n, 3);
				}
			}
		}
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return slot == 0 ? stack.is(TVItems.SCRIBING_TOOLS) : stack.is(TVItems.RESEARCH_NOTES);
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.contents.clear();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("tile.blockTable.research.name");
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ResearchTableMenu(id, inv, this);
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayer player) {
		return this.worldPosition;
	}
}
