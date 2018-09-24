package com.emmanuelmess.simpleaccounting.dataloading.async

interface AsyncFinishedListener<T> {
    fun onAsyncFinished(rowToDBRowConversion: T)
}
