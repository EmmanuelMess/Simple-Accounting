package com.emmanuelmess.simpleaccounting.dataloading.data

data class MonthData(val session: Session, val prevMonthBalance: Double?,
                     val dbRows: Array<Array<String>>, val rowToDBConversion: ArrayList<Int>)