package com.slayertracker.model;

import com.google.gson.annotations.Expose;
import lombok.Getter;

import java.beans.PropertyChangeListener;
import java.util.HashMap;

@Getter
public class AssignmentRecord extends Record {
    @Expose
    private final HashMap<Variant, Record> variantRecords = new HashMap<>();

    public AssignmentRecord(PropertyChangeListener pcl) {
        super(pcl);
    }
}