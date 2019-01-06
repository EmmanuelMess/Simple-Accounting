package com.emmanuelmess.simpleaccounting

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.emmanuelmess.simpleaccounting.R.id.table
import com.emmanuelmess.simpleaccounting.R.id.withText
import com.emmanuelmess.simpleaccounting.activities.MainActivity
import org.hamcrest.Matchers.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainTest {

	@get:Rule
	val activityRule = ActivityTestRule(MainActivity::class.java)

	@Test
	fun testFab() {
		for (i in 1..30) {
			onView(withId(R.id.fab)).perform(click())
			onView(withText("DONE")).perform(click())
		}
	}
}