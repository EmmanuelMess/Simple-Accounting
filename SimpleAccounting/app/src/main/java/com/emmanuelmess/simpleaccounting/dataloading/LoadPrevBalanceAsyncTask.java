package com.emmanuelmess.simpleaccounting.dataloading;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.MainActivity;
import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance;

import static com.emmanuelmess.simpleaccounting.MainActivity.EDIT_IDS;
import static com.emmanuelmess.simpleaccounting.MainActivity.TEXT_IDS;

/**
 * @author Emmanuel
 *         on 4/12/2016, at 13:47.
 */

public class LoadPrevBalanceAsyncTask extends AsyncTask<Void, Void, Double> {

	private int month, year;
	private TableMonthlyBalance tableMonthlyBalance;
	private TableLayout table;
	private LayoutInflater inflater;
	private AsyncFinishedListener<Void> listener;
	private MainActivity mainActivity;

	public LoadPrevBalanceAsyncTask(int m, int y, TableMonthlyBalance db, TableLayout t,
	                                LayoutInflater i, AsyncFinishedListener<Void> l, MainActivity a) {
		month = m;
		year = y;
		tableMonthlyBalance = db;
		table = t;
		inflater = i;
		listener = l;
		mainActivity = a;
	}

	@Override
	protected Double doInBackground(Void... v) {
		return tableMonthlyBalance.getBalanceLastMonthWithData(month, year);
	}

	@Override
	protected void onPostExecute(Double lastMonthData) {
		if (lastMonthData != null) {
			inflater.inflate(R.layout.newrow_main, table);

			int rowViewIndex = table.getChildCount() - 1;
			TableRow row = (TableRow) table.getChildAt(rowViewIndex);

			for (int j = 0; j < TEXT_IDS.length; j++) {
				row.findViewById(EDIT_IDS[j]).setVisibility(View.GONE);
				row.findViewById(TEXT_IDS[j]).setVisibility(View.VISIBLE);
			}

			((TextView) row.findViewById(R.id.textRef)).setText(R.string.previous_balance);
			((TextView) row.findViewById(R.id.textCredit)).setText("");
			((TextView) row.findViewById(R.id.textDebit)).setText("");

			TextView t = (TextView) row.findViewById(R.id.textBalance);
			String s = "$ " + String.valueOf(lastMonthData);
			t.setText(s);
			mainActivity.setFirstRealRow(2);
		}

		listener.OnAsyncFinished(null);
	}
}
