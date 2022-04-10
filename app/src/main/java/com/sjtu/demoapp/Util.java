package com.sjtu.demoapp;

import com.sjtu.demoapp.database.InfoDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {

    public static double logarithm(double x) {
        return Math.log1p(x - 1);
    }

    public static void logarithm(double[] array) {
        for (int i = 0; i < array.length; ++i)
            array[i] = Math.log1p(array[i] - 1);
    }

    public static void logarithm(double[][] array) {
        for (double[] doubles : array) logarithm(doubles);
    }

    private static class Logarithm implements Runnable {
        double[][] array;

        Logarithm(double[][] array) {
            this.array = array;
        }

        @Override
        public void run() {
            logarithm(array);
        }
    }

    public static void logarithm(double[][][] array) {
        Thread[] threads = new Thread[array.length];
        for (int i = 0; i < array.length; ++i) {
            threads[i] = new Thread(new Logarithm(array[i]));
            threads[i].start();
        }
        for (int i = 0; i < array.length; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                // logarithm(array[i]);
                e.printStackTrace();
            }
        }
    }

    // loc -> pci -> stat

    public static void calculateStat() {
        List<Location> locList = InfoDatabase.getInstance().locationDao().loadAll();
        for (Location loc : locList) {
            List<InfoStruct> infos = InfoDatabase.getInstance().infoDao().loadByLocId(loc.getId());
            HashMap<Integer, List<Integer>> pciRsrpListMap = new HashMap<>();
            for (InfoStruct info : infos) {
                HashMap<Integer, Integer> pciRsrpMap = info.getCellSignalStrengthMap();
                for (Map.Entry<Integer, Integer> pciRsrp : pciRsrpMap.entrySet()) {
                    pciRsrpListMap.computeIfAbsent(pciRsrp.getKey(), k -> new ArrayList<>()).add(pciRsrp.getValue());
                }
            }
            HashMap<Integer, Stat> statMap = new HashMap<>();
            for (Map.Entry<Integer, List<Integer>> entry : pciRsrpListMap.entrySet()) {
                statMap.put(entry.getKey(), new Stat(entry.getValue()));
            }
            InfoDatabase.getInstance().statDao().insert(new StatEntity(loc.getId(), statMap));
        }
    }

}

