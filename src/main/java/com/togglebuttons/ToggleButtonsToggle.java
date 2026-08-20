package com.togglebuttons;

/*
* Logic which handles *what* the toggle button targets to toggle
*/

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import javax.swing.SwingUtilities;

import lombok.extern.slf4j.Slf4j;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;

@Slf4j
@Singleton
class ToggleButtonsToggle
{
	private final PluginManager pluginManager;

	@Inject
	ToggleButtonsToggle(PluginManager pluginManager)
	{
		this.pluginManager = pluginManager;
	}

	void toggleAll(List<ToggleButtonsTarget> targets)
	{
		if (targets == null || targets.isEmpty())
		{
			return;
		}

		SwingUtilities.invokeLater(() ->
		{
			for (ToggleButtonsTarget target : targets)
			{
				toggleOnEdt(target);
			}
		});
	}

	// Targets held for reversion by a pressed toggle-while-held (peek) button
	private List<ToggleButtonsTarget> heldTargets;

	// Shared press semantics for the game overlay and the sidebar grid:
	// toggles the button's targets, and remembers them for reversion on
	// release when the button is in toggle-while-held (peek) mode
	void press(ToggleButtonsButton button)
	{
		log.debug("Button '{}' pressed", button.getName());
		toggleAll(button.getTargets());
		heldTargets = button.isToggleWhileHeld() ? button.getTargets() : null;
	}

	// Shared release semantics: reverts a peek button's toggles, no-op otherwise
	void release()
	{
		if (heldTargets != null)
		{
			log.debug("Peek button released, reverting toggles");
			toggleAll(heldTargets);
			heldTargets = null;
		}
	}

	List<String> getTogglablePluginNames()
	{
		final TreeSet<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		for (Plugin plugin : pluginManager.getPlugins())
		{
			if (plugin instanceof ToggleButtonsPlugin)
			{
				continue;
			}

			final PluginDescriptor descriptor = plugin.getClass().getAnnotation(PluginDescriptor.class);
			if (descriptor != null && !descriptor.hidden())
			{
				names.add(descriptor.name());
			}
		}
		return new ArrayList<>(names);
	}

	private void toggleOnEdt(ToggleButtonsTarget target)
	{
		final String name = target.getPluginName() == null ? "" : target.getPluginName().trim();
		if (name.isEmpty())
		{
			return;
		}

		final Plugin plugin = findPlugin(name);
		if (plugin == null)
		{
			log.debug("No plugin found matching '{}'", name);
			return;
		}

		final Mode mode = target.isDisablePlugin() ? Mode.ENABLE_DISABLE : Mode.ON_OFF;
		switch (mode)
		{
			case ENABLE_DISABLE:
				toggleEnabled(plugin, name);
				break;
			case ON_OFF:
				toggleRunning(plugin, name);
				break;
		}
	}

	private enum Mode
	{
		ENABLE_DISABLE,
		ON_OFF
	}

	// Enable/disable mode: the click flips the plugin's enabled setting in
	// RuneLite (the plugin-list checkbox), starting or stopping it to match
	private void toggleEnabled(Plugin plugin, String name)
	{
		final boolean enable = !pluginManager.isPluginEnabled(plugin);
		pluginManager.setPluginEnabled(plugin, enable);
		try
		{
			if (enable)
			{
				pluginManager.startPlugin(plugin);
			}
			else
			{
				pluginManager.stopPlugin(plugin);
			}
			log.debug("Toggled plugin '{}' enabled to {}", name, enable);
		}
		catch (PluginInstantiationException ex)
		{
			log.warn("Failed to toggle plugin '{}'", name, ex);
		}
	}

	// On/off mode: the click only toggles the plugin's running state, but
	// enables the plugin first so starting always works
	private void toggleRunning(Plugin plugin, String name)
	{
		final boolean start = !pluginManager.isPluginActive(plugin);
		pluginManager.setPluginEnabled(plugin, true);
		try
		{
			if (start)
			{
				pluginManager.startPlugin(plugin);
				log.debug("Started plugin '{}'", name);
			}
			else
			{
				pluginManager.stopPlugin(plugin);
				log.debug("Stopped plugin '{}'", name);
			}
		}
		catch (PluginInstantiationException ex)
		{
			log.warn("Failed to toggle plugin '{}'", name, ex);
		}
	}

	private Plugin findPlugin(String name)
	{
		for (Plugin plugin : pluginManager.getPlugins())
		{
			if (plugin instanceof ToggleButtonsPlugin)
			{
				continue;
			}

			final PluginDescriptor descriptor = plugin.getClass().getAnnotation(PluginDescriptor.class);
			if (descriptor != null && descriptor.name().equalsIgnoreCase(name))
			{
				return plugin;
			}
		}
		return null;
	}
}
