package com.slayertracker.views;

import com.slayertracker.SlayerTrackerConfig;
import com.slayertracker.groups.Assignment;
import com.slayertracker.groups.Variant;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.records.CustomRecord;
import com.slayertracker.records.Record;
import com.slayertracker.records.RecordMap;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import lombok.Getter;
import net.runelite.client.game.ItemManager;

public class GroupListPresenter implements RecordInteractionHandler
{
	@Getter
	private final Assignment assignment;
	@Getter
	private final AssignmentRecord record;
	private final RecordMap<Assignment, AssignmentRecord> assignmentRecords;
	private final PropertyChangeListener changeListener;
	private final Supplier<BiFunction<Record, SlayerTrackerConfig.LootUnit, Long>> sortFunctionSupplier;
	@Getter
	private final GroupListPanel view;

	public GroupListPresenter(
		Assignment assignment,
		PropertyChangeListener changeListener,
		RecordMap<Assignment, AssignmentRecord> assignmentRecords,
		SlayerTrackerConfig config,
		ItemManager itemManager,
		Supplier<BiFunction<Record, SlayerTrackerConfig.LootUnit, Long>> sortFunctionSupplier)
	{
		this.assignment = assignment;
		this.changeListener = changeListener;
		this.assignmentRecords = assignmentRecords;
		this.sortFunctionSupplier = sortFunctionSupplier;
		this.record = assignmentRecords.get(assignment);
		this.view = new GroupListPanel(assignment, record, config, itemManager, sortFunctionSupplier.get(), this);
	}

	public void update(BiFunction<Record, SlayerTrackerConfig.LootUnit, Long> sortFunction)
	{
		view.update(sortFunction);
	}

	@Override
	public void addCustomRecord()
	{
		CustomRecord customRecord = new CustomRecord(changeListener);
		System.out.println(customRecord.getInteractingNpcs());
		record.getCustomRecords().add(customRecord);
		update(sortFunctionSupplier.get());
	}

	@Override
	public void copyRecordToCustom(Record source)
	{
		record.getCustomRecords().add(new CustomRecord(source, changeListener));
		update(sortFunctionSupplier.get());
	}

	@Override
	public void deleteAssignment(Assignment assignment)
	{
		assignmentRecords.remove(assignment);
	}

	@Override
	public void deleteVariant(Variant variant)
	{
		record.getVariantRecords().remove(variant);
		update(sortFunctionSupplier.get());
	}

	@Override
	public void deleteCustomRecord(CustomRecord customRecord)
	{
		record.getCustomRecords().remove(customRecord);
		update(sortFunctionSupplier.get());
	}

	@Override
	public void setCustomRecording(CustomRecord customRecord, boolean recording)
	{
		customRecord.setRecording(recording);
		if (!recording)
		{
			customRecord.getInteractingNpcs().clear();
		}
		update(sortFunctionSupplier.get());
	}

	@Override
	public void renameCustomRecord(CustomRecord customRecord, String name)
	{
		customRecord.setName(name);
		update(sortFunctionSupplier.get());
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof GroupListPresenter))
		{
			return false;
		}
		GroupListPresenter that = (GroupListPresenter) o;
		return Objects.equals(assignment, that.assignment);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(assignment);
	}
}
