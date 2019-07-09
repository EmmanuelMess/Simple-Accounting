package com.emmanuelmess.simpleaccounting.dataloading

import android.os.AsyncTask
import com.emmanuelmess.simpleaccounting.db.TableGeneral

class GetMonthsWithDataAsyncTask(
	private val tableGeneral: TableGeneral,
	private val onAsyncFinished: (Array<IntArray>) -> Unit
): AsyncTask<Void, Void, Array<IntArray>>() {

	override fun doInBackground(vararg params: Void?): Array<IntArray>
		= tableGeneral.monthsWithData

	override fun onPostExecute(monthsWithData: Array<IntArray>) {
		onAsyncFinished(monthsWithData)
	}
}