package com.emmanuelmess.simpleaccounting.utils

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorRes
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView

import com.emmanuelmess.simpleaccounting.activities.MainActivity
import com.emmanuelmess.simpleaccounting.R
import com.emmanuelmess.simpleaccounting.db.TableGeneral

import java.lang.reflect.Field
import java.math.BigDecimal
import java.util.Objects

import com.emmanuelmess.simpleaccounting.activities.MainActivity.MONTH_STRINGS

object Utils {
	fun getTitle(context: Context, month: Int, year: Int, currency: String,
	             updateDate: IntArray): String = with(context) {
		var title: String = if (year != TableGeneral.OLDER_THAN_UPDATE) {
				"${getString(MONTH_STRINGS[month])}-$year"
			} else {
				(getString(R.string.before_update_1_2)
					+ " ${getString(MainActivity.MONTH_STRINGS[updateDate[0]]).toLowerCase()}"
					+ "-${updateDate[1]}")
			}

		if (currency != "") {
			title += " [$currency]"
		}

		return title
	}

	fun equal(o1: Any, o2: Any): Boolean {
		return o1 == o2
	}

	fun parseString(s: String): BigDecimal =
		if (s.isEmpty() || equal(s, "."))
			BigDecimal("0")
		else
			BigDecimal(s)

	fun getBackgroundColor(drawable: Drawable, @ColorRes defaultColor: Int): Int {
		if (Build.VERSION.SDK_INT >= 21)
			return defaultColor//Other method will fail

		if (drawable is ColorDrawable)
			return drawable.color

		try {
			var field = drawable.javaClass.getDeclaredField("mState")
			field.isAccessible = true
			val obj = field.get(drawable)
			field = obj.javaClass.getDeclaredField("mUseColor")
			field.isAccessible = true
			return field.getInt(obj)
		} catch (e: Exception) {
			return defaultColor
		}

	}
}
