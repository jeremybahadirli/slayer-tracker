/*
 * Copyright (c) 2026, Jeremy Bahadirli <https://github.com/jeremybahadirli>
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
import com.slayertracker.groups.Master;
import com.slayertracker.persistence.RecordRepository;
import com.slayertracker.persistence.SlayerTrackerSaveManager;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.state.TrackerState;
import com.slayertracker.tracker.Optimizer;
import com.slayertracker.tracker.TrackerService;
import com.slayertracker.views.SlayerTrackerPanel;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
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
	private ClientThread clientThread;
	@Inject
	private SlayerTrackerConfig config;
	@Inject
	private ItemManager itemManager;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private ScheduledExecutorService executor;
	@Inject
	private TrackerState trackerState;
	@Inject
	private TrackerService trackerService;

	private SlayerTrackerPanel slayerTrackerPanel;
	private NavigationButton navButton;

	private boolean loggingIn = false;

	@Override
	protected void startUp()
	{
		slayerTrackerPanel = new SlayerTrackerPanel(trackerState, config, itemManager);
		trackerState.addPropertyChangeListener(evt ->
			clientThread.invokeLater(() ->
				SwingUtilities.invokeLater(() ->
					slayerTrackerPanel.update())));
		trackerService.setRecordingModeController(slayerTrackerPanel.getRecordingModePanel());
		slayerTrackerPanel.getRecordingModePanel()
			.setPauseRequestHandler(() -> clientThread.invokeLater(trackerService::pauseRecordingIfNotInteracting));

		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/slayer_icon.png");
		navButton = NavigationButton.builder()
			.panel(slayerTrackerPanel)
			.tooltip("Slayer Tracker")
			.icon(icon)
			.priority(5)
			.build();
		clientToolbar.addNavigation(navButton);

		trackerService.handlePluginStart();
	}

	@Override
	protected void shutDown()
	{
		try
		{
			trackerService.saveRecords();
			clientToolbar.removeNavigation(navButton);
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
			switch (event.getGameState())
			{
				case LOGGING_IN:
					loggingIn = true;
					break;
				case LOGGED_IN:
					if (!loggingIn)
					{
						return;
					}
					loggingIn = false;
					trackerService.handleLogin();
					break;
				case LOGIN_SCREEN:
					trackerService.handleLogout();
					trackerService.getRecordingModeController().setRecording(false);
					break;
			}
		}
		catch (Exception e)
		{
			slayerTrackerPanel.displayFileError();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		switch (event.getGroup())
		{
			case SlayerTrackerConfig.GROUP_NAME:
				if (event.getKey().equals(SlayerTrackerConfig.LOOT_UNIT_KEY))
				{
					clientThread.invokeLater(() ->
						SwingUtilities.invokeLater(() ->
							slayerTrackerPanel.update()));
				}
				break;
			case SlayerConfig.GROUP_NAME:
				if (event.getKey().equals(SlayerConfig.TASK_NAME_KEY))
				{
					trackerService.handleSlayerTaskChange();
				}
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		trackerService.handleGameTick();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		trackerService.handleVarbitChanged(event);
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		trackerService.handleInteractingChanged(event);
	}

	@Subscribe
	private void onStatChanged(StatChanged event)
	{
		trackerService.handleStatChanged(event);
	}

	@Subscribe
	private void onNpcLootReceived(NpcLootReceived event)
	{
		trackerService.handleNpcLootReceived(event);
	}

	@Subscribe
	private void onChatMessage(ChatMessage event)
	{
		trackerService.handleChatMessage(event);
	}

	// TESTING
	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		if (event.getCommand().equals("t"))
		{
			trackerService.log("interacting npcs", trackerState.getCurrentAssignmentRecord().getInteractingNpcs());
			trackerService.log("ended interactions", trackerState.getEndedInteractions());
			trackerService.log("recent kills", trackerState.getKillEvents());
			trackerService.log("slayer xp drops", trackerState.getXpDropEvents());
			trackerService.log("task amount changes", trackerState.getTaskAmountChanges());
			trackerService.log("expeditious procs", trackerState.getExpeditiousProcs());
			trackerService.log("current assignment", trackerState.getCurrentAssignment());
			trackerService.log("current assignment record", trackerState.getCurrentAssignmentRecord());
			trackerService.log("remaining amount", trackerState.getRemainingAmount());
		}
		if (event.getCommand().equals(("o")))
		{
			List<Optimizer.Task> optimizerTasks = trackerState.getAssignmentRecords().keySet().stream().map(a -> {
					AssignmentRecord ar = trackerState.getAssignmentRecords().get(a);

					String name = a.getName();
					double rate = ar.getXp() / ar.getHours();
					double kcPerHour = ar.getKc() / ar.getHours();
					double hours = Master.DURADEL.getTaskByName(name).getAverageAmount(false) / kcPerHour;
					double minHours = Master.Task.BOSS_MIN_AMOUNT / kcPerHour;
					double maxHours = Master.Task.BOSS_MAX_AMOUNT / kcPerHour;
					double weight = Master.DURADEL.getTaskByName(name).getWeight();

					trackerService.log(name, "rate", rate, "hours", hours, "weight", weight);
					return a.isBoss()
						? Optimizer.Task.boss(name, rate, minHours, maxHours, weight)
						: Optimizer.Task.normal(name, rate, hours, weight);
				})
				.collect(Collectors.toList());

			Optimizer.Result r = Optimizer.optimize(optimizerTasks, Master.DURADEL.getTaskPointRevenueAtTimeScale(1000), 30, 1);
			System.out.println(Optimizer.formatResult(r));
		}
	}

	@Provides
	SlayerTrackerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SlayerTrackerConfig.class);
	}

	@Provides
	RecordRepository provideRecordRepository(SlayerTrackerSaveManager saveManager)
	{
		return saveManager;
	}
}
