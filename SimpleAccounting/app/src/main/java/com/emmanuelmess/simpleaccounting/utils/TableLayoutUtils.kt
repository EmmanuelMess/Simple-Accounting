package com.emmanuelmess.simpleaccounting.utils

import android.view.View
import android.view.ViewGroup

inline fun <reified T: View> ViewGroup.get(i: Int): T? {
	val child: View = this.getChildAt(i) ?: return null

	return child as T
}