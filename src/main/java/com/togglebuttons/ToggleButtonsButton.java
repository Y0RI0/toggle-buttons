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
	static final int MAX_TARGETS = 50;
	static final int MAX_BUTTONS = 100;

	private String id;
	private String name = "Button";
	private int iconItemId = -1;
	// Absolute path to a local image file; takes priority over iconItemId
	private String iconImagePath;
	private int width = DEFAULT_WIDTH;
	private int height = DEFAULT_HEIGHT;
	private boolean resizable;
	private int buttonColor = ToggleButtonsStyle.DEFAULT_BUTTON_COLOR;
	private int pressedColor = ToggleButtonsStyle.DEFAULT_PRESSED_COLOR;
	private ToggleButtonsShape shape = ToggleButtonsShape.ROUNDED_RECTANGLE;
	private boolean toggleWhileHeld;
	private List<ToggleButtonsTarget> targets = new ArrayList<>();
}
