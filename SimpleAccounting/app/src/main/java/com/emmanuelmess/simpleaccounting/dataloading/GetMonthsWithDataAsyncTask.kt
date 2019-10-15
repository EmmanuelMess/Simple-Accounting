package com.emmanuelmess.simpleaccounting.dataloading

import android.os.AsyncTask
import com.emmanuelmess.simpleaccounting.data.Month
import com.emmanuelmess.simpleaccounting.db.legacy.TableGeneral

class GetMonthsWithDataAsyncTask(
	private val tableGeneral: TableGeneral,
	private val onAsyncFinished: (List<Month>) -> Unit
): AsyncTask<Void, Void, List<Month>>() {

	override fun doInBackground(vararg params: Void?): List<Month>
		= tableGeneral.monthsWithData.map { Month(it[0], it[1]) }

	override fun onPostExecute(monthsWithData: List<Month>) {
		onAsyncFinished(monthsWithData)
	}
}