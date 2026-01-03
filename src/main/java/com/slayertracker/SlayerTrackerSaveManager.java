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
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import lombok.Setter;
import net.runelite.client.RuneLite;

public class SlayerTrackerSaveManager
{
	public static final String DATA_FOLDER_NAME = "slayer-tracker";
	public static final File DATA_FOLDER = new File(RuneLite.RUNELITE_DIR, DATA_FOLDER_NAME);

	private static final Type ASSIGNMENT_RECORD_MAP_TYPE = new TypeToken<HashMap<Assignment, AssignmentRecord>>()
	{
	}.getType();

	private final Gson gson;
	@Setter
	private String dataFileName;

	public SlayerTrackerSaveManager(SlayerTrackerPlugin plugin)
	{
		gson = new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.setPrettyPrinting()
			.registerTypeAdapter(AssignmentRecord.class, assignmentRecordCreator(plugin))
			.registerTypeAdapter(RecordMap.class, recordMapCreator(plugin))
			.registerTypeAdapter(CustomRecordSet.class, customRecordSetCreator(plugin))
			.registerTypeAdapter(Variant.class, new VariantAdapter())
			.create();
	}

	private File getDataFile() throws IOException
	{
		if (!DATA_FOLDER.exists() && !DATA_FOLDER.mkdirs())
		{
			throw new IOException("Could not create data folder: .runelite/slayer-tracker");
		}
		return new File(DATA_FOLDER, dataFileName);
	}

	HashMap<Assignment, AssignmentRecord> loadRecordsFromDisk() throws Exception
	{
		File dataFile = getDataFile();

		if (!dataFile.exists())
		{
			Writer writer = new FileWriter(dataFile);
			writer.write("{}");
			writer.close();
		}

		return gson.fromJson(new FileReader(dataFile), ASSIGNMENT_RECORD_MAP_TYPE);
	}

	void saveRecordsToDisk(RecordMap<Assignment, AssignmentRecord> assignmentRecords) throws Exception
	{
		if (dataFileName == null)
		{
			return;
		}
		File dataFile = getDataFile();

		Writer writer = new FileWriter(dataFile);
		gson.toJson(assignmentRecords, writer);
		writer.flush();
		writer.close();
	}

	private static InstanceCreator<Record> assignmentRecordCreator(SlayerTrackerPlugin plugin)
	{
		return type -> new AssignmentRecord(plugin);
	}

	private static InstanceCreator<RecordMap<?, ? extends Record>> recordMapCreator(SlayerTrackerPlugin plugin)
	{
		return type -> new RecordMap<>(plugin);
	}

	private static InstanceCreator<CustomRecordSet<CustomRecord>> customRecordSetCreator(SlayerTrackerPlugin plugin)
	{
		return type -> new CustomRecordSet<>(plugin);
	}

	public static class VariantAdapter extends TypeAdapter<Variant>
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
}
