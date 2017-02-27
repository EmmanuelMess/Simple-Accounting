package com.emmanuelmess.simpleaccounting.dataloading;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.MainActivity;
import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.utils.Utils;
import com.emmanuelmess.simpleaccounting.db.TableGeneral;
import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author Emmanuel
 *         on 27/11/2016, at 15:03.
 */

public class LoadMonthAsyncTask extends AsyncTask<Void, Void, String[][]> {

	private int month, year;
	private String currency;
	private TableGeneral tableGeneral;
	private TableMonthlyBalance tableMonthlyBalance;
	private TableLayout table;
	private LayoutInflater inflater;
	private ArrayList<Integer> rowToDBRowConversion = new ArrayList<>();
	private AsyncFinishedListener<ArrayList<Integer>> listener;
	private MainActivity mainActivity;
	private boolean invertCreditDebit = false;
	private static boolean alreadyLoading = false;

	public LoadMonthAsyncTask(int m, int y, String c, TableGeneral dbG, TableMonthlyBalance db, TableLayout t,
	                          LayoutInflater i, AsyncFinishedListener<ArrayList<Integer>> l,
	                          MainActivity a, boolean invert) {
		month = m;
		year = y;
		currency = c;
		tableGeneral = dbG;
		tableMonthlyBalance = db;
		table = t;
		inflater = i;
		listener = l;
		mainActivity = a;
		invertCreditDebit = invert;
	}

	@Override
	protected void onPreExecute() {
		if(table.getChildCount() - mainActivity.getFirstRealRow() > 0)
			throw new IllegalArgumentException("Table already contains "
					+ (table.getChildCount() - mainActivity.getFirstRealRow()) + " elements; " +
					"delete all rows before excecuting LoadMonthAsyncTask!");
	}

	@Override
	protected String[][] doInBackground(Void... p) {
		if(!alreadyLoading)
			alreadyLoading = true;
		else throw new IllegalStateException("Already loading month: " + year + "-" + (month+1));

		int[] data = tableGeneral.getIndexesForMonth(month, year, currency);

		for(int m : data)
			rowToDBRowConversion.add(m);

		return tableGeneral.getAllForMonth(month, year, currency);
	}

	@Override
	protected void onPostExecute(String[][] dbRows) {
		BigDecimal memBalance = BigDecimal.ZERO;

		if(mainActivity.getFirstRealRow() == 2) {
			memBalance = memBalance.add(Utils.parseString(
					((TextView) table.getChildAt(1).findViewById(R.id.textBalance))
							.getText().toString().substring(2)));
		}

		for (String[] dbRow : dbRows) {
			inflater.inflate(R.layout.newrow_main, table);

			View row = mainActivity.loadRow();

			int[] textIds = MainActivity.TEXT_IDS;
			int[] editIds = MainActivity.EDIT_IDS;

			for (int j = 0; j < textIds.length; j++) {
				row.findViewById(editIds[j]).setVisibility(View.GONE);

				TextView t = (TextView) row.findViewById(textIds[j]);
				t.setVisibility(View.VISIBLE);
				t.setText(dbRow[j]);
			}

			TextView t = (TextView) row.findViewById(R.id.textBalance);
			if (dbRow[2] != null)
				memBalance = memBalance.add(Utils.parseString(dbRow[2]));
			if (dbRow[3] != null)
				memBalance = memBalance.subtract(Utils.parseString(dbRow[3]));

			String s = "$ " + String.valueOf(memBalance);
			t.setText(s);
		}

		listener.OnAsyncFinished(rowToDBRowConversion);

		alreadyLoading = false;
	}

}
