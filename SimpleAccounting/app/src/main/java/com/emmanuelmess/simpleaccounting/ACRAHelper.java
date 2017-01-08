package com.emmanuelmess.simpleaccounting;

import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.acra.ACRA;
/**
 * @author Emmanuel
 *         on 26/12/2016, at 20:28.
 */

public class ACRAHelper {

	public static final String LAST_ROW = "last";
	public static final String CURRENT_ROW = "current";

	public static void writeData(TableLayout t, int row, MainActivity a) {
		if(row-1 >= a.getFirstRealRow())
			setData(LAST_ROW, t, row-1);

		setData(CURRENT_ROW, t, row);
	}

	public static void reset() {
		ACRA.getErrorReporter().clearCustomData();
	}


	private static void setData(String rowS, TableLayout t, int row) {
		StringBuilder s = new StringBuilder();
		TableRow r = (TableRow) t.getChildAt(row);

		for (int i = 0; i < r.getChildCount(); i++)
			s.append(" '").append(((TextView) r.getChildAt(i)).getText()).append("',");

		ACRA.getErrorReporter().putCustomData(rowS, s.substring(1, s.lastIndexOf(",")));
	}
}
