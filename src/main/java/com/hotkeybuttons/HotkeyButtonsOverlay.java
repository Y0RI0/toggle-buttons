package com.hotkeybuttons;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;

public class HotkeyButtonsOverlay extends Overlay
{
	private static final int MIN_SIZE = 16;

	private final HotkeyButtonsConfig config;

	@Getter
	@Setter
	private volatile boolean pressed;

	@Inject
	private HotkeyButtonsOverlay(HotkeyButtonsConfig config)
	{
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		setMovable(true);
		getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, "Configure", "Hotkey Buttons"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showButton())
		{
			return null;
		}

		if (isResizable() != config.resizable())
		{
			setResizable(config.resizable());
		}

		int width = config.buttonWidth();
		int height = config.buttonHeight();

		final Dimension preferred = getPreferredSize();
		if (config.resizable() && preferred != null)
		{
			width = Math.max(MIN_SIZE, preferred.width);
			height = Math.max(MIN_SIZE, preferred.height);
		}

		graphics.setColor(pressed ? config.pressedColor() : config.buttonColor());
		graphics.fillRect(0, 0, width, height);

		return new Dimension(width, height);
	}
}
