package com.togglebuttons;

// mentally I'm considering this "main"

import com.google.inject.Provides;

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

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

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
	private ToggleButtonsConfig config;
	@Inject
	private ItemManager itemManager;
	@Inject
	private ToggleButtonsButtonStore buttonStore;
	@Inject
	private ToggleButtonsMouseBehavior mouseBehavior;
	@Inject
	private ToggleButtonsVisibility visibility;
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

	@Override
	protected void startUp() throws Exception
	{
		rebuildOverlays();
		mouseBehavior.setOverlaySupplier(overlays::values);
		mouseBehavior.startUp();
		visibility.startUp();

		// Adds button to sidebar with icon
		final BufferedImage sidebarIcon = ImageUtil.loadImageResource(getClass(), "panel_icon.png");
		this.navButtonIsSelected = false;
		navButton = NavigationButton.builder()
			.tooltip("Toggle Buttons")
			.icon(sidebarIcon)
			.priority(6)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);
		SwingUtilities.invokeLater(panel::rebuild);
		log.debug("Toggle Buttons started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		removeOverlays();
		mouseBehavior.shutDown();
		visibility.shutDown();
		clientToolbar.removeNavigation(navButton);
		log.debug("Toggle Buttons stopped!");
	}

	private void rebuildOverlays()
	{
		removeOverlays();
		for (ToggleButtonsButton button : buttonStore.getButtons())
		{
			final ToggleButtonsOverlay overlay = new ToggleButtonsOverlay(config, button);
			// A local image takes priority over a searched item icon; never both
			final java.awt.image.BufferedImage fileImage = ToggleButtonsImageLoader.load(button.getIconImagePath());
			if (fileImage != null)
			{
				overlay.setIcon(fileImage);
			}
			else if (button.getIconItemId() >= 0)
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

		if ("sidebarClickMode".equals(event.getKey()))
		{
			SwingUtilities.invokeLater(panel::rebuild);
		}
	}

	@Provides
	ToggleButtonsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ToggleButtonsConfig.class);
	}
}
