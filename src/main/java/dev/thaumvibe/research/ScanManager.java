package dev.thaumvibe.research;

import dev.thaumvibe.api.aspects.Aspect;
import dev.thaumvibe.api.aspects.AspectList;
import dev.thaumvibe.aspects.ObjectAspects;
import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.tiles.NodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Port of thaumcraft.common.lib.research.ScanManager. Scan keys use registry ids instead of numeric item hashes. */
public final class ScanManager {
	/** type 1 = item/block, 2 = entity, 3 = phenomena (nodes). */
	public record ScanResult(int type, Item item, Entity entity, String phenomena) {
		public static ScanResult item(Item item) {
			return new ScanResult(1, item, null, "");
		}

		public static ScanResult entity(Entity e) {
			return new ScanResult(2, null, e, "");
		}

		/** A block with no item form that ConfigAspects still tagged (water, lava, fire); stored with the scanned objects. */
		public static ScanResult block(net.minecraft.world.level.block.Block block) {
			return new ScanResult(4, null, null, "block:" + BuiltInRegistries.BLOCK.getKey(block));
		}

		public static ScanResult node(BlockPos pos) {
			return new ScanResult(3, null, null, "NODE" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ());
		}

		public boolean sameAs(ScanResult o) {
			return o != null && o.type == this.type && o.item == this.item && o.entity == this.entity && o.phenomena.equals(this.phenomena);
		}
	}

	public static String itemKey(Item item) {
		return "item:" + BuiltInRegistries.ITEM.getKey(item);
	}

	public static String entityKey(Entity entity) {
		String hash = "ent:" + EntityType.getKey(entity.getType());
		if (entity instanceof Player p) {
			hash = "player_" + p.getName().getString();
		}
		if (entity instanceof LivingEntity le && le.isBaby()) {
			hash += "CHILD";
		}
		if (entity instanceof Creeper c) {
			if (c.getSwellDir() == 1) {
				hash += "FLASHING";
			}
			if (c.isPowered()) {
				hash += "POWERED";
			}
		}
		return hash;
	}

	private static ItemStack itemOf(ScanResult scan) {
		if (scan.type == 1) {
			return new ItemStack(scan.item);
		}
		if (scan.type == 2 && scan.entity instanceof ItemEntity ie) {
			return ie.getItem().copyWithCount(1);
		}
		return ItemStack.EMPTY;
	}

	private static String keyOf(ScanResult scan) {
		if (scan.type == 1) {
			return itemKey(scan.item);
		}
		if (scan.type == 2) {
			return scan.entity instanceof ItemEntity ie ? itemKey(ie.getItem().getItem()) : entityKey(scan.entity);
		}
		return scan.phenomena;
	}

	private static java.util.Set<String> listFor(PlayerKnowledge k, ScanResult scan) {
		if (scan.type == 1 || scan.type == 4 || scan.type == 2 && scan.entity instanceof ItemEntity) {
			return k.objects;
		}
		return scan.type == 2 ? k.entities : k.phenomena;
	}

	public static boolean isValidScanTarget(Player player, ScanResult scan, String prefix) {
		if (scan == null) {
			return false;
		}
		if (prefix.equals("@") && !isValidScanTarget(player, scan, "#")) {
			return false;
		}
		return !listFor(ResearchManager.knowledge(player), scan).contains(prefix + keyOf(scan));
	}

	public static boolean hasBeenScanned(Player player, ScanResult scan) {
		var list = listFor(ResearchManager.knowledge(player), scan);
		String k = keyOf(scan);
		return list.contains("@" + k) || list.contains("#" + k);
	}

	public static AspectList getScanAspects(ScanResult scan, Level level) {
		if (scan.type == 1 || scan.type == 2 && scan.entity instanceof ItemEntity) {
			return ObjectAspects.getAspects(itemOf(scan));
		}
		if (scan.type == 2) {
			return ObjectAspects.getEntityAspects(scan.entity);
		}
		if (scan.type == 3 && scan.phenomena.startsWith("NODE")) {
			return generateNodeAspects(level, scan.phenomena.substring(4));
		}
		if (scan.type == 4) {
			return BuiltInRegistries.BLOCK.getOptional(net.minecraft.resources.Identifier.parse(scan.phenomena.substring(6)))
				.map(ObjectAspects::getBlockAspects).orElse(null);
		}
		return null;
	}

	private static AspectList generateNodeAspects(Level level, String node) {
		String[] p = node.split(":");
		BlockPos pos = new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
		AspectList tags = new AspectList();
		if (level.getBlockEntity(pos) instanceof NodeBlockEntity tnb) {
			AspectList ta = tnb.getAspects();
			for (Aspect a : ta.getAspectsSorted()) {
				tags.merge(a, Math.max(4, ta.getAmount(a) / 10));
			}
			switch (tnb.getNodeType()) {
				case UNSTABLE -> tags.merge(Aspect.ENTROPY, 4);
				case HUNGRY -> tags.merge(Aspect.HUNGER, 4);
				case TAINTED -> tags.merge(Aspect.TAINT, 4);
				case PURE -> {
					tags.merge(Aspect.HEAL, 2);
					tags.add(Aspect.ORDER, 2);
				}
				case DARK -> {
					tags.merge(Aspect.DEATH, 2);
					tags.add(Aspect.DARKNESS, 2);
				}
				default -> {
				}
			}
		}
		return tags.size() > 0 ? tags : null;
	}

	/** Client half of validScan: checks parents and returns the notification to show, or null. */
	public static Component validScanError(AspectList aspects, Player player) {
		PlayerKnowledge k = ResearchManager.knowledge(player);
		if (aspects == null || aspects.size() == 0) {
			return Component.translatable("tc.unknownobject");
		}
		for (Aspect aspect : aspects.getAspects()) {
			if (!aspect.isPrimal() && !k.hasDiscoveredParentAspects(aspect)) {
				for (Aspect parent : aspect.getComponents()) {
					if (!k.hasDiscoveredAspect(parent)) {
						return Component.translatable("tc.discoveryerror", Component.translatable("tc.aspect.help." + parent.getTag()));
					}
				}
				return Component.translatable("tc.unknownobject");
			}
		}
		return null;
	}

	/** Server: ScanManager.completeScan. */
	public static boolean completeScan(ServerPlayer player, ScanResult scan, String prefix) {
		boolean scannedByThaumometer = prefix.equals("#") && !isValidScanTarget(player, scan, "@");
		AspectList aspects = getScanAspects(scan, player.level());
		Component err = validScanError(aspects, player);
		if (err != null) {
			return false;
		}
		Object clue = null;
		PlayerKnowledge k = ResearchManager.knowledge(player).copy();
		String key = prefix + keyOf(scan);
		var list = listFor(k, scan);
		list.add(key);
		if (prefix.equals("#")) {
			list.remove("@" + keyOf(scan));
		}
		ResearchManager.setKnowledge(player, k);
		if (scan.type == 1 || scan.type == 2 && scan.entity instanceof ItemEntity) {
			clue = itemOf(scan);
		} else if (scan.type == 2) {
			clue = EntityType.getKey(scan.entity.getType()).toString();
		}

		AspectList aspectsFinal = new AspectList();
		for (Aspect aspect : aspects.getAspects()) {
			if (ResearchManager.knowledge(player).hasDiscoveredParentAspects(aspect)) {
				int amt = aspects.getAmount(aspect);
				if (scannedByThaumometer) {
					amt = 0;
				}
				if (prefix.equals("#")) {
					amt++;
				}
				int a = checkAndSyncAspectKnowledge(player, aspect, amt);
				if (a > 0) {
					aspectsFinal.merge(aspect, a);
				}
			}
		}
		if (clue != null) {
			ResearchManager.createClue(player.level(), player, clue, aspectsFinal);
		}
		return true;
	}

	/** Config.aspectTotalCap = 100. */
	public static int checkAndSyncAspectKnowledge(ServerPlayer player, Aspect aspect, int amount) {
		PlayerKnowledge k = ResearchManager.knowledge(player).copy();
		int save = 0;
		if (!k.hasDiscoveredAspect(aspect)) {
			TVNetwork.sendAspectDiscovery(player, aspect);
			amount += 2;
			save = amount;
			k.addDiscoveredAspect(aspect);
		}
		if (k.getAspectPool(aspect) >= 100) {
			amount = (int) Math.sqrt(amount);
		}
		if (amount > 1 && k.getAspectPool(aspect) >= 125) {
			amount = 1;
		}
		if (k.addAspectPool(aspect, amount)) {
			TVNetwork.sendAspectPool(player, aspect, amount, k.getAspectPool(aspect));
			save = amount;
		}
		ResearchManager.setKnowledge(player, k);
		return save;
	}

	private ScanManager() {
	}
}
