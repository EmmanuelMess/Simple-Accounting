package com.emmanuelmess.simpleaccounting.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import com.emmanuelmess.simpleaccounting.R
import com.emmanuelmess.simpleaccounting.activities.views.LedgerRow
import com.emmanuelmess.simpleaccounting.activities.views.LedgerView
import com.emmanuelmess.simpleaccounting.data.Session
import com.emmanuelmess.simpleaccounting.data.TableDataManager
import com.emmanuelmess.simpleaccounting.db.TableGeneral
import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance
import com.emmanuelmess.simpleaccounting.utils.SimpleBalanceFormatter
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment(), LedgerView.LedgeCallbacks {

	companion object {
		private const val SESSION_KEY = "session"
		private const val GRAPH_UPDATE_MONTH_KEY = "updateMonth"
		private const val GRAPH_UPDATE_YEAR_KEY = "updateYear"

		@JvmStatic
		fun newInstance(session: Session) = MainFragment().apply {
			arguments = Bundle().apply {
				putParcelable(SESSION_KEY, session)
			}
		}
	}

	private val tableDataManager = TableDataManager()

	private lateinit var session: Session
	private lateinit var tableGeneral: TableGeneral
	private lateinit var tableMonthlyBalance: TableMonthlyBalance

	private lateinit var table: LedgerView
	private lateinit var scrollView: ScrollView
	private lateinit var space: View

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			session = it.getParcelable(SESSION_KEY) as Session
		}

		tableGeneral = TableGeneral(requireContext())//DO NOT change the order of table creation!
		tableMonthlyBalance = TableMonthlyBalance(requireContext())
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_main, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		fun <T: View> findViewById(id: Int) = view.findViewById<T>(id)

		scrollView = findViewById(R.id.scrollView)

		table = findViewById(R.id.table)
		table.setFormatter(SimpleBalanceFormatter())
		table.setListener(this)
		table.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
					table.viewTreeObserver.removeOnGlobalLayoutListener(this)
				else
					table.viewTreeObserver.removeGlobalOnLayoutListener(this)

				space = findViewById(R.id.space)
				space.minimumHeight = findViewById<View>(R.id.fab).height - findViewById<View>(R.id.fab).paddingTop

				loadMonth(editableMonth, editableYear, editableCurrency)
				//in case the activity gets destroyed
				invalidateTable = false
				invalidateToolbar = false
			}
		})

		if(session.isOlderThanUpdate()) {
			space.visibility = GONE
		}
	}

	fun onFabClicked() {
		scrollView.fullScroll(View.FOCUS_DOWN)

		if (table.getChildCount() > FIRST_REAL_ROW) {
			val day = SimpleDateFormat("dd", Locale.getDefault()).format(Date())

			val index = tableGeneral.newRowInMonth(Session(editableMonth, editableYear, editableCurrency))
			tableGeneral.update(index, TableGeneral.COLUMNS[0], day)

			rowToDBRowConversion.add(tableGeneral.lastIndex)
			val row = loadRow()

			row.setDate(day)
			row.requestFocus()

			//editableRowColumnsHash[0] = row.getDate().toString().hashCode();

			val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
			imm!!.showSoftInput(row.findViewById(R.id.editDate), InputMethodManager.SHOW_IMPLICIT)
		} else
			createNewRowWhenMonthLoaded = true
	}

	override fun onUpdateEditableRow(index: Int) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun onBeforeMakeRowNotEditable(row: View) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun onAfterMakeRowNotEditable(row: View) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

}
