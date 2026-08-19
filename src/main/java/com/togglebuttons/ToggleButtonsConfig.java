package com.togglebuttons;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

@ConfigGroup("togglebuttons")
public interface ToggleButtonsConfig extends Config
{
	@ConfigItem(
		keyName = "showButton",
		name = "Show button",
		description = "Whether the button is drawn on screen",
		position = 0
	)
	default boolean showButton()
	{
		return true;
	}

	@Range(min = 16, max = 256)
	@ConfigItem(
		keyName = "buttonWidth",
		name = "Width",
		description = "Fixed width of the button in pixels",
		position = 1
	)
	default int buttonWidth()
	{
		return 40;
	}

	@Range(min = 16, max = 256)
	@ConfigItem(
		keyName = "buttonHeight",
		name = "Height",
		description = "Fixed height of the button in pixels",
		position = 2
	)
	default int buttonHeight()
	{
		return 40;
	}

	@ConfigItem(
		keyName = "hotkey",
		name = "Hotkey",
		description = "Keybind that activates the button",
		position = 3
	)
	default Keybind hotkey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		keyName = "resizable",
		name = "Resizable",
		description = "Allow resizing the button by alt-dragging its edges",
		position = 4
	)
	default boolean resizable()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		keyName = "buttonColor",
		name = "Button color",
		description = "Fill color of the button",
		position = 5
	)
	default Color buttonColor()
	{
		return new Color(0, 128, 255, 128);
	}

	@Alpha
	@ConfigItem(
		keyName = "pressedColor",
		name = "Pressed color",
		description = "Fill color of the button while pressed",
		position = 6
	)
	default Color pressedColor()
	{
		return new Color(96, 192, 255, 210);
	}

	@ConfigItem(
		keyName = "iconItemId",
		name = "Icon item ID",
		description = "Item whose sprite is drawn on the button (-1 for none); set via right-click > Set icon",
		position = 7
	)
	default int iconItemId()
	{
		return -1;
	}

	@ConfigItem(
		keyName = "selectIcon",
		name = "Select icon",
		description = "Toggle on to search for an item to use as the button icon (requires being logged in)",
		position = 8
	)
	default boolean selectIcon()
	{
		return false;
	}

	@ConfigItem(
		keyName = "targetPlugin",
		name = "Target plugin",
		description = "Name of the plugin to toggle on/off when the button is pressed (as shown in the plugin list, e.g. 'Inventory Viewer')",
		position = 9
	)
	default String targetPlugin()
	{
		return "";
	}
}
