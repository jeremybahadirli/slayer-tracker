package com.slayertracker.state;

import com.slayertracker.groups.Assignment;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.records.RecordMap;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.NPC;

@Getter
public class TrackerState implements PropertyChangeListener
{
	private final PropertyChangeSupport support = new PropertyChangeSupport(this);

	private final RecordMap<Assignment, AssignmentRecord> assignmentRecords;
	private final Set<NPC> xpNpcQueue = new HashSet<>();
	private final Set<NPC> kcNpcQueue = new HashSet<>();
	private final Map<NPC, Assignment> lootNpcQueue = new HashMap<>();

	@Setter
	private Assignment currentAssignment;
	@Setter
	private int cachedXp = -1;
	@Setter
	private boolean loggingIn = false;
	@Setter
	private String profileFileName;

	public TrackerState()
	{
		assignmentRecords = new RecordMap<>(this);
	}

	public void clearQueues()
	{
		xpNpcQueue.clear();
		kcNpcQueue.clear();
		lootNpcQueue.clear();
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		support.addPropertyChangeListener(listener);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		support.firePropertyChange(evt);
	}
}
