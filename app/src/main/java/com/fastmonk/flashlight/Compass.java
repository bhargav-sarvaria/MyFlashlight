package com.fastmonk.flashlight;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Bhargav on 13-04-2018.
 */

public class Compass implements SensorEventListener {
    private static final String TAG = "Compass";

    private SensorManager sensorManager;
    private Sensor gsensor;
    private Sensor msensor;
    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float azimuth = 0f;
    private float currectAzimuth = 0;

    // compass arrow to rotate
    public ImageView arrowView = null;
    public CustomTextView direction = null;

    public Compass(Context context) {
        try {
            sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }catch (Exception e){
        }
    }

    public void start() {
        try {
            sensorManager.registerListener(this, gsensor,
                    SensorManager.SENSOR_DELAY_GAME);
            sensorManager.registerListener(this, msensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }catch (Exception e){
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    private void adjustArrow() {
        try {
            if (arrowView == null) {
                Log.i(TAG, "arrow view is not set");
                return;
            }

            Animation an = new RotateAnimation(-currectAzimuth, -azimuth,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                    0.5f);
            currectAzimuth = azimuth;

            an.setDuration(500);
            an.setRepeatCount(0);
            an.setFillAfter(true);

            arrowView.startAnimation(an);

            int i = (int)azimuth/45;
            switch (i){
                case 0:{
                    direction.setText("N "+(int)azimuth+"°");
                }break;
                case 1:{
                    direction.setText("NE "+((int)azimuth-45)+"°");
                }break;
                case 2:{
                    direction.setText("E "+((int)azimuth-90)+"°");
                }break;
                case 3:{
                    direction.setText("SE "+((int)azimuth-135)+"°");
                }break;
                case 4:{
                    direction.setText("S "+((int)azimuth-180)+"°");
                }break;
                case 5:{
                    direction.setText("SW "+((int)azimuth-225)+"°");
                }break;
                case 6:{
                    direction.setText("W "+((int)azimuth-270)+"°");
                }break;
                case 7:{
                    direction.setText("NW "+((int)azimuth-315)+"°");
                }break;
            }

        }catch (Exception e){
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            final float alpha = 0.97f;

            synchronized (this) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                    mGravity[0] = alpha * mGravity[0] + (1 - alpha)
                            * event.values[0];
                    mGravity[1] = alpha * mGravity[1] + (1 - alpha)
                            * event.values[1];
                    mGravity[2] = alpha * mGravity[2] + (1 - alpha)
                            * event.values[2];

                    // mGravity = event.values;

                    // Log.e(TAG, Float.toString(mGravity[0]));
                }

                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    // mGeomagnetic = event.values;

                    mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha)
                            * event.values[0];
                    mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha)
                            * event.values[1];
                    mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha)
                            * event.values[2];
                    // Log.e(TAG, Float.toString(event.values[0]));

                }

                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                        mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    // Log.d(TAG, "azimuth (rad): " + azimuth);
                    azimuth = (float) Math.toDegrees(orientation[0]); // orientation
                    azimuth = (azimuth + 360) % 360;
                    // Log.d(TAG, "azimuth (deg): " + azimuth);
                    adjustArrow();
                }
            }
        }catch (Exception e){
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
