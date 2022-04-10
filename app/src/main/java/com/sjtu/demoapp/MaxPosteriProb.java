package com.sjtu.demoapp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.math3.distribution.NormalDistribution;

public class MaxPosteriProb {

    private final Set<Location> L;
    private final HashMap<Location, HashMap<Integer, NormalDistribution>> omitProb;

    /**
     *
     * @param L     位置集合{0,1,...,L-1}
     * @param stats 各个位置上的各个基站的统计信息：<code>stats[l].get(pci)</code>是l位置pci基站的统计信息（均值和标准差）
     */
    @SuppressWarnings("unchecked")
    public MaxPosteriProb(Set<Location> L, HashMap<Location, HashMap<Integer, Stat>> stats) {
        assert L.size() == stats.size();
        //
        this.L = L;
        omitProb = new HashMap<>();

        for (Location loc : L) {
            HashMap<Integer, NormalDistribution> _omitProb = omitProb.computeIfAbsent(loc, k -> new HashMap<>());
            for (Entry<Integer, Stat> entry : Objects.requireNonNull(stats.get(loc)).entrySet()) {
                _omitProb.put(entry.getKey(), new NormalDistribution(entry.getValue().getMean(), entry.getValue().getStd()));
            }
        }
    }

    /**
     *
     * @param infoList (pci, rsrp) list
     * @return
     */
    public Location predict(HashMap<Integer, Integer> infoList) {
        Location argmax = null;
        double max = Double.NEGATIVE_INFINITY;
        for (Location loc : L) {
            double prob = 0;
            NormalDistribution pdf;
            for (Entry<Integer, Integer> entry : infoList.entrySet())
                if (prob != Double.NEGATIVE_INFINITY && (pdf = Objects.requireNonNull(omitProb.get(loc)).get(entry.getKey())) != null)
                        prob += Util.logarithm(pdf.density(entry.getValue()));
            if (prob > max) {
                max = prob;
                argmax = loc;
            }
        }
        return argmax;
    }

}

