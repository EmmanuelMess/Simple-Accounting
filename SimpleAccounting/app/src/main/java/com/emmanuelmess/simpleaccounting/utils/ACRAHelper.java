package com.emmanuelmess.simpleaccounting.utils;

import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.MainActivity;

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
		ACRA.getErrorReporter().clearCustomData();
	}


	private static void setData(TableLayout t, int year, int month) {
		StringBuilder s = new StringBuilder();

		s.append("Year: ").append(year).append("\nMonth: ").append(month);

		for(int i = 0; i < t.getChildCount(); i++) {
			TableRow r = (TableRow) t.getChildAt(i);

			for (int j = 0; j < r.getChildCount(); j++)
				if(r.getChildAt(j).getVisibility() == View.VISIBLE)
					s.append(" [").append(((TextView) r.getChildAt(j)).getText().length()).append("],");

			ACRA.getErrorReporter().putCustomData(ROWS, s.substring(1, s.lastIndexOf(",")));
		}
	}
}
