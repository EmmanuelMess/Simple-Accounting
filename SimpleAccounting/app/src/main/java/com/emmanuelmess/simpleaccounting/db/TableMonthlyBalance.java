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
	//Beware the columns in this array may not be in the real order
	private static final String[] COLUMNS = new String[] {"MONTH", "YEAR", "CURRENCY", "BALANCE"};
	private static final String TABLE_NAME = "MONTHLY_BALANCE";

	private static final int DATABASE_VERSION = 4;
	public static final String TABLE_CREATE = format("CREATE TABLE %1$s(%2$s INT, %3$s INT, %4$s INT, %5$s TEXT, %6$s REAL);",
			TABLE_NAME, NUMBER_COLUMN, COLUMNS[0], COLUMNS[1], COLUMNS[2], COLUMNS[3]);

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
			case 2:
				sql = "ALTER TABLE " + TABLE_NAME + " ADD " + COLUMNS[2] + " TEXT default '';";
				db.execSQL(sql);
		}
	}

	public void updateMonth(int month, int year, String currency, double balance) {
		if(!isMonthInBD(month, year, currency))
			createMonth(month, year, currency);

		CV.put(COLUMNS[3], balance);
		getWritableDatabase().update(TABLE_NAME, CV, SQLShort(AND, format("%1$s=%2$s" , COLUMNS[0], month),
				format("%1$s=%2$s" , COLUMNS[1], year), format("%1$s=%2$s" , COLUMNS[2], "?")), new String[] {currency});
		CV.clear();
	}

	public Double getBalanceLastMonthWithData(int month, int year, String currency) {
		double data = 0;

		String condMonth = SQLShort(AND, format("%1$s<%2$s" , COLUMNS[0], month), format("%1$s=%2$s" , COLUMNS[1] , year)),
		condYear = SQLShort(OR, format("%1$s<%2$s" , COLUMNS[1], year), "(" + condMonth + ")"),
		condCurrency = SQLShort(AND, "(" + condYear + ")", format("%1$s=%2$s" , COLUMNS[2], "?"));

		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[] {COLUMNS[3]}, condCurrency,
				new String[] {currency}, null, null, null);

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

	public void deleteAllForCurrency(String currency) {
		String condition = format("%1$s=%2$s" , COLUMNS[2], "?");
		getWritableDatabase().delete(TABLE_NAME, condition, new String[] {currency});
	}

	private boolean isMonthInBD(int month, int year, String currency) {
		boolean data;
		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[] {COLUMNS[3]},
				SQLShort(AND, format("%1$s=%2$s" , COLUMNS[0], month),
						format("%1$s=%2$s" , COLUMNS[1], year),
						format("%1$s=%2$s" , COLUMNS[2], "?")),
				new String[] {currency}, null, null, null, "1");

		data = c.getCount() != 0;
		c.close();

		return data;
	}

	private void createMonth(int month, int year, String currency) {
		if(!isMonthInBD(month, year, currency)) {
			CV.put(COLUMNS[0], month);
			CV.put(COLUMNS[1], year);
			CV.put(COLUMNS[2], currency);
			CV.put(COLUMNS[3], 0);
			getWritableDatabase().insert(TABLE_NAME, null, CV);
			CV.clear();
		}
	}

}
