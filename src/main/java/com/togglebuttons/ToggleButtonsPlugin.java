package com.togglebuttons;

import com.google.inject.Provides;

import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.swing.SwingUtilities;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

import net.runelite.client.game.ItemManager;

import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseManager;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.client.util.HotkeyListener;

import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Slf4j
@PluginDescriptor(
	name = "Toggle Buttons"
)
public class ToggleButtonsPlugin extends Plugin
{
	// Local API injections
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private MouseManager mouseManager;
	@Inject
	private KeyManager keyManager;
	@Inject
	private ToggleButtonsConfig config;
	@Inject
	private ConfigManager configManager;
	@Inject
	private ItemManager itemManager;
	@Inject
	private ToggleButtonsToggle toggle;
	@Inject
	private ToggleButtonsButtonStore buttonStore;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private ToggleButtonsPluginPanel panel;

	// Local Vars
	private final Map<String, ToggleButtonsOverlay> overlays = new LinkedHashMap<>();
	private NavigationButton navButton;

	@Setter
	@Getter
	private boolean navButtonIsSelected;

	private final MouseAdapter mouseAdapter = new MouseAdapter()
	{
		@Override
		public MouseEvent mousePressed(MouseEvent e)
		{
			if (!config.showButtons() ||
				!SwingUtilities.isLeftMouseButton(e) ||
				e.isAltDown())
			{
				return e;
			}

			for (ToggleButtonsOverlay overlay : overlays.values())
			{
				if (overlay.getBounds().contains(e.getPoint()))
				{
					overlay.setPressed(true);
					log.debug("Button '{}' activated via mouse", overlay.getButton().getName());
					toggle.toggleAll(overlay.getButton().getTargets());
					e.consume();
					break;
				}
			}
			return e;
		}

		@Override
		public MouseEvent mouseReleased(MouseEvent e)
		{
			if (SwingUtilities.isLeftMouseButton(e))
			{
				for (ToggleButtonsOverlay overlay : overlays.values())
				{
					overlay.setPressed(false);
				}
			}
			return e;
		}
	};

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.hotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			configManager.setConfiguration("togglebuttons", "showButtons", !config.showButtons());
		}
	};

	@Override
	protected void startUp() throws Exception
	{
		buttonStore.migrateLegacy();
		rebuildOverlays();
		mouseManager.registerMouseListener(mouseAdapter);
		keyManager.registerKeyListener(hotkeyListener);
		log.debug("Toggle Buttons started!");

		// Adds button to sidebar with icon
		final BufferedImage sidebarIcon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		this.navButtonIsSelected = false;
		navButton = NavigationButton.builder()
			.tooltip("Toggle Buttons")
			.icon(sidebarIcon)
			.priority(6)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		SwingUtilities.invokeLater(panel::rebuild);
	}

	@Override
	protected void shutDown() throws Exception
	{
		removeOverlays();
		mouseManager.unregisterMouseListener(mouseAdapter);
		keyManager.unregisterKeyListener(hotkeyListener);
		clientToolbar.removeNavigation(navButton);
		log.debug("Toggle Buttons stopped!");
	}

	private void rebuildOverlays()
	{
		removeOverlays();
		for (ToggleButtonsButton button : buttonStore.getButtons())
		{
			final ToggleButtonsOverlay overlay = new ToggleButtonsOverlay(config, button);
			if (button.getIconItemId() >= 0)
			{
				overlay.setIcon(itemManager.getImage(button.getIconItemId()));
			}
			overlays.put(button.getId(), overlay);
			overlayManager.add(overlay);
		}
	}

	private void removeOverlays()
	{
		for (ToggleButtonsOverlay overlay : overlays.values())
		{
			overlayManager.remove(overlay);
		}
		overlays.clear();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!"togglebuttons".equals(event.getGroup()))
		{
			return;
		}

		if (ToggleButtonsButtonStore.BUTTONS_KEY.equals(event.getKey()))
		{
			rebuildOverlays();
		}
	}

	@Provides
	ToggleButtonsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ToggleButtonsConfig.class);
	}
}
