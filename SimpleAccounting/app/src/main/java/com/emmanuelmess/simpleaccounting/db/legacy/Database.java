package com.emmanuelmess.simpleaccounting.db.legacy;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

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
	String SQLShort(int type, String ...s) {
		switch(type) {
			case AND:
				return TextUtils.join(" AND ", s);
			case OR:
				return TextUtils.join(" OR ", s);
			default:
				throw new IllegalArgumentException();
		}
	}
}
