package com.emmanuelmess.simpleaccounting.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static java.lang.String.format;

/**
 * @author Emmanuel
 *         on 14/11/2016, at 16:52.
 */
public class TableMonthlyBalance extends Database {
	private static final String[] COLUMNS = new String[] {"MONTH", "YEAR", "BALANCE"};
	public static final String TABLE_NAME = "MONTHLY_BALANCE";

	private static final int DATABASE_VERSION = 2;
	private static final String TABLE_CREATE = format("CREATE TABLE %1$s(%2$s INT, %3$s INT, %4$s INT, %5$s REAL);",
			TABLE_NAME, NUMBER_COLUMN, COLUMNS[0], COLUMNS[1], COLUMNS[2]);

	public TableMonthlyBalance(Context context) {
		super(context, TABLE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch(oldVersion) {
			/*I made a mistake on update 1.1.4, this should undo that*/
			case 1:
				String sql = "DROP TABLE " + TableMonthlyBalance.TABLE_NAME;
				db.execSQL(sql);
				db.execSQL(TABLE_CREATE);
		}
	}

	public void updateMonth(int month, int year, double balance) {
		if(!isMonthInBD(month, year))
			createMonth(month, year);

		CV.put(COLUMNS[2], balance);
		getWritableDatabase().update(TABLE_NAME, CV, SQLShort(AND, format("%1$s=%2$s" , COLUMNS[0], month),
				format("%1$s=%2$s" , COLUMNS[1], year)), null);
		CV.clear();
	}

	public Double getBalanceLastMonthWithData(int month, int year) {
		double data = 0;

		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[] {COLUMNS[2]},
				SQLShort(OR, format("%1$s<%2$s" , COLUMNS[1], year),
					"(" + SQLShort(AND, format("%1$s<%2$s" , COLUMNS[0], month),
							format("%1$s=%2$s" , COLUMNS[1] , year)) + ")"),
				null, null, null, null);

		c.moveToFirst();

		if(c.getCount() == 0)
			return null;
		else {
			for(int i = 0; i < c.getCount(); i++) {
				data += c.getDouble(0);
				c.moveToNext();
			}
		}

		c.close();

		return data;
	}

	private boolean isMonthInBD(int month, int year) {
		boolean data;
		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[] {COLUMNS[2]},
				SQLShort(AND, format("%1$s=%2$s" , COLUMNS[0], month),
						format("%1$s=%2$s" , COLUMNS[1], year)),
				null, null, null, null, "1");

		data = c.getCount() != 0;
		c.close();

		return data;
	}

	private void createMonth(int month, int year) {
		if(!isMonthInBD(month, year)) {
			CV.put(COLUMNS[0], month);
			CV.put(COLUMNS[1], year);
			CV.put(COLUMNS[2], 0);
			getWritableDatabase().insert(TABLE_NAME, null, CV);
			CV.clear();
		}
	}

}
