package com.t_doc41.thomaseyes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class BlockableScrollView extends ScrollView
{
    // true if we can scroll the ScrollView
    // false if we cannot scroll
    private boolean scrollable = true;

    public BlockableScrollView(final Context context)
    {
        super(context);
    }

    public BlockableScrollView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public BlockableScrollView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public BlockableScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                // if we can scroll pass the event to the superclass
                if (scrollable)
                {
                    return super.onTouchEvent(ev);
                }
                // only continue to handle the touch event if scrolling enabled
                return scrollable; // scrollable is always false at this point
            default:
                return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        if (!scrollable)
        {
            return false;
        }
        else
        {
            return super.onInterceptTouchEvent(ev);
        }
    }

    public void setScrollingEnabled(boolean scrollable)
    {
        this.scrollable = scrollable;
    }

    public boolean isScrollable()
    {
        return scrollable;
    }
}
