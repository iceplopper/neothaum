package dev.thaumvibe.tiles;

import com.mojang.serialization.Codec;
import dev.thaumvibe.registry.TVBlockEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Port of TileOwned: tracks owner and key-granted permissions for warded blocks/doors. */
public class OwnedBlockEntity extends BlockEntity {
	protected String ownerName = "";
	protected UUID ownerUuid;
	protected final List<String> accessList = new ArrayList<>();

	public OwnedBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.OWNED, pos, state);
	}

	protected OwnedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public void setOwner(Player player) {
		if (player != null) {
			this.ownerUuid = player.getGameProfile().id();
			this.ownerName = player.getGameProfile().name();
			this.setChanged();
		}
	}

	public String getOwnerName() {
		return this.ownerName;
	}

	public UUID getOwnerUuid() {
		return this.ownerUuid;
	}

	public boolean isOwner(Player player) {
		if (player == null) {
			return false;
		}
		if (this.ownerUuid != null && this.ownerUuid.equals(player.getGameProfile().id())) {
			return true;
		}
		return !this.ownerName.isEmpty() && this.ownerName.equalsIgnoreCase(player.getGameProfile().name());
	}

	public boolean hasAccess(Player player) {
		if (this.isOwner(player)) {
			return true;
		}
		if (player == null) {
			return false;
		}
		String name = player.getGameProfile().name().toLowerCase();
		for (String entry : this.accessList) {
			if (entry.length() > 1 && entry.substring(1).equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasAdminAccess(Player player) {
		if (this.isOwner(player)) {
			return true;
		}
		if (player == null) {
			return false;
		}
		String name = player.getGameProfile().name().toLowerCase();
		for (String entry : this.accessList) {
			if (entry.startsWith("1") && entry.substring(1).equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public boolean addAccess(String username, boolean admin) {
		String prefix = admin ? "1" : "0";
		String entry = prefix + username;
		if (!this.accessList.contains(entry)) {
			this.accessList.add(entry);
			this.setChanged();
			return true;
		}
		return false;
	}

	public List<String> getAccessList() {
		return this.accessList;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("owner", this.ownerName);
		output.storeNullable("owner_uuid", UUIDUtil.CODEC, this.ownerUuid);
		if (!this.accessList.isEmpty()) {
			output.store("access", Codec.STRING.listOf(), new ArrayList<>(this.accessList));
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.ownerName = input.getStringOr("owner", "");
		this.ownerUuid = input.read("owner_uuid", UUIDUtil.CODEC).orElse(null);
		this.accessList.clear();
		input.read("access", Codec.STRING.listOf()).ifPresent(this.accessList::addAll);
	}
}
