package com.emmanuelmess.simpleaccounting

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import android.view.View
import android.widget.TableLayout
import android.widget.TextView
import androidx.annotation.RequiresApi

import com.emmanuelmess.simpleaccounting.activities.MainActivity
import com.emmanuelmess.simpleaccounting.data.Session
import com.emmanuelmess.simpleaccounting.utils.Utils
import com.emmanuelmess.simpleaccounting.utils.get

import java.io.FileOutputStream
import java.io.IOException

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
class PPrintDocumentAdapter(
    private val context: Context,
    private val table: TableLayout,
    private val session: Session,
    private val updateDate: IntArray
) : PrintDocumentAdapter() {

    companion object {
        //IN  1/72"
        private const val DISTANCE_BETWEEN_LINES = 30
        private const val TOP_MARGIN = 100

        fun areAllTrue(array: BooleanArray): Boolean {
            for (b in array) if (!b) return false
            return true
        }
    }

    private lateinit var rawToPrint: List<List<String>>
    private lateinit var pdfDocument: PrintedPdfDocument
    private lateinit var title: String
    private var oldLinesPerPage = -1
    private var oldAmountOfPages = -1
    private var linesPerPage: Int = 0
    private var amountOfPages: Int = 0
    private lateinit var writtenPages: BooleanArray

    override fun onStart() {
        super.onStart()

        val raw = mutableListOf<List<String>>()

        for (i in 0 until (table.childCount - 1)) {
            val row = table.get<View>(i + 1)!!//+1 to account for the header
            val list = mutableListOf<String>()

            for (j in 0..3) {
                if (row.findViewById<View>(MainActivity.TEXT_IDS[j]) == null) {
                    list.add("")
                    continue
                }

                list.add(row.findViewById<TextView>(MainActivity.TEXT_IDS[j]).text.toString())
            }

            if(row.findViewById<View>(R.id.textBalance) == null) {
                list.add("")
                continue
            }

            list.add(row.findViewById<TextView>(R.id.textBalance).text.toString())

            raw.add(list)
        }

        rawToPrint = raw
    }

    override fun onLayout(oldAttributes: PrintAttributes, newAttributes: PrintAttributes,
                          cancellationSignal: CancellationSignal, layoutResultCallback: PrintDocumentAdapter.LayoutResultCallback,
                          bundle: Bundle) {
        try {
            pdfDocument = PrintedPdfDocument(context, newAttributes)

            if (cancellationSignal.isCanceled) {
                layoutResultCallback.onLayoutCancelled()
                return
            }

            linesPerPage = Math.ceil(((pdfDocument.pageHeight - 2 * TOP_MARGIN).toFloat() / DISTANCE_BETWEEN_LINES).toDouble()).toInt()
            amountOfPages = Math.ceil((rawToPrint.size / linesPerPage.toFloat()).toDouble()).toInt()

            writtenPages = BooleanArray(amountOfPages)

            title = Utils.getTitle(context, session, updateDate)

            val info = PrintDocumentInfo.Builder(title + ".pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(amountOfPages)
                    .build()

            val layoutChanged = !(oldLinesPerPage == linesPerPage && oldAmountOfPages == amountOfPages)

            oldLinesPerPage = linesPerPage
            oldAmountOfPages = amountOfPages

            layoutResultCallback.onLayoutFinished(info, layoutChanged)
        } catch (e: Exception) {
            layoutResultCallback.onLayoutFailed(e.localizedMessage)
        }

    }

    override fun onWrite(pageRanges: Array<PageRange>, parcelFileDescriptor: ParcelFileDescriptor,
                         cancellationSignal: CancellationSignal, writeResultCallback: PrintDocumentAdapter.WriteResultCallback) {
        val clipW = pdfDocument.pageContentRect.left
        val clipH = pdfDocument.pageContentRect.top

        val end = getEnd(pageRanges)

        //WARNING && i < writtenPages.length is a workaround because Android gives impossible PageRange (check 31 items in A2 page)
        var i = getStart(pageRanges)
        while (i <= end && i < writtenPages.size) {
            if (isPageInRange(pageRanges, i) && !writtenPages[i]) {
                val page = pdfDocument.startPage(i)
                if (cancellationSignal.isCanceled) {
                    writeResultCallback.onWriteCancelled()
                    pdfDocument.close()
                    return
                }

                val canvasEnd = pdfDocument.pageContentRect.right
                val c = page.canvas
                val p = Paint()
                p.color = Color.BLACK
                p.textSize = 10f

                p.textAlign = Paint.Align.CENTER
                c.drawText(
                        title + " (" + context.getString(R.string.page) + " " + (i + 1) + "/" + amountOfPages + ")",
                        (c.width / 2).toFloat(), clipH + TOP_MARGIN / 2f, p)//TODO correct centration

                p.textAlign = Paint.Align.CENTER
                c.drawText(this.context.getString(R.string.date), (clipW + 100).toFloat(), (clipH + TOP_MARGIN).toFloat(), p)

                p.textAlign = Paint.Align.LEFT
                c.drawText(this.context.getString(R.string.reference), (clipW + 150).toFloat(), (clipH + TOP_MARGIN).toFloat(), p)

                p.textAlign = Paint.Align.CENTER
                c.drawText(this.context.getString(R.string.credit), (canvasEnd - 300).toFloat(), (clipH + TOP_MARGIN).toFloat(), p)
                c.drawText(this.context.getString(R.string.debit), (canvasEnd - 200).toFloat(), (clipH + TOP_MARGIN).toFloat(), p)
                c.drawText(this.context.getString(R.string.balance), (canvasEnd - 100).toFloat(), (clipH + TOP_MARGIN).toFloat(), p)

                var j = i * linesPerPage
                var k = 1
                while (j < rawToPrint.size && k <= linesPerPage) {
                    p.textAlign = Paint.Align.CENTER
                    c.drawText(rawToPrint[j][0], (clipW + 100).toFloat(), (clipH + TOP_MARGIN + DISTANCE_BETWEEN_LINES * k).toFloat(), p)

                    p.textAlign = Paint.Align.LEFT
                    c.drawText(rawToPrint[j][1], (clipW + 150).toFloat(), (clipH + TOP_MARGIN + DISTANCE_BETWEEN_LINES * k).toFloat(), p)

                    p.textAlign = Paint.Align.CENTER
                    c.drawText(rawToPrint[j][2], (canvasEnd - 300).toFloat(),
                            (clipH + TOP_MARGIN + DISTANCE_BETWEEN_LINES * k).toFloat(), p)
                    c.drawText(rawToPrint[j][3], (canvasEnd - 200).toFloat(),
                            (clipH + TOP_MARGIN + DISTANCE_BETWEEN_LINES * k).toFloat(), p)
                    c.drawText(rawToPrint[j][4], (canvasEnd - 100).toFloat(),
                            (clipH + TOP_MARGIN + DISTANCE_BETWEEN_LINES * k).toFloat(), p)
                    j++
                    k++
                }

                pdfDocument.finishPage(page)
                writtenPages[i] = true
            }
            i++
        }

        // Write PDF document to file
        try {
            pdfDocument.writeTo(FileOutputStream(parcelFileDescriptor.fileDescriptor))
        } catch (e: IOException) {
            writeResultCallback.onWriteFailed(e.toString())
            return
        } finally {
            if (areAllTrue(writtenPages))
                pdfDocument.close()
        }
        // Signal the print framework the document is complete
        writeResultCallback.onWriteFinished(pageRanges)
    }


    private fun getStart(p: Array<PageRange>): Int {
        var page = amountOfPages//always bigger than the biggest as the biggest in p will be 0 index
        for (pr in p)
            if (pr.start < page)
                page = pr.start

        return page
    }

    private fun getEnd(p: Array<PageRange>): Int {
        var page = -1//smaller than smallest possible value in p
        for (pr in p)
            if (pr.end > page)
                page = pr.end

        return page
    }

    private fun isPageInRange(p: Array<PageRange>, page: Int): Boolean {
        for (pr in p)
            if (pr.start <= page && page <= pr.end)
                return true

        return false
    }
}