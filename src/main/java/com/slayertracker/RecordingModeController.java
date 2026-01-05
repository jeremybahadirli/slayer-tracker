package com.slayertracker;

import java.time.Instant;

public interface RecordingModeController
{
	boolean isContinuousRecordingMode();

	Instant getContinuousRecordingStartInstant();

	void setContinuousRecording(boolean active);
}
