package com.londonappbrewery.magiceightball;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Size;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Random;

public class Magic8BallActivity extends AppCompatActivity implements SensorEventListener {
    /*
     * The gForce that is necessary to register as shake.
	 * Must be greater than 1G (one earth gravity unit).
	 * You can install "G-Force", by Blake La Pierre
	 * from the Google Play Store and run it to see how
	 *  many G's it takes to register a shake
	 */
    private static final float SHAKE_THRESHOLD_GRAVITY = 1.3f;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    protected final int[] ballsArray = {
            R.drawable.ball1,
            R.drawable.ball2,
            R.drawable.ball3,
            R.drawable.ball4,
            R.drawable.ball5
    };
    protected Button mButtonAsk;
    protected ImageView mImageViewBall;
    protected OnShakeListener mListener;
    private SensorManager mSensorManager;
    private long mShakeTimestamp;
    private Sensor mAccelerometer;

    public Magic8BallActivity(Sensor accelerometer) {
        mAccelerometer = accelerometer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageViewBall = findViewById(R.id.imageViewBall);
        mButtonAsk = findViewById(R.id.buttonAsk);
        mButtonAsk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBallView();
            }
        });

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            setOnShakeListener(new OnShakeListener() {
                @Override
                public void onShake() {
                    updateBallView();
                }
            });
        } else {
            Log.i("Magic8Ball", "Could not find System Service for SENSOR_SERVICE, " +
                    "the device might not support this sensor.");
        }
    }

    private void updateBallView() {
        Random randomGen = new Random();
        mImageViewBall.setImageResource(ballsArray[randomGen.nextInt(ballsArray.length)]);
    }

    /**
     * Used for testing in @Magic8BallTest
     *
     * @param randomNum
     */
    protected void updateBallView(@Size(min = 0, max = 5) int randomNum) {
        int ballImage = ballsArray[randomNum];
        mImageViewBall.setImageResource(ballImage);
        mImageViewBall.setTag(ballImage);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener != null) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement.
            double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }
                mShakeTimestamp = now;
                mListener.onShake();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignore
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.mListener = listener;
    }

    public interface OnShakeListener {
        void onShake();
    }
}
