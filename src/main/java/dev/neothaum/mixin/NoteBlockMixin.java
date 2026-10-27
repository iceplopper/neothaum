package dev.neothaum.mixin;

import dev.neothaum.tiles.ArcaneEarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public class NoteBlockMixin {
	@Inject(method = "triggerEvent", at = @At("HEAD"))
	private void onTriggerEvent(BlockState state, Level level, BlockPos pos, int id, int param, CallbackInfoReturnable<Boolean> cir) {
		if (state.hasProperty(NoteBlock.INSTRUMENT)) {
			ArcaneEarBlockEntity.onNotePlayed(level, pos, state.getValue(NoteBlock.INSTRUMENT), param);
		}
	}
}
