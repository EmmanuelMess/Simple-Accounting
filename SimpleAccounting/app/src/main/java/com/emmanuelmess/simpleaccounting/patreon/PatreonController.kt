package com.emmanuelmess.simpleaccounting.patreon

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.emmanuelmess.simpleaccounting.utils.seconds

object PatreonController {

	object Metrics {
		private const val TIMES_STARTED = "times started"
		private const val DO_NOT_BOTHER = "do not bother"
		private const val PAUSE_TIME = "pause time"
		private val SHOW_DIALOG = intArrayOf(2, 8, 24, 60)
		/**
		 * See [onPauseWasCalled]
		 *
		 * 2 seconds to load broser app
		 * 7 seconds to load webpage
		 * 60 seconds to traverse webpage
		 */
		private val HEURISTIC_THRESHOLD_FOR_BROWSER = (2.seconds + 7.seconds + 60.seconds).toMillis().toLong()

		private var okWasClicked = false

		fun appWasStarted(context: Context, sharedPreferences: SharedPreferences) {
			if(sharedPreferences.contains(DO_NOT_BOTHER)) {
				return
			}

			val timesStarted = sharedPreferences.getInt(TIMES_STARTED, 0) + 1

			if(SHOW_DIALOG.contains(timesStarted)) {// || BuildConfig.DEBUG) {
				PatreonController.startDialog(context)
			}

			sharedPreferences.edit().putInt(TIMES_STARTED, timesStarted).apply()
		}

		fun okWasClicked() {
			okWasClicked = true
		}

		/**
		 * We have no way of knowing if the user has opened the browser from the Intent,
		 * or has closed it immediatly after opening.
		 * So we use the time between onPause() and onResume() calls to MainActivity as a heuristic
		 * approach.
		 */
		fun onPauseWasCalled(context: Context) {
			if(!okWasClicked) return

			val time = System.currentTimeMillis()
			getDefaultSharedPreferences(context).edit().putLong(PAUSE_TIME, time).apply()
		}

		fun onResumeWasCalled(context: Context) {
			val resumeTime = System.currentTimeMillis()
			val sharedPreferences = getDefaultSharedPreferences(context)

			if(!sharedPreferences.contains(PAUSE_TIME)) return

			val pauseTime = sharedPreferences.getLong(PAUSE_TIME, 0)

			if(resumeTime-pauseTime >= HEURISTIC_THRESHOLD_FOR_BROWSER) {
				dontBotherAnymore(sharedPreferences)
			}

			getDefaultSharedPreferences(context).edit().remove(PAUSE_TIME).apply()
		}

		private fun dontBotherAnymore(sharedPreferences: SharedPreferences) {
			//sharedPreferences.edit().putBoolean(DO_NOT_BOTHER, true).apply()
			Log.d("PatreonController", "DO NOT BOTHER")
		}
	}

	fun startDialog(context: Context) = context.createPatreonDialog().show()

	fun openPage(context: Context) {
		Intent(Intent.ACTION_VIEW).let {
			it.data = Uri.parse("https://patreon.com/emmanuelmess")
			context.startActivity(it)
		}
	}

}