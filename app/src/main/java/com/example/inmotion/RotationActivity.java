package com.example.inmotion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RotationActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;

    private float xPos, xAccel, xVel = 0.0f;
    private float yPos, yAccel, yVel = 0.0f;
    private float xMax, yMax;
    private Bitmap ball;

    private float U[] = new float[9];
    private float I[] = new float[9];

    private float[] mGravity;
    private float[] mGeomagnetic;

    private TextView viewPitch;
    private TextView viewRoll;

    private float[] startOrientation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_rotation);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener((SensorEventListener) this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener((SensorEventListener) this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);


        BallView ball = new BallView(this);
        setContentView(ball);
        ball.setOnTouchListener(handleTouch);

        Point size = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(size);
        xMax = (float) size.x - 100;
        yMax = (float) size.y - 200;

        xPos = xMax / 2;
        yPos = yMax / 2;

        //viewPitch = findViewById(R.id.ViewPitch);
        //viewRoll = findViewById(R.id.ViewRoll);
    }

        @Override
    public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;

            if (mGravity != null && mGeomagnetic != null) {

                boolean success = SensorManager.getRotationMatrix(U, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(U, orientation);
                    if(startOrientation == null){
                        startOrientation = new float[orientation.length];
                        //Copy orientation array to start orientation
                        System.arraycopy(orientation,0, startOrientation, 0, orientation.length );
                    }
                    //float azimuth = orientation[0]; // orientation contains: azimuth, pitch and roll

                    //convert from radian to degree
                    float pitch = (float) Math.toDegrees( orientation[1] - startOrientation[1]);
                    float roll = (float) Math.toDegrees(  orientation[2] - startOrientation[2]);
                    xPos = (float) ((xMax / 2) + (roll*xMax)/180);
                    yPos = (float) ((yMax / 2) - (pitch*yMax)/180);

                    if (xPos > xMax) {
                        xPos = xMax;
                    }
                    if (yPos > yMax) {
                        yPos = yMax;
                    }
                }
            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private View.OnTouchListener handleTouch = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() ==  MotionEvent.ACTION_UP){
                   startOrientation = null;
            }

            return true;
        }
    };


    private class BallView extends View {

        public BallView(Context context) {
            super(context);
            Bitmap ballSrc = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
            final int dstWidth = 100;
            final int dstHeight = 100;
            ball = Bitmap.createScaledBitmap(ballSrc, dstWidth, dstHeight, true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(ball, xPos, yPos, null);
            invalidate();
        }
    }

}
