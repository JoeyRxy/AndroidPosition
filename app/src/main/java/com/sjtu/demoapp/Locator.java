package com.sjtu.demoapp;

import android.icu.text.IDNA;
import android.util.Log;
import android.util.Pair;

import com.sjtu.demoapp.database.InfoDatabase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;


import bolts.Continuation;
import bolts.Task;

public class Locator {
    private static final String TAG = "Locator";
    public int n = 3;
    public int threshold = 5;
    public double neighborDistance = 0.2;
    public static double DIR_DISABLE = 100;
    public Locator() {

    }

    public Locator(int n, int threshold, int neighborDistance) {
        this.n = n;
        this.threshold = threshold;
        this.neighborDistance = neighborDistance;
    }

    void locateWithRSSI(InfoStruct info, LocateCallback callback, InfoStruct lastRes, Direction lastDir) {
        Task.callInBackground(new Callable<InfoStruct>() {
            @Override
            public InfoStruct call() throws Exception {
                //从数据库取数据
                List<InfoStruct> infoStructs = InfoDatabase.getInstance().infoDao().loadData(info.floor);
                //先找到现有数据里最大的那个
                int maxId = Integer.MIN_VALUE;
                int maxValue = Integer.MIN_VALUE;
                for (Map.Entry<Integer, Integer> entry : info.getCellSignalStrengthMap().entrySet()) {
                    if (entry.getValue() > maxValue) {
                        maxValue = entry.getValue();
                        maxId = entry.getKey();
                    }
                }
                //先比较最大的 做第一次划分 阈值3
                List<InfoStruct> sameAreaData = new ArrayList<>();
                for (InfoStruct infoStruct : infoStructs) {
                    Integer value = infoStruct.getCellSignalStrengthMap().get(maxId);
                    if (value != null && value >= maxValue - threshold && value <= maxValue + threshold) {
                        //加一个是否在附近的判断，再加入对应方向的筛选
                        if (lastRes != null) {
                            double mapDistance = Math.sqrt((lastRes.x - infoStruct.x) * (lastRes.x - infoStruct.x) + (lastRes.y - infoStruct.y) * (lastRes.y - infoStruct.y));
                            if (mapDistance <= neighborDistance) {
                                if (lastDir != Direction.NONE && neighborDistance != DIR_DISABLE) {
                                    switch (lastDir) {
                                        case UP:
                                            if (infoStruct.y <= lastRes.y) {
                                                sameAreaData.add(infoStruct);
                                            }
                                            break;
                                        case DOWN:
                                            if (infoStruct.y >= lastRes.y) {
                                                sameAreaData.add(infoStruct);
                                            }
                                            break;
                                        case LEFT:
                                            if (infoStruct.x <= lastRes.x) {
                                                sameAreaData.add(infoStruct);
                                            }
                                            break;
                                        case RIGHT:
                                            if (infoStruct.x >= lastRes.x) {
                                                sameAreaData.add(infoStruct);
                                            }
                                            break;
                                    }
                                } else {
                                    sameAreaData.add(infoStruct);
                                }
                            }
                        } else {
                            sameAreaData.add(infoStruct);
                        }
                    }
                }
                if (sameAreaData.size() == 0) return new InfoStruct();
                //然后对后续数据进行归一化之后再进行排序,求欧氏距离（暂时取消归一化）
                PriorityQueue<CompareData> minHeap = new PriorityQueue<CompareData>(11, new Comparator<CompareData>() { //大顶堆，容量11
                    @Override
                    public int compare(CompareData i1, CompareData i2) {
                        return Float.compare(i1.distance, i2.distance);
                    }
                });
                for (InfoStruct data : sameAreaData) {
                    double distance = calculateDataDistance(info, data, maxId);
                    CompareData dfl = new CompareData(data.x, data.y, data.floor, (float) distance);
                    minHeap.add(dfl);
                }

                //最小堆中现在已有排序后的距离数据了，取n个来平均，默认为5
                List<CompareData> locateData = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    CompareData d = minHeap.poll();
                    if (d != null) {
                        locateData.add(d);
                        Log.d(TAG, "定位中： 取出第" + i + "小的元素，为： " + d.distance);
                    } else {
                        break;
                    }
                }

                InfoStruct result = new InfoStruct();
                try {
                    result.floor = locateData.get(0).floor;
                    float totalPower = 0;
                    //权重是距离的倒数，距离越近应当权重越大
                    for (CompareData locateDatum : locateData) {
                        if (locateDatum.distance != 0) {
                            totalPower += 1 / locateDatum.distance;
                        } else {
                            InfoStruct res = new InfoStruct();
                            res.floor = locateDatum.floor;
                            res.x = locateDatum.x;
                            res.y = locateDatum.y;
                            return res;
                        }
                    }
                    result.x = 0;
                    result.y = 0;
                    for (CompareData locateDatum : locateData) {
                        result.x += locateDatum.x * (1 / locateDatum.distance) / totalPower;
                        result.y += locateDatum.y * (1 / locateDatum.distance) / totalPower;
                    }
                    Log.d(TAG, "call: 最终得到的坐标" + result.x + ", " + result.y + ", " + result.floor + "F");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return result;
            }
        }).continueWith(new Continuation<InfoStruct, Object>() {
            @Override
            public Object then(Task<InfoStruct> task) throws Exception {
                callback.run(task.getResult());
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);


    }

    private double calculateDataDistance(InfoStruct info, InfoStruct data, int maxId) {
        //map 拷贝一下 不修改原数据 避免意外
        HashMap<Integer, Integer> infoMap = new HashMap<>(info.getCellSignalStrengthMap());
        HashMap<Integer, Integer> dataMap = new HashMap<>(data.getCellSignalStrengthMap());
//        if(infoMap.get(maxId) == null || dataMap.get(maxId) == null) {
//            Log.d(TAG, "calculateDataDistance: Error! Null max value");
//            return 0;
//        }
        //归一化所有数据，减少整体波动带来的影响（暂时取消归一化）
//        int maxValue = infoMap.get(maxId);
//        infoMap.replaceAll((k, v) -> v - maxValue);
//        int dataMaxValue = dataMap.get(maxId);
//        dataMap.replaceAll((k, v) -> v - dataMaxValue);

        //开始计算欧氏距离
        float distance2 = 0;
        for (Map.Entry<Integer, Integer> entry : infoMap.entrySet()) {
            int infoRSS = entry.getValue();
            int dataRSS = dataMap.get(entry.getKey()) != null ? dataMap.get(entry.getKey()) : -140;
            distance2 += (infoRSS - dataRSS) * (infoRSS - dataRSS);
        }
        return Math.sqrt(distance2);
    }
}

interface LocateCallback {
    void run(InfoStruct position);
}