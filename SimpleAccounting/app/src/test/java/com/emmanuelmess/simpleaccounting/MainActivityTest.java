package com.emmanuelmess.simpleaccounting;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.activities.MainActivity;
import com.emmanuelmess.simpleaccounting.activities.views.LedgerRow;
import com.emmanuelmess.simpleaccounting.activities.views.LedgerView;
import com.emmanuelmess.simpleaccounting.fragments.EditRowFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.fakes.RoboMenu;

import androidx.appcompat.widget.Toolbar;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    protected Context context;
    protected SharedPreferences sharedPreferences;
    protected ActivityController<MainActivity> activityController;
    protected MainActivity activity;
    protected LedgerView table;
    protected FloatingActionButton fab;

    private boolean useFragmentExceptionHack = true;

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

        if(useFragmentExceptionHack) {
            Robolectric.getForegroundThreadScheduler().pause();//Fragment throws IllegalStateException, this hack fixes Robolectric issue#4021
        }

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

    /**
     * This hack fixes a IllegalStateException thrown by Fragments, but breaks AsyncTasks
     */
    protected void useFragmentExceptionHack(boolean useFragmentExceptionHack) {
        this.useFragmentExceptionHack = useFragmentExceptionHack;
    }

    protected void createNewRow() {
        fab.callOnClick();
        shadowOf(activity).clickMenuItem(R.id.action_done);
    }

    protected void createNewRow(String credit, String debit) {
        fab.callOnClick();

        EditRowFragment fragment = (EditRowFragment) activity
                .getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);

        shadowOf(activity).clickMenuItem(R.id.action_done);
    }

    private void setShowTutorial(boolean show) {
        sharedPreferences.edit().putBoolean(MainActivity.PREFS_FIRST_RUN, show).commit();
    }
}