package com.emmanuelmess.simpleaccounting.printing;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TableLayout;

import com.emmanuelmess.simpleaccounting.PrintHelper;
import com.emmanuelmess.simpleaccounting.R;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PrintDialogActivity extends Activity {
	private static final String PRINT_DIALOG_URL = "https://www.google.com/cloudprint/dialog.html";
	private static final String JS_INTERFACE = "AndroidPrintDialog";
	private static final String CONTENT_TRANSFER_ENCODING = "base64";

	private static final String ZXING_URL = "http://zxing.appspot.com";
	private static final int ZXING_SCAN_REQUEST = 65743;

	/**
	 * Post message that is sent by Print Dialog web page when the printing dialog
	 * needs to be closed.
	 */
	private static final String CLOSE_POST_MESSAGE_NAME = "cp-dialog-on-close";

	/**
	 * Web view element to show the printing dialog in.
	 */
	private WebView dialogWebView;

	/**
	 * Intent that started the action.
	 */
	Intent cloudPrintIntent;

	@Override
	@JavascriptInterface
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.activity_print_dialog);
		dialogWebView = (WebView) findViewById(R.id.webview);
		cloudPrintIntent = this.getIntent();

		WebSettings settings = dialogWebView.getSettings();
		settings.setJavaScriptEnabled(true);

		dialogWebView.setWebViewClient(new PrintDialogWebClient());
		dialogWebView.addJavascriptInterface(new PrintDialogJavaScriptInterface(), JS_INTERFACE);

		dialogWebView.loadUrl(PRINT_DIALOG_URL);
	}

	@Override
	@JavascriptInterface
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == ZXING_SCAN_REQUEST && resultCode == RESULT_OK) {
			dialogWebView.loadUrl(intent.getStringExtra("SCAN_RESULT"));
		}
	}

	final class PrintDialogJavaScriptInterface {
		public String getType() {
			return cloudPrintIntent.getType();
		}

		public String getTitle() {
			return cloudPrintIntent.getExtras().getString("title");
		}

		public String getContent() {
			try {
				ContentResolver contentResolver = getContentResolver();
				InputStream is = contentResolver.openInputStream(cloudPrintIntent.getData());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				byte[] buffer = new byte[4096];
				int n = is.read(buffer);
				while (n >= 0) {
					baos.write(buffer, 0, n);
					n = is.read(buffer);
				}
				is.close();
				baos.flush();

				return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "";
		}

		public String getEncoding() {
			return CONTENT_TRANSFER_ENCODING;
		}

		public void onPostMessage(String message) {
			if (message.startsWith(CLOSE_POST_MESSAGE_NAME)) {
				finish();
			}
		}
	}

	private final class PrintDialogWebClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith(ZXING_URL)) {
				Intent intentScan = new Intent("com.google.zxing.client.android.SCAN");
				intentScan.putExtra("SCAN_MODE", "QR_CODE_MODE");
				try {
					startActivityForResult(intentScan, ZXING_SCAN_REQUEST);
				} catch (ActivityNotFoundException error) {
					view.loadUrl(url);
				}
			} else {
				view.loadUrl(url);
			}
			return false;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			if (PRINT_DIALOG_URL.equals(url)) {
				// Submit print document.
				view.loadUrl("javascript:printDialog.setPrintDocument(printDialog.createPrintDocument("
						+ "window." + JS_INTERFACE + ".getType(),window." + JS_INTERFACE + ".getTitle(),"
						+ "window." + JS_INTERFACE + ".getContent(),window." + JS_INTERFACE + ".getEncoding()))");

				// Add post messages listener.
				view.loadUrl("javascript:window.addEventListener('message',"
						+ "function(evt){window." + JS_INTERFACE + ".onPostMessage(evt.data)}, false)");
			}
		}
	}

	public static class CreatePrintablePDF {

		public static Uri create(Context c, TableLayout t, int firstRealRow, int m, int y, int[] updateDate)
				throws IOException, DocumentException {
			String file = c.getCacheDir() + PrintHelper.createName(c, m, y, updateDate[0], updateDate[1]);

			// step 1
			Document document = new Document();
			// step 2
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
			// step 3
			document.open();
			// step 4
			// writes something to the direct content using a convenience method
			Phrase hello = new Phrase("Hello World");
			PdfContentByte canvas = writer.getDirectContentUnder();
			ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, hello, 36, 788, 0);
			// step 5
			document.close();

			return Uri.parse(file);
		}

	}

}