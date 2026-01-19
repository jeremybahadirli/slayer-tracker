package com.slayertracker.views;

import com.slayertracker.groups.Assignment;
import com.slayertracker.groups.Variant;
import com.slayertracker.records.CustomRecord;
import com.slayertracker.records.Record;

public interface RecordInteractionHandler
{
	void addCustomRecord();

	void copyRecordToCustom(Record record);

	void deleteAssignment(Assignment assignment);

	void deleteVariant(Variant variant);

	void deleteCustomRecord(CustomRecord record);

	void setCustomRecording(CustomRecord record, boolean recording);

	void renameCustomRecord(CustomRecord record, String name);
}
