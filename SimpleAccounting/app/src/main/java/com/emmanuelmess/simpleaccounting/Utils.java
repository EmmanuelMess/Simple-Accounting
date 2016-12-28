package com.emmanuelmess.simpleaccounting;

import android.os.Build;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Objects;
/**
 * @author Emmanuel
 *         on 2/12/2016, at 16:45.
 */

public class Utils {

	public static BigDecimal parseView(TextView v) {
		return parseString(parseViewToString(v));
	}

	public static String parseViewToString(TextView v) {
		return v.getText().toString();
	}

	public static BigDecimal parseString(String s) {
		if(s.length() == 0 || equal(s, "."))
			return new BigDecimal("0");
		else return new BigDecimal(s);
	}

	public static boolean equal(Object o1, Object o2) {
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Objects.equals(o1, o2)) || o1.equals(o2);
	}

}
