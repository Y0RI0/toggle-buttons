package com.togglebuttons;

/*
* Handles left mouse clicks on button overlays,
* triggering their plugin toggle states
*/

import java.awt.event.MouseEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseManager;

@Slf4j
@Singleton
class ToggleButtonsMouseBehavior extends MouseAdapter
{
	private final MouseManager mouseManager;
	private final ToggleButtonsConfig config;
	private final ToggleButtonsToggle toggle;

	@Setter
	private Supplier<Collection<ToggleButtonsOverlay>> overlaySupplier = Collections::emptyList;

	@Inject
	ToggleButtonsMouseBehavior(MouseManager mouseManager, ToggleButtonsConfig config, ToggleButtonsToggle toggle)
	{
		this.mouseManager = mouseManager;
		this.config = config;
		this.toggle = toggle;
	}

	void startUp()
	{
		mouseManager.registerMouseListener(this);
	}

	void shutDown()
	{
		mouseManager.unregisterMouseListener(this);
	}

	@Override
	public MouseEvent mousePressed(MouseEvent e)
	{
		if (!config.showButtons() ||
			!SwingUtilities.isLeftMouseButton(e) ||
			e.isAltDown())
		{
			return e;
		}

		for (ToggleButtonsOverlay overlay : overlaySupplier.get())
		{
			if (overlay.getBounds().contains(e.getPoint()))
			{
				overlay.setPressed(true);
				toggle.press(overlay.getButton());
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
			toggle.release();

			for (ToggleButtonsOverlay overlay : overlaySupplier.get())
			{
				overlay.setPressed(false);
			}
		}
		return e;
	}
}
