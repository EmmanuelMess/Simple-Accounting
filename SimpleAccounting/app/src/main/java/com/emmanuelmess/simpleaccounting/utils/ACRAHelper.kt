package com.emmanuelmess.simpleaccounting.utils

import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

import com.emmanuelmess.simpleaccounting.BuildConfig

import org.acra.ACRA
import org.w3c.dom.Text

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
				val r = t.get<TableRow>(i)
				r ?: continue

				for (j in 0 until r.childCount) {
					if (r.get<View>(j)!!.visibility == View.VISIBLE) {
						s.append(" [")

						val text = r.get<TextView>(j)?.text

						if (text == null) {
							s.append("null")
						} else {
							if (j != 8) {
								s.append(text.length)
							} else {
								s.append(text.length - 2)//for "$ " chars
							}
						}

						s.append("],")
					}
				}

				s.deleteCharAt(s.lastIndexOf(",")).append(" |")
			}

			ACRA.getErrorReporter().putCustomData(ROWS, s.toString())
		}
	}
}
