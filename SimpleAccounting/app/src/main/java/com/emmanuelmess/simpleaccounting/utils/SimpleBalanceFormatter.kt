package com.emmanuelmess.simpleaccounting.utils

import com.emmanuelmess.simpleaccounting.activities.views.LedgerView
import kotlinx.android.parcel.Parcelize

import java.math.BigDecimal

@Parcelize
object SimpleBalanceFormatter : LedgerView.BalanceFormatter {
    override fun format(balance: BigDecimal) = "$ " + balance.toString()
}
