package com.emmanuelmess.simpleaccounting

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.TableRow
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.platform.app.InstrumentationRegistry
import com.emmanuelmess.simpleaccounting.activities.MainActivity
import com.emmanuelmess.simpleaccounting.activities.views.LedgerRow
import com.emmanuelmess.simpleaccounting.activities.views.LedgerView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher

internal fun Any.getPrefs(): SharedPreferences {
	val context = InstrumentationRegistry.getInstrumentation().getTargetContext()
	return context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
}

fun Any.lastRowOf(tableMatch: Matcher<View>) = object : TypeSafeMatcher<View>() {
	override fun describeTo(description: Description) {
		description.appendText("last row of: ")
		tableMatch.describeTo(description)
	}

	override fun matchesSafely(row: View): Boolean {
		if (row !is LedgerRow) return false
		val table = row.parent as? LedgerView ?: return false
		if (!tableMatch.matches(table)) return false

		return row == table.getChildAt(table.childCount - 1)
	}
}

fun Matcher<View>.andParent(parentMatcher: Matcher<View>)
	= allOf(this, withParent(parentMatcher))!!