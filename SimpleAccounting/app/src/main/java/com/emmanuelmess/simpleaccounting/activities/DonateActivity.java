package com.emmanuelmess.simpleaccounting.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.emmanuelmess.simpleaccounting.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Emmanuel
 *         on 12/5/2017, at 23:39.
 */

public class DonateActivity extends AppCompatActivity {

	private static final String BITCOIN_DIRECTION = "1HFhPxH9bqMKvs44nHqXjEEPC2m7z1V8tW";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_donate);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			// Show the Up button in the action bar.
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		List<HashMap<String, String>> m = new ArrayList<>();
		HashMap<String, String> h = new HashMap<>();
		h.put("title", getString(R.string.bitcoin));
		h.put("summary", BITCOIN_DIRECTION);
		m.add(h);
		ListAdapter adapter = new SimpleAdapter(this, m, R.layout.item_donate,
				new String[]{"title", "summary"}, new int[]{R.id.title, R.id.summary});

		// create a new ListView, set the adapter and item click listener
		ListView listViewItems = (ListView) findViewById(R.id.listView);
		listViewItems.setAdapter(adapter);
		listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0:
						try {
							String url = "bitcoin:" + BITCOIN_DIRECTION + "?amount=0.0005";
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse(url));
							startActivity(i);
						} catch (ActivityNotFoundException e) {
							Snackbar.make(listViewItems, R.string.no_bitcoin_app,
									Snackbar.LENGTH_LONG).show();
						}
						break;
				}
			}
		});

	}
}