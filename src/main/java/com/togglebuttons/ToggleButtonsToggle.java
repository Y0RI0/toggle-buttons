package com.togglebuttons;

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

		try
		{
			if (target.isDisablePlugin())
			{
				final boolean enabled = pluginManager.isPluginEnabled(plugin);
				pluginManager.setPluginEnabled(plugin, !enabled);
				if (enabled)
				{
					pluginManager.stopPlugin(plugin);
				}
				else
				{
					pluginManager.startPlugin(plugin);
				}
				log.debug("Toggled plugin '{}' enabled to {}", name, !enabled);
			}
			else
			{
				if (pluginManager.isPluginActive(plugin))
				{
					pluginManager.stopPlugin(plugin);
					log.debug("Stopped plugin '{}'", name);
				}
				else
				{
					final boolean started = pluginManager.startPlugin(plugin);
					log.debug("Started plugin '{}': {}", name, started);
				}
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
