package dev.neothaum.client.addon.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.neothaum.client.gui.NeoThaumConfigScreen;

public class NeoThaumModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return NeoThaumConfigScreen::new;
	}
}
