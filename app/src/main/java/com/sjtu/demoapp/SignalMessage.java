package com.sjtu.demoapp;

public class SignalMessage {
    public int signalStrength;
    public int cqi = Integer.MIN_VALUE;
    public int rsrp = Integer.MIN_VALUE;
    public int csiSinr = Integer.MIN_VALUE;

    public SignalMessage(int signalStrength, int cqi, int rsrp, int csiSinr) {
        this.signalStrength = signalStrength;
        this.cqi = cqi;
        this.rsrp = rsrp;
        this.csiSinr = csiSinr;
    }

}
