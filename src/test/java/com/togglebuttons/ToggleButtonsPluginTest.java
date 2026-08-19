package com.togglebuttons;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ToggleButtonsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ToggleButtonsPlugin.class);
		RuneLite.main(args);
	}
}
