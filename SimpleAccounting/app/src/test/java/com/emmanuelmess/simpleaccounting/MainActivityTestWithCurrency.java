package com.emmanuelmess.simpleaccounting;

import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;

import com.emmanuelmess.simpleaccounting.activities.preferences.CurrencyPicker;
import com.emmanuelmess.simpleaccounting.utils.TinyDB;

import org.junit.Test;
import org.robolectric.shadows.ShadowAbsSpinner;
import org.robolectric.shadows.ShadowActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.robolectric.Shadows.shadowOf;

public class MainActivityTestWithCurrency extends MainActivityTest {
/*
TODO fix once Robolectric issues #4434 and #4435 have been fixed

    @Override
    public void startSetUp() {
        super.startSetUp();
        TinyDB tinyDB = new TinyDB(context);
        tinyDB.putListString(CurrencyPicker.KEY, new ArrayList<>(Arrays.asList("ARG", "U$D", "--", "fdfasd")));
    }

    @Test
    public void testCurrency() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        createNewRow();

        Menu menu = shadowOf(activity).getOptionsMenu();
        MenuItem currencyItem = menu.findItem(R.id.action_currency);
        Spinner currencyPicker = (Spinner) currencyItem.getActionView();
        currencyPicker.setSelection(1);

        assertEquals(1, table.getChildCount());

        currencyPicker.setSelection(0);

        assertEquals(2, table.getChildCount());
    }
*/
}
