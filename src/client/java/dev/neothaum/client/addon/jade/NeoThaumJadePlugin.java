package dev.neothaum.client.addon.jade;

import dev.neothaum.NeoThaum;
import dev.neothaum.api.aspects.Aspect;
import dev.neothaum.items.GogglesItem;
import dev.neothaum.registry.TVBlocks;
import dev.neothaum.tiles.CrucibleBlockEntity;
import dev.neothaum.tiles.InfusionMatrixBlockEntity;
import dev.neothaum.tiles.JarBlockEntity;
import dev.neothaum.tiles.NodeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class NeoThaumJadePlugin implements IWailaPlugin {

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		// Aura Node Provider
		registration.registerBlockComponent(new IBlockComponentProvider() {
			@Override
			public Identifier getUid() {
				return NeoThaum.id("aura_node");
			}

			@Override
			public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
				if (!GogglesItem.isWearingGoggles(accessor.getPlayer())) {
					return;
				}
				if (accessor.getBlockEntity() instanceof NodeBlockEntity node) {
					tooltip.add(Component.literal("§5Aura Node: §b" + node.getNodeType().name()
						+ (node.getNodeModifier() != null ? " (" + node.getNodeModifier().name() + ")" : "")));
					tooltip.add(Component.literal("§7Vis Storage: §f" + node.getAspects().visSize() + " / " + node.getAspectsBase().visSize()));
					for (Aspect aspect : node.getAspects().getAspects()) {
						int cur = node.getAspects().getAmount(aspect);
						int max = node.getNodeVisBase(aspect);
						tooltip.add(Component.literal(" §7• §r" + aspect.getName() + ": §f" + cur + " / " + max)
							.withColor(aspect.getColor()));
					}
				}
			}
		}, TVBlocks.AURA_NODE.getClass());

		// Crucible Provider
		registration.registerBlockComponent(new IBlockComponentProvider() {
			@Override
			public Identifier getUid() {
				return NeoThaum.id("crucible");
			}

			@Override
			public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
				if (!GogglesItem.isWearingGoggles(accessor.getPlayer())) {
					return;
				}
				if (accessor.getBlockEntity() instanceof CrucibleBlockEntity crucible) {
					boolean boiling = crucible.heat > 150;
					tooltip.add(Component.literal("§6Status: " + (boiling ? "§cBoiling" : (crucible.water > 0 ? "§eWarming" : "§7Empty"))
						+ " §8(" + crucible.water + " mB)"));
					if (crucible.aspects.size() > 0) {
						tooltip.add(Component.literal("§dDissolved Essentia:"));
						for (Aspect a : crucible.aspects.getAspects()) {
							tooltip.add(Component.literal(" §7• §r" + a.getName() + " x" + crucible.aspects.getAmount(a))
								.withColor(a.getColor()));
						}
					}
				}
			}
		}, TVBlocks.CRUCIBLE.getClass());

		// Jar Provider (Warded & Void Jars)
		registration.registerBlockComponent(new IBlockComponentProvider() {
			@Override
			public Identifier getUid() {
				return NeoThaum.id("jar");
			}

			@Override
			public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
				if (!GogglesItem.isWearingGoggles(accessor.getPlayer())) {
					return;
				}
				if (accessor.getBlockEntity() instanceof JarBlockEntity jar) {
					if (jar.aspect != null && jar.amount > 0) {
						tooltip.add(Component.literal("§dEssentia: §r" + jar.aspect.getName() + " §f" + jar.amount + " / " + JarBlockEntity.MAX)
							.withColor(jar.aspect.getColor()));
					} else if (jar.aspectFilter != null) {
						tooltip.add(Component.literal("§7Filtered: §r" + jar.aspectFilter.getName() + " §8(0 / " + JarBlockEntity.MAX + ")")
							.withColor(jar.aspectFilter.getColor()));
					} else {
						tooltip.add(Component.literal("§7Empty Jar").withStyle(ChatFormatting.ITALIC));
					}
					if (jar.isVoid()) {
						tooltip.add(Component.literal("§8Void Voiding Active"));
					}
				}
			}
		}, TVBlocks.WARDED_JAR.getClass());

		// Infusion Matrix Provider
		registration.registerBlockComponent(new IBlockComponentProvider() {
			@Override
			public Identifier getUid() {
				return NeoThaum.id("runic_matrix");
			}

			@Override
			public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
				if (!GogglesItem.isWearingGoggles(accessor.getPlayer())) {
					return;
				}
				if (accessor.getBlockEntity() instanceof InfusionMatrixBlockEntity matrix) {
					tooltip.add(Component.literal(matrix.active ? "§aInfusion Active" : "§7Matrix Idle"));
				}
			}
		}, TVBlocks.RUNIC_MATRIX.getClass());
	}
}
