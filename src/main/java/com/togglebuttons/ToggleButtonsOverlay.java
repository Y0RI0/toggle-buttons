package com.togglebuttons;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.MenuAction;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPosition;

public class ToggleButtonsOverlay extends Overlay
{
	private static final int MIN_SIZE = 16;

	private final ToggleButtonsConfig config;

	@Getter
	@Setter
	private volatile boolean pressed;

	@Setter
	private volatile BufferedImage icon;

	@Inject
	private ToggleButtonsOverlay(ToggleButtonsConfig config)
	{
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		setMovable(true);
		getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, "Configure", "Toggle Buttons"));
		getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY, "Set icon", "Toggle Buttons"));
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

		final BufferedImage img = icon;
		if (img != null)
		{
			final double scale = Math.min((double) width / img.getWidth(), (double) height / img.getHeight());
			final int iw = (int) (img.getWidth() * scale);
			final int ih = (int) (img.getHeight() * scale);
			graphics.drawImage(img, (width - iw) / 2, (height - ih) / 2, iw, ih, null);
		}

		return new Dimension(width, height);
	}
}
