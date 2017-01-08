package com.emmanuelmess.simpleaccounting.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.emmanuelmess.simpleaccounting.Utils;

import java.math.BigDecimal;

import static java.lang.String.format;

/**
 * @author Emmanuel
 *         on 2016-01-31, at 15:53.
 */
public class TableGeneral extends Database {

	public static final int OLDER_THAN_UPDATE = -2;
	public static final String[] COLUMNS = new String[] { "DATE", "REFERENCE", "CREDIT", "DEBT", "MONTH", "YEAR"};

	private static final int DATABASE_VERSION = 4;
	private static final String TABLE_NAME = "ACCOUNTING";
	private static final String TABLE_CREATE = format("CREATE TABLE %1$s" +
			" (%2$s INT, %3$s INT, %4$s TEXT, %5$s REAL, %6$s REAL, %7$s INT, %8$s INT);",
			TABLE_NAME, NUMBER_COLUMN, COLUMNS[0], COLUMNS[1], COLUMNS[2], COLUMNS[3], COLUMNS[4], COLUMNS[5]);
	private final ContentValues CV = new ContentValues();

	public TableGeneral(Context context) {super(context, TABLE_NAME, null, DATABASE_VERSION);}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql;
		final String tempTable = "temp";
		boolean solvingMistake = false;

		/*I made a mistake on update 1.1.4, this should undo that*/
		if(oldVersion == 3) {
			oldVersion = 1;
			solvingMistake = true;
		}

		switch (oldVersion) {
			case 1:
				sql = "CREATE TEMPORARY TABLE " + tempTable + "(" + COLUMNS[0] + "," + COLUMNS[1] + "," + COLUMNS[2] + "," + COLUMNS[3] + ");" +
						"INSERT INTO " + tempTable + " SELECT " + COLUMNS[0] + "," + COLUMNS[1] + "," + COLUMNS[2] + "," + COLUMNS[3] + " FROM " + TABLE_NAME + ";" +
						"DROP TABLE " + TABLE_NAME + ";" +
						"CREATE TABLE " + TABLE_NAME + "(" + COLUMNS[0] + "," + COLUMNS[1] + "," + COLUMNS[2] + "," + COLUMNS[3] + ");" +
						"INSERT INTO " + TABLE_NAME + " SELECT " + COLUMNS[0] + "," + COLUMNS[1] + "," + COLUMNS[2] + "," + COLUMNS[3] + " FROM " + tempTable + ";" +
						"DROP TABLE " + tempTable + ";";
				db.execSQL(sql);//"copy, drop table, create new table, copy back" technique bc ALTER...DROP COLUMN isn't in SQLite
			case 2:
				/*Updates this table*/{
					if(!solvingMistake) {
						sql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMNS[4] + " INT;";
						db.execSQL(sql);

						sql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMNS[5] + " INT;";
						db.execSQL(sql);
					}

					Cursor c = db.query(TABLE_NAME, new String[]{COLUMNS[0]}, null, null, null, null, null);
					CV.put(COLUMNS[4], OLDER_THAN_UPDATE);
					CV.put(COLUMNS[5], OLDER_THAN_UPDATE);
					for (int i = 0; i < c.getCount(); i++)
						db.update(TABLE_NAME, CV, NUMBER_COLUMN + "=" + i, null);
					CV.clear();
					c.close();
				}

				/*Updates MonthlyBalance*/ {
					TableMonthlyBalance tableMonthlyBalance = new TableMonthlyBalance(super.context);
					BigDecimal currentBalance = BigDecimal.ZERO;
					String[][] all = getAllForMonth(OLDER_THAN_UPDATE, OLDER_THAN_UPDATE, db);

					for (String[] data : all) {
						if (data[2] != null)
							currentBalance = currentBalance.add(Utils.parseString(data[2]));
						if (data[3] != null)
							currentBalance = currentBalance.subtract(Utils.parseString(data[3]));
					}

					tableMonthlyBalance.updateMonth(OLDER_THAN_UPDATE, OLDER_THAN_UPDATE, currentBalance.doubleValue());
				}
		}
	}

	public void update(int row, String column, String data) {
		CV.put(column, data);
		getWritableDatabase().update(TABLE_NAME, CV, NUMBER_COLUMN + "=" + row, null);
		CV.clear();
	}

	public void newRowInMonth(int month, int year) {
		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{NUMBER_COLUMN},
				null, null, null, null, null);
		int i;

		if(c.getCount() == 0) {
			i = 0;
		} else {
			c.moveToLast();
			i = c.getInt(0) + 1;
			c.close();
		}

		CV.put(NUMBER_COLUMN, i);
		CV.put(COLUMNS[4], month);
		CV.put(COLUMNS[5], year);
		getWritableDatabase().insert(TABLE_NAME, null, CV);
		CV.clear();
	}

	public int[][] getMonthsWithData() {
		return getMonthsWithData(getReadableDatabase());
	}

	private int[][] getMonthsWithData(SQLiteDatabase db) {
		int[][] data;

		Cursor c = db.query(TABLE_NAME, new String[] {COLUMNS[4], COLUMNS[5]},
				null, null, COLUMNS[4], null, null);

		if (c != null) {
			c.moveToFirst();
		} else return new int[0][0];

		data = new int[c.getCount()][2];
		for(int x = 0; x < c.getCount(); x++) {
			if(c.getString(0) != null)
				data[x]= new int[]{c.getInt(0), c.getInt(1)};
			else data[x] = new int[]{-1, -1};
			c.moveToNext();
		}
		c.close();

		return data;
	}

	public String[][] getAllForMonth(int month, int year) {
		return getAllForMonth(month, year, getReadableDatabase());
	}

	private String[][] getAllForMonth(int month, int year, SQLiteDatabase db) {
		String [][] data;

		Cursor c = db.query(TABLE_NAME, COLUMNS,
				SQLShort(AND, COLUMNS[4] + "=" + month, COLUMNS[5] + "=" + year),
				null, null, null, COLUMNS[0]);

		if (c != null) {
			c.moveToFirst();
		} else return new String[0][0];

		data = new String[c.getCount()][COLUMNS.length];
		for(int x = 0; x < data.length; x++) {
			for(int y = 0; y < COLUMNS.length; y++){
				data[x][y] = c.getString(y);
			}
			c.moveToNext();
		}
		c.close();

		return data;
	}

	public int[] getIndexesForMonth(int month, int year) {
		int[] data;

		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{NUMBER_COLUMN},
				format("%1$s = %2$s AND %3$s = %4$s", COLUMNS[4], month, COLUMNS[5], year),
				null, null, null, COLUMNS[0]);

		if (c != null) {
			c.moveToFirst();
		} else return new int[0];

		data = new int[c.getCount()];
		for(int x = 0; x < data.length; x++) {
			data[x] = c.getInt(0);
			c.moveToNext();
		}
		c.close();

		return data;
	}

	public int getLastIndex() {
		Cursor c = getReadableDatabase().query(TABLE_NAME, new String[]{NUMBER_COLUMN},
				format("%1$s = (SELECT MAX(%1$s) FROM %2$s)", NUMBER_COLUMN, TABLE_NAME),
				null, null, null, COLUMNS[0]);
		c.moveToFirst();
		int data = c.getInt(0);
		c.close();
		return data;
	}

}
