package com.onetap.orderdesk.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.onetap.orderdesk.data.local.entity.Order
import com.onetap.orderdesk.data.local.entity.OrderLine
import com.onetap.orderdesk.ui.orders.formatMoney
import java.io.File
import java.io.FileOutputStream

object InvoiceGenerator {

    fun generateAndShareInvoice(context: Context, order: Order, lines: List<OrderLine>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Header
        paint.color = Color.rgb(15, 37, 55) // Tech Navy
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("OneTap Order Desk", 40f, 60f, paint)

        paint.color = Color.rgb(241, 172, 48) // Gold
        paint.textSize = 14f
        canvas.drawText("Zahra.Shop Invoice", 40f, 85f, paint)

        // Customer Details
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Order ID: #${order.id}", 40f, 130f, paint)
        canvas.drawText("Date: ${java.text.SimpleDateFormat("dd-MMM-yyyy hh:mm a").format(order.date)}", 40f, 150f, paint)
        canvas.drawText("Customer: ${order.customerName}", 40f, 170f, paint)
        canvas.drawText("Phone: ${order.phone}", 40f, 190f, paint)
        if (order.address != null) canvas.drawText("Address: ${order.address}", 40f, 210f, paint)

        // Table Header
        val startY = 250f
        paint.isFakeBoldText = true
        canvas.drawText("Item Name", 40f, startY, paint)
        canvas.drawText("Qty", 300f, startY, paint)
        canvas.drawText("Rate", 380f, startY, paint)
        canvas.drawText("Total", 480f, startY, paint)

        paint.isFakeBoldText = false
        var y = startY + 20f

        // Order Lines
        lines.forEach { line ->
            canvas.drawText(line.productName.take(30), 40f, y, paint)
            canvas.drawText(line.quantity.toString(), 300f, y, paint)
            canvas.drawText(formatMoney(line.rate), 380f, y, paint)
            canvas.drawText(formatMoney(line.lineTotal), 480f, y, paint)
            y += 20f
        }

        // Totals
        y += 20f
        paint.isFakeBoldText = true
        canvas.drawText("Grand Total:", 380f, y, paint)
        canvas.drawText("PKR ${formatMoney(order.grandTotal)}", 470f, y, paint)

        // Footer
        paint.isFakeBoldText = false
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("Thank you for your business!", 40f, 800f, paint)
        canvas.drawText("Developed by Imran", 430f, 800f, paint)

        pdfDocument.finishPage(page)

        // Save File
        val file = File(context.cacheDir, "Order_${order.id}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            sharePdf(context, file, order)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sharePdf(context: Context, file: File, order: Order) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Order Confirmation #${order.id}")
            putExtra(Intent.EXTRA_TEXT, "Hello ${order.customerName},\n\nYour order #${order.id} has been confirmed.\nTotal Amount: PKR ${formatMoney(order.grandTotal)}\n\nPlease find the attached invoice.\n\nThank you for choosing Zahra.Shop!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        // Try to open WhatsApp directly, otherwise show standard share sheet
        try {
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
        } catch (e: Exception) {
            // WhatsApp not installed, open general share sheet
            intent.setPackage(null)
            context.startActivity(Intent.createChooser(intent, "Share Invoice via"))
        }
    }
}
