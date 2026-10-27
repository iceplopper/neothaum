package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectContainer;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.api.aspects.EssentiaTransport;
import dev.thaumvibe.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port of TileThaumatoriumTop:
 * Upper block of the Thaumatorium multiblock; proxies all essentia transport and inventory access down to the base Thaumatorium.
 */
public class ThaumatoriumTopBlockEntity extends TVBlockEntity implements AspectContainer, EssentiaTransport, WorldlyContainer {
	public ThaumatoriumTopBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.THAUMATORIUM_TOP, pos, state);
	}

	private ThaumatoriumBlockEntity getBase() {
		if (this.level != null && this.level.getBlockEntity(this.worldPosition.below()) instanceof ThaumatoriumBlockEntity base) {
			return base;
		}
		return null;
	}

	// ------------------------------------------------------------------ AspectContainer
	@Override
	public AspectList getAspects() {
		ThaumatoriumBlockEntity base = getBase();
		return base != null ? base.getAspects() : new AspectList();
	}

	@Override
	public void setAspects(AspectList aspects) {
		ThaumatoriumBlockEntity base = getBase();
		if (base != null) base.setAspects(aspects);
	}

	@Override
	public boolean doesContainerAccept(Aspect tag) {
		ThaumatoriumBlockEntity base = getBase();
		return base != null && base.doesContainerAccept(tag);
	}

	@Override
	public int addToContainer(Aspect tt, int am) {
		ThaumatoriumBlockEntity base = getBase();
		return base != null ? base.addToContainer(tt, am) : am;
	}

	@Override
	public boolean takeFromContainer(Aspect tt, int am) {
		ThaumatoriumBlockEntity base = getBase();
		return base != null && base.takeFromContainer(tt, am);
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
		ThaumatoriumBlockEntity base = getBase();
		return base != null && base.doesContainerContainAmount(tt, am);
	}

	@Override
	public int containerContains(Aspect tt) {
		ThaumatoriumBlockEntity base = getBase();
		return base != null ? base.containerContains(tt) : 0;
	}

	// ------------------------------------------------------------------ EssentiaTransport
	@Override
	public boolean isConnectable(Direction face) {
		ThaumatoriumBlockEntity base = getBase();
		return base != null && base.isConnectable(face);
	}

	@Override
	public boolean canInputFrom(Direction face) {
		ThaumatoriumBlockEntity base = getBase();
		return base != null && base.canInputFrom(face);
	}

	@Override
	public boolean canOutputTo(Direction face) {
		return false;
	}

	@Override
	public void setSuction(Aspect aspect, int amount) {
		ThaumatoriumBlockEntity base = getBase();
		if (base != null) base.setSuction(aspect, amount);
	}

	@Override
	public Aspect getSuctionType(Direction face) {
		ThaumatoriumBlockEntity base = getBase();
		return base != null ? base.getSuctionType(face) : null;
	}

	@Override
	public int getSuctionAmount(Direction face) {
		ThaumatoriumBlockEntity base = getBase();
		return base != null ? base.getSuctionAmount(face) : 0;
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
		ThaumatoriumBlockEntity base = getBase();
		return base != null ? base.addEssentia(aspect, amount, face) : 0;
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
		ThaumatoriumBlockEntity base = getBase();
		return base != null ? base.getContainerSize() : 0;
	}

	@Override
	public boolean isEmpty() {
		ThaumatoriumBlockEntity base = getBase();
		return base == null || base.isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		ThaumatoriumBlockEntity base = getBase();
		return base != null ? base.getItem(slot) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ThaumatoriumBlockEntity base = getBase();
		return base != null ? base.removeItem(slot, amount) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		ThaumatoriumBlockEntity base = getBase();
		return base != null ? base.removeItemNoUpdate(slot) : ItemStack.EMPTY;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		ThaumatoriumBlockEntity base = getBase();
		if (base != null) base.setItem(slot, stack);
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		ThaumatoriumBlockEntity base = getBase();
		if (base != null) base.clearContent();
	}

	// ------------------------------------------------------------------ WorldlyContainer
	@Override
	public int[] getSlotsForFace(Direction side) {
		ThaumatoriumBlockEntity base = getBase();
		return base != null ? base.getSlotsForFace(side) : new int[0];
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return true;
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return true;
	}
}
