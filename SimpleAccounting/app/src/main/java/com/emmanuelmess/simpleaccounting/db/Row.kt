package com.emmanuelmess.simpleaccounting.db

import androidx.room.*

@Dao
interface RowDao {
	@Query("SELECT * FROM rows")
	fun getAll(): List<Row>

	@Insert
	fun insert(rows: Collection<Row>)
}

@Entity(tableName = "rows")
data class Row(
	@PrimaryKey(autoGenerate = true) val uid: Int?,
	@ColumnInfo(name = "date") val date: Int,
	@ColumnInfo(name = "reference") val reference: String,
	@ColumnInfo(name = "credit") val credit: Int,
	@ColumnInfo(name = "debit") val debit: Int,
	@ColumnInfo(name = "month") val month: Int,
	@ColumnInfo(name = "year") val year: Int,
	@ColumnInfo(name = "currency") val currency: String?
)