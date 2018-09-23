package com.emmanuelmess.simpleaccounting.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.dataloading.async.LoadMonthAsyncTask;
import com.emmanuelmess.simpleaccounting.db.TableGeneral;
import com.emmanuelmess.simpleaccounting.utils.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.lang.Integer.parseInt;

public class GraphActivity extends AppCompatActivity {

	public static final String GRAPH_MONTH = "com.emmanuelmess.simpleaccounting.IntentGraphMonth",
			GRAPH_YEAR = "com.emmanuelmess.simpleaccounting.IntentGraphYear",
			GRAPH_CURRENCY = "com.emmanuelmess.simpleaccounting.IntentGraphCurrency",
			GRAPH_UPDATE_MONTH = "com.emmanuelmess.simpleaccounting.IntentGraphUpdateMonth",
			GRAPH_UPDATE_YEAR = "com.emmanuelmess.simpleaccounting.IntentGraphUpdateYear";

	private LoadMonthAsyncTask loadMonthAsyncTask;

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
		LineChart chart = findViewById(R.id.chart);

		Date d = new Date();
		int currentMonth = parseInt(new SimpleDateFormat("M", Locale.getDefault()).format(d)) - 1,
				currentYear = parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(d));

		int month = getIntent().getIntExtra(GRAPH_MONTH, currentMonth);
		int year = getIntent().getIntExtra(GRAPH_YEAR, currentYear);
		String currency = getIntent().getStringExtra(GRAPH_CURRENCY);
		int[] updateDate = {getIntent().getIntExtra(GRAPH_UPDATE_MONTH, -1),
				getIntent().getIntExtra(GRAPH_UPDATE_YEAR, -1)};

		((TextView) findViewById(R.id.title)).setText(Utils.getTitle(this, month, year, currency, updateDate));

		loadMonthAsyncTask = new LoadMonthAsyncTask(month, year, currency, new TableGeneral(this), (result) -> {
			if(result.first.length > 0) {
				List<Entry> entries = new ArrayList<>();
				BigDecimal bigDecimal = BigDecimal.ZERO;

				for (String[] s : result.first) {
					if(s[0] != null) {
						int day = parseInt(s[0]);

						float credit = s[2] != null ? Float.parseFloat(s[2]) : 0,
								debit = s[3] != null ? Float.parseFloat(s[3]) : 0;

						bigDecimal = bigDecimal.add(new BigDecimal(credit));
						bigDecimal = bigDecimal.subtract(new BigDecimal(debit));
						entries.add(new Entry(day, bigDecimal.floatValue()));
					}
				}

				if(entries.size() > 0) {
					for(int i = 0; i < entries.size(); i++) {
						int entriesInDay = 0, day = (int) entries.get(i).getX();
						for(int j = i; j < entries.size() && day == entries.get(j).getX(); j++) {
							entriesInDay++;
						}

						for(int j = i, k = 0; j < entries.size() && day == entries.get(j).getX(); j++, k++) {
							float separationWidth =  1 / (entriesInDay + 1f);//the amount of space between points in the same day (X axis)
							entries.get(j).setX(day + (k+1) * separationWidth);
						}

						while(day == entries.get(i).getX()) i++;
					}

					LineDataSet dataSet = new LineDataSet(entries, null);
					dataSet.setValueFormatter(new MoneyFormatter());

					chart.setData(new LineData(dataSet));
				} else {
					chart.setNoDataText(getString(R.string.nothing_to_graph));
					Snackbar.make(chart, R.string.without_day_data_is_not_charted, Snackbar.LENGTH_LONG).show();
				}
			} else {
				chart.setNoDataText(getString(R.string.nothing_to_graph));
			}

			chart.notifyDataSetChanged();
			chart.invalidate();
		});

		loadMonthAsyncTask.execute();

		chart.getAxisRight().setEnabled(false);
		chart.getXAxis().setValueFormatter(new DeleteNonWholeFormatter());
		chart.getXAxis().setAxisMinimum(0);
		chart.getXAxis().setAxisMaximum(32);
		chart.getXAxis().setDrawGridLines(false);
		chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
		chart.getLegend().setEnabled(false);
		chart.setDescription(null);
		chart.setNoDataText(getString(R.string.loading));
	}

	@Override
	public void onPause() {
		super.onPause();

		if(loadMonthAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
			loadMonthAsyncTask.cancel(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				//NavUtils.navigateUpFromSameTask(this);
				onBackPressed();//To make the MainActivity not destroy itself
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private class MoneyFormatter implements IValueFormatter {
		@Override
		public String getFormattedValue(float value, Entry entry, int dataSetIndex,
		                                ViewPortHandler viewPortHandler) {
			return "$ " + value;
		}
	}

	/**
	 * This formatter deletes non whole values from axis {1, 1.5, 1.75, 2, 3.2}->{1, 2}
	 */
	private class DeleteNonWholeFormatter implements IAxisValueFormatter {
		@Override
		public String getFormattedValue(float value, AxisBase axis) {
			if(value % 1 == 0) {//check if has decimal part
				return String.valueOf((int) value);
			} else {
				return "";
			}
		}
	}

}
