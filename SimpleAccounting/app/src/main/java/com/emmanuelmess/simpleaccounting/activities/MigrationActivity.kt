package com.emmanuelmess.simpleaccounting.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.emmanuelmess.simpleaccounting.R
import android.content.Intent
import com.emmanuelmess.simpleaccounting.services.DatabaseMigrationService

class MigrationActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_migration)

		val intent = Intent(this, DatabaseMigrationService::class.java)
		startService(intent)
	}
}
