package com.emmanuelmess.simpleaccounting.dataloading.async

import android.os.AsyncTask

import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance

/**
 * @author Emmanuel
 * on 4/12/2016, at 13:47.
 */

class LoadPrevBalanceAsyncTask(
	private val month: Int,
	private val year: Int,
	private val currency: String,
	private val tableMonthlyBalance: TableMonthlyBalance,
	private val listener: AsyncFinishedListener<Double?>
) : AsyncTask<Void, Void, Double>() {

	@Suppress("UsePropertyAccessSyntax")
	override fun onPreExecute() {
		tableMonthlyBalance.getReadableDatabase()//updates the database, calls onUpgrade()
	}

	override fun doInBackground(vararg v: Void): Double? {
		return tableMonthlyBalance.getBalanceLastMonthWithData(month, year, currency)
	}

	override fun onPostExecute(lastMonthData: Double?) {
		if (!isCancelled) {
			listener.onAsyncFinished(lastMonthData)
		}
	}
}
