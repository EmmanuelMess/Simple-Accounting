package com.emmanuelmess.simpleaccounting;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class TempMonthActivity extends ListActivity {

	private FileIO f;
	private ArrayAdapter<String> monthListAdapter;

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
		delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		f = new FileIO(this);


		(new AsyncTask<Void, Void, int[]>() {
			@Override
			protected void onPreExecute() {
			}

			@Override
			protected int[] doInBackground(Void... p) {
				return f.getMonthsWithData();
			}

			@Override
			protected void onPostExecute(int[] existingMonths) {
				ArrayList<String> monthListData = new ArrayList<>();

				for (int m : existingMonths) {
					if(m != -1)
						monthListData.add(getString(MainActivity.MONTH_STRINGS[m]));
					else {
						//TODO checked what failed
						Toast.makeText(getApplicationContext(),
								"Seems that the database is corrupted, repairing in background...",
								Toast.LENGTH_SHORT).show();
					}
				}

				// Create an empty adapter we will use to display the loaded data.
				// We pass null for the cursor, then update it in onLoadFinished()
				monthListAdapter = new ArrayAdapter<>(getApplicationContext(),
						android.R.layout.simple_list_item_1, monthListData);
				setListAdapter(monthListAdapter);
			}
		}).execute();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Do something when a list item is clicked
	}
}
