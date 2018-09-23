package com.emmanuelmess.simpleaccounting.dataloading.async;

import android.os.AsyncTask;

import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance;

/**
 * @author Emmanuel
 *         on 4/12/2016, at 13:47.
 */

public class LoadPrevBalanceAsyncTask extends AsyncTask<Void, Void, Double> {

	private int month, year;
	private String currency;
	private TableMonthlyBalance tableMonthlyBalance;
	private AsyncFinishedListener<Double> listener;

	public LoadPrevBalanceAsyncTask(int m, int y, String c, TableMonthlyBalance db,
									AsyncFinishedListener<Double> l) {
		month = m;
		year = y;
		currency = c;
		tableMonthlyBalance = db;
		listener = l;
	}

	@Override
	protected void onPreExecute() {
		tableMonthlyBalance.getReadableDatabase();//updates the database, calls onUpgrade()
	}

	@Override
	protected Double doInBackground(Void... v) {
		return tableMonthlyBalance.getBalanceLastMonthWithData(month, year, currency);
	}

	@Override
	protected void onPostExecute(Double lastMonthData) {
		if(!isCancelled())
			listener.onAsyncFinished(lastMonthData);
	}
}
