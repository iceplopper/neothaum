package dev.thaumvibe.client;

import dev.thaumvibe.client.mixin.ContainerScreenAccessor;
import dev.thaumvibe.items.ThaumometerItem;
import dev.thaumvibe.network.TVNetwork;
import dev.thaumvibe.registry.TVSounds;
import dev.thaumvibe.research.ScanManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.Slot;

/**
 * Thaumvibe addition (later Thaumcraft versions had it, TC4 did not): carry the thaumometer on the cursor and hold it over an item in an
 * inventory to scan that item. It takes as long as an in-world scan (the thaumometer's 25-tick use) and ticks the same way.
 */
public final class ThaumometerInventoryScan {
	private static final int SCAN_TICKS = 25;
	private static Slot slot;
	private static int ticks;

	public static void tick(Minecraft mc) {
		if (mc.player == null || !(mc.gui.screen() instanceof AbstractContainerScreen<?> screen)
			|| !(screen.getMenu().getCarried().getItem() instanceof ThaumometerItem)) {
			slot = null;
			return;
		}
		Slot hovered = ((ContainerScreenAccessor) screen).thaumvibe$hoveredSlot();
		if (hovered == null || !hovered.hasItem()) {
			slot = null;
			return;
		}
		ScanManager.ScanResult scan = ScanManager.ScanResult.item(hovered.getItem().getItem());
		if (!ScanManager.isValidScanTarget(mc.player, scan, "@")) {
			slot = null;
			return;
		}
		if (hovered != slot) {
			slot = hovered;
			ticks = 0;
		}
		ticks++;
		if (ticks % 2 == 0) {
			mc.player.playSound(TVSounds.CAMERATICKS, 0.2F, 0.45F + mc.player.getRandom().nextFloat() * 0.1F);
		}
		if (ticks >= SCAN_TICKS) {
			slot = null;
			var err = ScanManager.validScanError(ScanManager.getScanAspects(scan, mc.level), mc.player);
			if (err != null) {
				TVNetwork.clientNotify(err);
			} else {
				TVNetwork.sendScanToServer(scan);
			}
		}
	}

	private ThaumometerInventoryScan() {
	}
}
