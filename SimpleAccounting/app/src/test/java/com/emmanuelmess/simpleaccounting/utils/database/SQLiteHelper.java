package com.emmanuelmess.simpleaccounting.utils.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper {

    public static <T extends SQLiteOpenHelper> T setUpDatabase(T helper, SQLiteDatabase database) {
        helper.onCreate(database);
        return helper;
    }

}
