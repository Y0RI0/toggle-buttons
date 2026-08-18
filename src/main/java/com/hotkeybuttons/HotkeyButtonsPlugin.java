package com.hotkeybuttons;

import com.google.inject.Provides;
import java.awt.event.MouseEvent;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@Slf4j
@PluginDescriptor(
	name = "Hotkey Buttons"
)
public class HotkeyButtonsPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private HotkeyButtonsOverlay overlay;

	@Inject
	private HotkeyButtonsConfig config;

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
		log.debug("Hotkey Buttons started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		mouseManager.unregisterMouseListener(mouseAdapter);
		keyManager.unregisterKeyListener(hotkeyListener);
		overlay.setPressed(false);
		log.debug("Hotkey Buttons stopped!");
	}

	private void buttonActivated(String source)
	{
		log.debug("Button activated via {}", source);
	}

	@Provides
	HotkeyButtonsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HotkeyButtonsConfig.class);
	}
}
