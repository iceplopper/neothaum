package dev.neothaum;

import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.aspects.ObjectAspects;
import dev.neothaum.network.TVNetwork;
import dev.neothaum.registry.TVAttachments;
import dev.neothaum.registry.TVBlockEntities;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVItems;
import dev.neothaum.registry.TVMenus;
import dev.neothaum.registry.TVRecipes;
import dev.neothaum.registry.TVSounds;
import dev.neothaum.registry.TVTab;
import dev.neothaum.research.ResearchManager;
import dev.neothaum.wands.WandManager;
import dev.neothaum.world.TVWorldGen;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeoThaum implements ModInitializer {
	public static final String MOD_ID = "neothaum";
	public static final Logger LOGGER = LoggerFactory.getLogger("NeoThaum 4");

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		dev.neothaum.config.NeoThaumConfig.load();
		Aspect.bootstrap();
		TVSounds.init();
		TVComponents.init();
		dev.neothaum.fluids.TVFluids.init();
		TVBlocks.init();
		dev.neothaum.registry.TVMobEffects.init();
		TVItems.init();
		dev.neothaum.entity.TVEntities.init();
		TVBlockEntities.init();
		TVMenus.init();
		TVRecipes.init();
		TVAttachments.init();
		dev.neothaum.world.TCChestLoot.init();
		dev.neothaum.world.TCVillagers.init();
		dev.neothaum.world.TCVillages.init();
		TVTab.init();
		WandManager.init();
		dev.neothaum.wands.BlockSwapper.init();
		dev.neothaum.wands.WandFoci.init();
		dev.neothaum.items.equipment.TCEnchantments.init();
		dev.neothaum.items.ThaumometerItem.registerSneakScan();
		ResearchManager.init();
		ObjectAspects.init();
		TVNetwork.init();
		dev.neothaum.addon.VanillaCandles.init();
		dev.neothaum.addon.Additions.init();
		TVWorldGen.init();
		dev.neothaum.devtools.DevTools.init();
		dev.neothaum.items.equipment.ToolEvents.init();
		dev.neothaum.runic.RunicShielding.init();
		dev.neothaum.baubles.Baubles.initTicking();
		dev.neothaum.items.BathSaltsItem.init();
		dev.neothaum.warp.WarpEvents.init();
		dev.neothaum.events.AspectOrbEvents.init();
		dev.neothaum.entity.champion.ChampionEvents.init();
		dev.neothaum.golem.GolemBellItem.init();
		dev.neothaum.events.CommandThaumcraft.init();
		if (dev.neothaum.addon.trinkets.TrinketsCompat.LOADED) {
			dev.neothaum.addon.trinkets.TrinketsCompat.init();
		}
		LOGGER.info("NeoThaum 4 initialized: {} aspects", Aspect.aspects.size());
	}
}
