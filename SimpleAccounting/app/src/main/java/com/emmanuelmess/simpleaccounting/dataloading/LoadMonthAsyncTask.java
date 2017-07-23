package com.emmanuelmess.simpleaccounting.dataloading;

import android.os.AsyncTask;
import android.util.Pair;

import com.emmanuelmess.simpleaccounting.db.TableGeneral;

import java.util.ArrayList;

/**
 * @author Emmanuel
 *         on 27/11/2016, at 15:03.
 */

public class LoadMonthAsyncTask extends AsyncTask<Void, Void, Pair<String[][], ArrayList<Integer>>> {

	private int month, year;
	private String currency;
	private TableGeneral tableGeneral;
	private AsyncFinishedListener<Pair<String[][], ArrayList<Integer>>> listener;
	private static boolean alreadyLoading = false;

	public LoadMonthAsyncTask(int m, int y, String c, TableGeneral dbG,
							  AsyncFinishedListener<Pair<String[][], ArrayList<Integer>>> l) {
		month = m;
		year = y;
		currency = c;
		tableGeneral = dbG;
		listener = l;
	}

	@Override
	protected Pair<String[][], ArrayList<Integer>> doInBackground(Void... p) {
		if(!alreadyLoading)
			alreadyLoading = true;
		else throw new IllegalStateException("Already loading month: " + year + "-" + (month+1));

		int[] data = tableGeneral.getIndexesForMonth(month, year, currency);
		ArrayList<Integer> rowToDBRowConversion = new ArrayList<>();

		if(isCancelled()) return null;

		for(int m : data)
			rowToDBRowConversion.add(m);

		if(isCancelled()) return null;

		return new Pair<>(tableGeneral.getAllForMonth(month, year, currency), rowToDBRowConversion);
	}

	@Override
	protected void onPostExecute(Pair<String[][], ArrayList<Integer>> dbRowsPairedRowToDBConversion) {
		if(!isCancelled())
			listener.OnAsyncFinished(dbRowsPairedRowToDBConversion);

		alreadyLoading = false;
	}

	public static boolean isAlreadyLoading() {
		return alreadyLoading;
	}

}
