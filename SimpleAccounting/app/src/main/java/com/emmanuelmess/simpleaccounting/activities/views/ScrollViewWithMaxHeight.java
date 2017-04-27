package com.emmanuelmess.simpleaccounting.activities.views;

import android.content.Context;
import android.support.annotation.IntRange;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import org.acra.ACRA;

import java.util.logging.LogManager;
/**
 * @author JMPergar (https://gist.github.com/JMPergar)
 *
 */

public class ScrollViewWithMaxHeight extends ScrollView {

	public static int WITHOUT_MAX_HEIGHT_VALUE = -1;

	private static int maxHeight = WITHOUT_MAX_HEIGHT_VALUE;

	public ScrollViewWithMaxHeight(Context context) {
		super(context);
	}

	public ScrollViewWithMaxHeight(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollViewWithMaxHeight(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		try {
			int heightSize = MeasureSpec.getSize(heightMeasureSpec);
			if (maxHeight != WITHOUT_MAX_HEIGHT_VALUE && heightSize > maxHeight)
				heightSize = maxHeight;

			heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST);
			getLayoutParams().height = heightSize;
		} catch (Exception e) {
			ACRA.getErrorReporter().handleException(e);
		} finally {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	private static final int MODE_SHIFT = 30;

	public void setMaxHeight(@IntRange(from = 0, to = (1 << MODE_SHIFT) - 1) int maxHeight) {
		if(maxHeight < 0)
			throw new IllegalArgumentException("View height must not be < 0");

		ScrollViewWithMaxHeight.maxHeight = maxHeight;
	}

}