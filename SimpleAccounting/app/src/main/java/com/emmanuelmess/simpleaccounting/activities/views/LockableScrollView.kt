package com.emmanuelmess.simpleaccounting.activities.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * @author Joseph Earl
 */
class LockableScrollView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.scrollViewStyle
) : ScrollViewWithMaxHeight(context, attrs, defStyle) {

    // true if we can scroll (not locked)
    // false if we cannot scroll (locked)
    private var isScrollable = true

    fun setScrollingEnabled(enabled: Boolean) {
        isScrollable = enabled
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean =
	    when (ev.action) {
		    MotionEvent.ACTION_DOWN -> {
			    // if we can scroll pass the event to the superclass
			    if (isScrollable) super.onTouchEvent(ev) else isScrollable
			    // only continue to handle the touch event if scrolling enabled
			    // mScrollable is always false at this point
		    }
		    else -> super.onTouchEvent(ev)
	    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        return if (!isScrollable)
            false
        else
            super.onInterceptTouchEvent(ev)
    }

}
