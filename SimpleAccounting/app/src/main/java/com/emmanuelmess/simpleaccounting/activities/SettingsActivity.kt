package com.emmanuelmess.simpleaccounting.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.emmanuelmess.simpleaccounting.R
import com.emmanuelmess.simpleaccounting.fragments.settings.MainSettingsFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)
    }
}
