package com.emmanuelmess.simpleaccounting.utils

import android.content.res.Resources

val Int.millis: Milliseconds
	get() = Milliseconds(this.toLong())

val Int.seconds: Seconds
	get() = Seconds(this.toLong())

/*inline*/ class Seconds(val value: Long) {
	operator fun plus(other: Seconds) = Seconds(value + other.value)

	fun toMillis() = Milliseconds(value * 1000)
}

/*inline*/ class Milliseconds(val value: Long) {
	fun toLong() = value
}