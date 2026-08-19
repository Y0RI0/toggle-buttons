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
			List<ToggleButtonsButton> buttons = gson.fromJson(json, BUTTON_LIST_TYPE);
			if (buttons == null)
			{
				return new ArrayList<>();
			}
			if (buttons.size() > ToggleButtonsButton.MAX_BUTTONS)
			{
				log.warn("{} buttons stored; truncating to the maximum of {}",
					buttons.size(), ToggleButtonsButton.MAX_BUTTONS);
				buttons = new ArrayList<>(buttons.subList(0, ToggleButtonsButton.MAX_BUTTONS));
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

	// Returns null when the maximum number of buttons has been reached
	ToggleButtonsButton createButton()
	{
		final List<ToggleButtonsButton> buttons = getButtons();
		if (buttons.size() >= ToggleButtonsButton.MAX_BUTTONS)
		{
			log.debug("Cannot create button; already at the maximum of {}", ToggleButtonsButton.MAX_BUTTONS);
			return null;
		}

		final ToggleButtonsButton button = new ToggleButtonsButton();
		button.setId(UUID.randomUUID().toString());
		button.setName("Button " + (buttons.size() + 1));
		buttons.add(button);
		setButtons(buttons);
		return button;
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
		if (button.getTargets().size() > ToggleButtonsButton.MAX_TARGETS)
		{
			log.warn("Button '{}' has {} targets; truncating to the maximum of {}",
				button.getName(), button.getTargets().size(), ToggleButtonsButton.MAX_TARGETS);
			button.setTargets(new ArrayList<>(button.getTargets().subList(0, ToggleButtonsButton.MAX_TARGETS)));
		}
		if (button.getShape() == null)
		{
			button.setShape(ToggleButtonsShape.ROUNDED_RECTANGLE);
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
}
