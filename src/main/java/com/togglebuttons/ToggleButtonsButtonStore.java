package com.togglebuttons;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import net.runelite.client.config.ConfigManager;

@Slf4j
@Singleton
class ToggleButtonsButtonStore
{
	static final String CONFIG_GROUP = "togglebuttons";
	static final String BUTTONS_KEY = "buttons";
	private static final Type BUTTON_LIST_TYPE = new TypeToken<List<ToggleButtonsButton>>()
	{
	}.getType();
	private static final Type TARGET_LIST_TYPE = new TypeToken<List<ToggleButtonsTarget>>()
	{
	}.getType();

	private final ConfigManager configManager;
	private final Gson gson;

	@Inject
	ToggleButtonsButtonStore(ConfigManager configManager, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson;
	}

	List<ToggleButtonsButton> getButtons()
	{
		final String json = configManager.getConfiguration(CONFIG_GROUP, BUTTONS_KEY);
		if (json == null || json.isEmpty())
		{
			return new ArrayList<>();
		}

		try
		{
			final List<ToggleButtonsButton> buttons = gson.fromJson(json, BUTTON_LIST_TYPE);
			if (buttons == null)
			{
				return new ArrayList<>();
			}
			buttons.forEach(this::normalize);
			return buttons;
		}
		catch (JsonSyntaxException ex)
		{
			log.warn("Failed to parse stored buttons", ex);
			return new ArrayList<>();
		}
	}

	void setButtons(List<ToggleButtonsButton> buttons)
	{
		configManager.setConfiguration(CONFIG_GROUP, BUTTONS_KEY, gson.toJson(buttons, BUTTON_LIST_TYPE));
	}

	ToggleButtonsButton createButton()
	{
		final List<ToggleButtonsButton> buttons = getButtons();
		final ToggleButtonsButton button = new ToggleButtonsButton();
		button.setId(UUID.randomUUID().toString());
		button.setName("Button " + (buttons.size() + 1));
		buttons.add(button);
		setButtons(buttons);
		return button;
	}

	// Migrates config from the single-button era into a stored button list
	void migrateLegacy()
	{
		final String legacyShow = configManager.getConfiguration(CONFIG_GROUP, "showButton");
		if (legacyShow != null)
		{
			configManager.setConfiguration(CONFIG_GROUP, "showButtons", legacyShow);
			configManager.unsetConfiguration(CONFIG_GROUP, "showButton");
		}

		final String legacyTargetsJson = configManager.getConfiguration(CONFIG_GROUP, "targetPlugins");
		final String legacyTarget = configManager.getConfiguration(CONFIG_GROUP, "targetPlugin");
		final String legacyIcon = configManager.getConfiguration(CONFIG_GROUP, "iconItemId");
		final String legacyWidth = configManager.getConfiguration(CONFIG_GROUP, "buttonWidth");
		final String legacyHeight = configManager.getConfiguration(CONFIG_GROUP, "buttonHeight");
		final String legacyResizable = configManager.getConfiguration(CONFIG_GROUP, "resizable");
		final String legacyButtonColor = configManager.getConfiguration(CONFIG_GROUP, "buttonColor");
		final String legacyPressedColor = configManager.getConfiguration(CONFIG_GROUP, "pressedColor");

		final boolean hasLegacy = legacyTargetsJson != null || legacyTarget != null || legacyIcon != null
			|| legacyWidth != null || legacyHeight != null || legacyResizable != null
			|| legacyButtonColor != null || legacyPressedColor != null;

		if (hasLegacy && getButtons().isEmpty())
		{
			final ToggleButtonsButton button = new ToggleButtonsButton();
			button.setId(UUID.randomUUID().toString());
			button.setName("Button 1");
			button.setIconItemId(parseInt(legacyIcon, -1));
			button.setWidth(parseInt(legacyWidth, ToggleButtonsButton.DEFAULT_WIDTH));
			button.setHeight(parseInt(legacyHeight, ToggleButtonsButton.DEFAULT_HEIGHT));
			button.setResizable(Boolean.parseBoolean(legacyResizable));
			button.setButtonColor(parseInt(legacyButtonColor, ToggleButtonsButton.DEFAULT_BUTTON_COLOR));
			button.setPressedColor(parseInt(legacyPressedColor, ToggleButtonsButton.DEFAULT_PRESSED_COLOR));
			button.setTargets(parseLegacyTargets(legacyTargetsJson, legacyTarget));

			final List<ToggleButtonsButton> buttons = new ArrayList<>();
			buttons.add(button);
			setButtons(buttons);
			log.debug("Migrated legacy config into button '{}'", button.getName());
		}

		configManager.unsetConfiguration(CONFIG_GROUP, "targetPlugins");
		configManager.unsetConfiguration(CONFIG_GROUP, "targetPlugin");
		configManager.unsetConfiguration(CONFIG_GROUP, "iconItemId");
		configManager.unsetConfiguration(CONFIG_GROUP, "selectIcon");
		configManager.unsetConfiguration(CONFIG_GROUP, "buttonWidth");
		configManager.unsetConfiguration(CONFIG_GROUP, "buttonHeight");
		configManager.unsetConfiguration(CONFIG_GROUP, "resizable");
		configManager.unsetConfiguration(CONFIG_GROUP, "buttonColor");
		configManager.unsetConfiguration(CONFIG_GROUP, "pressedColor");
	}

	private List<ToggleButtonsTarget> parseLegacyTargets(String targetsJson, String singleTarget)
	{
		if (targetsJson != null && !targetsJson.isEmpty())
		{
			try
			{
				final List<ToggleButtonsTarget> targets = gson.fromJson(targetsJson, TARGET_LIST_TYPE);
				if (targets != null)
				{
					return targets;
				}
			}
			catch (JsonSyntaxException ex)
			{
				log.warn("Failed to parse legacy targets", ex);
			}
		}

		final List<ToggleButtonsTarget> targets = new ArrayList<>();
		if (singleTarget != null && !singleTarget.trim().isEmpty())
		{
			targets.add(new ToggleButtonsTarget(singleTarget.trim(), true));
		}
		return targets;
	}

	private void normalize(ToggleButtonsButton button)
	{
		if (button.getId() == null || button.getId().isEmpty())
		{
			button.setId(UUID.randomUUID().toString());
		}
		if (button.getName() == null)
		{
			button.setName("Button");
		}
		if (button.getTargets() == null)
		{
			button.setTargets(new ArrayList<>());
		}
		if (button.getWidth() < 16)
		{
			button.setWidth(ToggleButtonsButton.DEFAULT_WIDTH);
		}
		if (button.getHeight() < 16)
		{
			button.setHeight(ToggleButtonsButton.DEFAULT_HEIGHT);
		}
	}

	private static int parseInt(String value, int fallback)
	{
		if (value == null || value.isEmpty())
		{
			return fallback;
		}

		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException ex)
		{
			return fallback;
		}
	}
}
