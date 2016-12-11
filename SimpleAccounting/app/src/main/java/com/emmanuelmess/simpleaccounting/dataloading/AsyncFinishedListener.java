package com.emmanuelmess.simpleaccounting.dataloading;

/**
 * @author Emmanuel
 *         on 27/11/2016, at 15:16.
 */

public interface AsyncFinishedListener<T> {
	public void OnAsyncFinished (T rowToDBRowConversion);
}
