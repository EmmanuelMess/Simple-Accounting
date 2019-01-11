package com.emmanuelmess.simpleaccounting;

import android.view.ViewTreeObserver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.emmanuelmess.simpleaccounting.constants.SettingsConstants.INVERT_CREDIT_DEBIT_SETTING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

@RunWith(RobolectricTestRunner.class)
public class MainActivitySwitchCreditDebitTest extends MainActivityTest {
    protected void startSetUp() {
        super.startSetUp();

        getDefaultSharedPreferences(context).edit().putBoolean(INVERT_CREDIT_DEBIT_SETTING, true).apply();
    }

    @Test
    public void testSwitchCreditDebitTest() {
	    table.getViewTreeObserver().addOnGlobalLayoutListener(
			    new ViewTreeObserver.OnGlobalLayoutListener() {
				    @Override
				    public void onGlobalLayout() {
					    table.getViewTreeObserver().removeOnGlobalLayoutListener(this);

					    assertThat(table.findViewById(R.id.debit).getX(), lessThan(table.findViewById(R.id.credit).getX()));

					    table.setInvertCreditAndDebit(false);

					    assertThat(table.findViewById(R.id.credit).getX(), lessThan(table.findViewById(R.id.debit).getX()));
				    }
			    });
    }
}
