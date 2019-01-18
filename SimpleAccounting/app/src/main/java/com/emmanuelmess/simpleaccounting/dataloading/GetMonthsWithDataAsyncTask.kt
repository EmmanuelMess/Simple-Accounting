package com.emmanuelmess.simpleaccounting.dataloading

import android.os.AsyncTask
import com.emmanuelmess.simpleaccounting.db.TableGeneral

class GetMonthsWithDataAsyncTask(
	private val tableGeneral: TableGeneral,
	private val listener: AsyncFinishedListener<Array<IntArray>>
): AsyncTask<Void, Void, Array<IntArray>>() {

	override fun doInBackground(vararg params: Void?): Array<IntArray>
		= tableGeneral.monthsWithData

	override fun onPostExecute(monthsWithData: Array<IntArray>) {
		listener.onAsyncFinished(monthsWithData)
	}
}