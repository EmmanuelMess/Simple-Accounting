package com.emmanuelmess.simpleaccounting

import android.app.Application
import android.content.Context

import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraMailSender
import org.acra.annotation.AcraToast

@AcraCore(buildConfigClass = BuildConfig::class)
@AcraMailSender(
    mailTo = "emmanuelbendavid@gmail.com",
    reportAsFile = true
)
@AcraToast(resText = R.string.crash_toast_text)
class SimpleAccountingApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        // The following line triggers the initialization of ACRA
        if (!BuildConfig.DEBUG) ACRA.init(this)
    }
}

