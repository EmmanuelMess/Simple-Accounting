package com.emmanuelmess.simpleaccounting.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Emmanuel
 *         on 14/11/2016, at 17:03.
 */

abstract class Database extends SQLiteOpenHelper {

	static final String NUMBER_COLUMN = "NUMBER";
	final ContentValues CV = new ContentValues();
	Context context;

	Database(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(context, name, factory, version);

		this.context = context;
	}


	final int AND = 0, OR = 2;
	String SQLShort(int type, String s1, String s2) {
		switch(type) {
			case AND:
				return s1 + " AND " + s2;
			case OR:
				return s1 + " OR " + s2;
			default:
				throw new IllegalArgumentException();
		}
	}
}
