package dev.neothaum.client;

import dev.neothaum.NeoThaum;
import net.minecraft.client.KeyMapping;

/** NeoThaum's own section on the controls screen, holding the focus, wand-misc and harness keys. */
public final class TVKeys {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(NeoThaum.id("neothaum"));

	private TVKeys() {
	}
}
