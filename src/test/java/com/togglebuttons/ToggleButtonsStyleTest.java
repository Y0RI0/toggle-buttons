package com.togglebuttons;

/*
* Validates the tooltip styling helpers actually take effect:
* the HTML wrapper constrains tooltip width, and the panel
* raises the global tooltip dismiss delay.
*/

import java.awt.Dimension;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ToggleButtonsStyleTest
{
	private static final String LONG_TEXT =
		"Enable/disable: the button enables or disables the plugin in RuneLite. Persistent in your config. "
			+ "Turn on/off: the button only stops or starts the plugin without persisting across sessions.";

	@Test
	public void tooltipHtmlWrapsLongTextToConfiguredWidth() throws Exception
	{
		final Dimension[] sizes = new Dimension[2];
		SwingUtilities.invokeAndWait(() ->
		{
			final JToolTip wrapped = new JToolTip();
			wrapped.setTipText(ToggleButtonsStyle.tooltip(LONG_TEXT));
			sizes[0] = wrapped.getPreferredSize();

			final JToolTip plain = new JToolTip();
			plain.setTipText(LONG_TEXT);
			sizes[1] = plain.getPreferredSize();
		});

		final Dimension wrappedSize = sizes[0];
		final Dimension plainSize = sizes[1];

		// Plain text renders as one long unwrapped line
		assertTrue("plain tooltip should be wider than the wrap width, was " + plainSize.width,
			plainSize.width > ToggleButtonsStyle.TOOLTIP_WRAP_WIDTH);

		// Wrapped text must be constrained near TOOLTIP_WRAP_WIDTH (plus padding/insets)
		assertTrue("wrapped tooltip width " + wrappedSize.width + " should be near "
				+ ToggleButtonsStyle.TOOLTIP_WRAP_WIDTH,
			wrappedSize.width <= ToggleButtonsStyle.TOOLTIP_WRAP_WIDTH + 60);
		assertTrue("wrapped tooltip should be narrower than plain",
			wrappedSize.width < plainSize.width);

		// Wrapping trades width for height: multiple lines
		assertTrue("wrapped tooltip should be taller than the single plain line",
			wrappedSize.height > plainSize.height);
	}

	@Test
	public void panelConstructionRaisesTooltipDismissDelay() throws Exception
	{
		final ToggleButtonsToggle toggle = mock(ToggleButtonsToggle.class);
		final ToggleButtonsButtonStore store = mock(ToggleButtonsButtonStore.class);
		final ToggleButtonsIconSearch iconSearch = mock(ToggleButtonsIconSearch.class);
		final ItemManager itemManager = mock(ItemManager.class);
		final ColorPickerManager colorPickerManager = mock(ColorPickerManager.class);
		final ToggleButtonsConfig config = mock(ToggleButtonsConfig.class);
		when(store.getButtons()).thenReturn(new java.util.ArrayList<>());
		when(config.sidebarClickMode()).thenReturn(ToggleButtonsSidebarClickMode.EDIT);

		SwingUtilities.invokeAndWait(() ->
		{
			// Reset to Swing's default (4000ms) so the assertion is meaningful
			ToolTipManager.sharedInstance().setDismissDelay(4000);

			new ToggleButtonsPluginPanel(toggle, store, iconSearch, itemManager, colorPickerManager, config);
		});

		assertEquals("panel constructor should apply the configured dismiss delay",
			ToggleButtonsStyle.TOOLTIP_DISMISS_MS, ToolTipManager.sharedInstance().getDismissDelay());
	}
}
