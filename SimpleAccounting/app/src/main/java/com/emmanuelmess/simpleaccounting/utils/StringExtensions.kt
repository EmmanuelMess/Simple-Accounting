package com.emmanuelmess.simpleaccounting.utils

import java.lang.NumberFormatException
import java.math.BigDecimal

fun String.toBigDecimalSafe(): BigDecimal {
	if(isEmpty()) return BigDecimal.ZERO
	if(startsWith(".")) return "0$this".toBigDecimalSafe()

	return try {
		toBigDecimal()
	} catch (e: NumberFormatException) {
		BigDecimal.ZERO
	}
}