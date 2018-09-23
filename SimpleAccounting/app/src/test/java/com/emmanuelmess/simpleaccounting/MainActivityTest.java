package com.emmanuelmess.simpleaccounting;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.activities.views.LedgerView;
import com.emmanuelmess.simpleaccounting.utils.SimpleBalanceFormatter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;

import static junit.framework.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MainActivityTest {

    protected Context context;
    protected SharedPreferences sharedPreferences;
    protected ActivityController<MainActivity> activityController;
    protected MainActivity activity;
    protected LedgerView table;
    protected FloatingActionButton fab;

    @Before
    public void setUp() {
        startSetUp();
        /*
         * All SharedPreferences editing calls must be done before this point.
         * @see #endSetUp()
         */
        endSetUp();
    }

    protected void startSetUp() {
        context = RuntimeEnvironment.application.getApplicationContext();
        sharedPreferences = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);

        setShowTutorial(false);
    }

    /**
     * This is a hack, used to circumvent a call to park() that never ends.
     * In this method go all calls for creating and after creating an Activity.
     */
    protected void endSetUp() {
        activityController = Robolectric.buildActivity(MainActivity.class)
                .create().start().resume().visible();
        activity =  activityController.get();
        table = activity.findViewById(R.id.table);
        fab = activity.findViewById(R.id.fab);
    }

    @Test
    public void testFab() {
        int tableChildCount = table.getChildCount();
        assertEquals(1, tableChildCount);

        for(int i = 1; i <= 30; i++) {
            fab.callOnClick();
            assertEquals(tableChildCount+i, table.getChildCount());
        }
    }

    @Test
    public void testEditRow() {
        String c = "300", d = "500";

        TableRow row = createNewRow(c, d);

        TextView creditText = row.findViewById(R.id.textCredit);
        TextView debitText = row.findViewById(R.id.textDebit);

        assertEquals(c, creditText.getText().toString());
        assertEquals(d, debitText.getText().toString());

        table.rowViewToEditable(table.getChildCount()-1);

        row = (TableRow) table.getChildAt(table.getEditableRow());
        EditText creditEditable = row.findViewById(R.id.editCredit);
        EditText debitEditable = row.findViewById(R.id.editDebit);

        assertEquals(c, creditEditable.getText().toString());
        assertEquals(d, debitEditable.getText().toString());
    }

    @Test
    public void testArithmetic() {
        fab.callOnClick();

        SimpleBalanceFormatter formatter = new SimpleBalanceFormatter();

        BigDecimal zero = BigDecimal.ZERO.setScale(1); //0.0


        TableRow row = (TableRow) table.getChildAt(table.getEditableRow());
        TextView balance = row.findViewById(R.id.textBalance);

        assertEquals(formatter.format(BigDecimal.ZERO), balance.getText().toString());

        String[][] toTest = {{"300", "300"}, {"100", "200"}, {"500.1", "300"}, {"-52", "52.000001"}};

        BigDecimal result = zero;

        for(String[] current : toTest) {
            row = (TableRow) table.getChildAt(table.getEditableRow());
            EditText credit = row.findViewById(R.id.editCredit);
            EditText debit = row.findViewById(R.id.editDebit);
            balance = row.findViewById(R.id.textBalance);

            BigDecimal creditBigDecimal = new BigDecimal(current[0]),
                    debitBigDecimal = new BigDecimal(current[1]);

            BigDecimal newResult = result.add(creditBigDecimal).subtract(debitBigDecimal);

            credit.setText(creditBigDecimal.toString());
            debit.setText(debitBigDecimal.toString());

            assertEquals("Tested: " + newResult + " ( = " + result + " + " + creditBigDecimal + " - " + debitBigDecimal + ")",
                    formatter.format(newResult), balance.getText().toString());

            result = newResult;
            fab.callOnClick();
        }
    }

    protected TableRow createNewRow(String credit, String debit) {
        fab.callOnClick();

        TableRow row = (TableRow) table.getChildAt(table.getEditableRow());
        EditText creditEditable = row.findViewById(R.id.editCredit);
        EditText debitEditable = row.findViewById(R.id.editDebit);

        creditEditable.setText(credit);
        debitEditable.setText(debit);

        table.editableRowToView();

        return row;
    }

    private void setShowTutorial(boolean show) {
        sharedPreferences.edit().putBoolean(MainActivity.PREFS_FIRST_RUN, show).commit();
    }
}