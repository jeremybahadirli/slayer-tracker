/*
 * Copyright (c) 2022, Jeremy Bahadirli <https://github.com/jeremybahadirli>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.slayertracker;

import com.google.inject.Provides;
import com.slayertracker.persistence.ProfileContext;
import com.slayertracker.persistence.RecordRepository;
import com.slayertracker.persistence.SlayerTrackerSaveManager;
import com.slayertracker.state.TrackerState;
import com.slayertracker.tracker.TrackerService;
import com.slayertracker.views.SlayerTrackerPanel;
import java.awt.image.BufferedImage;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.slayer.SlayerConfig;
import net.runelite.client.plugins.slayer.SlayerPlugin;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Slayer Tracker"
)
@PluginDependency(SlayerPlugin.class)
public class SlayerTrackerPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private SlayerTrackerConfig config;
	@Inject
	private ItemManager itemManager;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private ScheduledExecutorService executor;

	private TrackerState trackerState;
	private TrackerService trackerService;
	private SlayerTrackerPanel slayerTrackerPanel;

	@Override
	protected void startUp()
	{
		System.out.println(client);
		trackerState = new TrackerState();
		ProfileContext profileContext = new ProfileContext();
		RecordRepository recordRepository = new SlayerTrackerSaveManager(trackerState);
		trackerService = new TrackerService(trackerState, recordRepository, profileContext);

		slayerTrackerPanel = new SlayerTrackerPanel(trackerState, config, itemManager);
		trackerState.addPropertyChangeListener(evt ->
			clientThread.invokeLater(() ->
				SwingUtilities.invokeLater(() ->
					slayerTrackerPanel.update())));
		trackerService.setRecordingModeController(slayerTrackerPanel.getRecordingModePanel());

		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/slayer_icon.png");
		NavigationButton navButton = NavigationButton.builder()
			.panel(slayerTrackerPanel)
			.tooltip("Slayer Tracker")
			.icon(icon)
			.priority(5)
			.build();
		clientToolbar.addNavigation(navButton);

		trackerService.onPluginStart();
	}

	@Override
	protected void shutDown()
	{
		try
		{
			trackerService.saveRecords();
		}
		catch (Exception e)
		{
			slayerTrackerPanel.displayFileError();
		}
	}

	@Subscribe
	public void onClientShutdown(ClientShutdown event)
	{
		event.waitFor(executor.submit(() -> {
			try
			{
				trackerService.saveRecords();
			}
			catch (Exception e)
			{
				SwingUtilities.invokeLater(() -> slayerTrackerPanel.displayFileError());
			}
		}));
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		try
		{
			trackerService.onGameStateChanged(event);
		}
		catch (Exception e)
		{
			slayerTrackerPanel.displayFileError();
		}

		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			slayerTrackerPanel.getRecordingModePanel().setContinuousRecording(false);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		switch (event.getGroup())
		{
			case SlayerTrackerConfig.GROUP_NAME:
				if (!event.getKey().equals(SlayerTrackerConfig.LOOT_UNIT_KEY))
				{
					break;
				}
				clientThread.invokeLater(() ->
					SwingUtilities.invokeLater(() ->
						slayerTrackerPanel.update()));
				break;
			case SlayerConfig.GROUP_NAME:
				trackerService.onSlayerConfigChanged(event);
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		trackerService.onGameTick();
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		trackerService.onInteractingChanged(event);
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		trackerService.onActorDeath(event);
	}

	@Subscribe
	private void onStatChanged(StatChanged event)
	{
		trackerService.onStatChanged(event);
	}

	@Subscribe
	private void onNpcLootReceived(NpcLootReceived event)
	{
		trackerService.onNpcLootReceived(event);
	}

	// TESTING
	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted)
	{
		if (commandExecuted.getCommand().equals("ttt") && trackerState != null)
		{
			System.out.println(trackerState.getXpNpcQueue().size());
			System.out.println(trackerState.getKcNpcQueue().size());
			System.out.println(trackerState.getLootNpcQueue().size());
		}
	}

	@Provides
	SlayerTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SlayerTrackerConfig.class);
	}
}
