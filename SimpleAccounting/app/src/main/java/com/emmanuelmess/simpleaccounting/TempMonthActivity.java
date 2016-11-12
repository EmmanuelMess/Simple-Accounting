package com.emmanuelmess.simpleaccounting;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class TempMonthActivity extends ListActivity {

	private FileIO f;
	private MonthListAdapter monthListAdapter;
	private ArrayList<Integer[]> dateIntValues = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_temp_month);

		AppCompatCallback callback = new AppCompatCallback() {
			@Override
			public void onSupportActionModeStarted(ActionMode actionMode) {
			}

			@Override
			public void onSupportActionModeFinished(ActionMode actionMode) {
			}

			@Nullable
			@Override
			public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
				return null;
			}
		};

		AppCompatDelegate delegate = AppCompatDelegate.create(this,callback);

		delegate.onCreate(savedInstanceState);
		delegate.setContentView(R.layout.activity_temp_month);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		delegate.setSupportActionBar(toolbar);
		ActionBar ab = delegate.getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);


		f = new FileIO(this);


		(new AsyncTask<Void, Void, int[][]>() {
			@Override
			protected void onPreExecute() {
			}

			@Override
			protected int[][] doInBackground(Void... p) {
				return f.getMonthsWithData();
			}

			@Override
			protected void onPostExecute(int[][] existingMonths) {
				ArrayList<String[]> monthListData = new ArrayList<>();

				for (int d[] : existingMonths) {
					int m = d[0];
					monthListData.add(new String[] {getString(MainActivity.MONTH_STRINGS[m]), String.valueOf(d[1])});
					dateIntValues.add(new Integer[]{m, d[1]});
				}

				// Create an empty adapter we will use to display the loaded data.
				// We pass null for the cursor, then update it in onLoadFinished()
				monthListAdapter = new MonthListAdapter(getApplicationContext(),
						monthListData.toArray(new String[monthListData.size()][2]));
				setListAdapter(monthListAdapter);
			}
		}).execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		Bundle extra = new Bundle();
		extra.putInt(MainActivity.MONTH, dateIntValues.get(position)[0]);
		extra.putInt(MainActivity.YEAR, dateIntValues.get(position)[1]);
		intent.putExtras(extra);
		startActivity(intent);
	}

	private class MonthListAdapter extends ArrayAdapter<String[]> {

		String[][] values;

		MonthListAdapter(Context context, String[][] values) {
			super(context, R.layout.list_item, values);
			this.values = values;
		}

		@NonNull
		@Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getApplicationContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if(convertView == null)
				convertView = inflater.inflate(R.layout.list_item, parent, false);

			TextView monthView = (TextView) convertView.findViewById(R.id.textMonth);
			TextView yearView = (TextView) convertView.findViewById(R.id.textYear);
			monthView.setText(values[position][0]);
			yearView.setText(values[position][1]);

			return convertView;
		}
	}
}
