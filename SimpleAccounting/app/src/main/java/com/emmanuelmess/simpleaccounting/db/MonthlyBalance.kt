package com.emmanuelmess.simpleaccounting.db

import androidx.room.*

@Dao
interface MonthlyBalanceDao {
	@Query("SELECT * FROM monthly_balance")
	fun getAll(): List<MonthlyBalance>

	@Insert
	fun insert(rows: Collection<MonthlyBalance>)


}

@Entity(tableName = "monthly_balance")
data class MonthlyBalance(
	@PrimaryKey val uid: Int?,
	@ColumnInfo(name = "month") val month: Int,
	@ColumnInfo(name = "year") val year: Int,
	@ColumnInfo(name = "currency") val currency: String?,
	@ColumnInfo(name = "balance") val balance: Int
)