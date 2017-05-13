package com.emmanuelmess.simpleaccounting.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.emmanuelmess.simpleaccounting.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class GraphActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			// Show the Up button in the action bar.
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// programmatically create a LineChart
		LineChart chart = (LineChart) findViewById(R.id.chart);

		List<Entry> entries = new ArrayList<>();

		entries.add(new Entry(1, 3000));
		entries.add(new Entry(3, 7000));
		entries.add(new Entry(5, 2000));
		entries.add(new Entry(18, 9800));

		LineDataSet dataSet = new LineDataSet(entries, "Normal");

		chart.getAxisRight().setEnabled(false);
		chart.getXAxis().setDrawGridLines(false);
		chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
		chart.getLegend().setEnabled(false);
		chart.setDescription(null);
		chart.setData(new LineData(dataSet));

	}

}
