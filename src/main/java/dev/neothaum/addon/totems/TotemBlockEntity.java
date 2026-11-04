package dev.neothaum.addon.totems;

import dev.neothaum.tiles.TVBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A totem's name, and whether it was found in the world. A freshly placed totem (generated, crafted, or mined and set down again) has no name
 * yet: on its first tick it surveys its surroundings, takes a Latin name and joins the world's list of totems.
 */
public class TotemBlockEntity extends TVBlockEntity {
	public String name = "";
	/** Generated with the world, not raised by a player: the first to touch one learns the totems' lore. */
	public boolean natural;
	private int age;

	public TotemBlockEntity(BlockPos pos, BlockState state) {
		super(TravellersTotems.TOTEM_ENTITY, pos, state);
	}

	public boolean isHome() {
		return this.getBlockState().getBlock() instanceof TotemBlock t && t.home;
	}

	public GlobalPos globalPos() {
		return GlobalPos.of(this.level.dimension(), this.worldPosition);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, TotemBlockEntity totem) {
		// a moment after it appears, so the land round a generated totem has finished generating
		if (!totem.name.isEmpty() || ++totem.age < 20 || !(level instanceof ServerLevel sl)) {
			return;
		}
		TotemData data = TotemData.get(sl.getServer());
		totem.name = TotemNames.name(sl, pos, data::nameTaken);
		data.put(new TotemData.Totem(totem.globalPos(), totem.name, totem.isHome()));
		totem.markForUpdate();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.name = input.getStringOr("Name", "");
		this.natural = input.getBooleanOr("Natural", false);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("Name", this.name);
		output.putBoolean("Natural", this.natural);
	}
}
