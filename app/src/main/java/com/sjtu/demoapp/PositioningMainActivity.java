package com.sjtu.demoapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.sjtu.demoapp.database.InfoDatabase;

import java.util.List;

public class PositioningMainActivity extends AppCompatActivity {
    private final int SAMPLE_COUNT_THRESHOLD = 5;
    PhotoView pv;
    int currentMap;
    int fps = 5;
    int sampleCount = 0;
    Paint paint = new Paint();
    float pointRadius;
    Button moveLeftBtn;
    Button moveRightBtn;
    Button moveUpBtn;
    Button moveDownBtn;
    Button changeMapBtn;
    Button getInfoBtn;
    Button intentBtn;
    Button relatedDataBtn;
    Button locateBtn;
    CheckBox disableBox;
    Locator locator = new Locator();
    EditText tvFps;
    EditText thresholdText;
    EditText neighborDisText;
    float currentMapHeight;
    float currentMapWidth;
    float nowX;
    float nowY;
    float lastScaleFactor = 1;
    float lastScaleX;
    float lastScaleY;
    float lastOri = 0;
    double neighborDistance = 0.2;
    private SensorManager sm;
    //需要两个Sensor
    private Sensor aSensor;
    private Sensor mSensor;
    private Sensor laSensor;
    MessageGetter getter = new MessageGetter(this);
    InfoStruct lastInfo = null;
    Location lastRes = null;
    boolean scaled = false;
    public final String TAG = "PositioningMain";
    private Direction lastDir = Direction.NONE;
    private float[] magneticFieldValues;
    private float[] accelerometerValues;

    private final SensorEventListener myListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_positioning_main);
        init();
        Bitmap map = BitmapFactory.decodeResource(getResources(),
                currentMap).copy(Bitmap.Config.ARGB_8888, true);
        currentMapHeight = map.getHeight();
        currentMapWidth = map.getWidth();
        pointRadius = (currentMapWidth * currentMapHeight)/50000f;

        setPosition(currentMapWidth/2f, currentMapHeight/2f, map);
//        lastScaleFactor *= 2;
//        scaled = true;
//        pv.post(new Runnable() {
//            @Override
//            public void run() {
//                pv.setScale(lastScaleFactor);
//            }
//        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    void init() {
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        laSensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        pv = findViewById(R.id.pv);
        pv.getAttacher().setOnScaleChangeListener(new OnScaleChangedListener() {
            @Override
            public void onScaleChange(float scaleFactor, float focusX, float focusY) {
                lastScaleFactor *= scaleFactor;
                lastScaleX = focusX;
                lastScaleY = focusY;
                scaled = true;
            }
        });
        currentMap = R.drawable.map;
        paint.setColor(Color.RED);
        moveLeftBtn = findViewById(R.id.moveLeftBtn);
        moveRightBtn = findViewById(R.id.moveRightBtn);
        moveUpBtn = findViewById(R.id.moveUpBtn);
        moveDownBtn = findViewById(R.id.moveDownBtn);
        changeMapBtn = findViewById(R.id.changeMapBtn);
        getInfoBtn = findViewById(R.id.getInfoBtn);
        intentBtn = findViewById(R.id.intentBtn);
        relatedDataBtn = findViewById(R.id.relatedDataBtn);
        locateBtn = findViewById(R.id.locateBtn);
        tvFps = findViewById(R.id.tv_fps);
        neighborDisText = findViewById(R.id.neighborDisText);
        thresholdText = findViewById(R.id.thresholdText);
        disableBox = findViewById(R.id.disableDirBox);
        thresholdText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int f = Integer.parseInt(s.toString());
                    if (f > 0) {
                        locator.defThreshold = f;
                    } else {
                        locator.defThreshold = 5;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        disableBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                locator.dirEnable = !isChecked;
            }
        });
        neighborDisText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    float f = Float.parseFloat(s.toString());
                    if (f > 0) {
                        locator.neighborDistance = f/100f;
                    } else {
                        locator.neighborDistance = 0.2;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        tvFps.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    int num = Integer.parseInt(s.toString());
                    if(num > 0) {
                        fps = num;
                    } else {
                        Toast.makeText(MyApplication.context, "please input a number bigger than 0", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    fps = 5;
                }
            }
        });
        //todo: 增加上下移动，移动距离随图像大小应有变化
        moveLeftBtn.setOnClickListener((view) -> {
            Bitmap map = BitmapFactory.decodeResource(getResources(),
                    currentMap).copy(Bitmap.Config.ARGB_8888, true);
            setPosition(nowX - currentMapWidth/64, nowY, map);
        });
        moveRightBtn.setOnClickListener((view) -> {
            Bitmap map = BitmapFactory.decodeResource(getResources(),
                    currentMap).copy(Bitmap.Config.ARGB_8888, true);
            setPosition(nowX + currentMapWidth/64, nowY, map);
        });
        moveUpBtn.setOnClickListener((view) -> {
            Bitmap map = BitmapFactory.decodeResource(getResources(),
                    currentMap).copy(Bitmap.Config.ARGB_8888, true);
            setPosition(nowX, nowY - currentMapHeight/64, map);
        });
        moveDownBtn.setOnClickListener((view) -> {
            Bitmap map = BitmapFactory.decodeResource(getResources(),
                    currentMap).copy(Bitmap.Config.ARGB_8888, true);
            setPosition(nowX, nowY + currentMapHeight/64, map);
        });

        //todo: 换地图方式 根据实际情况应有调整
        changeMapBtn.setOnClickListener((view) -> {
            changeMap(currentMap == R.drawable.map ? R.drawable.map2 : R.drawable.map);
        });

        getInfoBtn.setOnClickListener((view) -> {
            if(!MessageGetter.sampling){
                MessageGetter.sampling = true;
                getter.getMessageRepeatedly(fps, new CellInfoCallback() {
                    @Override
                    public void onCellInfoAnalysed(InfoStruct infoStruct) {
                        float x = nowX / currentMapWidth, y = nowY / currentMapHeight;
                        int floor = analyseFloor(currentMap);
                        Long locId = InfoDatabase.getInstance().locationDao().queryLocId(floor, x, y);
                        if (locId == null)
                            locId = InfoDatabase.getInstance().locationDao().insertLocation(new Location(floor, x, y));

                        infoStruct.setLocId(locId);
                        //去重操作
                        if(!infoStruct.equals(lastInfo)) {
                            InfoDatabase.getInstance().infoDao().insert(infoStruct);
                            lastInfo = infoStruct;
                            sampleCount = 0;
                        } else if (sampleCount == SAMPLE_COUNT_THRESHOLD){
                            sampleCount = 0;
                            InfoDatabase.getInstance().infoDao().insert(infoStruct);
                        } else {
                            sampleCount++;
                        }
                        if(MessageGetter.sampling) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    getInfoBtn.setText("" + MessageGetter.samplingTime);
                                }
                            });
                        }

                    }
                });

            } else {
                MessageGetter.sampling = false;
                getInfoBtn.setText("模拟采集数据");
            }

        });

        intentBtn.setOnClickListener((view) -> {
            Intent intent = new Intent(this, ShowInfoActivity.class);
            startActivity(intent);
        });
        relatedDataBtn.setOnClickListener((view) -> {
            Intent intent = new Intent(this, ShowInfoActivity.class);
            intent.putExtra("related", analyseFloor(currentMap));
            startActivity(intent);
        });
        locateBtn.setOnClickListener((view) -> {
            locate(new LocateCallback() {
                @Override
                public void run(InfoStruct position) {
                    Location loc = InfoDatabase.getInstance().locationDao().queryLocation(position.getLocId());
                    if(loc.getX() != 0 || loc.getY() != 0) {
                        setPosition(position);
                        float nowOri = getOrientation();
                        if (Math.abs(nowOri - lastOri) > 30){
                            lastDir = Direction.NONE;
                        } else if (lastRes != null){
                            if(Math.abs(loc.getX() - lastRes.getX()) > Math.abs(loc.getY() - lastRes.getY())){
                                if(loc.getX() >= lastRes.getX()) {
                                    lastDir = Direction.RIGHT;
                                } else {
                                    lastDir = Direction.LEFT;
                                }
                            } else {
                                if(loc.getY() >= lastRes.getY()) {
                                    lastDir = Direction.DOWN;
                                } else {
                                    lastDir = Direction.UP;
                                }
                            }
                        }
                        lastOri = nowOri;
                        lastRes = loc;
                    }
                }
            });
        });
    }

    private void setPosition(InfoStruct position) {
        Location loc = InfoDatabase.getInstance().locationDao().queryLocation(position.getLocId());
        int mapId = getMapId(loc.getFloor());
        Bitmap map = BitmapFactory.decodeResource(getResources(),
                mapId).copy(Bitmap.Config.ARGB_8888, true);
        if(mapId != currentMap) {
            scaled = false;
            lastScaleFactor = 1;
            currentMap = mapId;
            currentMapHeight = map.getHeight();
            currentMapWidth = map.getWidth();
            pointRadius = (currentMapWidth * currentMapHeight)/50000f;
        }
        setPosition(loc.getX() * currentMapWidth, loc.getY() * currentMapHeight, map);
    }

    private void setPosition(Location loc) {
        int mapId = getMapId(loc.getFloor());
        Bitmap map = BitmapFactory.decodeResource(getResources(),
                mapId).copy(Bitmap.Config.ARGB_8888, true);
        if(mapId != currentMap) {
            scaled = false;
            lastScaleFactor = 1;
            currentMap = mapId;
            currentMapHeight = map.getHeight();
            currentMapWidth = map.getWidth();
            pointRadius = (currentMapWidth * currentMapHeight)/50000f;
        }
        setPosition(loc.getX() * currentMapWidth, loc.getY() * currentMapHeight, map);
    }

    private int getMapId(int floor) {
        return floor == 1 ? R.drawable.map : R.drawable.map2;
    }

    private int analyseFloor(int currentMap) {
        //todo: 根据实际情况调整
        return currentMap == R.drawable.map ? 1 : 2;
    }

    void setPosition(float x, float y, Bitmap map) {

        Canvas canvas = new Canvas(map);
        canvas.drawOval(x - pointRadius/2.0f, y - pointRadius/2.0f,x + pointRadius/2.0f, y + pointRadius/2.0f, paint );
        BitmapDrawable bd = new BitmapDrawable(getResources(),map);
        pv.setImageDrawable(bd);
        nowX = x;
        nowY = y;

        if(scaled){
            float ratioX = pv.getWidth()/(map.getWidth()*1f);
            float ratioY = pv.getHeight()/(map.getHeight()*1f);
            if(lastScaleFactor >= pv.getMaximumScale()) lastScaleFactor = pv.getMaximumScale();
            pv.setScale(lastScaleFactor, x*ratioX, y*ratioY, false);
        }
    }

    Bitmap changeMap(int mapId) {
        Bitmap map = BitmapFactory.decodeResource(getResources(),
                mapId).copy(Bitmap.Config.ARGB_8888, true);
        float newHeight = map.getHeight();
        float newWidth = map.getWidth();
        nowY = newHeight/currentMapHeight * nowY;
        nowX = newWidth/currentMapWidth * nowX;
        currentMapHeight = newHeight;
        currentMapWidth = newWidth;
        scaled = false;
        lastScaleFactor = 1;
        pointRadius = (currentMapWidth * currentMapHeight)/50000f;
        currentMap = mapId;
        setPosition(nowX,nowY,map);
        return map;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    void locate(LocateCallback callback) {
        getter.getAllSignalInfo(new CellInfoCallback() {
            @Override
            public void onCellInfoAnalysed(InfoStruct infoStruct) {
//                locator.locateWithRSSI(infoStruct, callback, lastRes, lastDir);
                setPosition(locator.locateByMAP(infoStruct, analyseFloor(currentMap)));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sm.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(myListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(myListener, laSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(myListener);

    }

    private float getOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        // 要经过一次数据格式的转换，转换为度
        values[0] = (float) Math.toDegrees(values[0]);
        return values[0];
    }
}