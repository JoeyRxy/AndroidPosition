package com.sjtu.demoapp;

import android.icu.text.IDNA;
import android.util.Log;
import android.util.Pair;

import com.sjtu.demoapp.database.InfoDatabase;

import org.apache.commons.math3.analysis.function.Max;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.function.Function;


import bolts.Continuation;
import bolts.Task;

public class Locator {
    private static final String TAG = "Locator";
    public int n = 3;
    public int defThreshold = 5;
    public int threshold = defThreshold;
    public double neighborDistance = 0.2;
    public boolean dirEnable = true;
    private HashMap<Integer, MaxPosteriProb> mapOnFloor;

    public Locator() {
        HashMap<Integer, HashMap<Location, HashMap<Integer, Stat>>> locStatMapOnFloor = new HashMap<>();
        mapOnFloor = new HashMap<>();
        for (Location loc : InfoDatabase.getInstance().locationDao().loadAll()) {
            locStatMapOnFloor.computeIfAbsent(loc.getFloor(), k -> new HashMap<>()).put(loc, InfoDatabase.getInstance().statDao().getStat(loc.getId()).getStatMap());
        }
        for (Map.Entry<Integer, HashMap<Location, HashMap<Integer, Stat>>> entry : locStatMapOnFloor.entrySet()) {
            mapOnFloor.put(entry.getKey(), new MaxPosteriProb(entry.getValue().keySet(), entry.getValue()));
        }
    }

    public Locator(int n, int threshold, int neighborDistance, boolean dirEnable) {
        this.n = n;
        this.threshold = threshold;
        this.neighborDistance = neighborDistance;
        this.dirEnable = dirEnable;
    }

    public Location locateByMAP(final InfoStruct info, int floor) {
        return Objects.requireNonNull(mapOnFloor.get(floor)).predict(info.cellSignalStrengthMap);
    }

}

interface LocateCallback {
    void run(InfoStruct position);
}