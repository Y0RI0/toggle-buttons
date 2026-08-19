package com.togglebuttons;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
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
	private final ToggleButtonsButton button;

	@Getter
	@Setter
	private volatile boolean pressed;

	@Setter
	private volatile BufferedImage icon;

	ToggleButtonsOverlay(ToggleButtonsConfig config, ToggleButtonsButton button)
	{
		this.config = config;
		this.button = button;
		setPosition(OverlayPosition.TOP_LEFT);
		setMovable(true);
		getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, "Configure", "Toggle Buttons"));
	}

	@Override
	public String getName()
	{
		return "toggleButtonsButton_" + button.getId();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showButtons())
		{
			return null;
		}

		if (isResizable() != button.isResizable())
		{
			setResizable(button.isResizable());
		}

		int width = button.getWidth();
		int height = button.getHeight();

		final Dimension preferred = getPreferredSize();
		if (button.isResizable() && preferred != null)
		{
			width = Math.max(MIN_SIZE, preferred.width);
			height = Math.max(MIN_SIZE, preferred.height);
		}

		graphics.setColor(new Color(pressed ? button.getPressedColor() : button.getButtonColor(), true));
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
