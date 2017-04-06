package com.emmanuelmess.simpleaccounting.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.emmanuelmess.simpleaccounting.MainActivity;
import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.utils.Utils;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity implements Preference.OnPreferenceChangeListener {

	public static final String INVERT_CREDIT_DEBIT_SETTING = "pref_invertcreditdebit";
	public static final String CURRENCY_PICKER_SETTING = "pref_currencypicker";

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 *
	 */
	private void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(this);

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(
				preference.getContext());

		if(Utils.equal(preference.getKey(), INVERT_CREDIT_DEBIT_SETTING)) {
			// Trigger the listener immediately with the preference's
			// current value.
			boolean newValue = sharedPref.getBoolean(INVERT_CREDIT_DEBIT_SETTING, false);
			onPreferenceChange(preference, newValue);
			MainActivity.invalidateTableHeader(newValue);
		} else
			onPreferenceChange(preference, sharedPref.getString(preference.getKey(), ""));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			// Show the Up button in the action bar.
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * {@inheritDoc}
	 * Originally checked for isXLargeTablet(this) but would be weird to have 2 pane distribution
	 */
	@Override
	public boolean onIsMultiPane() {
		return false;
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {
		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.
		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to their values. When their values change,
		// their summaries are updated to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference(INVERT_CREDIT_DEBIT_SETTING));
		bindPreferenceSummaryToValue(findPreference(CURRENCY_PICKER_SETTING));
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return true;
	}
}
