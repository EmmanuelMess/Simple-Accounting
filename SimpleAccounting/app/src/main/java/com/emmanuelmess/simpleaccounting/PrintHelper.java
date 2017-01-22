package com.emmanuelmess.simpleaccounting;

import android.content.Context;

import com.emmanuelmess.simpleaccounting.db.TableGeneral;

import static com.emmanuelmess.simpleaccounting.MainActivity.MONTH_STRINGS;
/**
 * @author Emmanuel
 *         on 20/1/2017, at 16:25.
 */

public class PrintHelper {

	public static String createName(Context c, int m, int y, int updateMonth, int updateYear) {
		if(y != TableGeneral.OLDER_THAN_UPDATE)
			return c.getString(MONTH_STRINGS[m]) + "-" + y + ".pdf";
		else return c.getString(R.string.before_update_1_2)
				+ " " + c.getString(MainActivity.MONTH_STRINGS[updateMonth]).toLowerCase()
				+ "-" + String.valueOf(updateYear) + ".pdf";
	}

}
