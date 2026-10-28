package dev.neothaum.world;

import com.google.common.collect.ImmutableSet;
import dev.neothaum.NeoThaum;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

/**
 * ConfigEntities.entWizardId and entBankerId: the wizard of the wizard's tower and the banker of the banker's house. Neither has a workstation;
 * each lives in the building that brings it (placed with a little experience so it never forgets its trade). Their goods are TC4's, all offered
 * at once (trade_set/wizard and trade_set/banker): the wizard trades in thaumic odds and ends, the banker buys TC4's gold coins.
 */
public final class TCVillagers {
	public static final ResourceKey<VillagerProfession> WIZARD = key("wizard");
	public static final ResourceKey<VillagerProfession> BANKER = key("banker");

	private TCVillagers() {
	}

	private static ResourceKey<VillagerProfession> key(String name) {
		return ResourceKey.create(Registries.VILLAGER_PROFESSION, NeoThaum.id(name));
	}

	public static void init() {
		register(WIZARD);
		register(BANKER);
	}

	private static void register(ResourceKey<VillagerProfession> key) {
		var tradeSet = ResourceKey.create(Registries.TRADE_SET, NeoThaum.id(key.identifier().getPath() + "/level_1"));
		Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, key,
			new VillagerProfession(Component.translatable("entity.neothaum.villager." + key.identifier().getPath()), p -> false, p -> false,
				ImmutableSet.of(), ImmutableSet.of(), null, Int2ObjectMap.ofEntries(Int2ObjectMap.entry(1, tradeSet))));
	}
}
