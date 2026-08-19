package com.togglebuttons;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

// Defines 1 clickable "button" object's settings
@Data
class ToggleButtonsButton
{
	static final int DEFAULT_WIDTH = 40;
	static final int DEFAULT_HEIGHT = 40;
	static final int DEFAULT_BUTTON_COLOR = 0x800080FF;
	static final int DEFAULT_PRESSED_COLOR = 0xD260C0FF;

	private String id;
	private String name = "Button";
	private int iconItemId = -1;
	private int width = DEFAULT_WIDTH;
	private int height = DEFAULT_HEIGHT;
	private boolean resizable;
	private int buttonColor = DEFAULT_BUTTON_COLOR;
	private int pressedColor = DEFAULT_PRESSED_COLOR;
	private List<ToggleButtonsTarget> targets = new ArrayList<>();
}
