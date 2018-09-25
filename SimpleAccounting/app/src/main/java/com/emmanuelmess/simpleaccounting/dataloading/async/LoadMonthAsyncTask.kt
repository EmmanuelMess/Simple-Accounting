package com.emmanuelmess.simpleaccounting.dataloading.async

import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import android.util.Pair
import com.emmanuelmess.simpleaccounting.dataloading.data.MonthData
import com.emmanuelmess.simpleaccounting.dataloading.data.Session

import com.emmanuelmess.simpleaccounting.db.TableGeneral
import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance

import java.util.ArrayList

/**
 * @author Emmanuel
 * on 27/11/2016, at 15:03.
 */

class LoadMonthAsyncTask(
	private val month: Int,
	private val year: Int,
	private val currency: String,
	private val tableGeneral: TableGeneral,
	private val tableMonthlyBalance: TableMonthlyBalance?,
	private val listener: MutableLiveData<MonthData>
) : AsyncTask<Void, Void, MonthData>() {

	@Suppress("UsePropertyAccessSyntax")
	override fun onPreExecute() {
		tableGeneral.getReadableDatabase()//updates the database, calls onUpgrade()
		tableMonthlyBalance?.getReadableDatabase()//updates the database, calls onUpgrade()
	}

	override fun doInBackground(vararg p: Void): MonthData? {
		val data = tableGeneral.getIndexesForMonth(month, year, currency)
		val rowToDBRowConversion = ArrayList<Int>()

		if (isCancelled) return null

		for (m in data)
			rowToDBRowConversion.add(m)

		return (
			if (isCancelled) null
			else {
				MonthData(Session(month, year, currency),
					tableMonthlyBalance?.getBalanceLastMonthWithData(month, year, currency),
					tableGeneral.getAllForMonth(month, year, currency),
					rowToDBRowConversion)
			}
		)
	}

	override fun onPostExecute(dbRowsPairedRowToDBConversion: MonthData) {
		if (!isCancelled)
			listener.value = dbRowsPairedRowToDBConversion
	}

}
