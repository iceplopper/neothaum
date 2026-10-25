package dev.thaumvibe;

import java.util.function.Consumer;

/** Hooks the client installs so common code can open client-only screens (Thaumcraft proxy.openGui equivalents). */
public final class ClientBridge {
	public static Consumer<String> openScreen = s -> {
	};

	/** Dev tools: opens the node editor screen for the node at this position. */
	public static Consumer<net.minecraft.core.BlockPos> openNodeEditor = p -> {
	};

	/** ItemElementalPickaxe: RenderEventHandler.startScan round the block it was used on. */
	public static Consumer<net.minecraft.core.BlockPos> oreScan = p -> {
	};

	/** Thaumcraft.proxy.burst at an entity (client side only). */
	public static java.util.function.BiConsumer<net.minecraft.world.entity.Entity, Float> burst = (e, s) -> {
	};

	private ClientBridge() {
	}
}
