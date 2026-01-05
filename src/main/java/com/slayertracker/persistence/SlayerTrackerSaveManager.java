package com.slayertracker.persistence;

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
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import net.runelite.client.RuneLite;

public class SlayerTrackerSaveManager implements RecordRepository
{
	public static final String DATA_FOLDER_NAME = "slayer-tracker";
	public static final File DATA_FOLDER = new File(RuneLite.RUNELITE_DIR, DATA_FOLDER_NAME);

	private static final Type ASSIGNMENT_RECORD_MAP_TYPE = new TypeToken<HashMap<Assignment, AssignmentRecord>>()
	{
	}.getType();

	private final Gson gson;

	public SlayerTrackerSaveManager(PropertyChangeListener propertyChangeListener)
	{
		gson = new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.setPrettyPrinting()
			.registerTypeAdapter(AssignmentRecord.class, assignmentRecordCreator(propertyChangeListener))
			.registerTypeAdapter(RecordMap.class, recordMapCreator(propertyChangeListener))
			.registerTypeAdapter(CustomRecordSet.class, customRecordSetCreator(propertyChangeListener))
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

		return gson.fromJson(new FileReader(dataFile), ASSIGNMENT_RECORD_MAP_TYPE);
	}

	@Override
	public void save(RecordMap<Assignment, AssignmentRecord> assignmentRecords, String dataFileName) throws Exception
	{
		if (dataFileName == null)
		{
			return;
		}
		File dataFile = getDataFile(dataFileName);

		Writer writer = new FileWriter(dataFile);
		gson.toJson(assignmentRecords, writer);
		writer.flush();
		writer.close();
	}

	private static InstanceCreator<Record> assignmentRecordCreator(PropertyChangeListener propertyChangeListener)
	{
		return type -> new AssignmentRecord(propertyChangeListener);
	}

	private static InstanceCreator<RecordMap<?, ? extends Record>> recordMapCreator(PropertyChangeListener propertyChangeListener)
	{
		return type -> new RecordMap<>(propertyChangeListener);
	}

	private static InstanceCreator<CustomRecordSet<CustomRecord>> customRecordSetCreator(PropertyChangeListener propertyChangeListener)
	{
		return type -> new CustomRecordSet<>(propertyChangeListener);
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
