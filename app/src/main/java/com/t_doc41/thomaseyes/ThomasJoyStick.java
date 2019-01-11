package com.t_doc41.thomaseyes;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import java.lang.reflect.Field;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class ThomasJoyStick extends JoystickView
{
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
}

