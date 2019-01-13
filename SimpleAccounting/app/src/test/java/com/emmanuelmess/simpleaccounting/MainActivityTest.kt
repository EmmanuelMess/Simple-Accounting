package com.emmanuelmess.simpleaccounting

import android.content.Context
import android.content.SharedPreferences
import com.emmanuelmess.simpleaccounting.activities.MainActivity
import com.emmanuelmess.simpleaccounting.activities.views.LedgerView
import com.emmanuelmess.simpleaccounting.fragments.EditRowFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.junit.Before
import org.junit.Ignore
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
@Ignore
open class MainActivityTest {

	protected lateinit var context: Context
	protected lateinit var sharedPreferences: SharedPreferences
	protected lateinit var activityController: ActivityController<MainActivity>
	protected lateinit var activity: MainActivity
	protected lateinit var table: LedgerView
	protected lateinit var fab: FloatingActionButton

	private var useFragmentExceptionHack = true

	@Before
	fun setUp() {
		startSetUp()
		/*
         * All SharedPreferences editing calls must be done before this point.
         * @see #endSetUp()
         */
		endSetUp()
	}

	protected open fun startSetUp() {
		context = RuntimeEnvironment.application.applicationContext
		sharedPreferences = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

		if (useFragmentExceptionHack) {
			Robolectric.getForegroundThreadScheduler().pause()//Fragment throws IllegalStateException, this hack fixes Robolectric issue#4021
		}

		setShowTutorial(false)
	}

	/**
	 * This is a hack, used to circumvent a call to park() that never ends.
	 * In this method go all calls for creating and after creating an Activity.
	 */
	protected open fun endSetUp() {
		activityController = Robolectric.buildActivity(MainActivity::class.java)
			.create().start().resume().visible()

		activity = activityController.get()
		table = activity.findViewById(R.id.table)
		fab = activity.findViewById(R.id.fab)
	}

	/**
	 * This hack fixes a IllegalStateException thrown by Fragments, but breaks AsyncTasks
	 */
	protected fun useFragmentExceptionHack(useFragmentExceptionHack: Boolean) {
		this.useFragmentExceptionHack = useFragmentExceptionHack
	}

	protected fun createNewRow() {
		fab.callOnClick()
		shadowOf(activity).clickMenuItem(R.id.action_done)
	}

	protected fun createNewRow(credit: String, debit: String) {
		fab.callOnClick()

		val fragment = activity.supportFragmentManager
			.findFragmentById(R.id.fragmentContainer) as EditRowFragment

		shadowOf(activity).clickMenuItem(R.id.action_done)
	}

	private fun setShowTutorial(show: Boolean) {
		sharedPreferences.edit().putBoolean(MainActivity.PREFS_FIRST_RUN, show).commit()
	}
}