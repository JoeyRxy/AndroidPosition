package com.sjtu.demoapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.sjtu.demoapp.database.InfoDatabase;

import java.util.Timer;
import java.util.concurrent.Callable;

import bolts.Task;

public class PositioningMainActivity extends AppCompatActivity {
    PhotoView pv;
    int currentMap;
    int fps = 5;
    Paint paint = new Paint();
    float pointRadius;
    Button moveLeftBtn;
    Button moveRightBtn;
    Button changeMapBtn;
    Button getInfoBtn;
    Button intentBtn;
    Button relatedDataBtn;
    Button locateBtn;
    Locator locator = new Locator();
    EditText tvFps;
    float currentMapHeight;
    float currentMapWidth;
    float nowX;
    float nowY;
    float lastScaleFactor = 1;
    float lastScaleX;
    float lastScaleY;
    MessageGetter getter = new MessageGetter(this);
    boolean scaled = false;
    public final String TAG = "PositioningMain";

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
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    void init() {
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
        changeMapBtn = findViewById(R.id.changeMapBtn);
        getInfoBtn = findViewById(R.id.getInfoBtn);
        intentBtn = findViewById(R.id.intentBtn);
        relatedDataBtn = findViewById(R.id.relatedDataBtn);
        locateBtn = findViewById(R.id.locateBtn);
        tvFps = findViewById(R.id.tv_fps);
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
            setPosition(nowX + currentMapHeight/64, nowY, map);
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
                        infoStruct.x = nowX/currentMapWidth;
                        infoStruct.y = nowY/currentMapHeight;
                        infoStruct.floor = analyseFloor(currentMap);
//                  MessageHolder.getINSTANCE().Infos.add(infoStruct);
                        InfoDatabase.getInstance().infoDao().insert(infoStruct);
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
                    if(position.x != 0 || position.y != 0) {
                        setPosition(position);
                    }
                }
            });
        });
    }

    private void setPosition(InfoStruct position) {
        int mapId = getMapId(position.floor);
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
        setPosition(position.x * currentMapWidth, position.y * currentMapHeight, map);
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
                locator.locateWithRSSI(infoStruct, callback);
            }
        });
    }
}