package com.emmanuelmess.simpleaccounting.dataloading.async

import android.os.AsyncTask
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
	private val session: Session,
	private val tableMonthlyBalance: TableMonthlyBalance?,
	private val tableGeneral: TableGeneral,
	private val listener: AsyncFinishedListener<MonthData>
) : AsyncTask<Void, Void, MonthData>() {

	@Suppress("UsePropertyAccessSyntax")
	override fun onPreExecute() {
		tableMonthlyBalance?.getReadableDatabase()//updates the database, calls onUpgrade()
		tableGeneral.getReadableDatabase()//triggers onUpdate()

	}

	override fun doInBackground(vararg p: Void): MonthData? {
		if (!isAlreadyLoading)
			isAlreadyLoading = true
		else
			throw IllegalStateException("Already loading month: ${session.year}-${session.month + 1}");

		val data = tableGeneral.getIndexesForMonth(session)
		val rowToDBRowConversion = ArrayList<Int>()

		if (isCancelled) return null

		for (m in data)
			rowToDBRowConversion.add(m)

		return (
			if (isCancelled) null
			else MonthData(
				session,
				tableMonthlyBalance?.getBalanceLastMonthWithData(session),
				tableGeneral.getAllForMonth(session),
				rowToDBRowConversion)
		)
	}

	override fun onPostExecute(dbRowsPairedRowToDBConversion: MonthData) {
		if (!isCancelled)
			listener.onAsyncFinished(dbRowsPairedRowToDBConversion)

		isAlreadyLoading = false
	}

	override fun onCancelled(result: MonthData) {
		isAlreadyLoading = false
	}

	companion object {
		var isAlreadyLoading = false
			private set
	}

}
