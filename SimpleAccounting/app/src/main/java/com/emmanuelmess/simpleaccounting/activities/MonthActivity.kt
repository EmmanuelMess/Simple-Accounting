package com.emmanuelmess.simpleaccounting.activities

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity

import com.emmanuelmess.simpleaccounting.R
import com.emmanuelmess.simpleaccounting.data.Month
import com.emmanuelmess.simpleaccounting.dataloading.GetMonthsWithDataAsyncTask
import com.emmanuelmess.simpleaccounting.db.legacy.TableGeneral
import kotlinx.android.synthetic.main.activity_month.*
import kotlinx.android.synthetic.main.content_month.*
import kotlinx.android.synthetic.main.item_month.view.*

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @author Emmanuel
 */
class MonthActivity : AppCompatActivity(), AdapterView.OnItemClickListener {
	private lateinit var monthListAdapter: MonthListAdapter
	private val dateIntValues = mutableListOf<Month>()
	private var updateYear: Int = 0
	private var updateMonth: Int = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_month)

		setSupportActionBar(toolbar)
		supportActionBar!!.setDisplayHomeAsUpEnabled(true)

		val preferences = PreferenceManager.getDefaultSharedPreferences(this)

		GetMonthsWithDataAsyncTask(TableGeneral(this)) { existingMonths: List<Month> ->
			val monthListData = mutableListOf<MonthListAdapter.MonthItemData>()
			var olderThanAlreadyPut = false

			for ((m, y) in existingMonths) {
				if (m == TableGeneral.OLDER_THAN_UPDATE && y == TableGeneral.OLDER_THAN_UPDATE
					&& olderThanAlreadyPut)
					continue

				if (m == TableGeneral.OLDER_THAN_UPDATE && y == TableGeneral.OLDER_THAN_UPDATE) {
					olderThanAlreadyPut = true

					updateYear = preferences.getInt(MainActivity.UPDATE_YEAR_SETTING, -1)
					updateMonth = preferences.getInt(MainActivity.UPDATE_MONTH_SETTING, -1)

					monthListData.add(MonthListAdapter.MonthItemData(
						getString(R.string.before, getString(MainActivity.MONTH_STRINGS[updateMonth])).toLowerCase(),
						updateYear.toString()
					))
				} else {
					monthListData.add(MonthListAdapter.MonthItemData(
						getString(MainActivity.MONTH_STRINGS[m]),
						y.toString())
					)
				}

				dateIntValues.add(Month(m, y))
			}

			val currentM = Integer.parseInt(SimpleDateFormat("M", Locale.getDefault()).format(Date())) - 1
			//YEARS ALREADY START IN 0!!!
			val currentY = Integer.parseInt(SimpleDateFormat("yyyy", Locale.getDefault()).format(Date()))

			if (dateIntValues.size == 0 || dateIntValues[dateIntValues.size - 1] != Month(currentM, currentY)) {
				monthListData.add(MonthListAdapter.MonthItemData(
					getString(MainActivity.MONTH_STRINGS[currentM]),
					currentY.toString())
				)
				dateIntValues.add(Month(currentM, currentY))
			}

			monthListData.reverse()
			dateIntValues.reverse()

			monthListAdapter = MonthListAdapter(applicationContext, monthListData)
			monthList.adapter = monthListAdapter
			monthList.onItemClickListener = this@MonthActivity
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

	override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
		MainActivity.setDate(dateIntValues[position].month, dateIntValues[position].year)
		onBackPressed()
	}

	private class MonthListAdapter(
		context: Context,
		val values: List<MonthItemData>
	) : ArrayAdapter<MonthListAdapter.MonthItemData>(context, R.layout.item_month, values) {
		val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

		override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
			val view =
				if (convertView == null) inflater.inflate(R.layout.item_month, parent, false)
				else convertView

			val monthView = view.textMonth
			val yearView = view.textYear

			monthView.text = values[position].monthName
			yearView.text = values[position].yearName

			return view
		}

		data class MonthItemData(val monthName: String, val yearName: String)
	}
}
