package com.togglebuttons;

/*
* Contains helper logic for performing an icon search against the game's cached itemIds
*/

import java.util.function.IntConsumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Client;
import net.runelite.api.GameState;

import net.runelite.client.callback.ClientThread;
//Library which handles the fancy ability to item search as seen in many popular plugins
import net.runelite.client.game.chatbox.ChatboxItemSearch;

@Slf4j
@Singleton
class ToggleButtonsIconSearch
{
	private final Client client;
	private final ClientThread clientThread;
	private final ChatboxItemSearch itemSearch;

	@Inject
	ToggleButtonsIconSearch(Client client, ClientThread clientThread, ChatboxItemSearch itemSearch)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.itemSearch = itemSearch;
	}

	void open(IntConsumer onSelected)
	{
		clientThread.invokeLater(() ->
		{
			if (client.getGameState() != GameState.LOGGED_IN)
			{
				log.debug("Cannot open icon search before logging in");
				return;
			}

			itemSearch
				.tooltipText("Search for a button icon")
				.onItemSelected(onSelected::accept)
				.build();
		});
	}
}
