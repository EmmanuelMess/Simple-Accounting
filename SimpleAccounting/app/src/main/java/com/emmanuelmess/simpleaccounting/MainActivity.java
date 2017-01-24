package com.emmanuelmess.simpleaccounting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.print.PrintManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.emmanuelmess.simpleaccounting.activities.TempMonthActivity;
import com.emmanuelmess.simpleaccounting.dataloading.AsyncFinishedListener;
import com.emmanuelmess.simpleaccounting.dataloading.LoadMonthAsyncTask;
import com.emmanuelmess.simpleaccounting.dataloading.LoadPrevBalanceAsyncTask;
import com.emmanuelmess.simpleaccounting.db.TableGeneral;
import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.Target;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.emmanuelmess.simpleaccounting.Utils.equal;
import static com.emmanuelmess.simpleaccounting.Utils.parseString;
import static com.emmanuelmess.simpleaccounting.Utils.parseView;
import static com.emmanuelmess.simpleaccounting.Utils.parseViewToString;

/**
 * @author Emmanuel
 */
public class MainActivity extends AppCompatActivity implements AsyncFinishedListener<ArrayList<Integer>> {

	public static final String UPDATE_YEAR_SETTING = "update 1.2 year";
	public static final String UPDATE_MONTH_SETTING = "update 1.2 month";

	public static final String MONTH = "month", YEAR = "year";

	public static int[] MONTH_STRINGS = new int[] {R.string.january, R.string.february, R.string.march, R.string.april, R.string.may,
			R.string.june, R.string.july, R.string.august, R.string.september, R.string.october,
			R.string.november, R.string.december};

	private final String PREFS_NAME = "shared prefs", PREFS_FIRST_RUN = "first_run";

	public static final int[] EDIT_IDS = {R.id.editDate, R.id.editRef, R.id.editCredit, R.id.editDebit, R.id.textBalance};
	public static final int[] TEXT_IDS = {R.id.textDate, R.id.textRef, R.id.textCredit, R.id.textDebit};

	private int FIRST_REAL_ROW = 1;//excluding header and previous balance. HAS 2 STATES: 1 & 2
	
	private TableLayout table = null;
	private View space;
	private FloatingActionButton fab;
	private TableGeneral tableGeneral;
	private TableMonthlyBalance tableMonthlyBalance;
	private LayoutInflater inflater;
	private ScrollView scrollView;
	private AsyncTask<Void, Void, String[][]> loadingMonthTask = null;

	private int updateYear, updateMonth;

	//pointer to row being edited STARTS IN 1
	private int editableRow = -1;
	private boolean editedColumn[] = new boolean[4];

	/**
	 * Pointer to month being viewed
	 * CAN BE -1, -2 OR >=0.
	 * -1: no value
	 * -2: older that update 1.2
	 * >=0: 'normal' (month or year) value
	 */
	private static int editableMonth = -1, editableYear = -1;
	private static boolean dateChanged = false;

	private ArrayList<Integer> rowToDBRowConversion = new ArrayList<>();

	private boolean destroyFirst = false;
	private boolean reloadMonthOnChangeToView = false;
	private boolean createNewRowWhenMonthLoaded = false;

	public static void setDate(int month, int year) {
		editableMonth = month;
		editableYear = year;
		dateChanged = true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		table = (TableLayout) findViewById(R.id.table);
		tableGeneral = new TableGeneral(this);//DO NOT change the order of table creation!
		tableMonthlyBalance = new TableMonthlyBalance(this);

		Date d = new Date();
		int[] currentMonthYear = {Integer.parseInt(new SimpleDateFormat("M", Locale.getDefault()).format(d)) - 1,
		//YEARS ALREADY START IN 0!!!
		Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(d))};

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

		if(!preferences.contains(UPDATE_YEAR_SETTING)) {
			SharedPreferences.Editor prefEditor = preferences.edit();
			prefEditor.putInt(UPDATE_MONTH_SETTING, currentMonthYear[0]);
			prefEditor.putInt(UPDATE_YEAR_SETTING, currentMonthYear[1]);
			prefEditor.apply();
		}

		updateYear = preferences.getInt(UPDATE_YEAR_SETTING, -1);
		updateMonth = preferences.getInt(UPDATE_MONTH_SETTING, -1);

		int loadMonth, loadYear;

		if(getIntent().hasExtra(MONTH)) {
			Bundle b = getIntent().getExtras();
			loadMonth = b.getInt(MONTH);
			loadYear = b.getInt(YEAR);
		} else {
			loadMonth = currentMonthYear[0];
			loadYear = currentMonthYear[1];
		}

		table.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
					table.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				else//noinspection deprecation
					table.getViewTreeObserver().removeGlobalOnLayoutListener(this);

				space = findViewById(R.id.space);
				space.setMinimumHeight(findViewById(R.id.fab).getHeight()
							- findViewById(R.id.fab).getPaddingTop());

				loadMonth(loadMonth, loadYear);
				dateChanged = false;//in case the activity gets destroyed
			}
		});

		fab = (FloatingActionButton) findViewById(R.id.fab);
		if(editableMonth == TableGeneral.OLDER_THAN_UPDATE && editableYear == TableGeneral.OLDER_THAN_UPDATE) {
			fab.setVisibility(GONE);
			space.setVisibility(GONE);
		}

		fab.setOnClickListener(view->{
			inflater.inflate(R.layout.newrow_main, table);

			scrollView.fullScroll(View.FOCUS_DOWN);

			currentEditableToView();
			if (table.getChildCount() > FIRST_REAL_ROW) {
				updateEditableRow(table.getChildCount() - 1);

				tableGeneral.newRowInMonth(editableMonth, editableYear);
				rowToDBRowConversion.add(tableGeneral.getLastIndex());
				View row = loadRow();
				addToMonthsDB();

				EditText date = (EditText) row.findViewById(R.id.editDate);
				date.setText(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()));

				row.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(date, InputMethodManager.SHOW_IMPLICIT);
			} else createNewRowWhenMonthLoaded = true;
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(dateChanged) {
			loadMonth(editableMonth, editableYear);

			fab.setVisibility(editableMonth == TableGeneral.OLDER_THAN_UPDATE
					&& editableYear == TableGeneral.OLDER_THAN_UPDATE? GONE : VISIBLE);

			dateChanged = false;
		}
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
		getMenuInflater().inflate(R.menu.toolbar, menu);
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT)
			menu.removeItem(R.id.action_print);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id) {
			case R.id.action_currency:
				Toast.makeText(this, "To do", Toast.LENGTH_LONG).show();
				return true;
			case R.id.action_show_months:
				startActivity(new Intent(this, TempMonthActivity.class));
				return true;
			case R.id.action_print:
				if (table.getChildCount() > 1) {
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
						PrintManager printM = (PrintManager) getSystemService((Context.PRINT_SERVICE));

						String job = getString(R.string.app_name) + ": " +
								(editableMonth != TableGeneral.OLDER_THAN_UPDATE? getString(MONTH_STRINGS[editableMonth]):updateMonth);
						printM.print(job,
								new PPrintDocumentAdapter(this, table, editableMonth, editableYear,
										new int[] {updateMonth, updateYear}),
								null);
					}
				} else {
					Toast.makeText(this, getString(R.string.nothing_to_print), Toast.LENGTH_SHORT).show();
				}

				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public View loadRow() {
		int rowViewIndex = table.getChildCount() - 1;
		TableRow row = (TableRow) table.getChildAt(rowViewIndex);

		setListener(rowViewIndex);
		checkEditInBalance(rowViewIndex, row);
		checkDateChanged(rowViewIndex, row);
		addToDB(row);
		return row;
	}

	public int getFirstRealRow() {
		return FIRST_REAL_ROW;
	}
	public void setFirstRealRow(int firstRealRow) {
		this.FIRST_REAL_ROW = firstRealRow;
	}

	private void checkEditInBalance(final int index, TableRow row) {
		final EditText debit = (EditText) row.findViewById(R.id.editDebit),
				credit = (EditText) row.findViewById(R.id.editCredit);

		final TextView lastBalance = index > 1? (TextView) table.getChildAt(index - 1).findViewById(R.id.textBalance) : null;
		final TextView balance = (TextView) row.findViewById(R.id.textBalance);

		TextWatcher watcher = new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable editable) {
				if (editableRow == index) {
					if (equal(credit.getText().toString(), "."))
						credit.setText("0");

					if (equal(debit.getText().toString(), "."))
						debit.setText("0");

					BigDecimal balanceNum = (lastBalance != null?
							parseString(parseViewToString(lastBalance).substring(2)) : BigDecimal.ZERO)
							.add(parseView(credit))
							.subtract(parseView(debit));

					if (balanceNum.compareTo(BigDecimal.ZERO) == 0)
						balanceNum = balanceNum.setScale(1, BigDecimal.ROUND_UNNECESSARY);

					String s = "$ " + balanceNum.toPlainString();
					if(equal(s, "$ "))
						throw new IllegalStateException();
					balance.setText(s);

					updateBalances(index+1, balanceNum);
				}
			}

			private void updateBalances(int index, BigDecimal lastBalance) {
				TableRow row = (TableRow) table.getChildAt(index);
				if(row == null)
					return;

				TextView creditText = (TextView) row.findViewById(R.id.textCredit),
						debitText = (TextView) row.findViewById(R.id.textDebit),
						balanceText = (TextView) row.findViewById(R.id.textBalance);

				lastBalance = lastBalance
						.add(parseView(creditText))
						.subtract(parseView(debitText));

				if (lastBalance.compareTo(BigDecimal.ZERO) == 0)
					lastBalance = lastBalance.setScale(1, BigDecimal.ROUND_UNNECESSARY);

				String s = "$ " + lastBalance.toPlainString();
				if(equal(s, "$ "))
					throw new IllegalStateException();
				balanceText.setText(s);

				if(index+1 < row.getChildCount())
					updateBalances(index+1, lastBalance);
			}
		};

		credit.addTextChangedListener(watcher);
		debit.addTextChangedListener(watcher);
		if (lastBalance != null)
			lastBalance.addTextChangedListener(watcher);
	}

	void checkDateChanged(final int index, TableRow row) {
		final EditText DATE = (EditText) row.findViewById(R.id.editDate);

		TextWatcher watcher = new SimpleTextWatcher() {
			String mem = "";

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				if(equal(mem, ""))
					mem = s.toString();
			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (editableRow == index && !equal(mem, ""))
					reloadMonthOnChangeToView = !equal(mem, editable.toString());
			}
		};

		DATE.addTextChangedListener(watcher);
	}

	private void addToDB(View row) {
		for (int i = 0; i < EDIT_IDS.length - 1; i++) {
			final int colIndex = i;
			TextWatcher watcher = new SimpleTextWatcher() {
				@Override
				public void afterTextChanged(Editable editable) {
					editedColumn[colIndex] = true;
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

				t1.setVisibility(GONE);
				t.setVisibility(VISIBLE);
			}
			updateEditableRow(rowIndex);
			return true;
		});
	}

	private void currentEditableToView() {
		if (editableRow != -1) {
			View row = table.getChildAt(editableRow);

			for (int i = 0; i < EDIT_IDS.length - 1; i++) {
				if(editedColumn[i]) {
					String t = ((EditText) row.findViewById(EDIT_IDS[i])).getText().toString();
					tableGeneral.update(rowToDBRowConversion.get(editableRow-FIRST_REAL_ROW),
							TableGeneral.COLUMNS[i], (!equal(t, "")? t : null));
				}
			}

			TextView balanceText = ((TextView) row.findViewById(R.id.textBalance));

			if(balanceText != null && balanceText.getText() == "") {
				View previousRow = editableRow-1 == 0? null : table.getChildAt(editableRow - 1);
				TextView lastBalance;
				if(previousRow != null && (lastBalance = (TextView) previousRow.findViewById(R.id.textBalance)) != null)
					balanceText.setText(lastBalance.getText());
				else
					balanceText.setText("$ 0.0");
			}

			updateEditableRow(-1);

			for (int i = 0; i < TEXT_IDS.length; i++) {
				EditText t = (EditText) row.findViewById(EDIT_IDS[i]);
				TextView t1 = (TextView) row.findViewById(TEXT_IDS[i]);

				t.setOnTouchListener(null);

				t1.setText(t.getText());
				t.setText("");

				t.setVisibility(GONE);
				t1.setVisibility(VISIBLE);
			}

			if(reloadMonthOnChangeToView){
				reloadMonthOnChangeToView = false;
				loadMonth(editableMonth, editableYear);
			}
		}
	}

	private void loadMonth(int month, int year) {
		findViewById(R.id.progressBar).setVisibility(VISIBLE);

		FIRST_REAL_ROW = 1;

		if(table.getChildCount() > 1)
			for(int i = table.getChildCount()-1; i > 0; i--)
				table.removeViewAt(i);

		tableGeneral.getReadableDatabase();//triggers onUpdate()

		loadingMonthTask = new LoadMonthAsyncTask(month, year, tableGeneral, tableMonthlyBalance, table,
				inflater, this, this);

		editableMonth = month;
		editableYear = year;

		TextView monthText = (TextView) findViewById(R.id.textMonth);

		if(month != -1 && year != TableGeneral.OLDER_THAN_UPDATE) {
			((TextView) findViewById(R.id.textMonth)).setText(MONTH_STRINGS[month]);

			(new LoadPrevBalanceAsyncTask(month, year, tableMonthlyBalance, table, inflater,
					rowToDBRowConversion1->loadingMonthTask.execute(), this)).execute();
		} else {
			monthText.setText(getString(R.string.before_update_1_2)
					+ " " + getString(MONTH_STRINGS[updateMonth]).toLowerCase() + "-" + updateYear);
			loadingMonthTask.execute();
		}
	}

	@Override
	public void OnAsyncFinished(ArrayList<Integer> rowToDBRowConversion) {
		this.rowToDBRowConversion = rowToDBRowConversion;
		addToMonthsDB();

		scrollView.fullScroll(View.FOCUS_DOWN);

		findViewById(R.id.progressBar).setVisibility(GONE);

		loadShowcaseView(inflater, scrollView);

		if(createNewRowWhenMonthLoaded) {
			inflater.inflate(R.layout.newrow_main, table);

			scrollView.fullScroll(View.FOCUS_DOWN);

			currentEditableToView();
			editableRow = table.getChildCount() - 1;

			tableGeneral.newRowInMonth(editableMonth, editableYear);
			this.rowToDBRowConversion.add(tableGeneral.getLastIndex());
			View row = loadRow();
			addToMonthsDB();

			EditText date = (EditText) row.findViewById(R.id.editDate);
			date.setText(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()));

			row.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(date, InputMethodManager.SHOW_IMPLICIT);
			createNewRowWhenMonthLoaded = false;
		}
	}

	private void addToMonthsDB() {
		if(table.getChildCount()-1 >= FIRST_REAL_ROW) {
			View row = table.getChildAt(table.getChildCount()-1);

			TextWatcher watcher = new SimpleTextWatcher() {
				@Override
				public void afterTextChanged(Editable editable) {
					tableMonthlyBalance.updateMonth(editableMonth, editableYear, Double.parseDouble(editable.toString().substring(1)));
				}
			};
			((TextView) row.findViewById(R.id.textBalance)).addTextChangedListener(watcher);
		}
	}

	private void loadShowcaseView(LayoutInflater inflater, ScrollView scrollView) {
		SharedPreferences myPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		boolean isFirstRun = myPrefs.getBoolean(PREFS_FIRST_RUN, false);
		if (isFirstRun) {//|| BuildConfig.DEBUG) {

			final int rowToEdit = FIRST_REAL_ROW;

			if (table.getChildAt(rowToEdit) == null) {
				inflater.inflate(R.layout.newrow_main, table);

				scrollView.fullScroll(View.FOCUS_DOWN);

				updateEditableRow(rowToEdit);
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
  
  private void updateEditableRow(int value) {
		if(value == -1)
			ACRAHelper.reset();
		else
			ACRAHelper.writeData(table, value, this);

		editableRow = value;
	}
	
	private class SimpleTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

		@Override
		public void afterTextChanged(Editable editable) {}
	}

	public void debugChangeDate(int month, int year) {
		if(BuildConfig.DEBUG)
			loadMonth(month, year);
	}

}
