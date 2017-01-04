package com.emmanuelmess.simpleaccounting;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
/**
 * @author Emmanuel
 *         on 2016-01-26, at 16:16.
 */
@ReportsCrashes(
		mailTo = "emmanuelbendavid@gmail.com",
		mode = ReportingInteractionMode.TOAST,
		resToastText = R.string.crash_toast_text)
/*
//This was commented bc I receive no notification.
@ReportsCrashes(
		formUri = "https://emmanuelmess.cloudant.com/acra-simpleaccounting/_design/acra-storage/_update/report",
		reportType = org.acra.sender.HttpSender.Type.JSON,
		httpMethod = org.acra.sender.HttpSender.Method.PUT,
		formUriBasicAuthLogin="greardelecarecessingstin",
		formUriBasicAuthPassword="607cb9c826c6f7e4eca1bee1540f8e285e5a0a17",
		// Your usual ACRA configuration
		mode = ReportingInteractionMode.TOAST,
		resToastText = R.string.crash_toast_text
)
*/
public class SimpleAccountingApplication extends Application {
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);

		// The following line triggers the initialization of ACRA
		ACRA.init(this);
	}
}

