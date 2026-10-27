package dev.thaumvibe.tiles;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Port of TC4 TileBanner:
 * Carries banner dye color (-1 for cultist, 0..15 for Thaumcraft colors), optional aspect symbol, and facing.
 */
public class BannerBlockEntity extends TVBlockEntity {
	private int color = -1;
	private Aspect aspect = null;
	private boolean onWall = false;
	private int rotation = 0;
	private Direction facing = Direction.NORTH;

	public BannerBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.BANNER, pos, state);
	}

	public int getColor() {
		return this.color;
	}

	public void setColor(int color) {
		this.color = color;
		this.markForUpdate();
	}

	public Aspect getAspect() {
		return this.aspect;
	}

	public void setAspect(Aspect aspect) {
		this.aspect = aspect;
		this.markForUpdate();
	}

	public boolean isOnWall() {
		return this.onWall;
	}

	public void setOnWall(boolean onWall) {
		this.onWall = onWall;
		this.markForUpdate();
	}

	public int getRotation() {
		return this.rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
		this.markForUpdate();
	}

	public Direction getFacing() {
		return this.facing;
	}

	public void setFacing(Direction facing) {
		this.facing = facing;
		this.markForUpdate();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.color = input.getIntOr("color", -1);
		this.onWall = input.getBooleanOr("onWall", false);
		this.rotation = input.getIntOr("rotation", 0);
		this.facing = Direction.from3DDataValue(input.getIntOr("facing", Direction.NORTH.get3DDataValue()));
		this.aspect = input.read("Aspect", Aspect.CODEC).orElse(null);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("color", this.color);
		output.putBoolean("onWall", this.onWall);
		output.putInt("rotation", this.rotation);
		output.putInt("facing", this.facing.get3DDataValue());
		if (this.aspect != null) {
			output.store("Aspect", Aspect.CODEC, this.aspect);
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		tag.putInt("color", this.color);
		tag.putBoolean("onWall", this.onWall);
		tag.putInt("rotation", this.rotation);
		tag.putInt("facing", this.facing.get3DDataValue());
		if (this.aspect != null) {
			tag.putString("aspect", this.aspect.getTag());
		}
		return tag;
	}
}
