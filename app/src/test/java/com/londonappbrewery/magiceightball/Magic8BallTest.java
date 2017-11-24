package com.londonappbrewery.magiceightball;

import android.test.ActivityInstrumentationTestCase2;

import java.util.Random;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class Magic8BallTest extends ActivityInstrumentationTestCase2<Magic8BallActivity> {
    private Magic8BallActivity mActivity;

    public Magic8BallTest() {
        super(Magic8BallActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }

    public void testPreconditions() {
        assertNotNull(String.format("%s is null", Magic8BallActivity.class.getSimpleName()),
                mActivity);
        assertNotNull("Button Ask is null", mActivity.mButtonAsk);
        assertNotNull("Image with Ball is null", mActivity.mImageViewBall);
        assertNotNull("SeekBar for Elevation is null", mActivity.mImageViewBall);
    }

    /**
     * Test if the ballImage is updated when a shake occurs. Unfortunately, we can't recreate a
     * Shake Event, so we can only call the listeners onShake() method.
     */
    public void testShakeListener() {
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mActivity.mImageViewBall.setTag(-1);
                if (mActivity.mListener != null) {
                    mActivity.mListener.onShake();
                    assertNotSame(-1, mActivity.mImageViewBall.getTag());
                }
            }
        });
    }

    /**
     * Test if the button updates the ballImage.
     */
    public void testAskButton() {
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Random randomGen = new Random();
                int number = randomGen.nextInt(mActivity.ballsArray.length);
                mActivity.updateBallView(number);
                assertEquals(mActivity.ballsArray[number], mActivity.mImageViewBall.getTag());
            }
        });
    }
}