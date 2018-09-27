package com.emmanuelmess.simpleaccounting.data

data class MonthData(
	val session: Session,
	val prevBalance: Double?,
	val dbRows: Array<Array<String>>,
	val rowToDBConversion: ArrayList<Int>
)