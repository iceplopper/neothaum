package dev.neothaum.registry;

import dev.neothaum.NeoThaum;
import dev.neothaum.menu.AlchemyFurnaceMenu;
import dev.neothaum.menu.ArcaneWorkbenchMenu;
import dev.neothaum.menu.BaublesMenu;
import dev.neothaum.menu.ResearchTableMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class TVMenus {
	public static final ExtendedMenuType<ArcaneWorkbenchMenu, BlockPos> ARCANE_WORKBENCH = Registry.register(BuiltInRegistries.MENU,
		NeoThaum.id("arcane_workbench"), new ExtendedMenuType<>(ArcaneWorkbenchMenu::new, BlockPos.STREAM_CODEC));
	public static final ExtendedMenuType<ResearchTableMenu, BlockPos> RESEARCH_TABLE = Registry.register(BuiltInRegistries.MENU,
		NeoThaum.id("research_table"), new ExtendedMenuType<>(ResearchTableMenu::new, BlockPos.STREAM_CODEC));
	public static final ExtendedMenuType<AlchemyFurnaceMenu, BlockPos> ALCHEMY_FURNACE = Registry.register(BuiltInRegistries.MENU,
		NeoThaum.id("alchemy_furnace"), new ExtendedMenuType<>(AlchemyFurnaceMenu::new, BlockPos.STREAM_CODEC));
	public static final ExtendedMenuType<dev.neothaum.menu.PechMenu, Integer> PECH = Registry.register(BuiltInRegistries.MENU, NeoThaum.id("pech"),
		new ExtendedMenuType<>(dev.neothaum.menu.PechMenu::new, net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));
	public static final ExtendedMenuType<dev.neothaum.golem.menu.GolemMenu, Integer> GOLEM = Registry.register(BuiltInRegistries.MENU, NeoThaum.id("golem"),
		new ExtendedMenuType<>(dev.neothaum.golem.menu.GolemMenu::new, net.minecraft.network.codec.ByteBufCodecs.VAR_INT.cast()));
	public static final MenuType<BaublesMenu> BAUBLES = Registry.register(BuiltInRegistries.MENU, NeoThaum.id("baubles"),
		new MenuType<>(BaublesMenu::new, FeatureFlags.VANILLA_SET));

	public static final MenuType<dev.neothaum.menu.FocusPouchMenu> FOCUS_POUCH = Registry.register(BuiltInRegistries.MENU, NeoThaum.id("focus_pouch"),
		new MenuType<>(dev.neothaum.menu.FocusPouchMenu::new, FeatureFlags.VANILLA_SET));
	public static final MenuType<dev.neothaum.menu.HoverHarnessMenu> HOVER_HARNESS = Registry.register(BuiltInRegistries.MENU, NeoThaum.id("hover_harness"),
		new MenuType<>(dev.neothaum.menu.HoverHarnessMenu::new, FeatureFlags.VANILLA_SET));
	public static final MenuType<dev.neothaum.menu.HandMirrorMenu> HAND_MIRROR = Registry.register(BuiltInRegistries.MENU, NeoThaum.id("hand_mirror"),
		new MenuType<>(dev.neothaum.menu.HandMirrorMenu::new, FeatureFlags.VANILLA_SET));

	public static final ExtendedMenuType<dev.neothaum.menu.FocalManipulatorMenu, BlockPos> FOCAL_MANIPULATOR = Registry.register(BuiltInRegistries.MENU,
		NeoThaum.id("focal_manipulator"), new ExtendedMenuType<>(dev.neothaum.menu.FocalManipulatorMenu::new, BlockPos.STREAM_CODEC));
	public static final ExtendedMenuType<dev.neothaum.menu.ThaumatoriumMenu, BlockPos> THAUMATORIUM = Registry.register(BuiltInRegistries.MENU,
		NeoThaum.id("thaumatorium"), new ExtendedMenuType<>(dev.neothaum.menu.ThaumatoriumMenu::new, BlockPos.STREAM_CODEC));
	public static final ExtendedMenuType<dev.neothaum.menu.SpaMenu, BlockPos> SPA = Registry.register(BuiltInRegistries.MENU,
		NeoThaum.id("spa"), new ExtendedMenuType<>(dev.neothaum.menu.SpaMenu::new, BlockPos.STREAM_CODEC));
	public static final ExtendedMenuType<dev.neothaum.menu.DeconstructionTableMenu, BlockPos> DECONSTRUCTION_TABLE = Registry.register(BuiltInRegistries.MENU,
		NeoThaum.id("deconstruction_table"), new ExtendedMenuType<>(dev.neothaum.menu.DeconstructionTableMenu::new, BlockPos.STREAM_CODEC));
	public static final ExtendedMenuType<dev.neothaum.menu.ArcaneBoreMenu, BlockPos> ARCANE_BORE = Registry.register(BuiltInRegistries.MENU,
		NeoThaum.id("arcane_bore"), new ExtendedMenuType<>(dev.neothaum.menu.ArcaneBoreMenu::new, BlockPos.STREAM_CODEC));

	public static void init() {
	}
}
