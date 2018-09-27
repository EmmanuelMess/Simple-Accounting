package com.emmanuelmess.simpleaccounting.dataloading.data

data class MonthData(
	val prevBalance: Double?,
	val dbRows: Array<Array<String>>,
	val rowToDBConversion: ArrayList<Int>
)