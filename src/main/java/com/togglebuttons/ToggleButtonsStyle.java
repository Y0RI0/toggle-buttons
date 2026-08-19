package com.togglebuttons;

/*
* Central styling constants for the plugin's UI.
*/

import java.awt.Color;

import net.runelite.client.ui.ColorScheme;

final class ToggleButtonsStyle
{
	// "Runescape" text yellow: sidebar panel title text, scrollbar thumb border
	static final Color RUNESCAPE_YELLOW = new Color(0xFFFF00);

	// Primary text: title-adjacent button text ("Add Button +", "Remove button")
	static final Color TEXT_COLOR = Color.WHITE;

	// Panel background gray: header row, "Add Button +" background,
	// button grid + its wrapper, scrollbar thumb body
	static final Color PANEL_BACKGROUND = ColorScheme.DARK_GRAY_COLOR;

	// Darker accent gray: grid cell backgrounds, grid/editor borders,
	// per-plugin target row backgrounds, scrollbar track
	static final Color PANEL_ACCENT = ColorScheme.DARKER_GRAY_COLOR;

	// Selection highlight: border around the selected grid cell
	static final Color SELECTED_BORDER = ColorScheme.BRAND_ORANGE;

	// Destructive action red: "Remove button" background
	// Decidedly unfriendly to the red-green color blind folks. I'm sorry please forgive me.
	static final Color DESTRUCTIVE_BACKGROUND = new Color(0x802020);

	// Default in-game button fill colors for newly created buttons
	static final int DEFAULT_BUTTON_COLOR = 0x800080FF;
	static final int DEFAULT_PRESSED_COLOR = 0xD260C0FF;

	// Tooltips: wrap width in pixels and how long they stay on screen
	static final int TOOLTIP_WRAP_WIDTH = 80;
	static final int TOOLTIP_DISMISS_MS = 20000;

	private ToggleButtonsStyle()
	{
	}

	// Wraps tooltip text to TOOLTIP_WRAP_WIDTH so long tooltips are readable
	static String tooltip(String text)
	{
		return "<html><body style='width: " + TOOLTIP_WRAP_WIDTH + "px'>" + text + "</body></html>";
	}
}
