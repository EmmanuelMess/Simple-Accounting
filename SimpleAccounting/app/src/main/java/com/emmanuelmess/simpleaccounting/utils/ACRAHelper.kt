package com.emmanuelmess.simpleaccounting.utils

import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

import com.emmanuelmess.simpleaccounting.BuildConfig

import org.acra.ACRA

/**
 * @author Emmanuel
 * on 26/12/2016, at 20:28.
 */

object ACRAHelper {

	const val ROWS = "rows"

	fun writeData(t: TableLayout, year: Int, month: Int) {
		setData(t, year, month)
	}

	fun reset() {
		if (!BuildConfig.DEBUG) ACRA.getErrorReporter().clearCustomData()
	}

	private fun setData(t: TableLayout, year: Int, month: Int) {
		if (!BuildConfig.DEBUG) {
			val s = StringBuilder()

			s.append("Year: $year Month: $month |")

			for (i in 1 until t.childCount - 1) { //t.getChildCount() -1 because of last item being blank view?
				val r = t.getChildAt(i) as TableRow

				for (j in 0 until r.childCount) {
					if (r.getChildAt(j).visibility == View.VISIBLE) {
						s.append(" [")

						if (j != 8)
							s.append((r.getChildAt(j) as TextView).text.length)
						else
							s.append((r.getChildAt(j) as TextView).text.length - 2) //for "$ " chars

						s.append("],")
					}
				}

				s.deleteCharAt(s.lastIndexOf(",")).append(" |")
			}

			ACRA.getErrorReporter().putCustomData(ROWS, s.toString())
		}
	}
}
