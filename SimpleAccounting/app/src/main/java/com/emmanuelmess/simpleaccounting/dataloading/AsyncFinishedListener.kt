package com.emmanuelmess.simpleaccounting.dataloading

interface AsyncFinishedListener<T> {
    fun onAsyncFinished(rowToDBRowConversion: T)
}
