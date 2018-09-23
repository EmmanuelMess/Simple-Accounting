package com.emmanuelmess.simpleaccounting.activities.views

import android.content.Context
import android.support.annotation.IntRange
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView

import org.acra.ACRA

/**
 * @author JMPergar (https://gist.github.com/JMPergar)
 */

open class ScrollViewWithMaxHeight @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.scrollViewStyle
) : ScrollView(context, attrs, defStyle) {

    companion object {
        var WITHOUT_MAX_HEIGHT_VALUE = -1

        private var maxHeight = WITHOUT_MAX_HEIGHT_VALUE

        private const val MODE_SHIFT = 30
        private const val MAX_HEIGHT_UPPERBOUND = ((1 shl MODE_SHIFT) - 1).toLong()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        try {
            var heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
            if (maxHeight != WITHOUT_MAX_HEIGHT_VALUE && heightSize > maxHeight)
                heightSize = maxHeight

            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(heightSize, View.MeasureSpec.AT_MOST)
            layoutParams.height = heightSize
        } catch (e: Exception) {
            ACRA.getErrorReporter().handleException(e)
        } finally {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun setMaxHeight(@IntRange(from = 0, to = MAX_HEIGHT_UPPERBOUND) maxHeight: Int) {
        if (maxHeight < 0)
            throw IllegalArgumentException("View height must not be < 0")

        ScrollViewWithMaxHeight.maxHeight = maxHeight
    }

}