package com.slayertracker.model;

import com.google.gson.annotations.Expose;
import lombok.Getter;

import java.beans.PropertyChangeListener;

@Getter
public class VariantRecord extends Record {
    @Expose
    private Variant type;

    public VariantRecord(Variant type, PropertyChangeListener pcl) {
        super(pcl);
        this.type = type;
    }

    public VariantRecord(PropertyChangeListener pcl) {
        super(pcl);
    }
}