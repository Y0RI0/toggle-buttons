package com.togglebuttons;

// Left click behavior for buttons in the sidebar grid
// Public because it is returned by the ToggleButtonsConfig proxy
public enum ToggleButtonsSidebarClickMode
{
	EDIT("Open editor"),
	EXECUTE("Execute button");

	private final String displayName;

	ToggleButtonsSidebarClickMode(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
