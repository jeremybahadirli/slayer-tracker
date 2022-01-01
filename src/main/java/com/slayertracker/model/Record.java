package com.slayertracker.model;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.NPC;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Record {

    @Expose
    private int kc;
    @Expose
    private float hours;
    @Expose
    private int xp;
    @Expose
    private int ge;
    @Expose
    private int ha;

    Set<NPC> interactors;

    private Instant startInstant;

    private final PropertyChangeSupport support;

    public Record(PropertyChangeListener pcl) {
        support = new PropertyChangeSupport(this);
        support.addPropertyChangeListener(pcl);

        interactors = new HashSet<>();

        kc = 0;
        hours = 0f;
        xp = 0;
        ge = 0;
        ha = 0;
    }

    public void incrementKc() {
        int oldVal = kc;
        kc++;
        support.firePropertyChange("kc", oldVal, kc);
    }

    public void addToHours(Duration d) {
        float oldVal = hours;
        hours = hours + (d.getSeconds() / 3600f);
        support.firePropertyChange("hours", oldVal, hours);
    }

    public void addToXp(int i) {
        int oldVal = xp;
        xp += i;
        support.firePropertyChange("xp", oldVal, xp);
    }

    public void addToGe(int i) {
        int oldVal = ge;
        ge += i;
        support.firePropertyChange("ge", oldVal, ge);
    }

    public void addToHa(int i) {
        int oldVal = ha;
        ha += i;
        support.firePropertyChange("ha", oldVal, ha);
    }

    public void setStartInstant(Instant instant) {
        startInstant = instant;
    }

    public int getKcRate() {
        return Math.round(kc / hours);
    }

    public int getXpRate() {
        return Math.round(xp / hours);
    }

    public int getGeRate() {
        return Math.round(ge / hours);
    }

    public int getHaRate() {
        return Math.round(ha / hours);
    }
}