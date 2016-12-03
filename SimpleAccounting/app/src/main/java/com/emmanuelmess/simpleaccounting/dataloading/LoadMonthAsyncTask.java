package com.emmanuelmess.simpleaccounting.dataloading;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.MainActivity;
import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.Utils;
import com.emmanuelmess.simpleaccounting.db.DBGeneral;
import com.emmanuelmess.simpleaccounting.db.DBMonthlyBalance;

import java.util.ArrayList;

/**
 * @author Emmanuel
 *         on 27/11/2016, at 15:03.
 */

public class LoadMonthAsyncTask extends AsyncTask<Void, Void, String[][]> {

	private int month, year, firstRealRow;
	private DBGeneral dbGeneral;
	private DBMonthlyBalance dbMonthlyBalance;
	private TableLayout table;
	private LayoutInflater inflater;
	private ArrayList<Integer> rowToDBRowConversion = new ArrayList<>();
	private OnMonthFinishedLoading listener;
	private MainActivity mainActivity;
	private boolean alreadyLoading = false;

	public LoadMonthAsyncTask(int m, int y, int fRealRow, DBGeneral dbG, DBMonthlyBalance db, TableLayout t,
	                          LayoutInflater i, OnMonthFinishedLoading l,
	                          MainActivity a) {
		month = m;
		year = y;
		firstRealRow = fRealRow;
		dbGeneral = dbG;
		dbMonthlyBalance = db;
		table = t;
		inflater = i;
		listener = l;
		mainActivity = a;

		if(table.getChildCount() - mainActivity.getFirstRealRow() > 0)
			throw new IllegalArgumentException("Table already contains "
					+ (table.getChildCount() - 1 - mainActivity.getFirstRealRow()) + "elements!");
	}

	@Override
	protected String[][] doInBackground(Void... p) {
		if(!alreadyLoading)
			alreadyLoading = true;
		else throw new IllegalStateException("Already loading month: " + year + "-" + (month+1));

		dbMonthlyBalance.createMonth(month, year);

		int[] data = dbGeneral.getIndexesForMonth(month, year);

		for(int m : data)
			rowToDBRowConversion.add(m);

		return dbGeneral.getAllForMonth(month, year);
	}

	@Override
	protected void onPostExecute(String[][] dbRows) {
		double memBalance = 0;

		if(firstRealRow == 2) {
			memBalance += Double.parseDouble(((TextView) table.getChildAt(1)
					.findViewById(R.id.textBalance)).getText().toString().substring(1));
		}

		for (String[] dbRow : dbRows) {
			inflater.inflate(R.layout.newrow_main, table);

			int dbIndex = rowToDBRowConversion.get(table.getChildCount() - 1 - mainActivity.getFirstRealRow());
			View row = mainActivity.loadRow(dbIndex);

			for (int j = 0; j < MainActivity.TEXT_IDS.length; j++) {
				row.findViewById(MainActivity.EDIT_IDS[j]).setVisibility(View.GONE);

				TextView t = (TextView) row.findViewById(MainActivity.TEXT_IDS[j]);
				t.setVisibility(View.VISIBLE);
				t.setText(dbRow[j]);
			}

			TextView t = (TextView) row.findViewById(R.id.textBalance);
			if (dbRow[2] != null)
				memBalance += Utils.parse(dbRow[2]);
			if (dbRow[3] != null)
				memBalance -= Utils.parse(dbRow[3]);

			String s = "$ " + String.valueOf(memBalance);
			t.setText(s);
		}

		listener.OnMonthFinishedLoading(rowToDBRowConversion);

		alreadyLoading = false;
	}

}
