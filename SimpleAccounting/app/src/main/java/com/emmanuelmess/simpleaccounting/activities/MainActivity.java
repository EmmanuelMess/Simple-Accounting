package com.emmanuelmess.simpleaccounting.activities;

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
import android.widget.Spinner;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.PPrintDocumentAdapter;
import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.activities.preferences.CurrencyPicker;
import com.emmanuelmess.simpleaccounting.activities.views.LedgerRow;
import com.emmanuelmess.simpleaccounting.activities.views.LedgerView;
import com.emmanuelmess.simpleaccounting.activities.views.SpinnerNoUnwantedOnClick;
import com.emmanuelmess.simpleaccounting.data.TableDataManager;
import com.emmanuelmess.simpleaccounting.dataloading.AsyncFinishedListener;
import com.emmanuelmess.simpleaccounting.dataloading.LoadMonthAsyncTask;
import com.emmanuelmess.simpleaccounting.data.MonthData;
import com.emmanuelmess.simpleaccounting.data.Session;
import com.emmanuelmess.simpleaccounting.db.TableGeneral;
import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance;
import com.emmanuelmess.simpleaccounting.utils.ACRAHelper;
import com.emmanuelmess.simpleaccounting.utils.SimpleBalanceFormatter;
import com.emmanuelmess.simpleaccounting.utils.SimpleTextWatcher;
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
import static com.emmanuelmess.simpleaccounting.activities.preferences.CurrencyPicker.DFLT;
import static com.emmanuelmess.simpleaccounting.constants.SettingsConstants.INVERT_CREDIT_DEBIT_SETTING;

/**
 * @author Emmanuel
 */
public class MainActivity extends AppCompatActivity
		implements AsyncFinishedListener<MonthData>, LedgerView.LedgeCallbacks{

	public static final String UPDATE_YEAR_SETTING = "update 1.2 year";
	public static final String UPDATE_MONTH_SETTING = "update 1.2 month";


	public static int[] MONTH_STRINGS = new int[]{R.string.january, R.string.february, R.string.march, R.string.april, R.string.may,
			R.string.june, R.string.july, R.string.august, R.string.september, R.string.october, R.string.november, R.string.december};

	public static final String PREFS_NAME = "shared prefs", PREFS_FIRST_RUN = "first_run";

	//THESE COULD NOT BE IN ORDER (because of posible inversion between credit and debit)
	public static final int[] EDIT_IDS = {R.id.editDate, R.id.editRef, R.id.editCredit, R.id.editDebit, R.id.textBalance};
	public static final int[] TEXT_IDS = {R.id.textDate, R.id.textRef, R.id.textCredit, R.id.textDebit};

	private int FIRST_REAL_ROW = 1;//excluding header and previous balance. HAS 2 STATES: 1 & 2
	private int DEFAULT_CURRENCY = 0;

	private LedgerView table = null;
	private TableDataManager tableDataManager = null;
	private View space;
	private FloatingActionButton fab;
	private TableGeneral tableGeneral;
	private TableMonthlyBalance tableMonthlyBalance;
	private LayoutInflater inflater;
	private ScrollView scrollView;
	private LoadMonthAsyncTask loadingMonthTask = null;

	private int updateYear, updateMonth;

	private int[] editableRowColumnsHash = new int[4];
	private boolean reloadMonthOnChangeToView;

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
		//if (MainActivity.invertCreditDebit != invertCreditDebit)
		//	MainActivity.invertCreditDebit = invertCreditDebit;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		scrollView = findViewById(R.id.scrollView);
		table = findViewById(R.id.table);
		table.setFormatter(new SimpleBalanceFormatter());
		table.setListener(this);
		tableDataManager = new TableDataManager();
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

		fab = findViewById(R.id.fab);
		if (isSelectedMonthOlderThanUpdate()) {
			fab.setVisibility(GONE);
			space.setVisibility(GONE);
		}

		fab.setOnClickListener(view->{
			table.inflateEmptyRow();
			scrollView.fullScroll(View.FOCUS_DOWN);

			if (table.getChildCount() > FIRST_REAL_ROW) {
				String day = new SimpleDateFormat("dd", Locale.getDefault()).format(new Date());

				int index = tableGeneral.newRowInMonth(new Session(editableMonth, editableYear, editableCurrency));
				tableGeneral.update(index, TableGeneral.COLUMNS[0], day);

				rowToDBRowConversion.add(tableGeneral.getLastIndex());
				LedgerRow row = loadRow();

				row.setDate(day);
				row.requestFocus();

				//editableRowColumnsHash[0] = row.getDate().toString().hashCode();

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(row.findViewById(R.id.editDate), InputMethodManager.SHOW_IMPLICIT);
			} else createNewRowWhenMonthLoaded = true;
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		table.setInvertCreditAndDebit(getDefaultSharedPreferences(this).getBoolean(INVERT_CREDIT_DEBIT_SETTING, false));

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
		if (table.isEditingRow()) table.editableRowToView();
		else super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar, menu);

		TinyDB tinyDB = new TinyDB(this);
		ArrayList<String> currencies = tinyDB.getListString(CurrencyPicker.KEY); //DO NOT save this List (first item changed)

		if (currencies.size() != 0 && !isSelectedMonthOlderThanUpdate()) {
			if(Utils.INSTANCE.equal(currencies.get(0), DFLT))
				currencies.set(0, getString(R.string.default_short));

			MenuItem item = menu.findItem(R.id.action_currency);
			SpinnerNoUnwantedOnClick spinner =
					new SpinnerNoUnwantedOnClick((Spinner) MenuItemCompat.getActionView(item));
			ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_toolbar,
					currencies.toArray(new String[currencies.size()]));
			adapter.setDropDownViewResource(R.layout.item_spinner);
			spinner.setAdapter(adapter);
			spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if(table.isEditingRow())
						table.editableRowToView();

					if (pos == DEFAULT_CURRENCY)
						editableCurrency = "";
					else
						editableCurrency = ((TextView) view).getText().toString();

					currencyName = Utils.INSTANCE.equal(editableCurrency, "")?
							((TextView) view).getText().toString():editableCurrency;//repeated code at end of lambda

					loadMonth(editableMonth, editableYear, editableCurrency);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});

			currencyName = Utils.INSTANCE.equal(editableCurrency, "")? currencies.get(0):editableCurrency;
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
				i.putExtra(GraphActivity.GRAPH_SESSION, new Session(editableMonth, editableYear, editableCurrency));
				i.putExtra(GraphActivity.GRAPH_UPDATE_MONTH, updateMonth);
				i.putExtra(GraphActivity.GRAPH_UPDATE_YEAR, updateYear);
				startActivity(i);
				return true;
			case R.id.action_show_months:
				startActivity(new Intent(this, MonthActivity.class));
				return true;
			case R.id.action_print:
				if (table.getChildCount() > 1) {
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
						PrintManager printM = (PrintManager) getSystemService((Context.PRINT_SERVICE));

						String job = getString(R.string.app_name) + ": " +
								(!isSelectedMonthOlderThanUpdate()?
										getString(MONTH_STRINGS[editableMonth]):getString(MONTH_STRINGS[updateMonth]));
						printM.print(job,
								new PPrintDocumentAdapter(this, table, new Session(editableMonth, editableYear,
										currencyName), new int[]{updateMonth, updateYear}),
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

	public LedgerRow loadRow() {
		int rowViewIndex = table.getChildCount() - 1;

		LedgerRow row = (LedgerRow) table.getLastRow();
		tableDataManager.addRow();

		row.setBalance(tableDataManager.getTotal(getCorrectedIndexForDataManager(rowViewIndex)));

		setListener(rowViewIndex);
		checkEditInBalance(rowViewIndex, row);
		return row;
	}

	public int getFirstRealRow() {
		return FIRST_REAL_ROW;
	}

	public void setFirstRealRow(int firstRealRow) {
		this.FIRST_REAL_ROW = firstRealRow;
	}

	private void checkEditInBalance(final int editedTableIndex, LedgerRow row) {
		TextView lastBalance = editedTableIndex > 1?
				(TextView) table.getChildAt(editedTableIndex - 1).findViewById(R.id.textBalance):null;

		TextWatcher watcher = new SimpleTextWatcher() {
			@Override
			public void afterTextChanged(Editable editable) {
				if (table.getEditableRow() == editedTableIndex) {
					final int dataManagerIndex = getCorrectedIndexForDataManager(editedTableIndex);

					if (Utils.INSTANCE.equal(row.getCreditText().toString(), ".")) {
						tableDataManager.updateCredit(dataManagerIndex, BigDecimal.ZERO);
						row.setCredit("0");
					}

					if (Utils.INSTANCE.equal(row.getDebitText().toString(), ".")) {
						tableDataManager.updateCredit(dataManagerIndex, BigDecimal.ZERO);
						row.setDebit("0");
					}

					tableDataManager.updateCredit(dataManagerIndex, Utils.INSTANCE.parseString(row.getCreditText().toString()));
					tableDataManager.updateDebit(dataManagerIndex, Utils.INSTANCE.parseString((row.getDebitText().toString())));

					if (tableDataManager.getTotal(dataManagerIndex).toPlainString().isEmpty())
						throw new IllegalStateException();

					row.setBalance(tableDataManager.getTotal(dataManagerIndex));

					updateBalances(editedTableIndex+1,dataManagerIndex + 1);
				}
			}

			private void updateBalances(int tableIndex, int dataManagerIndex) {
			    if(tableIndex >= table.getChildCount()) return;

			    LedgerRow row = (LedgerRow) table.getChildAt(tableIndex);

				row.setBalance(tableDataManager.getTotal(dataManagerIndex));

				if (tableIndex + 1 < row.getChildCount()){
					updateBalances(tableIndex + 1, dataManagerIndex + 1);
				}
			}
		};

		((EditText) row.findViewById(R.id.editCredit)).addTextChangedListener(watcher);
		((EditText) row.findViewById(R.id.editDebit)).addTextChangedListener(watcher);
		if (lastBalance != null)
			lastBalance.addTextChangedListener(watcher);
	}
	
	private void setListener(final int rowIndex) {
		resetEditableHash(rowIndex);
		table.getChildAt(rowIndex).setOnLongClickListener(v->{
			table.editableRowToView();
			table.rowViewToEditable(rowIndex);
			return true;
		});
	}

	private void resetEditableHash(final int rowIndex) {
		for (int i = 0; i < TEXT_IDS.length; i++) {
			TextView t1 = table.getChildAt(rowIndex).findViewById(TEXT_IDS[i]);
			editableRowColumnsHash[i] = t1.getText().toString().hashCode();
		}
	}

	private void loadMonth(int month, int year, String currency) {
		if(!LoadMonthAsyncTask.Companion.isAlreadyLoading()) {
			findViewById(R.id.progressBar).setVisibility(VISIBLE);

			TextView monthText = findViewById(R.id.textMonth);

			if (month != -1 && !isSelectedMonthOlderThanUpdate()) {
				monthText.setText(MONTH_STRINGS[month]);
			} else {
				monthText.setText(getString(R.string.before_update_1_2)
						+ " " + getString(MONTH_STRINGS[updateMonth]).toLowerCase() + "-" + updateYear);
			}

			loadingMonthTask = new LoadMonthAsyncTask(new Session(month, year, currency),
					tableMonthlyBalance, tableGeneral, this);
			loadingMonthTask.execute();
		} else if(editableMonth != month || editableYear != year || !Utils.INSTANCE.equal(editableCurrency, currency)) {
			loadingMonthTask.cancel(true);
		}
	}

	@Override
	public void onAsyncFinished(MonthData dbData) {
		FIRST_REAL_ROW = 1;

		if (table.getChildCount() > 1) {//DO NOT remove first line, the column titles
			table.clear();
		}

		tableDataManager.clear();

		editableMonth = dbData.getSession().getMonth();
		editableYear = dbData.getSession().getYear();
		editableCurrency = dbData.getSession().getCurrency();

		if (dbData.getPrevBalance() != null) {
			LedgerRow row = (LedgerRow) table.inflateEmptyRow();

			setFirstRealRow(2);
			table.editableRowToView();

			tableDataManager.updateStartingTotal(new BigDecimal(dbData.getPrevBalance()));

			row.setReference(R.string.previous_balance);
			row.setCredit("");
			row.setDebit("");
			row.setBalance(tableDataManager.getStartingTotal());
		}

		int dataManagerIndex = 1;

		this.rowToDBRowConversion = dbData.getRowToDBConversion();

		for (String[] dbRow : dbData.getDbRows()) {
			table.inflateEmptyRow();

			LedgerRow row = loadRow();
			table.editableRowToView();

			int[] textIds = MainActivity.TEXT_IDS;

			for (int j = 0; j < textIds.length; j++) {
				if(dbRow[j] == null) continue;

				TextView v = row.findViewById(textIds[j]);

				v.setText(dbRow[j]);
				editableRowColumnsHash[j] = dbRow[j].hashCode();
			}

			if (dbRow[2] != null)
				tableDataManager.updateCredit(dataManagerIndex, Utils.INSTANCE.parseString(dbRow[2]));
			if (dbRow[3] != null)
				tableDataManager.updateDebit(dataManagerIndex, Utils.INSTANCE.parseString(dbRow[3]));

			row.setBalance(tableDataManager.getTotal(dataManagerIndex));
		dataManagerIndex++;
		}

		scrollView.fullScroll(View.FOCUS_DOWN);

		findViewById(R.id.progressBar).setVisibility(GONE);

		loadShowcaseView(inflater, scrollView);

		if (createNewRowWhenMonthLoaded && table != null) {// TODO: 24/06/18 duplicated code
			table.inflateEmptyRow();

			scrollView.fullScroll(View.FOCUS_DOWN);

			table.editableRowToView();

			tableGeneral.newRowInMonth(new Session(editableMonth, editableYear, editableCurrency));
			this.rowToDBRowConversion.add(tableGeneral.getLastIndex());
			LedgerRow row = loadRow();

			row.setDate(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()));
			row.requestFocus();

			editableRowColumnsHash[0] = row.getDate().toString().hashCode();

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(row.findViewById(R.id.editDate), InputMethodManager.SHOW_IMPLICIT);
			createNewRowWhenMonthLoaded = false;
		}
	}
	
	private void loadShowcaseView(LayoutInflater inflater, ScrollView scrollView) {
		SharedPreferences myPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		boolean isFirstRun = myPrefs.getBoolean(PREFS_FIRST_RUN, true);
		if (isFirstRun) { //|| BuildConfig.DEBUG) {

			final int rowToEdit = FIRST_REAL_ROW;

			if (table.getChildAt(rowToEdit) == null) {
				table.inflateEmptyRow();
				scrollView.fullScroll(View.FOCUS_DOWN);

				tableDataManager.addRow();
				tableDataManager.updateCredit(1, BigDecimal.ZERO);
				tableDataManager.updateDebit(1, new BigDecimal(100));

				table.editableRowToView();
				LedgerRow row = (LedgerRow) table.getChildAt(rowToEdit);

				row.setDate(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()));
				row.setReference(R.string.showcase_example_ref);
				row.setCredit(BigDecimal.ZERO);
				row.setDebit(new BigDecimal(100));
				row.setBalance(new BigDecimal(-100));

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
								table.clear();
							}
						}
					})
					.build();

			SharedPreferences.Editor pref_editor = myPrefs.edit();
			pref_editor.putBoolean(PREFS_FIRST_RUN, false);
			pref_editor.apply();
		}
	}

	private int getCorrectedIndexForDataManager(int tableIndex) {
		return getFirstRealRow() == 1? tableIndex : tableIndex-1;
	}

	/**
	 * The user should NEVER be allowed to edit in any way if this is true
	 *
	 * @return if the month selected is older than the update that added month selection
	 */
	private boolean isSelectedMonthOlderThanUpdate() {
		return new Session(editableMonth, editableYear, editableCurrency).isOlderThanUpdate();
	}

	@Override
	public void onUpdateEditableRow(int index) {
		if (index == -1 || table == null)
			ACRAHelper.INSTANCE.reset();
		else
			ACRAHelper.INSTANCE.writeData(table, editableYear, editableMonth);
	}

	@Override
	public void onBeforeMakeRowNotEditable(View row){
		if(table.getEditableRow() >= FIRST_REAL_ROW) {//Last month total row is editable for some time
			String editDateText = ((EditText) row.findViewById(R.id.editDate)).getText().toString();
			reloadMonthOnChangeToView = !editDateText.isEmpty()
					&& editableRowColumnsHash[0] != editDateText.hashCode();

			String editCreditText = ((EditText) row.findViewById(R.id.editCredit)).getText().toString();
			String editDebitText = ((EditText) row.findViewById(R.id.editDebit)).getText().toString();
			if(editableRowColumnsHash[2] != editCreditText.hashCode()
					|| editableRowColumnsHash[3] != editDebitText.hashCode()) {
				tableMonthlyBalance.updateMonth(editableMonth, editableYear, editableCurrency,
						tableDataManager.getTotal(getCorrectedIndexForDataManager(table.getEditableRow())).doubleValue());
			}

			for (int i = 0; i < EDIT_IDS.length - 1; i++) {
				String t = ((EditText) row.findViewById(EDIT_IDS[i])).getText().toString();

				if (editableRowColumnsHash[i] != t.hashCode()) {
					tableGeneral.update(rowToDBRowConversion.get(table.getEditableRow() - FIRST_REAL_ROW),
							TableGeneral.COLUMNS[i], (!t.isEmpty() ? t : null));
					editableRowColumnsHash[i] = -1;
				}
			}
		}

		resetEditableHash(table.getEditableRow());

		TextView balanceText = row.findViewById(R.id.textBalance);

		if (balanceText != null && balanceText.getText() == "") {
			View previousRow = table.getEditableRow() - 1 == 0 ? null : table.getChildAt(table.getEditableRow() - 1);
			if (previousRow != null)
				balanceText.setText("$ " + tableDataManager.getTotal(getCorrectedIndexForDataManager(table.getEditableRow())).toString());
			else
				balanceText.setText("$ 0.0");
		}
	}

	@Override
	public void onAfterMakeRowNotEditable(View row) {
		if (reloadMonthOnChangeToView) {
			reloadMonthOnChangeToView = false;
			loadMonth(editableMonth, editableYear, editableCurrency);
		}
	}
}
