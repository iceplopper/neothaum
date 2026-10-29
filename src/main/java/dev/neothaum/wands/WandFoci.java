package dev.neothaum.wands;

import dev.neothaum.api.wands.Architect;
import dev.neothaum.api.wands.FocusItem;
import dev.neothaum.api.wands.FocusUpgradeType;
import dev.neothaum.baubles.Baubles;
import dev.neothaum.items.FocusPouchItem;
import dev.neothaum.items.WandItem;
import dev.neothaum.items.foci.TradeFocusItem;
import dev.neothaum.registry.TVComponents;
import dev.neothaum.registry.TVSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** The focus half of thaumcraft.common.items.wands.WandManager: cast cooldowns, swapping foci (F key) and the architect area (G key). */
public final class WandFoci {
	/** ItemWandCasting.onEntitySwing: a left click with the equal trade focus swaps the block instead of digging it. */
	public static void init() {
		net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT.register((player, level, hand, pos, dir) -> {
			ItemStack wand = player.getItemInHand(hand);
			if (wand.getItem() instanceof WandItem && WandItem.getFocus(wand) instanceof TradeFocusItem trade) {
				if (!level.isClientSide() && !isOnCooldown(player)) {
					setCooldown(player, trade.getActivationCooldown(WandItem.getFocusStack(wand)));
					trade.onEntitySwing(player, wand);
				}
				return net.minecraft.world.InteractionResult.FAIL;
			}
			return net.minecraft.world.InteractionResult.PASS;
		});
	}

	private static final Map<Integer, Long> COOLDOWN_SERVER = new ConcurrentHashMap<>();
	private static final Map<Integer, Long> COOLDOWN_CLIENT = new ConcurrentHashMap<>();

	public static boolean isOnCooldown(LivingEntity e) {
		Long until = (e.level().isClientSide() ? COOLDOWN_CLIENT : COOLDOWN_SERVER).get(e.getId());
		return until != null && until > System.currentTimeMillis();
	}

	/** Seconds of cooldown left (0 or less when ready), for the wand HUD. */
	public static float getCooldown(LivingEntity e) {
		Long until = (e.level().isClientSide() ? COOLDOWN_CLIENT : COOLDOWN_SERVER).get(e.getId());
		return until == null ? 0.0F : (until - System.currentTimeMillis()) / 1000.0F;
	}

	public static void setCooldown(LivingEntity e, int cd) {
		if (cd == 0) {
			COOLDOWN_CLIENT.remove(e.getId());
			COOLDOWN_SERVER.remove(e.getId());
		} else {
			(e.level().isClientSide() ? COOLDOWN_CLIENT : COOLDOWN_SERVER).put(e.getId(), System.currentTimeMillis() + cd);
		}
	}

	// ---------------------------------------------------------------- foci on hand

	/** A focus found on the player: where it is (inventory slot, or pouch index * 1000 + pouch slot) and the stack itself. */
	public record Found(String key, int location, ItemStack stack) {
	}

	/** Every focus the player carries or keeps in a pouch, keyed by sorting helper (the radial menu's order). */
	public static TreeMap<String, Found> carriedFoci(Player player, Map<Integer, Integer> pouches) {
		TreeMap<String, Found> foci = new TreeMap<>();
		int pouchcount = 0;
		// a worn pouch is keyed -(slot + 1), counting NeoThaum's own bauble slots and then any an addon adds
		List<Baubles.Worn> worn = Baubles.wornSlots(player);
		for (int a = 0; a < worn.size(); a++) {
			ItemStack b = worn.get(a).stack();
			if (b.getItem() instanceof FocusPouchItem) {
				pouchcount++;
				pouches.put(pouchcount, -(a + 1));
				List<ItemStack> inv = FocusPouchItem.getInventory(b);
				for (int q = 0; q < inv.size(); q++) {
					ItemStack it = inv.get(q);
					if (it.getItem() instanceof FocusItem f) {
						String k = f.getSortingHelper(it);
						foci.put(k, new Found(k, q + pouchcount * 1000, it));
					}
				}
			}
		}
		var inv = player.getInventory();
		for (int ax = 0; ax < 36; ax++) {
			ItemStack it = inv.getItem(ax);
			if (it.getItem() instanceof FocusItem f) {
				String k = f.getSortingHelper(it);
				foci.put(k, new Found(k, ax, it));
			}
			if (it.getItem() instanceof FocusPouchItem) {
				pouches.put(++pouchcount, ax);
				List<ItemStack> pinv = FocusPouchItem.getInventory(it);
				for (int qx = 0; qx < pinv.size(); qx++) {
					ItemStack f2 = pinv.get(qx);
					if (f2.getItem() instanceof FocusItem f) {
						String k = f.getSortingHelper(f2);
						foci.put(k, new Found(k, qx + pouchcount * 1000, f2));
					}
				}
			}
		}
		return foci;
	}

	/** WandManager.changeFocus: fit the focus with this sorting key (or the next one along), stowing the old focus; "REMOVE" just stows it. */
	public static void changeFocus(ItemStack wand, Player player, String focus) {
		Map<Integer, Integer> pouches = new HashMap<>();
		TreeMap<String, Found> foci = carriedFoci(player, pouches);
		if (!focus.equals("REMOVE") && !foci.isEmpty()) {
			String newkey = focus;
			if (!foci.containsKey(focus)) {
				newkey = foci.higherKey(focus);
			}
			if (newkey == null || !foci.containsKey(newkey)) {
				newkey = foci.firstKey();
			}
			int loc = foci.get(newkey).location();
			ItemStack item = null;
			if (loc >= 0 && loc < 1000) {
				item = player.getInventory().getItem(loc).copy();
			} else {
				int pid = loc / 1000;
				if (pouches.containsKey(pid)) {
					item = fetchFocusFromPouch(player, loc - pid * 1000, pouches.get(pid));
				}
			}
			if (item == null || item.isEmpty()) {
				return;
			}
			if (loc >= 0 && loc < 1000) {
				player.getInventory().setItem(loc, ItemStack.EMPTY);
			}
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.CAMERATICKS, SoundSource.PLAYERS, 0.3F, 1.0F);
			ItemStack old = WandItem.getFocusStack(wand);
			if (!old.isEmpty() && (addFocusToPouch(player, old.copy(), pouches) || player.getInventory().add(old.copy()))) {
				WandItem.setFocus(wand, ItemStack.EMPTY);
			}
			if (WandItem.getFocusStack(wand).isEmpty()) {
				WandItem.setFocus(wand, item);
			} else if (!addFocusToPouch(player, item, pouches)) {
				player.getInventory().add(item);
			}
		} else {
			ItemStack old = WandItem.getFocusStack(wand);
			if (!old.isEmpty() && (addFocusToPouch(player, old.copy(), pouches) || player.getInventory().add(old.copy()))) {
				WandItem.setFocus(wand, ItemStack.EMPTY);
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), TVSounds.CAMERATICKS, SoundSource.PLAYERS, 0.3F, 0.9F);
			}
		}
	}

	private static ItemStack pouchAt(Player player, int pouchslot) {
		if (pouchslot >= 0) {
			return player.getInventory().getItem(pouchslot);
		}
		List<Baubles.Worn> worn = Baubles.wornSlots(player);
		return -pouchslot - 1 < worn.size() ? worn.get(-pouchslot - 1).stack() : ItemStack.EMPTY;
	}

	private static void putPouch(Player player, int pouchslot, ItemStack pouch) {
		if (pouchslot >= 0) {
			player.getInventory().setItem(pouchslot, pouch);
		} else {
			List<Baubles.Worn> worn = Baubles.wornSlots(player);
			if (-pouchslot - 1 < worn.size()) {
				worn.get(-pouchslot - 1).set().accept(pouch);
			}
		}
	}

	private static ItemStack fetchFocusFromPouch(Player player, int focusid, int pouchslot) {
		ItemStack pouch = pouchAt(player, pouchslot).copy();
		List<ItemStack> inv = new ArrayList<>(FocusPouchItem.getInventory(pouch));
		ItemStack contents = inv.get(focusid);
		if (!(contents.getItem() instanceof FocusItem)) {
			return null;
		}
		inv.set(focusid, ItemStack.EMPTY);
		FocusPouchItem.setInventory(pouch, inv);
		putPouch(player, pouchslot, pouch);
		return contents.copy();
	}

	private static boolean addFocusToPouch(Player player, ItemStack focus, Map<Integer, Integer> pouches) {
		for (int pouchslot : pouches.values()) {
			ItemStack pouch = pouchAt(player, pouchslot).copy();
			List<ItemStack> inv = new ArrayList<>(FocusPouchItem.getInventory(pouch));
			for (int q = 0; q < inv.size(); q++) {
				if (inv.get(q).isEmpty()) {
					inv.set(q, focus.copy());
					FocusPouchItem.setInventory(pouch, inv);
					putPouch(player, pouchslot, pouch);
					return true;
				}
			}
		}
		return false;
	}

	/** The radial menu's list: one stack per sorting key, in key order. */
	public static LinkedHashMap<String, ItemStack> radialFoci(Player player) {
		LinkedHashMap<String, ItemStack> out = new LinkedHashMap<>();
		for (var e : carriedFoci(player, new HashMap<>()).entrySet()) {
			out.put(e.getKey(), e.getValue().stack());
		}
		return out;
	}

	// ---------------------------------------------------------------- architect area

	public static void toggleMisc(ItemStack wand, Player player) {
		ItemStack fs = WandItem.getFocusStack(wand);
		if (!(fs.getItem() instanceof FocusItem focus) || !(focus instanceof Architect) || !focus.isUpgradedWith(fs, FocusUpgradeType.architect)) {
			return;
		}
		int dim = getAreaDim(wand);
		if (player.isShiftKeyDown()) {
			if (++dim > (focus instanceof TradeFocusItem ? 2 : 3)) {
				dim = 0;
			}
			setArea(wand, getRaw(wand, 0, -1), getRaw(wand, 1, -1), getRaw(wand, 2, -1), dim);
		} else {
			int max = focus.getMaxAreaSize(fs);
			int ax = getAreaX(wand);
			int ay = getAreaY(wand);
			int az = getAreaZ(wand);
			if (dim == 0) {
				ax++;
				az++;
				ay++;
			} else if (dim == 1) {
				ax++;
			} else if (dim == 2) {
				az++;
			} else if (dim == 3) {
				ay++;
			}
			setArea(wand, ax > max ? 0 : ax, ay > max ? 0 : ay, az > max ? 0 : az, dim);
		}
	}

	private static int getRaw(ItemStack wand, int i, int def) {
		List<Integer> l = wand.get(TVComponents.WAND_AREA);
		return l == null || l.size() <= i ? def : l.get(i);
	}

	private static void setArea(ItemStack wand, int x, int y, int z, int dim) {
		wand.set(TVComponents.WAND_AREA, List.of(x, y, z, dim));
	}

	public static int getAreaDim(ItemStack wand) {
		return Math.max(0, getRaw(wand, 3, 0));
	}

	private static int area(ItemStack wand, int i) {
		ItemStack fs = WandItem.getFocusStack(wand);
		int max = fs.getItem() instanceof FocusItem f ? f.getMaxAreaSize(fs) : 1;
		int a = getRaw(wand, i, -1);
		return a < 0 ? max : Math.min(a, max);
	}

	public static int getAreaX(ItemStack wand) {
		return area(wand, 0);
	}

	public static int getAreaY(ItemStack wand) {
		return area(wand, 1);
	}

	public static int getAreaZ(ItemStack wand) {
		return area(wand, 2);
	}

	private WandFoci() {
	}
}
