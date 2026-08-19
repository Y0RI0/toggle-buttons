package com.togglebuttons;

import com.google.inject.Provides;
import java.awt.event.MouseEvent;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@Slf4j
@PluginDescriptor(
	name = "Toggle Buttons"
)
public class ToggleButtonsPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ToggleButtonsOverlay overlay;

	@Inject
	private ToggleButtonsConfig config;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ChatboxItemSearch itemSearch;

	@Inject
	private PluginManager pluginManager;

	private final MouseAdapter mouseAdapter = new MouseAdapter()
	{
		@Override
		public MouseEvent mousePressed(MouseEvent e)
		{
			if (config.showButton()
				&& SwingUtilities.isLeftMouseButton(e)
				&& !e.isAltDown()
				&& overlay.getBounds().contains(e.getPoint()))
			{
				overlay.setPressed(true);
				buttonActivated("mouse");
				e.consume();
			}
			return e;
		}

		@Override
		public MouseEvent mouseReleased(MouseEvent e)
		{
			if (overlay.isPressed() && SwingUtilities.isLeftMouseButton(e))
			{
				overlay.setPressed(false);
			}
			return e;
		}
	};

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.hotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			overlay.setPressed(true);
			buttonActivated("hotkey");
		}

		@Override
		public void hotkeyReleased()
		{
			overlay.setPressed(false);
		}
	};

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		mouseManager.registerMouseListener(mouseAdapter);
		keyManager.registerKeyListener(hotkeyListener);
		updateIcon();
		log.debug("Toggle Buttons started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		mouseManager.unregisterMouseListener(mouseAdapter);
		keyManager.unregisterKeyListener(hotkeyListener);
		overlay.setPressed(false);
		log.debug("Toggle Buttons stopped!");
	}

	private void buttonActivated(String source)
	{
		log.debug("Button activated via {}", source);
		toggleTargetPlugin();
	}

	private void toggleTargetPlugin()
	{
		final String target = config.targetPlugin().trim();
		if (target.isEmpty())
		{
			return;
		}

		SwingUtilities.invokeLater(() ->
		{
			for (Plugin plugin : pluginManager.getPlugins())
			{
				if (plugin == this)
				{
					continue;
				}

				final PluginDescriptor descriptor = plugin.getClass().getAnnotation(PluginDescriptor.class);
				if (descriptor == null || !descriptor.name().equalsIgnoreCase(target))
				{
					continue;
				}

				try
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
					log.debug("Toggled plugin '{}' to {}", descriptor.name(), !enabled);
				}
				catch (PluginInstantiationException ex)
				{
					log.warn("Failed to toggle plugin '{}'", target, ex);
				}
				return;
			}

			log.debug("No plugin found matching '{}'", target);
		});
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!"togglebuttons".equals(event.getGroup()))
		{
			return;
		}

		if ("selectIcon".equals(event.getKey()) && config.selectIcon())
		{
			configManager.setConfiguration("togglebuttons", "selectIcon", false);
			openIconSearch();
			return;
		}

		updateIcon();
	}

	private void openIconSearch()
	{
		clientThread.invokeLater(() ->
		{
			if (client.getGameState() != GameState.LOGGED_IN)
			{
				log.debug("Cannot open icon search while logged out");
				return;
			}

			itemSearch
				.tooltipText("Set button icon")
				.onItemSelected(itemId -> configManager.setConfiguration("togglebuttons", "iconItemId", itemId))
				.build();
		});
	}

	private void updateIcon()
	{
		final int itemId = config.iconItemId();
		overlay.setIcon(itemId >= 0 ? itemManager.getImage(itemId) : null);
	}

	@Provides
	ToggleButtonsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ToggleButtonsConfig.class);
	}
}
