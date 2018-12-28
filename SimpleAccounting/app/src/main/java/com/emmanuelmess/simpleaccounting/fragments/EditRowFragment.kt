package com.emmanuelmess.simpleaccounting.fragments

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.view.*
import com.emmanuelmess.simpleaccounting.R
import com.emmanuelmess.simpleaccounting.activities.views.LedgerRow
import com.emmanuelmess.simpleaccounting.activities.views.LedgerView
import com.emmanuelmess.simpleaccounting.fragments.superclasses.OnBackPressableFragment
import com.emmanuelmess.simpleaccounting.utils.*
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_editrow.*
import kotlinx.android.synthetic.main.fragment_editrow.view.*
import java.math.BigDecimal


class EditRowFragment : OnBackPressableFragment() {

	companion object {
		private val MODEL_KEY = "model"
		private val ORIGINAL_ROW_ID = "rowId"
		private val Y_POSITION_KEY = "yPosition"

		fun newInstance(
			model: EditRowFragmentModel,
			rowIndex: Int,
			yPosition: Float
		): EditRowFragment = with(EditRowFragment()) {
			val bundle = Bundle()
			bundle.putParcelable(MODEL_KEY, model)
			bundle.putInt(ORIGINAL_ROW_ID, rowIndex)
			bundle.putFloat(Y_POSITION_KEY, yPosition)

			arguments = bundle

			return this
		}
	}

	/* lateinit */ var endEditCallback: EndEditCallback? = null

	private lateinit var model: EditRowFragmentModel
	private /* lateinit */ var originalRowId: Int? = null

	private /*lateinit*/ var INITIAL_ROW_Y: Float? = null
	private /*lateinit*/ var INITIAL_ROW_LAYOUT_GRAVITY: Int? = null
	private /*lateinit*/ var CORRECT_ROW_Y: Float? = null

	override fun onAttach(context: Context) {
		super.onAttach(context)

		val args = arguments!!
		model = args.getParcelable(MODEL_KEY)!!
		originalRowId = args.getInt(ORIGINAL_ROW_ID)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setHasOptionsMenu(true)
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val args = arguments!!
		val yPosition = args.getFloat(Y_POSITION_KEY)

		inflater.inflate(R.layout.fragment_editrow, container, false).let { rootView ->
			rootView.editCredit.setHint(R.string.credit)
			rootView.editDebit.setHint(R.string.debit)

			rootView.editDate.textDisplay = model.date
			rootView.editRef.textDisplay = model.reference
			rootView.editCredit.textDisplay = if(model.credit != BigDecimal.ZERO) model.credit.toPlainString() else ""
			rootView.editDebit.textDisplay = if(model.debit != BigDecimal.ZERO) model.debit.toPlainString() else ""
			rootView.textBalance.textDisplay = model.formatter.format(model.oldBalance + model.credit - model.debit)

			rootView.row.let { tableRow ->
				INITIAL_ROW_Y = tableRow.y
				INITIAL_ROW_LAYOUT_GRAVITY = tableRow.layoutGravity

				val topMargin = (tableRow.layoutParams as ViewGroup.MarginLayoutParams).topMargin
				CORRECT_ROW_Y = yPosition - topMargin - tableRow.paddingTop

				tableRow.y = CORRECT_ROW_Y!!
			}

			return rootView
		}
	}

	override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
		super.onViewCreated(rootView, savedInstanceState)

		rootView.editRef.openKeyboard()

		rootView.editCredit.addTextChangedListener(object : SimpleTextWatcher() {
			override fun afterTextChanged(editable: Editable) {
				model = model.copy(credit = editable.toString().toBigDecimalSafe())
				updateBalance()
			}
		})
		rootView.editDebit.addTextChangedListener(object : SimpleTextWatcher() {
			override fun afterTextChanged(editable: Editable) {
				model = model.copy(debit = editable.toString().toBigDecimalSafe())
				updateBalance()
			}
		})
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		menu.clear()
		inflater.inflate(R.menu.editrow_toolbar, menu)
		menu.findItem(R.id.action_done).setOnMenuItemClickListener(::onClickDone)

		super.onCreateOptionsMenu(menu, inflater)
	}

	private fun onClickDone(item: MenuItem): Boolean {
		endEditing(originalRowId!!)

		return true
	}

	override fun onKeyboardOpened() {
		row.layoutGravity = Gravity.BOTTOM
		row.y = INITIAL_ROW_Y!!
	}

	override fun onKeyboardClosed() {
		row?.let {// This method will be called after the fragment was removed, if the keyboard had been open
			row.layoutGravity = INITIAL_ROW_LAYOUT_GRAVITY!!
			//the keyboard opening apparently changes the top of the view
			row.y = CORRECT_ROW_Y!! + row.top
		}
	}

	private fun updateBalance() {
		textBalance.textDisplay = model.formatter.format(model.oldBalance + model.credit - model.debit)
	}

	override fun onBackPressed(): Boolean {
		endEditCallback!!.onDiscardEdit(this, originalRowId!!)
		return true
	}

	private fun endEditing(index: Int) {
		val date = editDate.textDisplay.toString()
		val reference = editRef.textDisplay.toString()
		val credit = editCredit.textDisplay.toString()
		val debit = editDebit.textDisplay.toString()
		val balance = textBalance.textDisplay.toString()
		val rowModel = LedgerRow.LedgerRowModel(date, reference, credit, debit, balance)
		endEditCallback!!.onSaveEdit(this, rowModel, index, model.credit, model.debit)
	}

	interface EndEditCallback {
		fun onSaveEdit(fragment: EditRowFragment, model: LedgerRow.LedgerRowModel, index: Int,
		               credit: BigDecimal, debit: BigDecimal)
		fun onDiscardEdit(fragment: EditRowFragment, index: Int)
	}

	@Parcelize
	data class EditRowFragmentModel(
		val date: String,
		val reference: String,
		val credit: BigDecimal,
		val debit: BigDecimal,
		val oldBalance: BigDecimal,
		val formatter: LedgerView.BalanceFormatter
	) : Parcelable
}

