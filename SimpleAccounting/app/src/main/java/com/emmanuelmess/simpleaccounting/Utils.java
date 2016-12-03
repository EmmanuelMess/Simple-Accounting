package com.emmanuelmess.simpleaccounting;

/**
 * @author Emmanuel
 *         on 2/12/2016, at 16:45.
 */

public class Utils {

	public static double parse(String s) {
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return 0d;
		}
	}

}
