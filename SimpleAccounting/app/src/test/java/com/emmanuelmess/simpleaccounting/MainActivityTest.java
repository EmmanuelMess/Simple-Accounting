package com.emmanuelmess.simpleaccounting;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.activities.views.LedgerRow;
import com.emmanuelmess.simpleaccounting.activities.views.LedgerView;
import com.emmanuelmess.simpleaccounting.utils.SimpleBalanceFormatter;
import com.google.common.collect.Table;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainActivityTest {

    @Test
    public void testFab() {
        setShowTutorial(false);
        MainActivity activity =  Robolectric.buildActivity(MainActivity.class)
                .create().start().resume().visible().get();
        LedgerView table = activity.findViewById(R.id.table);
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        int tableChildCount = table.getChildCount();
        assertEquals(1, tableChildCount);

        for(int i = 1; i <= 30; i++) {
            fab.callOnClick();
            assertEquals(tableChildCount+i, table.getChildCount());
        }
    }

    @Test
    public void testArithmetic() {
        setShowTutorial(false);
        MainActivity activity =  Robolectric.buildActivity(MainActivity.class)
                .create().start().resume().visible().get();
        LedgerView table = activity.findViewById(R.id.table);
        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.callOnClick();

        TableRow row = (TableRow) table.getChildAt(table.getEditableRow());
        EditText credit = row.findViewById(R.id.editCredit);
        EditText debit = row.findViewById(R.id.editDebit);
        TextView balance = row.findViewById(R.id.textBalance);
        SimpleBalanceFormatter formatter = new SimpleBalanceFormatter();

        BigDecimal zero = BigDecimal.ZERO.setScale(1); //0.0


        assertEquals(formatter.format(zero), balance.getText().toString());

        String c = 300 + "";
        credit.setText(c);
        String d = 300 + "";
        debit.setText(d);
        assertEquals(formatter.format(zero), balance.getText().toString());
    }

    private void setShowTutorial(boolean show) {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(MainActivity.PREFS_FIRST_RUN, show).commit();
    }
}