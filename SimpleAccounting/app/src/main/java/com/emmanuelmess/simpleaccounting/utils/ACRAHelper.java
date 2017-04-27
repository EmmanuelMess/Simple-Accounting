package com.emmanuelmess.simpleaccounting.utils;

import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.BuildConfig;

import org.acra.ACRA;
/**
 * @author Emmanuel
 *         on 26/12/2016, at 20:28.
 */

public class ACRAHelper {

	public static final String ROWS = "rows";

	public static void writeData(TableLayout t, int year, int month) {
		setData(t, year, month);
	}

	public static void reset() {
		if (!BuildConfig.DEBUG) ACRA.getErrorReporter().clearCustomData();
	}

	private static void setData(TableLayout t, int year, int month) {
		if (!BuildConfig.DEBUG) {
			StringBuilder s = new StringBuilder();

			s.append("Year: ").append(year).append(" Month: ").append(month).append(" |");

			for (int i = 1; i < t.getChildCount()-1; i++) { //t.getChildCount() -1 because of last item being blank view?
				TableRow r = (TableRow) t.getChildAt(i);

				for (int j = 0; j < r.getChildCount(); j++) {
					if (r.getChildAt(j).getVisibility() == View.VISIBLE) {
						s.append(" [");

						if(j != 8) s.append(((TextView) r.getChildAt(j)).getText().length());
						else s.append(((TextView) r.getChildAt(j)).getText().length() - 2); //for "$ " chars

						s.append("],");
					}
				}

				s.deleteCharAt(s.lastIndexOf(",")).append(" |");
			}

			ACRA.getErrorReporter().putCustomData(ROWS, s.toString());
		}
	}
}
