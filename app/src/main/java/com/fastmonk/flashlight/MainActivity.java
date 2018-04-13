package com.fastmonk.flashlight;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.icu.text.LocaleDisplayNames;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.noob.noobcameraflash.managers.NoobCameraManager;
import com.warkiz.widget.IndicatorSeekBar;


public class MainActivity extends AppCompatActivity {

    private boolean isFlashOn;
    private boolean hasFlash;
    boolean toggle;
    int lastValue =0;
    int currentT= 6;

    IndicatorSeekBar rangeSeekBar;

    private AdView mAdView;

    private static final String TAG = "CompassActivity";

    private Compass compass;

    final  int REQUEST_CAMERA_PERMISSION = 1;
    ImageView flashSwitch;
    CustomTextView flashValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CustomTextView unavailable = (CustomTextView)findViewById(R.id.notAvailableText);
        flashValue = (CustomTextView)findViewById(R.id.flashValue);

        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        try {
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
                compass = new Compass(this);
                compass.arrowView = (ImageView) findViewById(R.id.compass);
                compass.direction = (CustomTextView)findViewById(R.id.notAvailableText);
            } else {
                Log.d("here", "not");
                unavailable.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            Log.d("here", "not");
            unavailable.setVisibility(View.VISIBLE);
        }
        MobileAds.initialize(this, "ca-app-pub-2071412357822032~9015785139");

        mAdView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        isFlashOn = false;

        flashSwitch = (ImageView)findViewById(R.id.flashButton);

        rangeSeekBar = (IndicatorSeekBar) findViewById(R.id.rangeBar);
        rangeSeekBar.setProgress(0);
        rangeSeekBar.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                Log.d("pCHan",seekBar.getProgress()+"");
            }

            @Override
            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {
                Log.d("cCHan",seekBar.getProgress()+"");
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                if(lastValue!=seekBar.getProgress()) {
                    lastValue = seekBar.getProgress();
                    blink(lastValue);
                }
            }
        });

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            myMethod();
        }
    }

    void myMethod(){
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");
            alert.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    flashSwitch.setVisibility(View.GONE);
                    flashValue.setVisibility(View.GONE);
                    rangeSeekBar.setVisibility(View.GONE);
                }
            });
            alert.show();
            return;
        }
        try {
            NoobCameraManager.getInstance().init(getApplicationContext());
        }catch (Exception e){
            e.printStackTrace();
        }

        flashSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("version",android.os.Build.VERSION.SDK_INT+"");

                if (isFlashOn) {
                    // turn off flash
                    turnOffFlash();
                } else {
                    // turn on flash
                    turnOnFlash();
                    blink(lastValue);
                }

                if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP){

                } else {
                }
            }
        });
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myMethod();
                } else {
                    Toast.makeText(this, "Cannot turn on flash without getting permission to access Camera", Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish();
                }
            }
            break;
        }
    }
    private void turnOnFlash() {

        if (!isFlashOn) {
            if (NoobCameraManager.getInstance()==null) {
                return;
            }
            // play sound

            try {
                NoobCameraManager.getInstance().turnOnFlash();
                isFlashOn = true;
                flashSwitch.setImageDrawable(getResources().getDrawable(R.drawable.pressed_button));
                flashValue.setText("ON");
            }catch (Exception e){
                e.printStackTrace();
            }

            // changing button/switch image
        }
    }
    private void FlashOn() {
        // play sound
        try{
            NoobCameraManager.getInstance().turnOnFlash();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void turnOffFlash() {
        if (isFlashOn) {
            if (NoobCameraManager.getInstance()==null) {
                return;
            }

            try {
                NoobCameraManager.getInstance().turnOffFlash();
                isFlashOn = false;
                flashSwitch.setImageDrawable(getResources().getDrawable(R.drawable.unpressed_button));
                flashValue.setText("OFF");
            }catch (Exception e){
                e.printStackTrace();
            }

            // changing button/switch image
        }
    }
    private void FlashOff() {
        try{
            NoobCameraManager.getInstance().turnOffFlash();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    void blink(int val){
        if(isFlashOn) {
            if (val != 0) {
                final Handler handler = new Handler();
                toggle = true;
                final int t = 6 - val;
                currentT = t;

                final Runnable r = new Runnable() {
                    public void run() {
                        if (isFlashOn && currentT == t) {
                            if (toggle) {
                                toggle = false;
                                FlashOff();
                            } else {
                                toggle = true;
                                FlashOn();
                            }
                            handler.postDelayed(this, t * 100);
                        }
                    }
                };
                handler.postDelayed(r, t * 100);

            }else{
                currentT = 6;
                // play sound
                try {
                    Log.d("reached here","true");
                    NoobCameraManager.getInstance().turnOnFlash();
                    isFlashOn = true;
                    flashSwitch.setImageDrawable(getResources().getDrawable(R.drawable.pressed_button));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }else{
            turnOffFlash();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(compass!=null) {
            Log.d(TAG, "start compass");
            compass.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(compass!=null) {
            compass.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(compass!=null) {
            compass.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(compass!=null) {
            Log.d(TAG, "stop compass");
            compass.stop();
        }
    }
}
