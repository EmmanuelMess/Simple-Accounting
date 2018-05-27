package com.emmanuelmess.simpleaccounting;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;

import com.emmanuelmess.simpleaccounting.activities.views.LedgerView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

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
        fab.callOnClick();
        assertEquals(2, table.getChildCount());
    }

    private void setShowTutorial(boolean show) {
        Context context = RuntimeEnvironment.application.getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(MainActivity.PREFS_FIRST_RUN, show).commit();
    }
}