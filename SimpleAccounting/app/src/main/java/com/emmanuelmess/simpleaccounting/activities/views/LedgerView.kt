package com.emmanuelmess.simpleaccounting.activities.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

import com.emmanuelmess.simpleaccounting.R

import java.math.BigDecimal

import com.emmanuelmess.simpleaccounting.activities.MainActivity.EDIT_IDS
import com.emmanuelmess.simpleaccounting.activities.MainActivity.TEXT_IDS
import com.emmanuelmess.simpleaccounting.activities.controllers.LedgerRowEditingController
import com.emmanuelmess.simpleaccounting.utils.get

class LedgerView(context: Context, attrs: AttributeSet) : TableLayout(context, attrs) {

	private val inflater: LayoutInflater
	private lateinit var listener: LedgeCallbacks
	private lateinit var editingController: LedgerRowEditingController

	private var invertCreditAndDebit = false
	/**
	 * pointer to row being edited STARTS IN 1
	 */
	var editableRow = -1
		private set

	private lateinit var formatter: BalanceFormatter

	val lastRow: TableRow?
		get() {
			val rowViewIndex = childCount - 1
			return get(rowViewIndex)
		}

	val isEditingRow: Boolean
		get() = editableRow != -1

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

	fun setActionBarController(c: LedgerRowEditingController) {
		editingController = c
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
	fun inflateEmptyRow(): View {
		editableRowToView()
		return inflateRow()
	}

	fun rowViewToEditable(index: Int) {
		if (index <= 0) throw IllegalArgumentException("Can't edit table header!")

		val row = get<LedgerRow>(index) ?: throw IllegalArgumentException("View at index doesn't exist!")

		row.makeRowEditable()
		editingController.startEditing()

		for (i in TEXT_IDS.indices) {
			val t1 = row.findViewById<TextView>(TEXT_IDS[i])
			val t = row.findViewById<EditText>(EDIT_IDS[i])

			t.setText(t1.text)
			t1.text = ""

			t1.visibility = View.GONE
			t.visibility = View.VISIBLE
		}

		updateEditableRow(index)
	}

	/**
	 * Converts editable row into not editable.
	 */
	fun editableRowToView() {
		val row = get<LedgerRow>(editableRow)
		if (row != null && editableRow >= 0) {
			listener.onBeforeMakeRowNotEditable(row)

			updateEditableRow(-1)

			editingController.stopEditing()
			row.makeRowNotEditable()

			listener.onAfterMakeRowNotEditable(row)
		}
	}

	fun clear() {
		for (i in childCount - 1 downTo 1) {//DO NOT remove first line, the column titles
			removeViewAt(i)
		}

		updateEditableRow(-1)
	}

	private fun inflateRow(): LedgerRow {
		inflater.inflate(R.layout.row_main, this)
		editableRow = childCount - 1
		val row = get<LedgerRow>(editableRow)!!
		row.formatter = formatter
		row.editingController = editingController

		if (invertCreditAndDebit) {
			row.invertDebitCredit()
		}

		return row
	}

	private fun updateEditableRow(index: Int) {
		listener.onUpdateEditableRow(index)
		editableRow = index
	}

	interface LedgeCallbacks {
		fun onUpdateEditableRow(index: Int)
		fun onBeforeMakeRowNotEditable(row: View)
		fun onAfterMakeRowNotEditable(row: View)
	}

	interface BalanceFormatter {
		fun format(balance: BigDecimal): String
	}

}
