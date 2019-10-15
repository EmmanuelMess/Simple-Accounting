package com.emmanuelmess.simpleaccounting.db

import androidx.room.*
import androidx.room.Database

@Database(entities = arrayOf(Row::class, MonthlyBalance::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
	abstract fun rowDao(): RowDao
	abstract fun monthlyBalanceDao(): MonthlyBalanceDao
}