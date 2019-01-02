package com.emmanuelmess.simpleaccounting.activities.views

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

import com.emmanuelmess.simpleaccounting.R

import java.math.BigDecimal

import com.emmanuelmess.simpleaccounting.utils.get

class LedgerView(context: Context, attrs: AttributeSet) : TableLayout(context, attrs) {

	private val inflater: LayoutInflater

	private lateinit var listener: LedgeCallbacks

	private var invertCreditAndDebit = false

	private lateinit var formatter: BalanceFormatter

	val lastRow: TableRow?
		get() {
			val rowViewIndex = childCount - 1
			return get(rowViewIndex)
		}

	init {
		addView(View.inflate(getContext(), R.layout.view_ledger, null))

		inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
	}

	fun setFormatter(f: BalanceFormatter) {
		formatter = f
	}

	fun setListener(l: LedgeCallbacks) {
		listener = l
	}

	fun setInvertCreditAndDebit(invert: Boolean) {
		if (invert != invertCreditAndDebit) {
			val tempId = 0

			findViewById<View>(R.id.credit).id = tempId
			findViewById<View>(R.id.debit).id = R.id.credit
			findViewById<View>(tempId).id = R.id.debit

			(findViewById<View>(R.id.credit) as TextView).setText(R.string.credit)
			(findViewById<View>(R.id.debit) as TextView).setText(R.string.debit)

			invertCreditAndDebit = invert
		}
	}

	/**
	 * Creates and inflates a new row.
	 * Restores editable row to view.
	 */
	fun inflateEmptyRow(editOnLongClick: Boolean = true): View {
		val row = inflateRow()

		if(editOnLongClick) {
			row.setOnLongClickListener { _ ->
				rowViewToEditable(row)
				true
			}
		} else {
			row.removeSelectableItemBackground()
		}

		return row
	}

	fun rowViewToEditable(row: LedgerRow) {
		listener.onLongPressItem(row)
	}

	fun clear() {
		for (i in childCount - 1 downTo 1) {//DO NOT remove first line, the column titles
			removeViewAt(i)
		}
	}

	private fun inflateRow(): LedgerRow {
		inflater.inflate(R.layout.row_main, this)
		val row = get<LedgerRow>(childCount -1)!!
		row.formatter = formatter

		if (invertCreditAndDebit) {
			row.invertDebitCredit()
		}

		return row
	}

	interface LedgeCallbacks {
		fun onLongPressItem(pressedRow: LedgerRow)
	}

	interface BalanceFormatter: Parcelable {
		fun format(balance: BigDecimal): String
	}

}
