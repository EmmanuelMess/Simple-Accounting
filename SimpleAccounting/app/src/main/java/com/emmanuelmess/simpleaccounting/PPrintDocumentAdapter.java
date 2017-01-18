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
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import com.emmanuelmess.simpleaccounting.db.TableGeneral;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.emmanuelmess.simpleaccounting.MainActivity.MONTH_STRINGS;

/**
 * @author Emmanuel
 *         on 11/12/2016, at 13:15.
 */

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class PPrintDocumentAdapter extends PrintDocumentAdapter {

	//IN  1/72"
	private final int DISTANCE_BETWEEN_LINES = 30,
			TOP_MARGIN = 100;

	private Context c;
	private TableLayout table;
	private int month, year;
	private int updateMonth, updateYear;
	private int firstRealRow;
	private String[][] rawToPrint;
	private PrintedPdfDocument pdfDocument;
	private String title;
	private int linesPerPage, amountOfPages;

	public PPrintDocumentAdapter(Context c, TableLayout t, int firstRealRow, int m, int y, int[] updateDate) {
		super();

		this.c = c;
		table = t;
		this.firstRealRow = firstRealRow;
		month = m;
		year = y;
		if(updateDate != null) {
			updateMonth = updateDate[0];
			updateYear = updateDate[1];
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		rawToPrint = new String[table.getChildCount()-1][5];//-1 to account for the header, but not last balance

		for(int i = 0; i < rawToPrint.length; i++) {
			View row = table.getChildAt(i+1);//+1 to account for the header
			rawToPrint[i] = new String[5];

			for(int j = 0; j < 4; j++)
				rawToPrint[i][j] = row.findViewById(MainActivity.TEXT_IDS[j]) != null?
						((TextView) row.findViewById(MainActivity.TEXT_IDS[j])).getText().toString():"";

			rawToPrint[i][4] = row.findViewById(R.id.textBalance) != null?
					((TextView) row.findViewById(R.id.textBalance)).getText().toString():"";

		}

		//rawToPrint = tableGeneral.getAllForMonth(month, year);
	}

	@Override
	public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
	                     CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback,
	                     Bundle bundle) {
		try {
			pdfDocument = new PrintedPdfDocument(c, newAttributes);

			if (cancellationSignal.isCanceled()) {
				layoutResultCallback.onLayoutCancelled();
				return;
			}

			linesPerPage = (int) Math.ceil((float) (pdfDocument.getPageHeight() - 2*TOP_MARGIN)/DISTANCE_BETWEEN_LINES);
			//amountOfPages = (int) Math.ceil((rawToPrint.length + (prev != null? 1 : 0))/((float) linesPerPage));
			amountOfPages = (int) Math.ceil(rawToPrint.length/((float) linesPerPage));

			if(year != TableGeneral.OLDER_THAN_UPDATE)
				title = c.getString(MONTH_STRINGS[month]) + "-" + year + ".pdf";
			else title = c.getString(R.string.before_update_1_2)
						+ " " + c.getString(MainActivity.MONTH_STRINGS[updateMonth]).toLowerCase()
						+ "-" + String.valueOf(updateYear) + ".pdf";

			PrintDocumentInfo info = new PrintDocumentInfo.Builder(title)
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
				
				p.setTextAlign(Paint.Align.CENTER);
				c.drawText(title, c.getWidth()/2, clipH + TOP_MARGIN/2f, p);//TODO correct centration

				p.setTextAlign(Paint.Align.CENTER);
				c.drawText(this.c.getString(R.string.date), clipW + 100, clipH + TOP_MARGIN, p);
				c.drawText(this.c.getString(R.string.credit), clipW + 300, clipH + TOP_MARGIN, p);
				c.drawText(this.c.getString(R.string.debit),
						pdfDocument.getPageContentRect().right - 200, clipH + TOP_MARGIN, p);
				c.drawText(this.c.getString(R.string.balance),
						pdfDocument.getPageContentRect().right - 100, clipH + TOP_MARGIN, p);

				p.setTextAlign(Paint.Align.LEFT);
				c.drawText(this.c.getString(R.string.reference), clipW + 200, clipH + TOP_MARGIN, p);

				p.setTextAlign(Paint.Align.CENTER);
				for(int j = i*linesPerPage, k = 1; j <= linesPerPage && j < rawToPrint.length; j++, k++) {
					c.drawText(rawToPrint[j][0], clipW + 100, clipH + TOP_MARGIN + DISTANCE_BETWEEN_LINES*k, p);

					p.setTextAlign(Paint.Align.LEFT);
					c.drawText(rawToPrint[j][1], clipW + 200, clipH + TOP_MARGIN + DISTANCE_BETWEEN_LINES*k, p);

					p.setTextAlign(Paint.Align.CENTER);
					c.drawText(rawToPrint[j][2], clipW + 300, clipH + TOP_MARGIN + DISTANCE_BETWEEN_LINES*k, p);
					c.drawText(rawToPrint[j][3],
							pdfDocument.getPageContentRect().right - 200,
							clipH + TOP_MARGIN + DISTANCE_BETWEEN_LINES*k, p);
					c.drawText(rawToPrint[j][4],
							pdfDocument.getPageContentRect().right - 100,
							clipH + TOP_MARGIN + DISTANCE_BETWEEN_LINES*k, p);
				}

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