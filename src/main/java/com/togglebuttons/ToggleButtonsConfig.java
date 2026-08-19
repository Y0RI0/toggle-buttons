package com.togglebuttons;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("togglebuttons")
public interface ToggleButtonsConfig extends Config
{
	@ConfigItem(
		keyName = "showButtons",
		name = "Show buttons",
		description = "Whether the buttons are drawn on screen",
		position = 0
	)
	default boolean showButtons()
	{
		return true;
	}

	@ConfigItem(
		keyName = "hotkey",
		name = "Show/hide buttons hotkey",
		description = "Keybind that shows or hides all buttons",
		position = 1
	)
	default Keybind hotkey()
	{
		return Keybind.NOT_SET;
	}
}
