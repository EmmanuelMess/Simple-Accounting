package com.emmanuelmess.simpleaccounting.dataloading;

import java.util.ArrayList;
/**
 * @author Emmanuel
 *         on 27/11/2016, at 15:16.
 */

public interface OnMonthFinishedLoading {
	public void OnMonthFinishedLoading(ArrayList<Integer> rowToDBRowConversion);
}
