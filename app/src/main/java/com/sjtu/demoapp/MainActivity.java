package com.sjtu.demoapp;

import static android.telephony.PhoneStateListener.LISTEN_CELL_INFO;
import static android.telephony.PhoneStateListener.LISTEN_CELL_LOCATION;
import static android.telephony.PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Permissions;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainTag";
    TextView infoText;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        infoText = findViewById(R.id.infoDisText);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 2);
        }
        ((Button) findViewById(R.id.getInfoBtn)).setOnClickListener((view) -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                getLocation();
//                timer = System.currentTimeMillis();
//                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                tm.listen(mPhoneStateListener, LISTEN_CELL_INFO | LISTEN_CELL_LOCATION);
            }

        });
        ((Button) findViewById(R.id.enterBtn)).setOnClickListener((view) -> {
            Intent intent = new Intent(this, PositioningMainActivity.class);
            startActivity(intent);
        });
        ((Button) findViewById(R.id.sensorBtn)).setOnClickListener((view) -> {
            Intent intent = new Intent(this, SensorActivity.class);
            startActivity(intent);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
//            timer = System.currentTimeMillis();
//            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            tm.listen(mPhoneStateListener, LISTEN_SIGNAL_STRENGTHS);
        } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "获取权限失败", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    void getLocation() {
        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String operator = mTelephonyManager.getNetworkOperator();
        int mcc = Integer.parseInt(operator.substring(0, 3));
        int mnc = Integer.parseInt(operator.substring(3));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            infoText.setText("你没有权限");
            return;
        }
        StringBuilder res = new StringBuilder();
        SubscriptionManager subscriptionManager = (SubscriptionManager) this.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        // 0 对应卡槽0 所对应的系统中的subId，1 对应卡槽1
        int[] subIds0 = subscriptionManager.getSubscriptionIds(0);
        int[] subIds1 = subscriptionManager.getSubscriptionIds(1);
        // 判断是否有效，如果有效，取返回的数组中的第一位，为subId
        int subId0 = subIds0 != null && subIds0.length > 0 ? subIds0[0] : SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        int subId1 = subIds1 != null && subIds1.length > 0 ? subIds1[0] : SubscriptionManager.INVALID_SUBSCRIPTION_ID;

        // 获取系统的TelphoneManager对象 系统的telehonyM
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        // 卡槽0 对应的TelephonyManager
        TelephonyManager tm0 = telephonyManager.createForSubscriptionId(subId0);
        // 卡槽1 对应的TelephonyManager
        TelephonyManager tm1 = telephonyManager.createForSubscriptionId(subId1);
        // 获取邻区基站信息

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
        List<CellInfo> cellInfos = null;
        List<NeighboringCellInfo> neighboringCellInfos = null;
        cellInfos = mTelephonyManager.getAllCellInfo();

        tm0.requestCellInfoUpdate(Executors.newSingleThreadExecutor(), new TelephonyManager.CellInfoCallback() {
            @Override
            public void onCellInfo(@NonNull List<CellInfo> cellInfo) {
                Log.d(TAG, "onCellInfo: tm0 requested");
            }
        });

        tm1.requestCellInfoUpdate(Executors.newSingleThreadExecutor(), new TelephonyManager.CellInfoCallback() {
            @Override
            public void onCellInfo(@NonNull List<CellInfo> cellInfo) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
//                mPhoneStateListener.onCellInfoChanged(tm1.getAllCellInfo());
//                List<CellInfo> allCellInfo = tm1.getAllCellInfo();
//                CellInfoLte cellInfoLte = (CellInfoLte) allCellInfo.get(0);
//                Log.d(TAG, "onCellInfo: AsuLevel："+ cellInfoLte.getCellSignalStrength().getAsuLevel());
//                Log.d(TAG, "onCellInfo: Dbm："+ cellInfoLte.getCellSignalStrength().getDbm());
//                Log.d(TAG, "onCellInfo: Cqi："+ cellInfoLte.getCellSignalStrength().getCqi());
//                Log.d(TAG, "onCellInfo: Rsrq："+ cellInfoLte.getCellSignalStrength().getRsrq());
//                Log.d(TAG, "onCellInfo: Rssnr："+ cellInfoLte.getCellSignalStrength().getRssnr());
//                Log.d(TAG, "onCellInfo: Rsrp："+ cellInfoLte.getCellSignalStrength().getRsrp());
//                Log.d(TAG, "onCellInfo: Rssi："+ cellInfoLte.getCellSignalStrength().getRssi());

                res.append(tm1.getAllCellInfo().toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        infoText.setText(res.toString());
                        Log.d(TAG, res.toString());
                    }
                });
            }
        });

    }

    public String getNetworkOperatorName() {
        String opeType = "unknown";
        // No sim
        if (!hasSim(getApplication().getApplicationContext())) {
            return opeType;
        }

        TelephonyManager tm = (TelephonyManager) getApplication().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String operator = tm.getSimOperator();
        if ("46001".equals(operator) || "46006".equals(operator) || "46009".equals(operator)) {
            opeType = "中国联通";
        } else if ("46000".equals(operator) || "46002".equals(operator) || "46004".equals(operator) || "46007".equals(operator)) {
            opeType = "中国移动";

        } else if ("46003".equals(operator) || "46005".equals(operator) || "46011".equals(operator)) {
            opeType = "中国电信";
        } else {
            opeType = "unknown";
        }
        return opeType;
    }

    /**
     * 检查手机是否有sim卡
     */
    private boolean hasSim(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = tm.getSimOperator();
        if (TextUtils.isEmpty(operator)) {
            return false;
        }
        return true;
    }

}