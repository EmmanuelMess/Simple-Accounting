package com.emmanuelmess.simpleaccounting.dataloading.data

/**
 * [month], [year]
 * Pointer to month being viewed
 * CAN BE -1, -2 OR >=0.
 * -1: no value
 * -2: older that update 1.2
 * >=0: 'normal' (month or year) value
 *
 * [currency]
 * same as [MainActivity.currencyName], except when it is the default in that case it is ""
 */
data class Session(val month: Int, val year: Int, val currency: String)