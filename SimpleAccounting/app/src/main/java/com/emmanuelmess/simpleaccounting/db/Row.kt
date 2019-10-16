package com.emmanuelmess.simpleaccounting.db

import androidx.room.*
import com.emmanuelmess.simpleaccounting.data.Session

@Dao
interface RowDao {
	@Query("SELECT * FROM rows")
	fun getAll(): List<Row>

	@Insert
	fun insert(rows: Collection<Row>)

	@Insert
	fun insert(row: Row)

	fun newRowInMonth(day: String?, session: Session) {
		insert(Row(null, day, "", null, null, session.month, session.year, session.currency))
	}

	@Query("SELECT id FROM rows WHERE id = (SELECT MAX(id) FROM rows) ORDER BY date")
	fun getLastIndex(): Int

	@Query("UPDATE rows SET date = :date, reference = :reference, credit = :credit, debit = :debit WHERE id = :id")
	fun update(id: Int, date: String, reference: String, credit: String, debit: String)
}

@Entity(tableName = "rows")
data class Row(
	@PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val uid: Int?,
	@ColumnInfo(name = "date") val date: String?,
	@ColumnInfo(name = "reference") val reference: String,
	@ColumnInfo(name = "credit") val credit: String?,
	@ColumnInfo(name = "debit") val debit: String?,
	@ColumnInfo(name = "month") val month: Int,
	@ColumnInfo(name = "year") val year: Int,
	@ColumnInfo(name = "currency") val currency: String?
)