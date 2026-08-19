package com.togglebuttons;

/*
* Toggles visibility of all buttons via the
* show/hide hotkey from the plugin config
* Also stored as radio dial value
*/

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.util.HotkeyListener;

@Singleton
class ToggleButtonsVisibility
{
	private final KeyManager keyManager;
	private final HotkeyListener hotkeyListener;

	@Inject
	ToggleButtonsVisibility(KeyManager keyManager, ToggleButtonsConfig config, ConfigManager configManager)
	{
		this.keyManager = keyManager;
		this.hotkeyListener = new HotkeyListener(config::hotkey)
		{
			@Override
			public void hotkeyPressed()
			{
				configManager.setConfiguration(ToggleButtonsButtonStore.CONFIG_GROUP, "showButtons", !config.showButtons());
			}
		};
	}

	void startUp()
	{
		keyManager.registerKeyListener(hotkeyListener);
	}

	void shutDown()
	{
		keyManager.unregisterKeyListener(hotkeyListener);
	}
}
