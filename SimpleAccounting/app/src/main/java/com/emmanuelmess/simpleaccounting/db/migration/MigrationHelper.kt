package com.emmanuelmess.simpleaccounting.db.migration

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.Room
import com.emmanuelmess.simpleaccounting.db.AppDatabase
import com.emmanuelmess.simpleaccounting.db.MonthlyBalance
import com.emmanuelmess.simpleaccounting.db.Row
import com.emmanuelmess.simpleaccounting.db.legacy.TableGeneral
import com.emmanuelmess.simpleaccounting.db.legacy.TableMonthlyBalance

val MIGRATED_DATABASE_SETTING = "migrated database"

class MigrationHelper(val applicationContext: Context) {
	val database = Room.databaseBuilder(
		applicationContext,
		AppDatabase::class.java, "database-simple-accounting"
	).build()
	val tableGeneral = TableGeneral(applicationContext)
	val tableMonthlyBalance = TableMonthlyBalance(applicationContext)

	@WorkerThread
	fun migrate() {
		val dbTableGeneral = tableGeneral.writableDatabase

		dbTableGeneral.beginTransaction()
		try {
			dbTableGeneral.rawQuery(
				"SELECT ${TableGeneral.COLUMNS[0]}, ${TableGeneral.COLUMNS[1]}, " +
					"${TableGeneral.COLUMNS[2]}, ${TableGeneral.COLUMNS[3]}, " +
					"${TableGeneral.COLUMNS[4]}, ${TableGeneral.COLUMNS[5]}, " +
					"${TableGeneral.COLUMNS[6]} FROM ${TableGeneral.TABLE_NAME}",
				null
			).let { cursor ->
				val rows = generateSequence { if (cursor.moveToNext()) cursor else null }
					.map { itRow ->
						Row(
							null,
							itRow.getInt(1),
							itRow.getString(2),
							itRow.getInt(3),
							itRow.getInt(4),
							itRow.getInt(5),
							itRow.getInt(6),
							itRow.getString(7)
						)
					}.toList()
				database.rowDao().insert(rows)

				cursor.close()
			}
			dbTableGeneral.setTransactionSuccessful()
			applicationContext.deleteDatabase(TableGeneral.TABLE_NAME)
		} finally {
			dbTableGeneral.endTransaction()
		}

		val dbTableMonthlyBalance = tableMonthlyBalance.readableDatabase

		dbTableMonthlyBalance.beginTransaction()

		try {
			dbTableMonthlyBalance.rawQuery(
				"SELECT ${TableMonthlyBalance.COLUMNS[0]}, ${TableMonthlyBalance.COLUMNS[1]}, " +
					"${TableMonthlyBalance.COLUMNS[2]}, ${TableMonthlyBalance.COLUMNS[3]} " +
					"FROM ${TableMonthlyBalance.TABLE_NAME}",
				null
			).let { cursor ->
				val rows = generateSequence { if (cursor.moveToNext()) cursor else null }
					.map { itRow ->
						MonthlyBalance(
							null,
							itRow.getInt(1),
							itRow.getInt(2),
							itRow.getString(3),
							itRow.getInt(4)
						)
					}.toList()
				database.monthlyBalanceDao().insert(rows)

				cursor.close()
			}

			dbTableMonthlyBalance.setTransactionSuccessful()
			applicationContext.deleteDatabase(TableMonthlyBalance.TABLE_NAME)
		} finally {
			dbTableMonthlyBalance.endTransaction()
		}
	}
}