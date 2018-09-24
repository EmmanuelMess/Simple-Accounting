package com.emmanuelmess.simpleaccounting.activities.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import com.emmanuelmess.simpleaccounting.dataloading.async.LoadMonthAsyncTask
import com.emmanuelmess.simpleaccounting.dataloading.async.LoadPrevBalanceAsyncTask
import com.emmanuelmess.simpleaccounting.dataloading.data.MonthData
import com.emmanuelmess.simpleaccounting.dataloading.data.Session
import com.emmanuelmess.simpleaccounting.db.TableGeneral
import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance

class TableData: ViewModel() {
	private val prevBalanceLiveData by lazy { MutableLiveData<Double?>() }
	private val monthDataLiveData by lazy { MutableLiveData<MonthData>() }

	private var loadPrevBalanceTask: LoadPrevBalanceAsyncTask? = null
	private var loadMonthTask: LoadMonthAsyncTask? = null

	fun getLoadPrevBalance(session: Session, tableMonthlyBalance: TableMonthlyBalance): LiveData<Double?> {
		loadPrevBalance(session, tableMonthlyBalance)
		return prevBalanceLiveData
	}

	fun getMonthData(session: Session, tableGeneral: TableGeneral): MutableLiveData<MonthData> {
		loadMonthData(session, tableGeneral)
		return monthDataLiveData
	}

	private fun loadPrevBalance(session: Session, tableMonthlyBalance: TableMonthlyBalance) {
		loadPrevBalanceTask?.let {
			if(!it.isCancelled) {
				it.cancel(true)
			}
		}

		loadPrevBalanceTask = LoadPrevBalanceAsyncTask(session.month, session.year, session.currency,
			tableMonthlyBalance, prevBalanceLiveData)
		loadPrevBalanceTask!!.execute()
	}

	private fun loadMonthData(session: Session, tableGeneral: TableGeneral) {
		loadMonthTask?.let {
			if(!it.isCancelled) {
				it.cancel(true)
			}
		}

		loadMonthTask = LoadMonthAsyncTask(session.month, session.year, session.currency,
			tableGeneral, monthDataLiveData)
		loadMonthTask!!.execute()
	}

	fun loadMonthIsNotRunning() = loadMonthTask?.status !== AsyncTask.Status.RUNNING

}
