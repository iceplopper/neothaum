package dev.thaumvibe.registry;

import dev.thaumvibe.Thaumvibe;
import dev.thaumvibe.menu.AlchemyFurnaceMenu;
import dev.thaumvibe.menu.ArcaneWorkbenchMenu;
import dev.thaumvibe.menu.BaublesMenu;
import dev.thaumvibe.menu.ResearchTableMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class TVMenus {
	public static final ExtendedMenuType<ArcaneWorkbenchMenu, BlockPos> ARCANE_WORKBENCH = Registry.register(BuiltInRegistries.MENU,
		Thaumvibe.id("arcane_workbench"), new ExtendedMenuType<>(ArcaneWorkbenchMenu::new, BlockPos.STREAM_CODEC));
	public static final ExtendedMenuType<ResearchTableMenu, BlockPos> RESEARCH_TABLE = Registry.register(BuiltInRegistries.MENU,
		Thaumvibe.id("research_table"), new ExtendedMenuType<>(ResearchTableMenu::new, BlockPos.STREAM_CODEC));
	public static final ExtendedMenuType<AlchemyFurnaceMenu, BlockPos> ALCHEMY_FURNACE = Registry.register(BuiltInRegistries.MENU,
		Thaumvibe.id("alchemy_furnace"), new ExtendedMenuType<>(AlchemyFurnaceMenu::new, BlockPos.STREAM_CODEC));
	public static final ExtendedMenuType<dev.thaumvibe.menu.PechMenu, Integer> PECH = Registry.register(BuiltInRegistries.MENU, Thaumvibe.id("pech"),
		new ExtendedMenuType<>(dev.thaumvibe.menu.PechMenu::new, net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));
	public static final MenuType<BaublesMenu> BAUBLES = Registry.register(BuiltInRegistries.MENU, Thaumvibe.id("baubles"),
		new MenuType<>(BaublesMenu::new, FeatureFlags.VANILLA_SET));

	public static final MenuType<dev.thaumvibe.menu.FocusPouchMenu> FOCUS_POUCH = Registry.register(BuiltInRegistries.MENU, Thaumvibe.id("focus_pouch"),
		new MenuType<>(dev.thaumvibe.menu.FocusPouchMenu::new, FeatureFlags.VANILLA_SET));
	public static final MenuType<dev.thaumvibe.menu.HoverHarnessMenu> HOVER_HARNESS = Registry.register(BuiltInRegistries.MENU, Thaumvibe.id("hover_harness"),
		new MenuType<>(dev.thaumvibe.menu.HoverHarnessMenu::new, FeatureFlags.VANILLA_SET));
	public static final MenuType<dev.thaumvibe.menu.HandMirrorMenu> HAND_MIRROR = Registry.register(BuiltInRegistries.MENU, Thaumvibe.id("hand_mirror"),
		new MenuType<>(dev.thaumvibe.menu.HandMirrorMenu::new, FeatureFlags.VANILLA_SET));

	public static final ExtendedMenuType<dev.thaumvibe.menu.FocalManipulatorMenu, BlockPos> FOCAL_MANIPULATOR = Registry.register(BuiltInRegistries.MENU,
		Thaumvibe.id("focal_manipulator"), new ExtendedMenuType<>(dev.thaumvibe.menu.FocalManipulatorMenu::new, BlockPos.STREAM_CODEC));
	public static final ExtendedMenuType<dev.thaumvibe.menu.ThaumatoriumMenu, BlockPos> THAUMATORIUM = Registry.register(BuiltInRegistries.MENU,
		Thaumvibe.id("thaumatorium"), new ExtendedMenuType<>(dev.thaumvibe.menu.ThaumatoriumMenu::new, BlockPos.STREAM_CODEC));

	public static void init() {
	}
}
