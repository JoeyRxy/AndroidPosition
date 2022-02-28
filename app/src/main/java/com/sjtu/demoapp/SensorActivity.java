package com.sjtu.demoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class SensorActivity extends AppCompatActivity {

    private SensorManager sm;
    //需要两个Sensor
    private Sensor aSensor;
    private Sensor mSensor;
    private Sensor laSensor;
    private Sensor stepSensor;
    private int steps = 0;
    private int windowlength = 30;
    private long t = System.currentTimeMillis();
    private long spdT = 0;
    private TextView accText;
    private TextView oriText;
    private TextView spdText;
    private TextView stepText;


    float[] accelerometerValues = new float[3];
    float[] linearAccelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];
    float[] xAccWindow = new float[windowlength];
    float[] yAccWindow = new float[windowlength];
    float lastX = 0;
    float lastY = 0;
    float xTotal = 0;
    float yTotal = 0;
    float speedX = 0;
    float speedY = 0;
    int count = 0;
    int ptr = 0;
    private static final String TAG = "sensor";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        oriText = findViewById(R.id.oriText);
        accText = findViewById(R.id.accText);
        spdText = findViewById(R.id.spdText);
        stepText = findViewById(R.id.stepText);
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        laSensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        stepSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //更新显示数据的方法
        calculateOrientation();

    }

    //再次强调：注意activity暂停的时候释放
    public void onPause(){
        sm.unregisterListener(myListener);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sm.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(myListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(myListener, laSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(myListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    final SensorEventListener myListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                if (sensorEvent.values[0] == 1.0){
                    steps++;
                    stepText.setText(steps + "");
                }

            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = sensorEvent.values;
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = sensorEvent.values;
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                linearAccelerometerValues = sensorEvent.values;
                updateAccWindow(linearAccelerometerValues);
            }
            calculateOrientation();
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    private  void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        // 要经过一次数据格式的转换，转换为度
        values[0] = (float) Math.toDegrees(values[0]);
//        Log.i(TAG, values[0]+"");
        //values[1] = (float) Math.toDegrees(values[1]);
        //values[2] = (float) Math.toDegrees(values[2]);

        if(values[0] >= -5 && values[0] < 5){
//            Log.i(TAG, "正北");
            oriText.setText("正北");
        }
        else if(values[0] >= 5 && values[0] < 85){
//            Log.i(TAG, "东北");
            oriText.setText("东北");
        }
        else if(values[0] >= 85 && values[0] <=95){
//            Log.i(TAG, "正东");
            oriText.setText("正东");
        }
        else if(values[0] >= 95 && values[0] <175){
//            Log.i(TAG, "东南");
            oriText.setText("东南");
        }
        else if((values[0] >= 175 && values[0] <= 180) || (values[0]) >= -180 && values[0] < -175){
//            Log.i(TAG, "正南");
            oriText.setText("正南");
        }
        else if(values[0] >= -175 && values[0] <-95){
//            Log.i(TAG, "西南");
            oriText.setText("西南");
        }
        else if(values[0] >= -95 && values[0] < -85){
//            Log.i(TAG, "正西");
            oriText.setText("正西");
        }
        else if(values[0] >= -85 && values[0] <-5){
//            Log.i(TAG, "西北");
            oriText.setText("西北");
        }
        if(System.currentTimeMillis() - t > 100) {
            accText.setText(linearAccelerometerValues[0] + ", " + linearAccelerometerValues[1]);
            spdText.setText(speedX + ", " + speedY);
            t = System.currentTimeMillis();
        }
    }

    void updateAccWindow(float[] values) {
        if (count < windowlength) {
            count++;
        } else if (ptr == windowlength) {
            ptr = 0;
            xTotal -= xAccWindow[ptr];
            yTotal -= yAccWindow[ptr];
        } else {
            xTotal -= xAccWindow[ptr];
            yTotal -= yAccWindow[ptr];
        }
        xTotal += values[0];
        yTotal += values[1];
        xAccWindow[ptr] = values[0];
        yAccWindow[ptr] = values[1];
//        if (spdT != 0) {
//            long x = System.currentTimeMillis() - spdT;
        speedX +=  values[0];
        speedY +=  values[1];
//        }
        //        spdT = System.currentTimeMillis();
//
//        lastX = values[0];
//        lastY = values[1];
        ptr++;
    }

}