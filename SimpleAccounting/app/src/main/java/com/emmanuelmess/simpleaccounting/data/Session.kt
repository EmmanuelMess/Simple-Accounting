package com.emmanuelmess.simpleaccounting.data

import android.os.Parcelable
import com.emmanuelmess.simpleaccounting.db.TableGeneral
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Session(
	val month: Int,
	val year: Int,
	val currency: String
): Parcelable {
	fun isOlderThanUpdate() = month == TableGeneral.OLDER_THAN_UPDATE || year == TableGeneral.OLDER_THAN_UPDATE
}