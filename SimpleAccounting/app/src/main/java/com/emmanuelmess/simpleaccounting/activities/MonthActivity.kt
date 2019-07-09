package com.emmanuelmess.simpleaccounting.activities

import android.app.ListActivity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

import com.emmanuelmess.simpleaccounting.R
import com.emmanuelmess.simpleaccounting.dataloading.GetMonthsWithDataAsyncTask
import com.emmanuelmess.simpleaccounting.db.TableGeneral
import kotlinx.android.synthetic.main.activity_month.*
import kotlinx.android.synthetic.main.item_month.view.*

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.Date
import java.util.Locale

/**
 * @author Emmanuel
 */
class MonthActivity : ListActivity() {

	private var monthListAdapter: MonthListAdapter? = null
	private val dateIntValues = ArrayList<Array<Int>>()
	private var updateYear: Int = 0
	private var updateMonth: Int = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_month)

		val callback = object : AppCompatCallback {
			override fun onSupportActionModeStarted(actionMode: ActionMode) {}

			override fun onSupportActionModeFinished(actionMode: ActionMode) {}

			override fun onWindowStartingSupportActionMode(callback: ActionMode.Callback): ActionMode? {
				return null
			}
		}

		val delegate = AppCompatDelegate.create(this, callback)

		delegate.onCreate(savedInstanceState)
		delegate.setContentView(R.layout.activity_month)

		delegate.setSupportActionBar(toolbar)
		delegate.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

		val preferences = PreferenceManager.getDefaultSharedPreferences(this)

		GetMonthsWithDataAsyncTask(TableGeneral(this)) { existingMonths: Array<IntArray> ->
			val monthListData = ArrayList<Array<String>>()
			var olderThanAlreadyPut = false

			for (d in existingMonths) {
				val m = d[0]
				val y = d[1]

				if (m == TableGeneral.OLDER_THAN_UPDATE && y == TableGeneral.OLDER_THAN_UPDATE
					&& olderThanAlreadyPut)
					continue

				if (m == TableGeneral.OLDER_THAN_UPDATE && y == TableGeneral.OLDER_THAN_UPDATE) {
					olderThanAlreadyPut = true

					updateYear = preferences.getInt(MainActivity.UPDATE_YEAR_SETTING, -1)
					updateMonth = preferences.getInt(MainActivity.UPDATE_MONTH_SETTING, -1)

					monthListData.add(arrayOf(getString(R.string.before_update_1_2)
						+ " " + getString(MainActivity.MONTH_STRINGS[updateMonth]).toLowerCase(), updateYear.toString()))
					dateIntValues.add(arrayOf(m, y))
				} else {
					monthListData.add(arrayOf(getString(MainActivity.MONTH_STRINGS[m]), y.toString()))
					dateIntValues.add(arrayOf(m, y))
				}
			}

			val currentM = Integer.parseInt(SimpleDateFormat("M", Locale.getDefault()).format(Date())) - 1
			//YEARS ALREADY START IN 0!!!
			val currentY = Integer.parseInt(SimpleDateFormat("yyyy", Locale.getDefault()).format(Date()))

			if (dateIntValues.size == 0 || !Arrays.equals(dateIntValues[dateIntValues.size - 1], arrayOf(currentM, currentY))) {
				monthListData.add(arrayOf(getString(MainActivity.MONTH_STRINGS[currentM]), currentY.toString()))
				dateIntValues.add(arrayOf(currentM, currentY))
			}

			Collections.reverse(monthListData)
			Collections.reverse(dateIntValues)

			monthListAdapter = MonthListAdapter(applicationContext,
				monthListData.toTypedArray())
			listAdapter = monthListAdapter
		}.execute()
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			android.R.id.home -> {
				//NavUtils.navigateUpFromSameTask(this);
				onBackPressed()//To make the MainActivity not destroy itself
				return true
			}
			else -> return super.onOptionsItemSelected(item)
		}
	}

	public override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
		MainActivity.setDate(dateIntValues[position][0], dateIntValues[position][1])
		onBackPressed()
	}

	private class MonthListAdapter(
		context: Context,
		val values: Array<Array<String>>
	) : ArrayAdapter<Array<String>>(context, R.layout.item_month, values) {
		val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

		override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
			val view =
				if (convertView == null) inflater.inflate(R.layout.item_month, parent, false)
				else convertView

			val monthView = view.textMonth
			val yearView = view.textYear

			monthView.text = values[position][0]
			yearView.text = values[position][1]

			return view
		}
	}
}
