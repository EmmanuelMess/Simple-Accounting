package com.emmanuelmess.simpleaccounting.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.emmanuelmess.simpleaccounting.activities.MainActivity
import com.emmanuelmess.simpleaccounting.db.migration.MIGRATED_DATABASE_SETTING
import com.emmanuelmess.simpleaccounting.db.migration.MigrationHelper
import com.emmanuelmess.simpleaccounting.iu.notifications.migrationNotification
import java.lang.ref.WeakReference

class DatabaseMigrationService: Service() {
	private lateinit var migrationAsyncTask: MigrationAsyncTask

	override fun onCreate() {
		super.onCreate()

		migrationAsyncTask = MigrationAsyncTask(this, applicationContext)
		migrationAsyncTask.execute()
	}

	override fun onBind(intent: Intent?): IBinder? {
		return null
	}

	class MigrationAsyncTask(service: Service, applicationContext: Context): AsyncTask<Unit, Unit, Unit>() {
		val NOTIFICATION_ID = 0

		val context = WeakReference(applicationContext)

		val migrationHelper = MigrationHelper(service)

		override fun onPreExecute() {
			context.get()!!.let { context ->
				val notification = migrationNotification(context)

				NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
			}
		}

		override fun doInBackground(vararg params: Unit?) {
			migrationHelper.migrate()
		}

		override fun onPostExecute(result: Unit?) {
			context.get()?.let { context ->
				val preferences: SharedPreferences = getDefaultSharedPreferences(context)

				preferences.edit().putBoolean(MIGRATED_DATABASE_SETTING, true).apply()
				NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)

				val i = Intent(context, MainActivity::class.java)
				startActivity(context, i, null)
			}
		}
	}
}