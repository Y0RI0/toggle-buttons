package com.togglebuttons;

// Available shapes for a button overlay
enum ToggleButtonsShape
{
	ROUNDED_RECTANGLE("Rounded rectangle"),
	SQUARE("Square"),
	CIRCLE("Circle"),
	DIAMOND("Diamond"),
	TRIANGLE_UP("Triangle (up)"),
	TRIANGLE_DOWN("Triangle (down)"),
	HEXAGON("Hexagon"),
	OCTAGON("Octagon"),
	PARALLELOGRAM("Parallelogram"),
	STAR("Star"),
	GEAR("Gear");

	private final String displayName;

	ToggleButtonsShape(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
