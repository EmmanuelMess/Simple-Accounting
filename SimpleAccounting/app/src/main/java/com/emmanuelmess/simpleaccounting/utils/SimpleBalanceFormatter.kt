package com.emmanuelmess.simpleaccounting.utils

import com.emmanuelmess.simpleaccounting.activities.views.LedgerView

import java.math.BigDecimal

class SimpleBalanceFormatter : LedgerView.BalanceFormatter {
    override fun format(balance: BigDecimal) = "$ " + balance.toString()
}
