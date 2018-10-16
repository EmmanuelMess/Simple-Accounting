package com.emmanuelmess.simpleaccounting.activities.preferences

import android.content.Context
import android.content.res.TypedArray
import androidx.preference.DialogPreference
import android.text.TextUtils
import android.util.AttributeSet

import com.emmanuelmess.simpleaccounting.R
import com.emmanuelmess.simpleaccounting.utils.TinyDB

import java.util.ArrayList

class CurrencyPicker(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {

    companion object {
        private val DEFAULT_VALUE = ArrayList<String>()
        const val KEY = "currency_picker"
        const val DFLT = "DFLT"
    }

    private var summary: String? = null
    private val tinyDB = TinyDB(context)

    init {
        dialogLayoutResource = R.layout.dialog_currencypicker
        setPositiveButtonText(android.R.string.ok)
        setNegativeButtonText(android.R.string.cancel)

        dialogIcon = null
        setTitle(R.string.costumize_currencies)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun getSummary(): CharSequence {
        val summary = this.summary ?: createSummary(getPersistedStringList(DEFAULT_VALUE))
        this.summary = summary
        return summary
    }

    fun setCurrencyList(list: ArrayList<String>) {
        persistStringList(list)
        notifyChanged()
    }

    override fun notifyChanged() {
        summary = createSummary(getPersistedStringList(DEFAULT_VALUE))
        super.notifyChanged()
    }

    fun getPersistedStringList(defaultValue: ArrayList<String>): ArrayList<String> {
        return tinyDB.getListString(KEY)
    }

    private fun persistStringList(value: ArrayList<String>): Boolean {
        if (shouldPersist()) {
            if (value === getPersistedStringList(value)) {
                // It's already there, so the same as persisting
                return true
            }

            tinyDB.putListString(KEY, value)
            return true
        }
        return false
    }

    private fun createSummary(currentValue: ArrayList<String>): String {
        return if (currentValue.size != 0) {
                val myStringList = currentValue.toTypedArray()
                TextUtils.join(", ", myStringList)
            } else {
                context.getString(R.string.with_no_items_deactivated)
            }
    }
}
