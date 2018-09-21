package com.emmanuelmess.simpleaccounting.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.emmanuelmess.simpleaccounting.R;
import com.emmanuelmess.simpleaccounting.fragments.settings.MainSettingsFragment;

public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_settings);
	}
}
