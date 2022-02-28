package com.sjtu.demoapp;

import java.util.ArrayList;

public class MessageHolder {
    private static MessageHolder INSTANCE;
    public ArrayList<InfoStruct> Infos;
    public static MessageHolder getINSTANCE(){
        if(INSTANCE == null) {
            INSTANCE = new MessageHolder();
            INSTANCE.Infos = new ArrayList<>();
        }
        return INSTANCE;
    }
}
