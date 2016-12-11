package com.emmanuelmess.simpleaccounting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.support.annotation.RequiresApi;

import com.emmanuelmess.simpleaccounting.db.TableGeneral;
import com.emmanuelmess.simpleaccounting.db.TableMonthlyBalance;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Emmanuel
 *         on 11/12/2016, at 13:15.
 */

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class PPrintDocumentAdapter extends PrintDocumentAdapter {

	//IN  1/72"
	private final int DISTANCE_BETWEEN_LINES = 30,
			TOP_MARGIN = 100;

	private Context context;
	private TableGeneral tableGeneral;
	private TableMonthlyBalance tableMonthlyBalance;
	private int month, year;
	private Double prev;
	private String[][] rawToPrint;
	private PrintedPdfDocument pdfDocument;
	private int amountOfPages;

	public PPrintDocumentAdapter(Context c, TableGeneral tg, TableMonthlyBalance tmb, int m, int y) {
		super();

		context = c;
		tableGeneral = tg;
		tableMonthlyBalance = tmb;
		month = m;
		year = y;
	}

	@Override
	public void onStart() {
		super.onStart();
		prev = tableMonthlyBalance.getBalanceLastMonthWithData(month, year);
		rawToPrint = tableGeneral.getAllForMonth(month, year);
	}

	@Override
	public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
	                     CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback,
	                     Bundle bundle) {
		try {
			pdfDocument = new PrintedPdfDocument(context, newAttributes);

			if (cancellationSignal.isCanceled()) {
				layoutResultCallback.onLayoutCancelled();
				return;
			}

			int linesPerPage = (int) Math.ceil((float) (pdfDocument.getPageHeight() - 2*TOP_MARGIN)/DISTANCE_BETWEEN_LINES);
			amountOfPages = (int) Math.ceil((rawToPrint.length + (prev != null? 1 : 0))/((float) linesPerPage));

			PrintDocumentInfo info = new PrintDocumentInfo
					.Builder(context.getString(MainActivity.MONTH_STRINGS[month]) + "-" + year + ".pdf")
					.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
					.setPageCount(amountOfPages)
					.build();

			if(oldAttributes.getMediaSize() != null &&
					oldAttributes.getMediaSize().getHeightMils() == pdfDocument.getPageHeight())
				layoutResultCallback.onLayoutFinished(info, false);
			else layoutResultCallback.onLayoutFinished(info, true);
		} catch (Exception e) {
			layoutResultCallback.onLayoutFailed(e.getLocalizedMessage());
		}
	}

	@Override
	public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor parcelFileDescriptor,
	                    CancellationSignal cancellationSignal,
	                    WriteResultCallback writeResultCallback) {

		int clipW = pdfDocument.getPageContentRect().left;
		int clipH = pdfDocument.getPageContentRect().top;

		for (int i = 0; i < amountOfPages; i++) {
			if(isPageInRange(pageRanges, i)) {
				PdfDocument.Page page = pdfDocument.startPage(i);
				if (cancellationSignal.isCanceled()) {
					writeResultCallback.onWriteCancelled();
					pdfDocument.close();
					return;
				}

				Canvas c = page.getCanvas();
				Paint p = new Paint();
				p.setColor(Color.BLACK);
				p.setTextSize(10);

				c.drawText(context.getString(MainActivity.EDIT_IDS[0]), clipW + 100, clipH + TOP_MARGIN, p);
				c.drawText(context.getString(MainActivity.EDIT_IDS[1]), clipW + 200, clipH + TOP_MARGIN, p);
				c.drawText(context.getString(MainActivity.EDIT_IDS[2]), clipW + 300, clipH + TOP_MARGIN, p);
				c.drawText(context.getString(MainActivity.EDIT_IDS[3]),
						200 - pdfDocument.getPageContentRect().right, clipH + TOP_MARGIN, p);
				c.drawText(context.getString(MainActivity.EDIT_IDS[4]),
						100 - pdfDocument.getPageContentRect().right, clipH + TOP_MARGIN, p);

				pdfDocument.finishPage(page);
			}
		}

		try {
			pdfDocument.writeTo(new FileOutputStream(parcelFileDescriptor.getFileDescriptor()));
		} catch (IOException e) {
			writeResultCallback.onWriteFailed(e.getLocalizedMessage());
		} finally {
			pdfDocument.close();
		}

		writeResultCallback.onWriteFinished(pageRanges);
	}

	private boolean isPageInRange(PageRange[] p, int page) {
		for(PageRange pr : p)
			if(pr.getStart() <= page && page <= pr.getEnd())
				return true;

		return false;
	}

}
