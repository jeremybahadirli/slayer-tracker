/*
 * Copyright (c) 2023, Jeremy Bahadirli <https://github.com/jeremybahadirli>
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.slayertracker.groups.Assignment;
import com.slayertracker.records.AssignmentRecord;
import com.slayertracker.records.CustomRecord;
import com.slayertracker.records.CustomRecordSet;
import com.slayertracker.records.Record;
import com.slayertracker.records.RecordMap;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.util.HashMap;
import lombok.Setter;
import net.runelite.client.RuneLite;

public class SlayerTrackerSaveManager
{
	private final Gson gson;

	public static final String DATA_FOLDER_NAME = "slayer-tracker";
	public static final File DATA_FOLDER = new File(RuneLite.RUNELITE_DIR, DATA_FOLDER_NAME);
	@Setter
	private String dataFileName;

	public SlayerTrackerSaveManager(SlayerTrackerPlugin plugin)
	{
		// GSON serializes record data to JSON for disk storage
		gson = new GsonBuilder()
			// Only serialize fields with @Expose
			.excludeFieldsWithoutExposeAnnotation()
			// Save as human-readable JSON (newlines/tabs)
			.setPrettyPrinting()
			// When reconstructing records from JSON, apply property change listeners
			.registerTypeAdapter(AssignmentRecord.class, (InstanceCreator<Record>) type -> new AssignmentRecord(plugin))
			.registerTypeAdapter(RecordMap.class, (InstanceCreator<RecordMap<?, ? extends Record>>) type -> new RecordMap<>(plugin))
			.registerTypeAdapter(CustomRecordSet.class, (InstanceCreator<CustomRecordSet<CustomRecord>>) type -> new CustomRecordSet<>(plugin))
			// GSON doesn't recognize Instant, so serialize/deserialize as a long
			.registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (instant, type, context) ->
				new JsonPrimitive(instant.getEpochSecond()))
			.registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, type, context) ->
				Instant.ofEpochSecond(json.getAsLong()))
			.create();
	}

	private File getDataFile() throws IOException
	{
		// Throw exception if data folder could not be created
		if (!DATA_FOLDER.exists() && !DATA_FOLDER.mkdirs())
		{
			throw new IOException("Could not create data folder: .runelite/slayer-tracker");
		}
		return new File(DATA_FOLDER, dataFileName);
	}

	HashMap<Assignment, AssignmentRecord> loadRecordsFromDisk() throws Exception
	{
		File dataFile = getDataFile();

		// If data file doesn't exist, create one with an empty assignment RecordMap
		if (!dataFile.exists())
		{
			Writer writer = new FileWriter(dataFile);
			writer.write("{}");
			writer.close();
		}

		// Deserialize json from data file, as HashMap<Assignment, AssignmentRecord>
		// then copy it into assignmentRecords
		// Must copy it in, because the ui has already received this RecordMap instance
		return gson.fromJson(new FileReader(dataFile), new TypeToken<HashMap<Assignment, AssignmentRecord>>()
		{
		}.getType());
	}

	void saveRecordsToDisk(RecordMap<Assignment, AssignmentRecord> assignmentRecords) throws Exception
	{
		if (dataFileName == null)
		{
			return;
		}
		File dataFile = getDataFile();

		// Serialize assignmentRecords to json and write to the data file
		Writer writer = new FileWriter(dataFile);
		gson.toJson(assignmentRecords, writer);
		writer.flush();
		writer.close();
	}
}
