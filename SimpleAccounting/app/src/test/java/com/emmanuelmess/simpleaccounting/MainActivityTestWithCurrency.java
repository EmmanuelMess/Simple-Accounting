package com.emmanuelmess.simpleaccounting;

import org.junit.Ignore;

@Ignore
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
