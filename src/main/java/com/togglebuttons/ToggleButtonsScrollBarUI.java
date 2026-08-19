package com.togglebuttons;

/*
* Theming the sidebar panel scrollbar
*/

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

class ToggleButtonsScrollBarUI extends BasicScrollBarUI
{
	@Override
	protected void configureScrollBarColors()
	{
		trackColor = ToggleButtonsStyle.PANEL_ACCENT;
		thumbColor = ToggleButtonsStyle.PANEL_BACKGROUND;
	}

	@Override
	protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)
	{
		if (thumbBounds.isEmpty() || !scrollbar.isEnabled())
		{
			return;
		}

		final Graphics2D g2 = (Graphics2D) g;
		g2.setColor(thumbColor);
		g2.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
		g2.setColor(ToggleButtonsStyle.RUNESCAPE_YELLOW);
		g2.drawRect(thumbBounds.x, thumbBounds.y, thumbBounds.width - 1, thumbBounds.height - 1);
	}

	@Override
	protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds)
	{
		g.setColor(trackColor);
		g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
	}

	// Hide the arrow buttons, it's not 2003
	@Override
	protected JButton createDecreaseButton(int orientation)
	{
		return zeroSizeButton();
	}

	@Override
	protected JButton createIncreaseButton(int orientation)
	{
		return zeroSizeButton();
	}

	private static JButton zeroSizeButton()
	{
		final JButton button = new JButton();
		final Dimension zero = new Dimension(0, 0);
		button.setPreferredSize(zero);
		button.setMinimumSize(zero);
		button.setMaximumSize(zero);
		return button;
	}
}
