package com.t_doc41.thomaseyes;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import java.lang.reflect.Field;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class ThomasJoyStick extends JoystickView
{
    private static float jsRange = 200.0f;
    private int mPosX = 0;
    private int mPosY = 0;
    private int mButtonRadius = 0;

    public ThomasJoyStick(Context context)
    {
        super(context);
    }

    public ThomasJoyStick(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public ThomasJoyStick(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    /**
     * Return the relative X coordinate of button center related
     * to top-left virtual corner of the border
     *
     * @return coordinate of X (normalized between 0 and 100)
     */
    @Override
    public int getNormalizedX()
    {
        try
        {
            Field mPosXField = JoystickView.class.getDeclaredField("mPosX");
            if (!mPosXField.isAccessible())
            {
                mPosXField.setAccessible(true);
            }
            mPosX = (int) mPosXField.get(this);

            Field mButtonRadiusField = JoystickView.class.getDeclaredField("mButtonRadius");
            if (!mButtonRadiusField.isAccessible())
            {
                mButtonRadiusField.setAccessible(true);
            }
            mButtonRadius = (int) mButtonRadiusField.get(this);
        }
        catch (Exception e)
        {
            Log.e(ThomasJoyStick.class.getSimpleName().toUpperCase(), "Error getting normalized X", e);
        }

        if (getWidth() == 0)
        {
            return (int) jsRange / 2;
        }
        return Math.round((mPosX - mButtonRadius) * jsRange / (getWidth() - mButtonRadius * 2));
    }

    /**
     * Return the relative Y coordinate of the button center related
     * to top-left virtual corner of the border
     *
     * @return coordinate of Y (normalized between 0 and 100)
     */
    @Override
    public int getNormalizedY()
    {
        try
        {
            Field mPosYField = JoystickView.class.getDeclaredField("mPosY");
            if (!mPosYField.isAccessible())
            {
                mPosYField.setAccessible(true);
            }
            mPosY = (int) mPosYField.get(this);

            Field mButtonRadiusField = JoystickView.class.getDeclaredField("mButtonRadius");
            if (!mButtonRadiusField.isAccessible())
            {
                mButtonRadiusField.setAccessible(true);
            }
            mButtonRadius = (int) mButtonRadiusField.get(this);
        }
        catch (Exception e)
        {
            Log.e(ThomasJoyStick.class.getSimpleName().toUpperCase(), "Error getting normalized Y", e);
        }

        if (getHeight() == 0)
        {
            return (int) jsRange / 2;
        }
        return Math.round((mPosY - mButtonRadius) * jsRange / (getHeight() - mButtonRadius * 2));
    }

    @Override
    public void run()
    {
        try
        {
            Log.d("ThomasJoyStick", "JOYSTICK RUNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");

            final Field ml = JoystickView.class.getDeclaredField("mCallback");
            ml.setAccessible(true);
            OnMoveListener mLister = (OnMoveListener) ml.get(ThomasJoyStick.this);
            Field lI = JoystickView.class.getDeclaredField("mLoopInterval");
            lI.setAccessible(true);

            while (!Thread.interrupted())
            {
                if (mLister != null)
                {
                    mLister.onMove(0, 0);
                }

                try
                {
                    long millis = (long) lI.get(ThomasJoyStick.this);
                    Thread.sleep(millis);
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            Log.e(ThomasJoyStick.class.getSimpleName().toUpperCase(), "Error using reflection on JoystickView", e);
        }
    }

    private void log(final String str)
    {
        Log.d(ThomasJoyStick.class.getSimpleName().toUpperCase(), str);
    }
}

