package dev.neothaum.tiles;

import dev.neothaum.registry.TVBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Port of TileArcanePressurePlate: configurable pressure plate with 3 owner/mob trigger settings. */
public class ArcanePressurePlateBlockEntity extends OwnedBlockEntity {
	private int setting = 0; // 0 = all, 1 = all except owner/permitted, 2 = only owner/permitted

	public ArcanePressurePlateBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ARCANE_PRESSURE_PLATE, pos, state);
	}

	public int getSetting() {
		return this.setting;
	}

	public void setSetting(int setting) {
		this.setting = Math.floorMod(setting, 3);
		this.setChanged();
	}

	public void cycleSetting(Player player) {
		this.setting = (this.setting + 1) % 3;
		this.setChanged();
		if (player != null && !player.level().isClientSide()) {
			Component msg = switch (this.setting) {
				case 0 -> Component.translatable("It will now trigger on everything.");
				case 1 -> Component.translatable("It will now trigger on everything except you.");
				default -> Component.translatable("It will now trigger on just you.");
			};
			player.sendOverlayMessage(msg);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putByte("setting", (byte) this.setting);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.setting = input.getByteOr("setting", (byte) 0) & 0xFF;
	}
}
