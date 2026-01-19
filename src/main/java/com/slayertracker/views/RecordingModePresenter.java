package com.slayertracker.views;

import com.slayertracker.RecordingModeController;

public class RecordingModePresenter implements RecordingModeController
{
	private final RecordingModePanel view;
	private RecordingModePanel.RecordingMode recordingMode;
	private boolean recording;

	public RecordingModePresenter(RecordingModePanel view)
	{
		this.view = view;
		this.recordingMode = RecordingModePanel.RecordingMode.IN_COMBAT;

		view.setRecordingModeChangeListener(this::onRecordingModeChanged);
		view.setRecordingModeValue(recordingMode);
	}

	private void onRecordingModeChanged(RecordingModePanel.RecordingMode mode)
	{
		recordingMode = mode;
		view.setRecordingModeValue(mode);
	}

	@Override
	public RecordingModePanel.RecordingMode getRecordingMode()
	{
		return recordingMode;
	}

	@Override
	public boolean isRecording()
	{
		return recording;
	}

	@Override
	public void setRecording(boolean active)
	{
		recording = active;
		view.setRecordingState(active);
	}

	public void setPauseRequestHandler(Runnable pauseRequestHandler)
	{
		view.setPauseRequestHandler(pauseRequestHandler);
	}
}
