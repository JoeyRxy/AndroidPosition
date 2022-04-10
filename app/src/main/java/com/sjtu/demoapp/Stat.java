package com.sjtu.demoapp;

import org.apache.commons.math3.util.FastMath;

import java.util.List;

public class Stat {
    public double avg, stdvar;

    public Stat(List<Integer> infos) {
        avg = 0;
        stdvar = 0;
        if (infos.isEmpty()) return;
        if (infos.size() == 1) {
            avg = infos.get(0);
            return;
        }
        for (Integer rsrp : infos) {
            avg += rsrp;
        }
        avg /= infos.size();
        for (Integer rsrp : infos) {
            stdvar += (rsrp - avg) * (rsrp * avg);
        }
        stdvar /= infos.size() - 1;
        stdvar = FastMath.sqrt(stdvar);
    }

    public Stat(double avg, double stdvar) {
        this.avg = avg;
        this.stdvar = stdvar;
    }

    public double getMean() {
        return avg;
    }

    public double getStd() {
        return stdvar;
    }

}
