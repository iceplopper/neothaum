package dev.thaumvibe;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.aspects.ObjectAspects;
import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.registry.TVAttachments;
import dev.thaumvibe.registry.TVBlockEntities;
import dev.thaumvibe.registry.TVBlocks;
import dev.thaumvibe.registry.TVComponents;
import dev.thaumvibe.registry.TVItems;
import dev.thaumvibe.registry.TVMenus;
import dev.thaumvibe.registry.TVRecipes;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.registry.TVTab;
import dev.thaumvibe.research.ResearchManager;
import dev.thaumvibe.wands.WandManager;
import dev.thaumvibe.world.TVWorldGen;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thaumvibe implements ModInitializer {
	public static final String MOD_ID = "thaumvibe";
	public static final Logger LOGGER = LoggerFactory.getLogger("Thaumvibe 4");

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		Aspect.bootstrap();
		TVSounds.init();
		TVComponents.init();
		TVBlocks.init();
		dev.thaumvibe.registry.TVMobEffects.init();
		TVItems.init();
		dev.thaumvibe.entity.TVEntities.init();
		TVBlockEntities.init();
		TVMenus.init();
		TVRecipes.init();
		TVAttachments.init();
		TVTab.init();
		WandManager.init();
		dev.thaumvibe.wands.BlockSwapper.init();
		dev.thaumvibe.wands.WandFoci.init();
		dev.thaumvibe.items.equipment.TCEnchantments.init();
		dev.thaumvibe.items.ThaumometerItem.registerSneakScan();
		ResearchManager.init();
		ObjectAspects.init();
		TVNetwork.init();
		dev.thaumvibe.addon.VanillaCandles.init();
		TVWorldGen.init();
		dev.thaumvibe.devtools.DevTools.init();
		dev.thaumvibe.items.equipment.ToolEvents.init();
		dev.thaumvibe.runic.RunicShielding.init();
		dev.thaumvibe.baubles.Baubles.initTicking();
		dev.thaumvibe.items.BathSaltsItem.init();
		dev.thaumvibe.warp.WarpEvents.init();
		LOGGER.info("Thaumvibe 4 initialized: {} aspects", Aspect.aspects.size());
	}
}
