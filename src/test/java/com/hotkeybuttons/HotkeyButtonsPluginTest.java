package com.hotkeybuttons;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HotkeyButtonsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HotkeyButtonsPlugin.class);
		RuneLite.main(args);
	}
}
