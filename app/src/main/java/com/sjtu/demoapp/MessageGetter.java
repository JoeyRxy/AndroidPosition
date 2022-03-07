package com.sjtu.demoapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthNr;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.sjtu.demoapp.database.InfoDatabase;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;

public class MessageGetter {
    private static final String TAG = "MessageGetter";
    public static volatile boolean sampling = false;
    public static int samplingTime = 0;
    private final Context context;

    public MessageGetter(Context context) {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    void getAllSignalInfo(CellInfoCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getAllSignalInfo: 权限请求失败，检查权限");
            return;
        }
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        // 0 对应卡槽0 所对应的系统中的subId，1 对应卡槽1
        int[] subIds0 = subscriptionManager.getSubscriptionIds(0);
        int[] subIds1 = subscriptionManager.getSubscriptionIds(1);
        // 判断是否有效，如果有效，取返回的数组中的第一位，为subId
        int subId0 = subIds0 != null && subIds0.length > 0 ? subIds0[0] : SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        int subId1 = subIds1 != null && subIds1.length > 0 ? subIds1[0] : SubscriptionManager.INVALID_SUBSCRIPTION_ID;

        // 获取系统的TelphoneManager对象 系统的telehonyM
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // 卡槽0 对应的TelephonyManager
        TelephonyManager tm0 = telephonyManager.createForSubscriptionId(subId0);
        // 卡槽1 对应的TelephonyManager
        TelephonyManager tm1 = telephonyManager.createForSubscriptionId(subId1);


        TaskCompletionSource<Boolean> taskCompletionSource1 = new TaskCompletionSource<>();
        tm0.requestCellInfoUpdate(Executors.newSingleThreadExecutor(), new TelephonyManager.CellInfoCallback() {
            @Override
            public void onCellInfo(@NonNull List<CellInfo> cellInfo) {
                Log.d(TAG, "onCellInfo: tm0 requested");
                taskCompletionSource1.setResult(true);
            }
        });
        Task<Boolean> task1 = taskCompletionSource1.getTask();
        TaskCompletionSource<Boolean> taskCompletionSource2 = new TaskCompletionSource<>();
        tm1.requestCellInfoUpdate(Executors.newSingleThreadExecutor(), new TelephonyManager.CellInfoCallback() {
            @Override
            public void onCellInfo(@NonNull List<CellInfo> cellInfo) {
                taskCompletionSource2.setResult(true);
            }
        });
        Task<Boolean> task2 = taskCompletionSource2.getTask();
        Task.whenAll(Arrays.asList(task1, task2)).continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                callback.onCellInfoAnalysed(analyseCellInfo());
                return null;
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public InfoStruct analyseCellInfo() {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
        InfoStruct infoStruct = new InfoStruct();
        int firstDbm = 0;

        // 这里把部分cid改成了pci 更短更好用 有问题就改回去

        for (CellInfo cellInfo : cellInfos) {
//            boolean first = infoStruct.cellSignalStrengthMap.isEmpty();
            if (cellInfo instanceof CellInfoGsm) {
                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                if (filterCell(cellInfoGsm.getCellIdentity().getCid())) {
                    infoStruct.cellSignalStrengthMap.put(cellInfoGsm.getCellIdentity().getCid(),
                            cellInfoGsm.getCellSignalStrength().getDbm());
                }
            } else if (cellInfo instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                if (filterCell(cellInfoLte.getCellIdentity().getCi())) {
                    infoStruct.cellSignalStrengthMap.put(cellInfoLte.getCellIdentity().getPci(),
                            cellInfoLte.getCellSignalStrength().getDbm());
                }
            } else if (cellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                if (filterCell(cellInfoWcdma.getCellIdentity().getCid())) {
                    infoStruct.cellSignalStrengthMap.put(cellInfoWcdma.getCellIdentity().getCid(),
                            cellInfoWcdma.getCellSignalStrength().getDbm());
                }
            } else if (cellInfo instanceof CellInfoNr) {
                CellInfoNr cellInfoNr = (CellInfoNr) cellInfo;
                CellIdentityNr cellIdentityNr = (CellIdentityNr) cellInfoNr.getCellIdentity();
                if (filterCell(cellIdentityNr.getPci())) {
                    infoStruct.cellSignalStrengthMap.put(cellIdentityNr.getPci(),
                            ((CellSignalStrengthNr)cellInfoNr.getCellSignalStrength()).getSsRsrp()*-1);
                }
            }
        }
        return infoStruct;
    }
    void getMessageRepeatedly(int frequency, CellInfoCallback callback) {
        Task.callInBackground(new Callable<Object>() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public Object call() throws Exception {
                while(sampling) {
                    for(int i = 0; i < frequency; i++) {
                        getAllSignalInfo(callback);
                        Thread.sleep(1000/frequency);
                    }
                    samplingTime++;
                }
                samplingTime = 0;
                return null;
            }
        });

    }
    public boolean filterCell(long cellId) {
        //todo: 过滤实际使用的基站
        return true;
    }
}
