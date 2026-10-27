package dev.neothaum.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Port of thaumcraft.api.TileThaumcraft: block entity whose full data is synced to clients on update. */
public abstract class TVBlockEntity extends BlockEntity {
	protected TVBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveWithoutMetadata(registries);
	}

	/** world.markBlockForUpdate equivalent. */
	public void markForUpdate() {
		this.setChanged();
		if (this.level != null) {
			BlockState s = this.getBlockState();
			this.level.sendBlockUpdated(this.worldPosition, s, s, 3);
		}
	}
}
