package dev.thaumvibe.tiles;

import dev.thaumvibe.blocks.ArcaneEarBlock;
import dev.thaumvibe.registry.TVBlockEntities;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Port of TileSensor: listens for matching note block pitch/tone and outputs redstone pulses. */
public class ArcaneEarBlockEntity extends BlockEntity {
	private static final Set<ArcaneEarBlockEntity> LOADED_EARS = Collections.newSetFromMap(new WeakHashMap<>());

	private int note = 0; // 0..24
	private int redstoneSignal = 0; // cooldown timer in ticks (10 when triggered)

	public ArcaneEarBlockEntity(BlockPos pos, BlockState state) {
		super(TVBlockEntities.ARCANE_EAR, pos, state);
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		if (level != null && !level.isClientSide()) {
			LOADED_EARS.add(this);
		}
	}

	@Override
	public void setRemoved() {
		LOADED_EARS.remove(this);
		super.setRemoved();
	}

	public int getNote() {
		return this.note;
	}

	public int getRedstoneSignal() {
		return this.redstoneSignal;
	}

	public void cycleNote() {
		this.note = (this.note + 1) % 25;
		this.setChanged();
	}

	/** Determines which instrument this ear expects based on the block it is attached to. */
	public NoteBlockInstrument getInstrument() {
		if (this.level == null) {
			return NoteBlockInstrument.HARP;
		}
		BlockState myState = this.getBlockState();
		if (!myState.hasProperty(ArcaneEarBlock.FACING)) {
			return NoteBlockInstrument.HARP;
		}
		Direction attachedFace = myState.getValue(ArcaneEarBlock.FACING).getOpposite();
		BlockPos supportingPos = this.worldPosition.relative(attachedFace);
		BlockState supportingState = this.level.getBlockState(supportingPos);
		return supportingState.instrument();
	}

	public void trigger() {
		this.redstoneSignal = 10;
		this.setChanged();
		if (this.level != null && !this.level.isClientSide()) {
			this.level.setBlock(this.worldPosition, this.getBlockState().setValue(ArcaneEarBlock.POWERED, true), 3);
			this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ArcaneEarBlockEntity ear) {
		if (ear.redstoneSignal > 0) {
			ear.redstoneSignal--;
			if (ear.redstoneSignal == 0) {
				level.setBlock(pos, state.setValue(ArcaneEarBlock.POWERED, false), 3);
				level.updateNeighborsAt(pos, state.getBlock());
			}
		}
	}

	/** Dispatches note block plays across the world to all active ears within 64 blocks. */
	public static void onNotePlayed(Level level, BlockPos notePos, NoteBlockInstrument instrument, int note) {
		if (level == null || level.isClientSide()) {
			return;
		}
		for (ArcaneEarBlockEntity ear : LOADED_EARS) {
			if (ear.level == level && !ear.isRemoved()) {
				if (ear.worldPosition.distSqr(notePos) <= 4096.0) { // 64 blocks
					if (ear.getNote() == note && ear.getInstrument() == instrument) {
						ear.trigger();
					}
				}
			}
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putByte("note", (byte) this.note);
		output.putInt("signal", this.redstoneSignal);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.note = input.getByteOr("note", (byte) 0) & 0xFF;
		if (this.note > 24) {
			this.note = 0;
		}
		this.redstoneSignal = input.getIntOr("signal", 0);
	}
}
