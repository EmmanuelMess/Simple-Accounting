package com.emmanuelmess.simpleaccounting.fragments.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat

import com.emmanuelmess.simpleaccounting.activities.MainActivity
import com.emmanuelmess.simpleaccounting.R
import com.emmanuelmess.simpleaccounting.activities.preferences.CurrencyPicker
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder

import com.emmanuelmess.simpleaccounting.constants.SettingsConstants.INVERT_CREDIT_DEBIT_SETTING
import com.emmanuelmess.simpleaccounting.constants.SettingsConstants.LIBRARIES_SETTING

class MainSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.pref_general)
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) = Unit

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences
                .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPref: SharedPreferences, key: String) {
        when (key) {
            INVERT_CREDIT_DEBIT_SETTING -> {
                val newValue = sharedPref.getBoolean(INVERT_CREDIT_DEBIT_SETTING, false)
                MainActivity.invalidateTableHeader(newValue)
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is CurrencyPicker) {
            val dialogFragment = CurrencyPickerFragment.newInstance(preference.getKey())
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(requireFragmentManager(), null)
        } else
            super.onDisplayPreferenceDialog(preference)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            LIBRARIES_SETTING -> LibsBuilder()
                    .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                    .withActivityTitle(getString(R.string.libraries))
                    .withAboutIconShown(true)
                    .withAboutVersionShownName(true)
                    .withAboutDescription(getString(R.string.simpleaccounting_description))
                    .withAboutSpecial1(getString(R.string.license))
                    .withAboutSpecial1Description(getString(R.string.simpleaccounting_license))
                    .withLicenseShown(true)
                    .start(requireContext())
        }

        return super.onPreferenceTreeClick(preference)
    }

}
