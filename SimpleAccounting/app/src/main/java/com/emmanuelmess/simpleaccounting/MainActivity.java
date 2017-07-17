package com.emmanuelmess.simpleaccounting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.print.PrintManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.activities.DonateActivity;
import com.emmanuelmess.simpleaccounting.activities.GraphActivity;
import com.emmanuelmess.simpleaccounting.activities.SettingsActivity;
import com.emmanuelmess.simpleaccounting.activities.TempMonthActivity;
import com.emmanuelmess.simpleaccounting.activities.dialogs.CurrencyPicker;
import com.emmanuelmess.simpleaccounting.activities.views.SpinnerNoUnwantedOnClick;
import com.emmanuelmess.simpleaccounting.dataloading.AsyncFinishedListener;
import com.emmanuelmess.simpleaccounting.dataloading.LoadMonthAsyncTask;
import com.emmanuelmess.simpleaccounting.dataloading.LoadPrevBalanceAsyncTask;
import com.emmanuelmess.simpleaccounting.db.TableGeneral;
import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance;
import com.emmanuelmess.simpleaccounting.utils.ACRAHelper;
import com.emmanuelmess.simpleaccounting.utils.TinyDB;
import com.emmanuelmess.simpleaccounting.utils.Utils;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.Target;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.emmanuelmess.simpleaccounting.activities.SettingsActivity.INVERT_CREDIT_DEBIT_SETTING;
import static com.emmanuelmess.simpleaccounting.activities.dialogs.CurrencyPicker.DFLT;
import static com.emmanuelmess.simpleaccounting.utils.Utils.equal;
import static com.emmanuelmess.simpleaccounting.utils.Utils.parseString;
import static com.emmanuelmess.simpleaccounting.utils.Utils.parseView;
import static com.emmanuelmess.simpleaccounting.utils.Utils.parseViewToString;

/**
 * @author Emmanuel
 */
public class MainActivity extends AppCompatActivity
		implements AsyncFinishedListener<Pair<String[][], ArrayList<Integer>>> {

	public static final String UPDATE_YEAR_SETTING = "update 1.2 year";
	public static final String UPDATE_MONTH_SETTING = "update 1.2 month";


	public static int[] MONTH_STRINGS = new int[]{R.string.january, R.string.february, R.string.march, R.string.april, R.string.may,
			R.string.june, R.string.july, R.string.august, R.string.september, R.string.october, R.string.november, R.string.december};

	private static final String PREFS_NAME = "shared prefs", PREFS_FIRST_RUN = "first_run";

	//THESE COULD NOT BE IN ORDER (because of posible inversion between credit and debit)
	public static final int[] EDIT_IDS = {R.id.editDate, R.id.editRef, R.id.editCredit, R.id.editDebit, R.id.textBalance};
	public static final int[] TEXT_IDS = {R.id.textDate, R.id.textRef, R.id.textCredit, R.id.textDebit};

	private static boolean invertCreditDebit = false;

	private int FIRST_REAL_ROW = 1;//excluding header and previous balance. HAS 2 STATES: 1 & 2
	private int DEFAULT_CURRENCY = 0;

	private TableLayout table = null;
	private View space;
	private FloatingActionButton fab;
	private TableGeneral tableGeneral;
	private TableMonthlyBalance tableMonthlyBalance;
	private LayoutInflater inflater;
	private ScrollView scrollView;
	private AsyncTask<Void, Void, Pair<String[][], ArrayList<Integer>>> loadingMonthTask = null;
	private AsyncTask<Void, Void, Double> loadPrevBalance = null;

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
	private static String editableCurrency = "";//same as currencyName, except when it is the default in that case it is ""
	private static String currencyName = "";// the string the user entered on PreferenceActivity

	private static boolean invalidateTable = false, invalidateToolbar = false;

	private ArrayList<Integer> rowToDBRowConversion = new ArrayList<>();

	private boolean destroyFirst = false;
	private boolean reloadMonthOnChangeToView = false;
	private boolean createNewRowWhenMonthLoaded = false;

	public static void setDate(int month, int year) {
		editableMonth = month;
		editableYear = year;
		invalidateTable();
	}

	public static String getCurrency() {
		return editableCurrency;
	}

	public static void setCurrency(String currency) {
		editableCurrency = currency;
		//invalidateTable(); TODO why is this unnecessary?
	}

	public static void invalidateTable() {
		invalidateTable = true;
	}

	public static void invalidateToolbar() {
		invalidateToolbar = true;
	}

	public static void invalidateTableHeader(boolean invertCreditDebit) {
		if (MainActivity.invertCreditDebit != invertCreditDebit)
			MainActivity.invertCreditDebit = invertCreditDebit;
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

		SharedPreferences preferences = getDefaultSharedPreferences(this);

		if (!preferences.contains(UPDATE_YEAR_SETTING)) {
			SharedPreferences.Editor prefEditor = preferences.edit();
			prefEditor.putInt(UPDATE_MONTH_SETTING, currentMonthYear[0]);
			prefEditor.putInt(UPDATE_YEAR_SETTING, currentMonthYear[1]);
			prefEditor.apply();
		}

		updateMonth = preferences.getInt(UPDATE_MONTH_SETTING, -1);
		updateYear = preferences.getInt(UPDATE_YEAR_SETTING, -1);

		editableMonth = currentMonthYear[0];
		editableYear = currentMonthYear[1];

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

				loadMonth(editableMonth, editableYear, editableCurrency);
				//in case the activity gets destroyed
				invalidateTable = false;
				invalidateToolbar = false;
			}
		});

		fab = (FloatingActionButton) findViewById(R.id.fab);
		if (isSelectedMonthOlderThanUpdate()) {
			fab.setVisibility(GONE);
			space.setVisibility(GONE);
		}

		fab.setOnClickListener(view->{
			inflateNewRow();
			scrollView.fullScroll(View.FOCUS_DOWN);

			currentEditableToView();
			if (table.getChildCount() > FIRST_REAL_ROW) {
				updateEditableRow(table.getChildCount() - 1);

				tableGeneral.newRowInMonth(editableMonth, editableYear, editableCurrency);
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
		if (invertCreditDebit !=
				getDefaultSharedPreferences(this).getBoolean(INVERT_CREDIT_DEBIT_SETTING, false)) {

			int tempId = 0;

			table.findViewById(R.id.credit).setId(tempId);
			table.findViewById(R.id.debit).setId(R.id.credit);
			table.findViewById(tempId).setId(R.id.debit);

			((TextView) findViewById(R.id.credit)).setText(R.string.credit);
			((TextView) findViewById(R.id.debit)).setText(R.string.debit);

			invertCreditDebit =
					getDefaultSharedPreferences(this).getBoolean(INVERT_CREDIT_DEBIT_SETTING, false);
		}
		if (invalidateTable && (loadingMonthTask == null || loadingMonthTask.getStatus() != AsyncTask.Status.RUNNING)) {
			loadMonth(editableMonth, editableYear, editableCurrency);

			fab.setVisibility(isSelectedMonthOlderThanUpdate()? GONE:VISIBLE);

			invalidateTable = false;
		}
		if (invalidateToolbar) {
			invalidateOptionsMenu();

			invalidateToolbar = false;
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

		TinyDB tinyDB = new TinyDB(this);
		ArrayList<String> currencies = tinyDB.getListString(CurrencyPicker.KEY); //DO NOT save this List (first item changed)

		if (currencies.size() != 0 && !isSelectedMonthOlderThanUpdate()) {
			if(Utils.equal(currencies.get(0), DFLT))
				currencies.set(0, getString(R.string.default_short));

			MenuItem item = menu.findItem(R.id.action_currency);
			SpinnerNoUnwantedOnClick spinner =
					new SpinnerNoUnwantedOnClick(MenuItemCompat.getActionView(item));
			ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item,
					currencies.toArray(new String[currencies.size()]));
			adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
			spinner.setAdapter(adapter);
			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if(editableRow != -1)
						currentEditableToView();

					if (pos == DEFAULT_CURRENCY)
						editableCurrency = "";
					else
						editableCurrency = ((TextView) view).getText().toString();

					currencyName = Utils.equal(editableCurrency, "")?
							((TextView) view).getText().toString():editableCurrency;//repeated code at end of lambda

					loadMonth(editableMonth, editableYear, editableCurrency);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});

			currencyName = Utils.equal(editableCurrency, "")? currencies.get(0):editableCurrency;
		} else {
			menu.removeItem(R.id.action_currency);

			if(isSelectedMonthOlderThanUpdate()) editableCurrency = "";//make sure
		}

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
			case R.id.action_graph:
				Intent i = new Intent(getApplicationContext(), GraphActivity.class);
				i.putExtra(GraphActivity.GRAPH_MONTH, editableMonth);
				i.putExtra(GraphActivity.GRAPH_YEAR, editableYear);
				i.putExtra(GraphActivity.GRAPH_CURRENCY, editableCurrency);
				i.putExtra(GraphActivity.GRAPH_UPDATE_MONTH, updateMonth);
				i.putExtra(GraphActivity.GRAPH_UPDATE_YEAR, updateYear);
				startActivity(i);
				return true;
			case R.id.action_show_months:
				startActivity(new Intent(this, TempMonthActivity.class));
				return true;
			case R.id.action_print:
				if (table.getChildCount() > 1) {
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
						PrintManager printM = (PrintManager) getSystemService((Context.PRINT_SERVICE));

						String job = getString(R.string.app_name) + ": " +
								(!isSelectedMonthOlderThanUpdate()?
										getString(MONTH_STRINGS[editableMonth]):getString(MONTH_STRINGS[updateMonth]));
						printM.print(job,
								new PPrintDocumentAdapter(this, table, editableMonth, editableYear,
										currencyName, new int[]{updateMonth, updateYear}),
								null);
					}
				} else {
					Snackbar.make(table, getString(R.string.nothing_to_print), Snackbar.LENGTH_SHORT).show();
				}

				return true;
			case R.id.action_donate:
				startActivity(new Intent(getApplicationContext(), DonateActivity.class));
				return true;
			case R.id.action_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				return true;
			case R.id.action_feedback:
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
						Uri.fromParts("mailto", getString(R.string.email), null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT,
						getString(R.string.feedback_about, getString(R.string.app_name)));
				emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_content));
				emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.email)});//makes it work in 4.3
				Intent chooser = Intent.createChooser(emailIntent, getString(R.string.choose_email));
				chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(chooser);//prevents exception
				startActivity(chooser);
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

		TextView lastBalance = index > 1? (TextView) table.getChildAt(index - 1).findViewById(R.id.textBalance):null;
		TextView balance = (TextView) row.findViewById(R.id.textBalance);

		TextWatcher watcher = new Utils.SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable editable) {
				if (editableRow == index) {
					if (equal(credit.getText().toString(), "."))
						credit.setText("0");

					if (equal(debit.getText().toString(), "."))
						debit.setText("0");

					BigDecimal balanceNum = (lastBalance != null?
							parseString(parseViewToString(lastBalance).substring(2)):BigDecimal.ZERO)
							.add(parseView(credit))
							.subtract(parseView(debit));

					if (balanceNum.compareTo(BigDecimal.ZERO) == 0)
						balanceNum = balanceNum.setScale(1, BigDecimal.ROUND_UNNECESSARY);

					String s = "$ " + balanceNum.toPlainString();
					if (equal(s, "$ "))
						throw new IllegalStateException();
					balance.setText(s);

					updateBalances(index + 1, balanceNum);
				}
			}

			private void updateBalances(int index, BigDecimal lastBalance) {
				TableRow row = (TableRow) table.getChildAt(index);
				if (row == null)
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
				if (equal(s, "$ "))
					throw new IllegalStateException();
				balanceText.setText(s);

				if (index + 1 < row.getChildCount())
					updateBalances(index + 1, lastBalance);
			}
		};

		credit.addTextChangedListener(watcher);
		debit.addTextChangedListener(watcher);
		if (lastBalance != null)
			lastBalance.addTextChangedListener(watcher);
	}

	void checkDateChanged(final int index, TableRow row) {
		final EditText DATE = (EditText) row.findViewById(R.id.editDate);

		TextWatcher watcher = new Utils.SimpleTextWatcher() {
			String mem = "";

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				if (equal(mem, ""))
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
			TextWatcher watcher = new Utils.SimpleTextWatcher() {
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
		View row = table.getChildAt(editableRow);
		if (row != null && editableRow >= 0) {//TODO change to editableRow != -1 if this is problematic
			for (int i = 0; i < EDIT_IDS.length - 1; i++) {
				if (editedColumn[i]) {
					String t = ((EditText) row.findViewById(EDIT_IDS[i])).getText().toString();
					tableGeneral.update(rowToDBRowConversion.get(editableRow - FIRST_REAL_ROW),
							TableGeneral.COLUMNS[i], (!equal(t, "")? t:null));
				}
			}

			TextView balanceText = ((TextView) row.findViewById(R.id.textBalance));

			if (balanceText != null && balanceText.getText() == "") {
				View previousRow = editableRow - 1 == 0? null:table.getChildAt(editableRow - 1);
				TextView lastBalance;
				if (previousRow != null && (lastBalance = (TextView) previousRow.findViewById(R.id.textBalance)) != null)
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

			if (reloadMonthOnChangeToView) {
				reloadMonthOnChangeToView = false;
				loadMonth(editableMonth, editableYear, editableCurrency);
			}
		}
	}

	private void loadMonth(int month, int year, String currency) {
		if(!LoadMonthAsyncTask.isAlreadyLoading()) {
			findViewById(R.id.progressBar).setVisibility(VISIBLE);

			FIRST_REAL_ROW = 1;

			if (table.getChildCount() > 1)
				for (int i = table.getChildCount() - 1; i > 0; i--)
					table.removeViewAt(i);

			tableGeneral.getReadableDatabase();//triggers onUpdate()

			loadingMonthTask = new LoadMonthAsyncTask(month, year, currency, tableGeneral, this);

			editableMonth = month;
			editableYear = year;
			editableCurrency = currency;

			TextView monthText = (TextView) findViewById(R.id.textMonth);

			if (month != -1 && !isSelectedMonthOlderThanUpdate()) {
				((TextView) findViewById(R.id.textMonth)).setText(MONTH_STRINGS[month]);

				loadPrevBalance = new LoadPrevBalanceAsyncTask(month, year, editableCurrency, tableMonthlyBalance,
						(lastMonthData) -> {
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
								setFirstRealRow(2);
							}
							loadingMonthTask.execute();
						});

				loadPrevBalance.execute();
			} else {
				monthText.setText(getString(R.string.before_update_1_2)
						+ " " + getString(MONTH_STRINGS[updateMonth]).toLowerCase() + "-" + updateYear);
				loadingMonthTask.execute();
			}
		} else if(editableMonth != month || editableYear != year || !Utils.equal(editableCurrency, currency)) {
			loadPrevBalance.cancel(true);
			loadingMonthTask.cancel(true);
		}
	}

	@Override
	public void OnAsyncFinished(Pair<String[][], ArrayList<Integer>> dbRowsPairedRowToDBConversion) {
		if(table.getChildCount() - getFirstRealRow() > 0)
			throw new IllegalArgumentException("Table already contains "
					+ (table.getChildCount() - getFirstRealRow()) + " elements; " +
					"delete all rows before executing LoadMonthAsyncTask!");

		BigDecimal memBalance = BigDecimal.ZERO;

		if (getFirstRealRow() == 2) {
			memBalance = memBalance.add(Utils.parseString(
					((TextView) table.getChildAt(1).findViewById(R.id.textBalance))
							.getText().toString().substring(2)));
		}

		for (String[] dbRow : dbRowsPairedRowToDBConversion.first) {
			inflateNewRow();

			View row = loadRow();

			int[] textIds = MainActivity.TEXT_IDS;
			int[] editIds = MainActivity.EDIT_IDS;

			for (int j = 0; j < textIds.length; j++) {
				row.findViewById(editIds[j]).setVisibility(View.GONE);

				TextView t = (TextView) row.findViewById(textIds[j]);
				t.setVisibility(View.VISIBLE);
				t.setText(dbRow[j]);
			}

			TextView t = (TextView) row.findViewById(R.id.textBalance);
			if (dbRow[2] != null)
				memBalance = memBalance.add(Utils.parseString(dbRow[2]));
			if (dbRow[3] != null)
				memBalance = memBalance.subtract(Utils.parseString(dbRow[3]));

			String s = "$ " + String.valueOf(memBalance);
			t.setText(s);
		}

		this.rowToDBRowConversion = dbRowsPairedRowToDBConversion.second;
		addToMonthsDB();

		scrollView.fullScroll(View.FOCUS_DOWN);

		findViewById(R.id.progressBar).setVisibility(GONE);

		loadShowcaseView(inflater, scrollView);

		if (createNewRowWhenMonthLoaded && table != null) {
			inflateNewRow();

			scrollView.fullScroll(View.FOCUS_DOWN);

			currentEditableToView();
			editableRow = table.getChildCount() - 1;

			tableGeneral.newRowInMonth(editableMonth, editableYear, editableCurrency);
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
		if (table.getChildCount() - 1 >= FIRST_REAL_ROW) {
			View row = table.getChildAt(table.getChildCount() - 1);

			TextWatcher watcher = new Utils.SimpleTextWatcher() {
				@Override
				public void afterTextChanged(Editable editable) {
					double balance =
							editable.toString().length() > 1?
									Double.parseDouble(editable.toString().substring(1)):0;

					tableMonthlyBalance.updateMonth(editableMonth, editableYear, editableCurrency,
							balance);
				}
			};
			((TextView) row.findViewById(R.id.textBalance)).addTextChangedListener(watcher);
		}
	}

	private void loadShowcaseView(LayoutInflater inflater, ScrollView scrollView) {
		SharedPreferences myPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		boolean isFirstRun = myPrefs.getBoolean(PREFS_FIRST_RUN, true);
		if (isFirstRun) { //|| BuildConfig.DEBUG) {

			final int rowToEdit = FIRST_REAL_ROW;

			if (table.getChildAt(rowToEdit) == null) {
				inflateNewRow();

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

	private void inflateNewRow() {
		inflater.inflate(R.layout.newrow_main, table);

		if (invertCreditDebit) {
			View r = table.getChildAt(table.getChildCount() - 1);

			r.findViewById(R.id.textCredit).setId(0);
			r.findViewById(R.id.textDebit).setId(R.id.textCredit);
			r.findViewById(0).setId(R.id.textDebit);

			r.findViewById(R.id.editCredit).setId(0);
			r.findViewById(R.id.editDebit).setId(R.id.editCredit);
			r.findViewById(0).setId(R.id.editDebit);

			((EditText) r.findViewById(R.id.editCredit)).setHint(R.string.credit);
			((EditText) r.findViewById(R.id.editDebit)).setHint(R.string.debit);
		}
	}

	private void updateEditableRow(int value) {
		if (value == -1 || table == null)
			ACRAHelper.reset();
		else
			ACRAHelper.writeData(table, editableYear, editableMonth);

		editableRow = value;
	}

	/**
	 * The user should NEVER be allowed to edit in any way if this is true
	 *
	 * @return if the month selected is older than the update that added month selection
	 */
	private boolean isSelectedMonthOlderThanUpdate() {
		return editableMonth == TableGeneral.OLDER_THAN_UPDATE
				|| editableYear == TableGeneral.OLDER_THAN_UPDATE;
	}

}
