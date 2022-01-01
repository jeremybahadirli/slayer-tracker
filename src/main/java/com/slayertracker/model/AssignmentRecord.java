package com.slayertracker.model;

import com.google.gson.annotations.Expose;
import lombok.Getter;

import java.beans.PropertyChangeListener;
import java.util.HashMap;

@Getter
public class AssignmentRecord extends Record {
    public static final String RECORD_KEY = "a-";

    @Expose
    private Assignment type;
    @Expose
    private final HashMap<Variant, VariantRecord> variantRecords = new HashMap<>();

    public AssignmentRecord(Assignment type, PropertyChangeListener pcl) {
        super(pcl);
        this.type = type;
    }

    public AssignmentRecord(PropertyChangeListener pcl) {
        super(pcl);
    }
}
