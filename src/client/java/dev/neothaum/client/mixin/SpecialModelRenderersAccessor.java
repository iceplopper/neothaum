package dev.neothaum.client.mixin;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Lets NeoThaum register its own "minecraft:special" item renderer types (thaumometer, crystal clusters). */
@Mixin(SpecialModelRenderers.class)
public interface SpecialModelRenderersAccessor {
	@Accessor("ID_MAPPER")
	static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpecialModelRenderer.Unbaked<?>>> neothaum$idMapper() {
		throw new AssertionError();
	}
}
