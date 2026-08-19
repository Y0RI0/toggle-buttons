package com.togglebuttons;

/*
* Game window button overlay rendering logic
*/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
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

	// Mostly borrowed from prior art in
	// https://github.com/StationEarthxo/Stations-Cozy-Carts/blob/master/src/main/java/com/cartmount/CartToggleOverlay.java
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

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(new Color(pressed ? button.getPressedColor() : button.getButtonColor(), true));
		graphics.fill(buildShape(button.getShape(), width, height));

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

	private static Shape buildShape(ToggleButtonsShape shape, int w, int h)
	{
		switch (shape)
		{
		case SQUARE:
			return new Rectangle2D.Double(0, 0, w, h);
		case CIRCLE:
			final int d = Math.min(w, h);
			return new Ellipse2D.Double((w - d) / 2.0, (h - d) / 2.0, d, d);
		case DIAMOND:
			return polygon(new double[] { w / 2.0, w, w / 2.0, 0 }, new double[] { 0, h / 2.0, h, h / 2.0 });
		case TRIANGLE_UP:
			return polygon(new double[] { w / 2.0, w, 0 }, new double[] { 0, h, h });
		case TRIANGLE_DOWN:
			return polygon(new double[] { 0, w, w / 2.0 }, new double[] { 0, 0, h });
		case HEXAGON:
			return polygon(
				new double[] { w * 0.25, w * 0.75, w, w * 0.75, w * 0.25, 0 },
				new double[] { 0, 0, h / 2.0, h, h, h / 2.0 });
		case OCTAGON:
			final double c = Math.min(w, h) / 3.0;
			return polygon(
				new double[] { c, w - c, w, w, w - c, c, 0, 0 },
				new double[] { 0, 0, c, h - c, h, h, h - c, c });
		case PARALLELOGRAM:
			final double skew = w / 4.0;
			return polygon(new double[] { skew, w, w - skew, 0 }, new double[] { 0, 0, h, h });
		case STAR:
			return star(w, h);
		case GEAR:
			return gear(w, h);
		case ROUNDED_RECTANGLE:
		default:
			final int arc = Math.min(w, h) / 4;
			return new RoundRectangle2D.Double(0, 0, w, h, arc, arc);
		}
	}

	private static Shape polygon(double[] xs, double[] ys)
	{
		final Path2D.Double path = new Path2D.Double();
		path.moveTo(xs[0], ys[0]);
		for (int i = 1; i < xs.length; i++)
		{
			path.lineTo(xs[i], ys[i]);
		}
		path.closePath();
		return path;
	}

	// 5-point star scaled to the button bounds
	private static Shape star(int w, int h)
	{
		final double cx = w / 2.0;
		final double cy = h / 2.0;
		final double outerX = w / 2.0;
		final double outerY = h / 2.0;
		final double innerX = outerX * 0.4;
		final double innerY = outerY * 0.4;

		final Path2D.Double path = new Path2D.Double();
		for (int i = 0; i < 10; i++)
		{
			final double angle = -Math.PI / 2 + i * Math.PI / 5;
			final double rx = (i % 2 == 0) ? outerX : innerX;
			final double ry = (i % 2 == 0) ? outerY : innerY;
			final double x = cx + rx * Math.cos(angle);
			final double y = cy + ry * Math.sin(angle);
			if (i == 0)
			{
				path.moveTo(x, y);
			}
			else
			{
				path.lineTo(x, y);
			}
		}
		path.closePath();
		return path;
	}

	// Circular gear with 8 teeth scaled to the button bounds
	private static Shape gear(int w, int h)
	{
		final double cx = w / 2.0;
		final double cy = h / 2.0;
		final double r = Math.min(w, h) / 2.0;
		final double bodyRadius = r * 0.78;
		final double toothWidth = r * 0.35;

		final Area area = new Area(new Ellipse2D.Double(cx - bodyRadius, cy - bodyRadius, bodyRadius * 2, bodyRadius * 2));
		for (int i = 0; i < 8; i++)
		{
			final java.awt.geom.AffineTransform rotate = java.awt.geom.AffineTransform.getRotateInstance(i * Math.PI / 4, cx, cy);
			final Shape tooth = rotate.createTransformedShape(
				new Rectangle2D.Double(cx - toothWidth / 2, cy - r, toothWidth, r));
			area.add(new Area(tooth));
		}
		return area;
	}
}
