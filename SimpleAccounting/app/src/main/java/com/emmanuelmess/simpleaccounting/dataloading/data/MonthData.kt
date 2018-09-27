package com.emmanuelmess.simpleaccounting.dataloading.data

data class MonthData(
	val month: Int,
	val year: Int,
	val currency: String,
	val prevBalance: Double?,
	val dbRows: Array<Array<String>>,
	val rowToDBConversion: ArrayList<Int>
)