package com.emmanuelmess.simpleaccounting.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.dataloading.LoadMonthAsyncTask;
import com.emmanuelmess.simpleaccounting.db.TableGeneral;
import com.emmanuelmess.simpleaccounting.utils.Utils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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

		Date d = new Date();
		int currentMonth = parseInt(new SimpleDateFormat("M", Locale.getDefault()).format(d)) - 1,
				currentYear = parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(d));

		int month = getIntent().getIntExtra(GRAPH_MONTH, currentMonth),
				year = getIntent().getIntExtra(GRAPH_YEAR, currentYear);
		String currency = getIntent().getStringExtra(GRAPH_CURRENCY);
		int[] updateDate = {getIntent().getIntExtra(GRAPH_UPDATE_MONTH, -1),
				getIntent().getIntExtra(GRAPH_UPDATE_YEAR, -1)};

		((TextView) findViewById(R.id.title))
				.setText(Utils.getTitle(this, month, year, currency, updateDate));

		new LoadMonthAsyncTask(month, year, currency, new TableGeneral(this), (result) -> {
			if(result.first.length > 0) {
				List<Entry> entries = new ArrayList<>();

				BigDecimal bigDecimal = BigDecimal.ZERO;

				for (String[] s : result.first) {
					float credit = s[2] != null? Float.parseFloat(s[2]):0,
							debit = s[3] != null? Float.parseFloat(s[3]):0;

					bigDecimal = bigDecimal.add(new BigDecimal(credit));
					bigDecimal = bigDecimal.subtract(new BigDecimal(debit));
					entries.add(new Entry(parseInt(s[0]), bigDecimal.floatValue()));
				}

				LineDataSet dataSet = new LineDataSet(entries, null);
				dataSet.setValueFormatter(new MoneyFormatter());

				chart.setData(new LineData(dataSet));
			} else {
				chart.setNoDataText(getString(R.string.nothing_to_graph));
			}

			chart.notifyDataSetChanged();
			chart.invalidate();
		}).execute();

		chart.getAxisRight().setEnabled(false);
		chart.getXAxis().setDrawGridLines(false);
		chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
		chart.getLegend().setEnabled(false);
		chart.setDescription(null);
		chart.setNoDataText(getString(R.string.loading));
	}

	private class MoneyFormatter implements IValueFormatter {
		@Override
		public String getFormattedValue(float value, Entry entry, int dataSetIndex,
		                                ViewPortHandler viewPortHandler) {
			return "$ " + value;
		}
	}

}
