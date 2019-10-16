package com.emmanuelmess.simpleaccounting.db

import androidx.room.*

@Dao
interface MonthlyBalanceDao {
	@Query("SELECT * FROM monthly_balance")
	fun getAll(): List<MonthlyBalance>

	@Insert
	fun insert(rows: MonthlyBalance)

	@Query("SELECT EXISTS(SELECT * FROM monthly_balance WHERE month = :month AND year = :year AND currency = :currency LIMIT 1)")
	fun checkForMonth(month: Int, year: Int, currency: String?): Boolean

	@Query("UPDATE monthly_balance SET balance = balance WHERE month = :month AND year = :year AND currency = :currency")
	fun updateMonth(month: Int, year: Int, currency: String?, balance: Double)

	fun updateMonthCreating(month: Int, year: Int, currency: String?, balance: Double) {
		if(!checkForMonth(month, year, currency)) {
			insert(MonthlyBalance(null, month, year, currency, balance))
		}

		updateMonth(month, year, currency, balance)
	}
}

@Entity(tableName = "monthly_balance")
data class MonthlyBalance(
	@PrimaryKey val uid: Int?,
	@ColumnInfo(name = "month") val month: Int,
	@ColumnInfo(name = "year") val year: Int,
	@ColumnInfo(name = "currency") val currency: String?,
	@ColumnInfo(name = "balance") val balance: Double
)