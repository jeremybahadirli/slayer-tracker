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

package com.slayertracker.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.slayertracker.groups.Assignment;
import com.slayertracker.groups.Variant;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.records.CustomRecord;
import com.slayertracker.records.CustomRecordSet;
import com.slayertracker.records.Record;
import com.slayertracker.records.RecordMap;
import com.slayertracker.state.TrackerState;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.RuneLite;

@Singleton
public class SlayerTrackerSaveManager implements RecordRepository
{
	public static final String DATA_FOLDER_NAME = "slayer-tracker";
	public static final File DATA_FOLDER = new File(RuneLite.RUNELITE_DIR, DATA_FOLDER_NAME);
	private static final int CURRENT_SCHEMA_VERSION = 1;

	private final Gson gson;
	private final PropertyChangeListener changeListener;

	@Inject
	public SlayerTrackerSaveManager(TrackerState trackerState)
	{
		this.changeListener = trackerState;
		gson = new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.setPrettyPrinting()
			.registerTypeAdapter(AssignmentRecord.class, assignmentRecordCreator(trackerState))
			.registerTypeAdapter(CustomRecord.class, customRecordCreator(trackerState))
			.registerTypeAdapter(RecordMap.class, recordMapCreator(trackerState))
			.registerTypeAdapter(CustomRecordSet.class, customRecordSetCreator(trackerState))
			.registerTypeAdapter(Instant.class, new InstantAdapter())
			.registerTypeAdapter(Variant.class, new VariantAdapter())
			.create();
	}

	private File getDataFile(String dataFileName) throws IOException
	{
		if (dataFileName == null)
		{
			throw new IOException("Data file name not available");
		}
		if (!DATA_FOLDER.exists() && !DATA_FOLDER.mkdirs())
		{
			throw new IOException("Could not create data folder: .runelite/slayer-tracker");
		}
		return new File(DATA_FOLDER, dataFileName);
	}

	@Override
	public RecordMap<Assignment, AssignmentRecord> load(String dataFileName) throws Exception
	{
		File dataFile = getDataFile(dataFileName);

		if (!dataFile.exists())
		{
			Writer writer = new FileWriter(dataFile);
			writer.write("{}");
			writer.close();
		}

		try (FileReader reader = new FileReader(dataFile))
		{
			SaveFile saveFile = gson.fromJson(reader, SaveFile.class);
			if (saveFile == null)
			{
				return new RecordMap<>(changeListener);
			}

			return migrateIfNeeded(saveFile.schemaVersion, saveFile.records);
		}
	}

	@Override
	public void save(RecordMap<Assignment, AssignmentRecord> assignmentRecords, String dataFileName) throws Exception
	{
		if (dataFileName == null)
		{
			return;
		}
		File dataFile = getDataFile(dataFileName);

		SaveFile saveFile = new SaveFile();
		saveFile.schemaVersion = CURRENT_SCHEMA_VERSION;
		saveFile.records = assignmentRecords;

		try (Writer writer = new FileWriter(dataFile))
		{
			gson.toJson(saveFile, writer);
		}
	}

	private static InstanceCreator<AssignmentRecord> assignmentRecordCreator(PropertyChangeListener propertyChangeListener)
	{
		return type -> new AssignmentRecord(propertyChangeListener);
	}

	private static InstanceCreator<CustomRecord> customRecordCreator(PropertyChangeListener propertyChangeListener)
	{
		return type -> new CustomRecord(propertyChangeListener);
	}

	private static InstanceCreator<RecordMap<?, ? extends Record>> recordMapCreator(PropertyChangeListener propertyChangeListener)
	{
		return type -> new RecordMap<>(propertyChangeListener);
	}

	private static InstanceCreator<CustomRecordSet<CustomRecord>> customRecordSetCreator(PropertyChangeListener propertyChangeListener)
	{
		return type -> new CustomRecordSet<>(propertyChangeListener);
	}

	private RecordMap<Assignment, AssignmentRecord> migrateIfNeeded(int schemaVersion, RecordMap<Assignment, AssignmentRecord> records)
	{
		// Placeholder for future migrations; for now, just ensure the map is non-null
		return records == null ? new RecordMap<>(changeListener) : records;
	}

	private static class VariantAdapter extends TypeAdapter<Variant>
	{
		@Override
		public void write(JsonWriter out, Variant variant) throws IOException
		{
			if (variant == null)
			{
				out.nullValue();
				return;
			}

			out.value(variant.getId());
		}

		@Override
		public Variant read(JsonReader in) throws IOException
		{
			if (in.peek() == JsonToken.NULL)
			{
				in.nextNull();
				return null;
			}

			String id = in.nextString();
			return Variant.getById(id).orElseThrow(() -> new IOException("Unknown variant id: " + id));
		}
	}

	private static class SaveFile
	{
		@Expose
		int schemaVersion = CURRENT_SCHEMA_VERSION;
		@Expose
		RecordMap<Assignment, AssignmentRecord> records;
	}

	private static class InstantAdapter extends TypeAdapter<Instant>
	{
		@Override
		public void write(JsonWriter out, Instant instant) throws IOException
		{
			if (instant == null)
			{
				out.nullValue();
				return;
			}

			out.value(instant.toString());
		}

		@Override
		public Instant read(JsonReader in) throws IOException
		{
			if (in.peek() == JsonToken.NULL)
			{
				in.nextNull();
				return Instant.now();
			}

			String value = in.nextString();

			try
			{
				return Instant.parse(value);
			}
			catch (DateTimeParseException e)
			{
				try
				{
					return Instant.ofEpochMilli(Long.parseLong(value));
				}
				catch (NumberFormatException ignored)
				{
					throw new IOException("Invalid instant value: " + value, e);
				}
			}
		}
	}
}
