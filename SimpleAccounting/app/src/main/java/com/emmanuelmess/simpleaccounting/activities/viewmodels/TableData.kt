package com.emmanuelmess.simpleaccounting.activities.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import com.emmanuelmess.simpleaccounting.dataloading.async.LoadMonthAsyncTask
import com.emmanuelmess.simpleaccounting.dataloading.data.MonthData
import com.emmanuelmess.simpleaccounting.dataloading.data.Session
import com.emmanuelmess.simpleaccounting.db.TableGeneral
import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance

class TableData: ViewModel() {
	private val monthDataLiveData by lazy { MutableLiveData<MonthData>() }

	private var loadMonthTask: LoadMonthAsyncTask? = null

	fun getMonthData(session: Session, tableGeneral: TableGeneral,
	                 tableMonthlyBalance: TableMonthlyBalance?): MutableLiveData<MonthData> {
		loadMonthData(session, tableGeneral, tableMonthlyBalance)
		return monthDataLiveData
	}

	private fun loadMonthData(session: Session, tableGeneral: TableGeneral,
	                          tableMonthlyBalance: TableMonthlyBalance?) {
		loadMonthTask?.let {
			if(!it.isCancelled) {
				it.cancel(true)
			}
		}

		loadMonthTask = LoadMonthAsyncTask(session, tableGeneral, tableMonthlyBalance, monthDataLiveData)
		loadMonthTask!!.execute()
	}

	fun loadMonthIsNotRunning() = loadMonthTask?.status !== AsyncTask.Status.RUNNING

}
