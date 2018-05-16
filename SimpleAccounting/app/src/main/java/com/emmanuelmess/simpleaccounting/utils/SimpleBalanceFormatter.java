package com.emmanuelmess.simpleaccounting.utils;

import com.emmanuelmess.simpleaccounting.activities.views.LedgerView;

import java.math.BigDecimal;

/**
 * @author Emmanuel
 *         on 27/11/2017, at 14:56.
 */

public class SimpleBalanceFormatter implements LedgerView.BalanceFormatter {
	@Override
	public String format(BigDecimal balance) {
		return "$ " + String.valueOf(balance);
	}
}
