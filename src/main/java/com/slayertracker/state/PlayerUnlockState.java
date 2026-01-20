package com.slayertracker.state;

import com.slayertracker.groups.Assignment;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PlayerUnlockState
{
	@Setter
	boolean westernDiary;
	@Setter
	boolean kourendDiary;

	Set<Assignment> extendedAssignments;

	PlayerUnlockState()
	{
		westernDiary = false;
		kourendDiary = false;
		extendedAssignments = new HashSet<>();
	}

	public void addExtendedAssignment(Assignment assignment)
	{
		extendedAssignments.add(assignment);
	}

	public void clear()
	{
		westernDiary = false;
		kourendDiary = false;
		extendedAssignments.clear();
	}

	@Override
	public String toString()
	{
		return "PlayerUnlockState{"
			+ "westernDiary=" + westernDiary
			+ ", kourendDiary=" + kourendDiary
			+ ", extendedAssignments=" + extendedAssignments + '}';
	}
}
