package com.emmanuelmess.simpleaccounting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.Target;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

	public static final String MONTH = "month", YEAR = "year";

	public static int[] MONTH_STRINGS = new int[] {R.string.january, R.string.february, R.string.march, R.string.april, R.string.may,
			R.string.june, R.string.july, R.string.august, R.string.september, R.string.october,
			R.string.november, R.string.december};

	private final String PREFS_NAME = "shared prefs", PREFS_FIRST_RUN = "first_run";

	private TableLayout table = null;
	private FileIO f;
	private final int[] EDIT_IDS = {R.id.editDate, R.id.editRef, R.id.editCredit, R.id.editDebit, R.id.textBalance},
			TEXT_IDS = {R.id.textDate, R.id.textRef, R.id.textCredit, R.id.textDebit};
	private LayoutInflater inflater;
	private ScrollView scrollView;

	//pointer to row being edited
	private int editableRow = -1;
	//pointer to month being viewed
	private static int editableMonth = -1, editableYear = -1;

	private boolean destroyFirst = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		table = (TableLayout) findViewById(R.id.table);
		f = new FileIO(this);

		int loadMonth, loadYear;

		if(getIntent().hasExtra(MONTH)) {
			Bundle b = getIntent().getExtras();
			loadMonth = b.getInt(MONTH);
			loadYear = b.getInt(YEAR);
		} else {
			loadMonth = Integer.parseInt(new SimpleDateFormat("M", Locale.getDefault()).format(new Date())) - 1;
			//YEARS ALREADY START IN 0!!!
			loadYear = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date()));
		}

		table.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
					table.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				else//noinspection deprecation
					table.getViewTreeObserver().removeGlobalOnLayoutListener(this);

				findViewById(R.id.space).setMinimumHeight(findViewById(R.id.fab).getHeight()
						- findViewById(R.id.fab).getPaddingTop());

				loadMonth(loadMonth, loadYear);
			}
		});

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(view->{
			inflater.inflate(R.layout.newrow_main, table);

			scrollView.fullScroll(View.FOCUS_DOWN);

			currentEditableToView();
			editableRow = table.getChildCount() - 1;

			f.newRowInMonth(editableMonth, editableYear);
			View row = loadRow();

			EditText date = (EditText) row.findViewById(R.id.editDate);
			date.setText(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()));

			row.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(date, InputMethodManager.SHOW_IMPLICIT);
		});
	}

	@Override
	public void onBackPressed() {
		if (editableRow != -1)
			currentEditableToView();
		else
			super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.toolbar, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id) {
			case R.id.action_show_months:
				startActivity(new Intent(this, TempMonthActivity.class));
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private int getScreenHeight() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			display.getSize(size);
			return size.y;
		} else {
			return display.getHeight();
		}

	}

	private void loadShowcaseView(LayoutInflater inflater, ScrollView scrollView) {
		SharedPreferences myPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		boolean isFirstRun = myPrefs.getBoolean(PREFS_FIRST_RUN, false);
		if (isFirstRun) { //|| BuildConfig.DEBUG) {

			final int rowToEdit = 1;

			if (table.getChildAt(rowToEdit) == null) {
				inflater.inflate(R.layout.newrow_main, table);

				scrollView.fullScroll(View.FOCUS_DOWN);

				editableRow = rowToEdit;
				View row = table.getChildAt(rowToEdit);

				EditText date = (EditText) row.findViewById(R.id.editDate);
				date.setText(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()));
				EditText ref = (EditText) row.findViewById(R.id.editRef);
				ref.setText(R.string.showcase_example_ref);
				EditText credit = (EditText) row.findViewById(R.id.editCredit);
				credit.setText("0");
				EditText debit = (EditText) row.findViewById(R.id.editDebit);
				debit.setText("100");
				TextView balance = (TextView) row.findViewById(R.id.textBalance);
				balance.setText("$ -100.0");

				currentEditableToView();

				destroyFirst = true;
			}

			Target target = ()->{
				View row = table.getChildAt(rowToEdit);
				int[] location = new int[2];
				row.getLocationInWindow(location);
				return new Point(location[0] + row.getWidth()/2, location[1] + row.getHeight()/2);
			};

			new ShowcaseView.Builder(this)
					.withMaterialShowcase()
					.setTarget(target)
					.setContentTitle(R.string.showcase_main_title)
					.setShowcaseEventListener(new SimpleShowcaseEventListener() {
						@Override
						public void onShowcaseViewHide(ShowcaseView showcaseView) {
							if (destroyFirst) {
								table.removeView(table.getChildAt(rowToEdit));
								editableRow = -1;
							}
						}
					})
					.build();

			SharedPreferences.Editor pref_editor = myPrefs.edit();
			pref_editor.putBoolean(PREFS_FIRST_RUN, false);
			pref_editor.apply();
		}
	}

	private View loadRow() {
		int rowViewIndex = table.getChildCount() - 1, dbIndex = rowViewIndex - 1;
		TableRow row = (TableRow) table.getChildAt(rowViewIndex);
		setListener(rowViewIndex);
		checkStatus(rowViewIndex, row);
		//f.update(dbIndex, FileIO.COLUMNS[4], "$0.0");// TODO: 16/10/2016 needs testing
		addToDB(dbIndex, row);
		return row;
	}

	private void checkStatus(final int index, TableRow row) {
		final EditText debit = (EditText) row.findViewById(R.id.editDebit),
				credit = (EditText) row.findViewById(R.id.editCredit);
		final TextView lastBalance = index > 1? (TextView) table.getChildAt(index - 1).findViewById(R.id.textBalance):null,
				balance = (TextView) row.findViewById(R.id.textBalance);

		TextWatcher watcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void afterTextChanged(Editable editable) {
				if (editableRow == index) {
					balance.setText(lastBalance != null? lastBalance.getText():"$ 0.0");

					BigDecimal balanceNum = new BigDecimal(0);
					balanceNum = balanceNum.add(new BigDecimal(lastBalance != null? parse(lastBalance.getText().toString().substring(1)):0));
					balanceNum = balanceNum.add(new BigDecimal(parse(credit.getText().toString())));
					balanceNum = balanceNum.subtract(new BigDecimal(parse(debit.getText().toString())));

					String s = "$ " + balanceNum.toString();
					balance.setText(s);

					for (int i = index + 1; i < table.getChildCount(); i++) {
						TableRow row = (TableRow) table.getChildAt(i);

						TextView lastBalanceText = (TextView) table.getChildAt(i - 1).findViewById(R.id.textBalance),
								creditText = (TextView) row.findViewById(R.id.textCredit),
								debitText = (TextView) row.findViewById(R.id.textDebit),
								balanceText = (TextView) row.findViewById(R.id.textBalance);

						double b;
						b = parse(lastBalanceText.getText().toString().substring(1));
						b = b + parse(creditText.getText().toString())
								- parse(debitText.getText().toString());

						String str = "$ " + b;
						balanceText.setText(str);
					}
				}
			}
		};

		credit.addTextChangedListener(watcher);
		debit.addTextChangedListener(watcher);
		if (lastBalance != null)
			lastBalance.addTextChangedListener(watcher);
	}

	private double parse(String s) {
		try {
			return Double.valueOf(s);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private void addToDB(final int index, View row) {
		for (int i = 0; i < EDIT_IDS.length - 1; i++) {
			final String rowName = FileIO.COLUMNS[i];
			TextWatcher watcher = new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void afterTextChanged(Editable editable) {
					if (!equal(editable.toString(), ""))
						f.update(index, rowName, editable.toString());
				}
			};

			((TextView) row.findViewById(EDIT_IDS[i])).addTextChangedListener(watcher);
		}
	}

	private void setListener(final int rowIndex) {
		final View row = table.getChildAt(rowIndex);

		row.setOnLongClickListener(v->{
			currentEditableToView();

			for (int i = 0; i < TEXT_IDS.length; i++) {
				TextView t1 = (TextView) row.findViewById(TEXT_IDS[i]);
				EditText t = (EditText) row.findViewById(EDIT_IDS[i]);

				t.setText(t1.getText());
				t1.setText("");

				t1.setVisibility(View.GONE);
				t.setVisibility(View.VISIBLE);
			}
			editableRow = rowIndex;
			return true;
		});
	}

	private void currentEditableToView() {
		if (editableRow != -1) {
			View row = table.getChildAt(editableRow);
			TextView balanceText = ((TextView) row.findViewById(R.id.textBalance));

			if(balanceText.getText() == "") {
				View previousRow = table.getChildAt(editableRow - 1);
				if(previousRow != null) {
					TextView lastBalance = (TextView) previousRow.findViewById(R.id.textBalance);
					balanceText.setText(lastBalance.getText());
				} else
					balanceText.setText("$ 0.0");
			}

			editableRow = -1;

			for (int i = 0; i < TEXT_IDS.length; i++) {
				EditText t = (EditText) row.findViewById(EDIT_IDS[i]);
				TextView t1 = (TextView) row.findViewById(TEXT_IDS[i]);

				t.setOnTouchListener(null);

				t1.setText(t.getText());
				t.setText("");

				t.setVisibility(View.GONE);
				t1.setVisibility(View.VISIBLE);
			}
		}
	}

	private void loadMonth(int month, int year) {
		(new AsyncTask<Void, Void, String[][]>() {
			@Override
			protected void onPreExecute() {
				editableMonth = month;
				editableYear = year;
				((TextView) findViewById(R.id.textMonth)).setText(MONTH_STRINGS[month]);
			}

			@Override
			protected String[][] doInBackground(Void... p) {
				return f.getAllForMonth(month, year);
			}

			@Override
			protected void onPostExecute(String[][] dbRows) {
				float memBalance = 0;
				for (String[] dbRow : dbRows) {
					inflater.inflate(R.layout.newrow_main, table);

					View row = loadRow();

					for (int j = 0; j < TEXT_IDS.length; j++) {
						row.findViewById(EDIT_IDS[j]).setVisibility(View.GONE);

						TextView t = (TextView) row.findViewById(TEXT_IDS[j]);
						t.setVisibility(View.VISIBLE);
						t.setText(dbRow[j]);
					}

					TextView t = (TextView) row.findViewById(R.id.textBalance);
					if (dbRow[2] != null)
						memBalance += Float.valueOf(dbRow[2]);
					if (dbRow[3] != null)
						memBalance -= Float.valueOf(dbRow[3]);

					String s = "$ " + String.valueOf(memBalance);
					t.setText(s);
				}

				scrollView.fullScroll(View.FOCUS_DOWN);

				findViewById(R.id.progressBar).setVisibility(View.GONE);

				loadShowcaseView(inflater, scrollView);
			}
		}).execute();
	}

	private boolean equal(Object o1, Object o2) {
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Objects.equals(o1, o2)) || o1.equals(o2);
	}

}
