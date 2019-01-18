package com.emmanuelmess.simpleaccounting.utils

import android.content.res.Resources

val Int.px: Pixels
	get() = Pixels(this.toFloat())

val Int.dp: DensityPixels
	get() = DensityPixels(this.toFloat())

/*inline*/ class Pixels(val value: Float) {
	fun toInt() = value.toInt()
	fun toDp() = DensityPixels(value / Resources.getSystem().displayMetrics.density)
}

/*inline*/ class DensityPixels(val value: Float) {
	fun toInt() = value.toInt()
	fun toPx(): Pixels = Pixels(value * Resources.getSystem().displayMetrics.density)
}