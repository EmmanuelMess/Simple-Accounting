package com.emmanuelmess.simpleaccounting.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Session(
	val month: Int,
	val year: Int,
	val currency: String
): Parcelable