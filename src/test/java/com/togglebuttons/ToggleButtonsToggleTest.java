package com.togglebuttons;

/*
* Verifies that toggling a button with many plugin targets
* completes correctly and without hanging or throwing
*/

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ToggleButtonsToggleTest
{
	@PluginDescriptor(name = "Test Plugin 1")
	static class TestPlugin1 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 2")
	static class TestPlugin2 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 3")
	static class TestPlugin3 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 4")
	static class TestPlugin4 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 5")
	static class TestPlugin5 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 6")
	static class TestPlugin6 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 7")
	static class TestPlugin7 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 8")
	static class TestPlugin8 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 9")
	static class TestPlugin9 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 10")
	static class TestPlugin10 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 11")
	static class TestPlugin11 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 12")
	static class TestPlugin12 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 13")
	static class TestPlugin13 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 14")
	static class TestPlugin14 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 15")
	static class TestPlugin15 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 16")
	static class TestPlugin16 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 17")
	static class TestPlugin17 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 18")
	static class TestPlugin18 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 19")
	static class TestPlugin19 extends Plugin
	{
	}

	@PluginDescriptor(name = "Test Plugin 20")
	static class TestPlugin20 extends Plugin
	{
	}

	private static final int PLUGIN_COUNT = 20;

	private PluginManager pluginManager;
	private ToggleButtonsToggle toggle;
	private List<Plugin> plugins;

	@Before
	public void setUp() throws Exception
	{
		plugins = new ArrayList<>();
		plugins.add(new TestPlugin1());
		plugins.add(new TestPlugin2());
		plugins.add(new TestPlugin3());
		plugins.add(new TestPlugin4());
		plugins.add(new TestPlugin5());
		plugins.add(new TestPlugin6());
		plugins.add(new TestPlugin7());
		plugins.add(new TestPlugin8());
		plugins.add(new TestPlugin9());
		plugins.add(new TestPlugin10());
		plugins.add(new TestPlugin11());
		plugins.add(new TestPlugin12());
		plugins.add(new TestPlugin13());
		plugins.add(new TestPlugin14());
		plugins.add(new TestPlugin15());
		plugins.add(new TestPlugin16());
		plugins.add(new TestPlugin17());
		plugins.add(new TestPlugin18());
		plugins.add(new TestPlugin19());
		plugins.add(new TestPlugin20());

		pluginManager = mock(PluginManager.class);
		when(pluginManager.getPlugins()).thenReturn(plugins);
		when(pluginManager.isPluginEnabled(any(Plugin.class))).thenReturn(true);
		when(pluginManager.isPluginActive(any(Plugin.class))).thenReturn(true);
		when(pluginManager.startPlugin(any(Plugin.class))).thenReturn(true);
		when(pluginManager.stopPlugin(any(Plugin.class))).thenReturn(true);

		toggle = new ToggleButtonsToggle(pluginManager);
	}

	// Flushes the EDT so all invokeLater work from toggleAll has finished
	private static void awaitEdt() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
		});
	}

	private static List<ToggleButtonsTarget> targets(int count, boolean disableMode)
	{
		final List<ToggleButtonsTarget> targets = new ArrayList<>();
		for (int i = 1; i <= count; i++)
		{
			targets.add(new ToggleButtonsTarget("Test Plugin " + i, disableMode));
		}
		return targets;
	}

	@Test
	public void manyTargetsAllToggleInDisableMode() throws Exception
	{
		toggle.toggleAll(targets(PLUGIN_COUNT, true));
		awaitEdt();

		for (Plugin plugin : plugins)
		{
			verify(pluginManager).setPluginEnabled(plugin, false);
			verify(pluginManager).stopPlugin(plugin);
		}
	}

	@Test
	public void manyTargetsAllToggleInOnOffMode() throws Exception
	{
		toggle.toggleAll(targets(PLUGIN_COUNT, false));
		awaitEdt();

		for (Plugin plugin : plugins)
		{
			verify(pluginManager).stopPlugin(plugin);
			verify(pluginManager, never()).setPluginEnabled(any(Plugin.class), any(Boolean.class));
		}
	}

	@Test
	public void unknownTargetsAreSkippedWithoutError() throws Exception
	{
		final List<ToggleButtonsTarget> targets = new ArrayList<>();
		for (int i = 0; i < 50; i++)
		{
			targets.add(new ToggleButtonsTarget("No Such Plugin " + i, true));
		}

		toggle.toggleAll(targets);
		awaitEdt();

		verify(pluginManager, never()).stopPlugin(any(Plugin.class));
		verify(pluginManager, never()).startPlugin(any(Plugin.class));
	}

	@Test
	public void manyRepeatedTargetsCompleteInBoundedTime() throws Exception
	{
		// 200 targets cycling over the 20 plugins simulates a heavily loaded button
		final List<ToggleButtonsTarget> targets = new ArrayList<>();
		for (int i = 0; i < 200; i++)
		{
			targets.add(new ToggleButtonsTarget("Test Plugin " + (i % PLUGIN_COUNT + 1), i % 2 == 0));
		}

		final long start = System.nanoTime();
		toggle.toggleAll(targets);
		awaitEdt();
		final long elapsedMs = (System.nanoTime() - start) / 1_000_000;

		// The toggle bookkeeping itself must not blow up with target count;
		// real slowness comes from the plugins' own startUp/shutDown work
		assertTrue("toggleAll took " + elapsedMs + "ms for 200 targets", elapsedMs < 2000);
		verify(pluginManager, times(200)).getPlugins();
	}

	@Test
	public void nullAndEmptyTargetListsAreNoOps() throws Exception
	{
		toggle.toggleAll(null);
		toggle.toggleAll(new ArrayList<>());
		awaitEdt();

		verify(pluginManager, never()).getPlugins();
	}

	@Test
	public void targetsBeyondMaximumAreTruncatedByStore()
	{
		final com.google.gson.Gson gson = new com.google.gson.Gson();
		final net.runelite.client.config.ConfigManager configManager =
			mock(net.runelite.client.config.ConfigManager.class);

		final ToggleButtonsButton button = new ToggleButtonsButton();
		button.setId("test-id");
		button.setTargets(targets(ToggleButtonsButton.MAX_TARGETS + 25, true));
		final List<ToggleButtonsButton> buttons = new ArrayList<>();
		buttons.add(button);

		when(configManager.getConfiguration(
			ToggleButtonsButtonStore.CONFIG_GROUP, ToggleButtonsButtonStore.BUTTONS_KEY))
			.thenReturn(gson.toJson(buttons));

		final ToggleButtonsButtonStore store = new ToggleButtonsButtonStore(configManager, gson);
		final List<ToggleButtonsButton> loaded = store.getButtons();

		assertTrue(loaded.get(0).getTargets().size() == ToggleButtonsButton.MAX_TARGETS);
	}
}
