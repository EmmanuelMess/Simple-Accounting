package com.emmanuelmess.simpleaccounting.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import static java.lang.String.format;

/**
 * @author Emmanuel
 *         on 14/11/2016, at 16:52.
 */
public class DBMonthlyBalance extends DBs {
	private static final String[] COLUMNS = new String[] {"MONTH", "YEAR", "BALANCE"};

	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "MONTHLY_BALANCE";
	private static final String TABLE_CREATE = format("CREATE TABLE %1$s(%2$s INT, %3$s INT, %4$s INT, %5$s REAL);",
			TABLE_NAME, NUMBER_COLUMN, COLUMNS[0], COLUMNS[1], COLUMNS[2]);
	private DBGeneral dbGeneral = null;

	public DBMonthlyBalance(Context context, @Nullable DBGeneral dbGeneral) {
		super(context, TABLE_NAME, null, DATABASE_VERSION);
		this.dbGeneral = dbGeneral;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
		if(dbGeneral != null) {
			int [][] existentMonths = dbGeneral.getMonthsWithData();
			for(int[] month : existentMonths) {
				int m  = month[0], y = month[1];
				String[][] all = dbGeneral.getAllForMonth(m, y);

				Thread createMonth = (new Thread() {
					@Override
					public void run() {
						super.run();
						createMonth(m, y);
					}
				});

				double currentBalance;

				currentBalance = 0;

				for(String[] data : all) {
					if(data[2] != null)
						currentBalance += Integer.parseInt(data[2]);
					if(data[3] != null)
						currentBalance -= Integer.parseInt(data[3]);
				}

				try {
					createMonth.join();
					updateMonth(m, y, currentBalance);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public void createMonth(int month, int year) {
		if(isMonthInBD(month, year)) {
			Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{NUMBER_COLUMN},
					null, null, null, null, null);
			int i;

			c.moveToFirst();

			if (c.getCount() == 0) {
				i = 0;
			} else {
				c.moveToLast();
				i = c.getInt(0) + 1;
				c.close();
			}

			CV.put(NUMBER_COLUMN, i);
			CV.put(COLUMNS[0], month);
			CV.put(COLUMNS[1], year);
			CV.put(COLUMNS[2], 0);
			getWritableDatabase().insert(TABLE_NAME, null, CV);
			CV.clear();
		}
	}

	public void updateMonth(int month, int year, double balance) {
		CV.put(COLUMNS[2], balance);
		getWritableDatabase().update(TABLE_NAME, CV, SQLShort(AND, format("%1$s=%2$s" , COLUMNS[0], month),
				format("%1$s=%2$s" , COLUMNS[1], year)), null);
		CV.clear();
	}

	public double getBalanceLastMonthWithData(int month, int year) {
		double data = 0;

		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[] {COLUMNS[2]},
				SQLShort(OR, format("%1$s<%2$s" , COLUMNS[1], year),
					SQLShort(AND, format("%1$s<%2$s" , COLUMNS[0], month),
							format("%1$s=%2$s" , COLUMNS[1], year))),
				null, null, null, null);

		c.moveToFirst();

		if(c.getCount() == 0)
			data = -1;
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

}
