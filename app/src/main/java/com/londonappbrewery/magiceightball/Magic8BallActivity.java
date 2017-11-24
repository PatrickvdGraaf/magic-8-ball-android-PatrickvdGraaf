package com.londonappbrewery.magiceightball;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Size;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Random;

/**
 * This Activity shows the basic understanding of creating an Activity, implementing an Interface
 * and updating an Image when the user preforms certain actions.
 */
public class Magic8BallActivity extends AppCompatActivity implements SensorEventListener {
    /*
     * The gForce that is necessary to register as shake.
	 * Must be greater than 1G (one earth gravity unit).
	 */
    private static final float SHAKE_THRESHOLD_GRAVITY = 1.3f;
    private static final int SHAKE_SLOP_TIME_MS = 500;

    private static final int ANIM_PREPARE_DURATION = 70;
    private static final int ANIM_SHAKE_DURATION = 140;
    private static final int ANIM_RESET_DURATION = 120;

    @VisibleForTesting
    protected final int[] ballsArray = {
            R.drawable.ball1,
            R.drawable.ball2,
            R.drawable.ball3,
            R.drawable.ball4,
            R.drawable.ball5
    };
    @VisibleForTesting
    protected Button mButtonAsk;
    @VisibleForTesting
    protected ImageView mImageViewBall;
    @VisibleForTesting
    protected OnShakeListener mListener;
    private SensorManager mSensorManager;
    private long mShakeTimestamp;
    private Sensor mAccelerometer;

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
                    "the device might not support sensors.");
        }
    }

    /**
     * Update the mImageViewBall with a randomly chosen image.
     */
    private void updateBallView() {
        setBallImage(ballsArray[new Random().nextInt(ballsArray.length)]);
    }

    /**
     * Used for testing in Magic8BallTest. By defining the random number in the Test, we can
     * check if the right image from the ballsArray is set into the mImageViewBall.
     * <p>
     * Since the Test can't call mImageViewBall.getImageResource, we put the id of the Drawable into
     * the mImageViewBall Tag.
     *
     * @param randomNum A randomly generated number from the test method. Used to set the Ball Image
     *                  from outside the Magic8BallActivity.
     */
    @VisibleForTesting
    protected void updateBallView(@Size(min = 0, max = 5) int randomNum) {
        setBallImage(ballsArray[randomNum]);
    }

    /**
     * This method handles the animations while changing the image.
     * Since the shaking will happen between -10% and -10%, I first start a prepare animation that
     * moves the view to -10% and pun it there with setFillAfter(true)
     * <p>
     * Using AnimationListeners, the second, 'real' shaking animation starts right after the first
     * one ends.
     *
     * @param image Image Drawable from the ballsArray
     */
    private void setBallImage(@DrawableRes final int image) {
        Animation prepareAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.prepare);
        prepareAnimation.setFillAfter(true);
        prepareAnimation.setDuration(ANIM_PREPARE_DURATION);
        final Animation shakeAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.shake);
        shakeAnimation.setFillAfter(true);
        shakeAnimation.setDuration(ANIM_SHAKE_DURATION);
        shakeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mImageViewBall.setImageResource(image);
                mImageViewBall.setTag(image);

                Animation resetAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.reset);
                resetAnimation.setFillAfter(true);
                resetAnimation.setDuration(ANIM_RESET_DURATION);
                mImageViewBall.startAnimation(resetAnimation);
            }

            @Override
            public void onAnimationStart(Animation animation) {
                //ignore
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //ignore
            }
        });
        prepareAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mImageViewBall.startAnimation(shakeAnimation);
            }

            @Override
            public void onAnimationStart(Animation animation) {
                //ignore
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //ignore
            }
        });
        mImageViewBall.startAnimation(prepareAnimation);
    }

    /**
     * If a listener exists (if the device has sensor services) we use the coordinates in the
     * sensorEvent to determine whether a shake has occurred and if it was severe enough to
     * trigger the updateBallView method.
     *
     * @param event SensorEvent indicating if and how much a device has moved/tilted/turned.
     */
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
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @VisibleForTesting
    protected int getAnimationDuration() {
        return ANIM_PREPARE_DURATION + ANIM_SHAKE_DURATION + ANIM_RESET_DURATION;
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
