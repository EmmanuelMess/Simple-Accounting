package com.emmanuelmess.simpleaccounting.fragments.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.emmanuelmess.simpleaccounting.MainActivity;
import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.activities.preferences.CurrencyPicker;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import static com.emmanuelmess.simpleaccounting.constants.SettingsConstants.INVERT_CREDIT_DEBIT_SETTING;
import static com.emmanuelmess.simpleaccounting.constants.SettingsConstants.LIBRARIES_SETTING;

public class MainSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.pref_general);
	}

	@Override
	public void onCreatePreferences(Bundle bundle, String s) {

	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
		switch (key) {
			case INVERT_CREDIT_DEBIT_SETTING:
				boolean newValue = sharedPref.getBoolean(INVERT_CREDIT_DEBIT_SETTING, false);
				MainActivity.invalidateTableHeader(newValue);
				break;
		}
	}

	@Override
	public void onDisplayPreferenceDialog(Preference preference) {
		if (preference instanceof CurrencyPicker) {
			CurrencyPickerFragment dialogFragment = CurrencyPickerFragment.newInstance(preference.getKey());
			dialogFragment.setTargetFragment(this, 0);
			dialogFragment.show(requireFragmentManager(), null);
		} else super.onDisplayPreferenceDialog(preference);
	}

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		switch (preference.getKey()) {
			case LIBRARIES_SETTING:
				new LibsBuilder()
						.withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
						.withActivityTitle(getString(R.string.libraries))
						.withAboutIconShown(true)
						.withAboutVersionShownName(true)
						.withAboutDescription(getString(R.string.simpleaccounting_description))
						.withAboutSpecial1(getString(R.string.license))
						.withAboutSpecial1Description(getString(R.string.simpleaccounting_license))
						.withLicenseShown(true)
						.start(requireContext());
				break;
		}

		return super.onPreferenceTreeClick(preference);
	}

}
