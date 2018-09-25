package com.emmanuelmess.simpleaccounting.dataloading.data

import java.math.BigDecimal

data class RowData(val date: String?, val reference: String?, val credit: String?, val debit: String?,
                   val balance: BigDecimal)