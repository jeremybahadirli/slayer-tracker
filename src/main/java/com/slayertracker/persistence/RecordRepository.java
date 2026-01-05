package com.slayertracker.persistence;

import com.slayertracker.groups.Assignment;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.records.RecordMap;

public interface RecordRepository
{
	RecordMap<Assignment, AssignmentRecord> load(String dataFileName) throws Exception;

	void save(RecordMap<Assignment, AssignmentRecord> assignmentRecords, String dataFileName) throws Exception;
}
