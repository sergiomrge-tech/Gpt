package com.lotusdistribuidora.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object PdfUtil {
    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val LEFT = 24f
    private const val RIGHT = 571f
    private const val TABLE_TOP = 222f
    private const val ROWS_PER_PAGE = 13

    private val dateFmt = SimpleDateFormat("dd-MM-yyyy", Locale("pt", "BR"))
    private val timeFmt = SimpleDateFormat("HH:mm:ss", Locale("pt", "BR"))

    private val ink = Color.rgb(32, 32, 32)
    private val dark = Color.rgb(58, 58, 58)
    private val border = Color.rgb(75, 75, 75)
    private val light = Color.rgb(245, 245, 245)
    private val summary = Color.rgb(249, 249, 249)
    private val discountRed = Color.rgb(198, 40, 40)

    fun shareQuote(context: Context, company: Company, client: Client?, quote: Quote) {
        val calc = calculateQuote(quote)
        val status = if (quote.finalized) "FINALIZADO" else "PENDENTE"
        val remaining = if (quote.finalized) 0.0 else calc.charged
        val file = createPdf(
            context = context,
            fileName = "orcamento_lotus_" + quote.id + ".pdf",
            metaTitle = "DADOS DO ORÇAMENTO",
            documentLabel = "ORÇAMENTO",
            number = compactNumber(quote.id),
            company = company,
            client = client,
            fallbackClientName = quote.clientName,
            items = quote.items,
            payment = quote.payment,
            installments = quote.installments,
            cardFeePercent = quote.cardFeePercent,
            passCardFee = quote.passCardFee,
            subtotal = calc.subtotal,
            discount = calc.itemDiscounts + calc.generalDiscount,
            total = calc.charged,
            remaining = remaining,
            status = status,
            notes = quote.notes,
            date = quote.createdAt
        )
        share(context, file, "Orçamento - " + company.name)
    }

    fun shareQuoteJpg(context: Context, company: Company, client: Client?, quote: Quote) {
        val calc = calculateQuote(quote)
        val status = if (quote.finalized) "FINALIZADO" else "PENDENTE"
        val remaining = if (quote.finalized) 0.0 else calc.charged
        val pdf = createPdf(
            context = context,
            fileName = "orcamento_lotus_" + quote.id + "_jpg_source.pdf",
            metaTitle = "DADOS DO ORÇAMENTO",
            documentLabel = "ORÇAMENTO",
            number = compactNumber(quote.id),
            company = company,
            client = client,
            fallbackClientName = quote.clientName,
            items = quote.items,
            payment = quote.payment,
            installments = quote.installments,
            cardFeePercent = quote.cardFeePercent,
            passCardFee = quote.passCardFee,
            subtotal = calc.subtotal,
            discount = calc.itemDiscounts + calc.generalDiscount,
            total = calc.charged,
            remaining = remaining,
            status = status,
            notes = quote.notes,
            date = quote.createdAt
        )
        val jpg = renderPdfToJpeg(
            context,
            pdf,
            "orcamento_lotus_" + quote.id + ".jpg"
        )
        shareFile(context, jpg, "image/jpeg", "Orçamento - " + company.name, "Enviar orçamento em JPG")
    }

    fun shareSale(context: Context, company: Company, client: Client?, sale: Sale) {
        val subtotal = sale.items.sumOf { it.qty * it.unitPrice }
        val file = createPdf(
            context = context,
            fileName = "recibo_lotus_" + sale.id + ".pdf",
            metaTitle = "DADOS DA VENDA",
            documentLabel = "RECIBO DE VENDA",
            number = compactNumber(sale.id),
            company = company,
            client = client,
            fallbackClientName = sale.clientName,
            items = sale.items,
            payment = sale.payment,
            installments = sale.installments,
            cardFeePercent = sale.cardFeePercent,
            passCardFee = sale.passCardFee,
            subtotal = subtotal,
            discount = sale.discountAmount,
            total = sale.chargedTotal,
            remaining = 0.0,
            status = "FINALIZADA",
            notes = sale.notes,
            date = sale.createdAt
        )
        share(context, file, "Recibo - " + company.name)
    }

    private fun createPdf(
        context: Context,
        fileName: String,
        metaTitle: String,
        documentLabel: String,
        number: String,
        company: Company,
        client: Client?,
        fallbackClientName: String,
        items: List<QuoteItem>,
        payment: String,
        installments: Int,
        cardFeePercent: Double,
        passCardFee: Boolean,
        subtotal: Double,
        discount: Double,
        total: Double,
        remaining: Double,
        status: String,
        notes: String,
        date: Long
    ): File {
        val dir = File(context.cacheDir, "docs").apply { mkdirs() }
        val file = File(dir, fileName)
        val pdf = PdfDocument()
        val logo = loadLogo(context, company)
        val chunks: List<List<QuoteItem>> =
            if (items.isEmpty()) listOf(emptyList()) else items.chunked(ROWS_PER_PAGE)

        chunks.forEachIndexed { pageIndex, pageItems ->
            val page = pdf.startPage(
                PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageIndex + 1).create()
            )
            val canvas = page.canvas

            drawHeader(canvas, company, logo, documentLabel)
            drawSaleMeta(canvas, metaTitle, number, company, date)
            drawClientBlock(canvas, client, fallbackClientName)

            val tableBottom = drawProductsTable(
                canvas = canvas,
                pageItems = pageItems,
                allItemCount = items.sumOf { it.qty },
                pageSubtotal = pageItems.sumOf { lineTotal(it) },
                pageIndex = pageIndex,
                pageCount = chunks.size
            )

            if (pageIndex == chunks.lastIndex) {
                val summaryTop = max(568f, tableBottom + 28f)
                drawSummary(
                    canvas = canvas,
                    top = summaryTop,
                    remaining = remaining,
                    status = status,
                    subtotal = subtotal,
                    discount = discount,
                    total = total
                )
                drawPaymentAndFooter(
                    canvas = canvas,
                    company = company,
                    client = client,
                    fallbackClientName = fallbackClientName,
                    payment = payment,
                    installments = installments,
                    cardFeePercent = cardFeePercent,
                    passCardFee = passCardFee,
                    notes = notes
                )
            } else {
                val p = textPaint(8.5f, ink, false, Paint.Align.CENTER)
                canvas.drawText(
                    "Continua na página " + (pageIndex + 2) + " de " + chunks.size,
                    PAGE_W / 2f,
                    802f,
                    p
                )
            }

            pdf.finishPage(page)
        }

        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        logo?.recycle()
        return file
    }

    private fun renderPdfToJpeg(context: Context, pdfFile: File, fileName: String): File {
        val dir = File(context.cacheDir, "docs").apply { mkdirs() }
        val output = File(dir, fileName)
        val descriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        try {
            val targetWidth = 1240
            val targetHeight = 1754
            val pageCount = renderer.pageCount.coerceAtLeast(1)
            val combined = Bitmap.createBitmap(
                targetWidth,
                targetHeight * pageCount,
                Bitmap.Config.ARGB_8888
            )
            val combinedCanvas = Canvas(combined)
            combinedCanvas.drawColor(Color.WHITE)

            for (i in 0 until pageCount) {
                val page = renderer.openPage(i)
                val pageBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                pageBitmap.eraseColor(Color.WHITE)
                page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                combinedCanvas.drawBitmap(pageBitmap, 0f, (i * targetHeight).toFloat(), null)
                pageBitmap.recycle()
                page.close()
            }

            FileOutputStream(output).use { out ->
                combined.compress(Bitmap.CompressFormat.JPEG, 96, out)
            }
            combined.recycle()
        } finally {
            renderer.close()
            descriptor.close()
        }
        return output
    }

    private fun drawHeader(
        canvas: Canvas,
        company: Company,
        logo: Bitmap?,
        documentLabel: String
    ) {
        if (logo != null) {
            drawBitmapFit(canvas, logo, RectF(26f, 24f, 116f, 79f))
        }

        val companyPaint = textPaint(13.5f, ink, true)
        val infoPaint = textPaint(7.6f, ink)
        drawFitText(
            canvas,
            company.name.ifBlank { "Lotus Produtos para Estética" },
            132f,
            38f,
            350f,
            companyPaint,
            10f
        )
        drawFitText(
            canvas,
            "Endereço: " + company.address.ifBlank { "Dados da empresa configuráveis no aplicativo" },
            132f,
            52f,
            425f,
            infoPaint,
            6.3f
        )
        drawFitText(
            canvas,
            "Telefone: " + company.phone.ifBlank { "-" },
            132f,
            63f,
            200f,
            infoPaint,
            6.3f
        )
        drawFitText(
            canvas,
            "E-mail: " + company.email.ifBlank { "-" },
            132f,
            74f,
            265f,
            infoPaint,
            6.3f
        )

        val docPaint = textPaint(7.2f, Color.rgb(95, 95, 95), true, Paint.Align.RIGHT)
        canvas.drawText(documentLabel, RIGHT, 77f, docPaint)
        line(canvas, LEFT, 88f, RIGHT, 88f, 1.2f)
    }

    private fun drawSaleMeta(
        canvas: Canvas,
        title: String,
        number: String,
        company: Company,
        timestamp: Long
    ) {
        sectionTitle(canvas, title, 96f, 113f)

        val top = 113f
        val bottom = 137f
        val widths = floatArrayOf(142f, 118f, 104f, 183f)
        var x = LEFT
        widths.forEach {
            canvas.drawRect(x, top, x + it, bottom, strokePaint(0.8f))
            x += it
        }

        val d = Date(timestamp)
        metaCell(canvas, LEFT, top, widths[0], bottom, "Número:", number)
        metaCell(canvas, LEFT + widths[0], top, widths[1], bottom, "Data:", dateFmt.format(d))
        metaCell(canvas, LEFT + widths[0] + widths[1], top, widths[2], bottom, "Hora:", timeFmt.format(d))
        metaCell(
            canvas,
            LEFT + widths[0] + widths[1] + widths[2],
            top,
            widths[3],
            bottom,
            "Vendedor:",
            company.sellerName.ifBlank { "-" }
        )
    }

    private fun drawClientBlock(canvas: Canvas, client: Client?, fallbackName: String) {
        sectionTitle(canvas, "DADOS DO CLIENTE", 147f, 164f)

        val name = client?.name?.takeIf { it.isNotBlank() } ?: fallbackName
        val address = clientAddress(client)
        val phone = client?.phone.orEmpty()
        val email = client?.email.orEmpty()

        val top = 164f
        val rowH = 18f
        val pLabel = textPaint(7.6f, ink)
        val pValue = textPaint(7.9f, ink, true)

        canvas.drawRect(LEFT, top, RIGHT, top + rowH, strokePaint(0.8f))
        canvas.drawText("Nome:", LEFT + 7f, top + 12f, pLabel)
        drawFitText(canvas, name, LEFT + 43f, top + 12f, RIGHT - LEFT - 50f, pValue, 6.5f)

        canvas.drawRect(LEFT, top + rowH, RIGHT, top + rowH * 2f, strokePaint(0.8f))
        canvas.drawText("Endereço:", LEFT + 7f, top + rowH + 12f, pLabel)
        drawFitText(
            canvas,
            address.ifBlank { "-" },
            LEFT + 55f,
            top + rowH + 12f,
            RIGHT - LEFT - 62f,
            textPaint(7.6f, ink),
            6.2f
        )

        canvas.drawRect(LEFT, top + rowH * 2f, RIGHT, top + rowH * 3f, strokePaint(0.8f))
        canvas.drawText("Telefone:", LEFT + 7f, top + rowH * 2f + 12f, pLabel)
        drawFitText(canvas, phone.ifBlank { "-" }, LEFT + 54f, top + rowH * 2f + 12f, 155f, textPaint(7.6f, ink), 6.2f)
        canvas.drawText("Celular:", 302f, top + rowH * 2f + 12f, pLabel)
        drawFitText(canvas, phone.ifBlank { "-" }, 343f, top + rowH * 2f + 12f, 94f, textPaint(7.6f, ink), 6.2f)
        canvas.drawText("E-mail:", 451f, top + rowH * 2f + 12f, pLabel)
        drawFitText(canvas, email.ifBlank { "-" }, 488f, top + rowH * 2f + 12f, 78f, textPaint(7.1f, ink), 5.8f)
    }

    private fun drawProductsTable(
        canvas: Canvas,
        pageItems: List<QuoteItem>,
        allItemCount: Int,
        pageSubtotal: Double,
        pageIndex: Int,
        pageCount: Int
    ): Float {
        val widths = floatArrayOf(70f, 282f, 75f, 45f, 75f)
        val headers = arrayOf("CÓDIGO", "PRODUTO", "VAL UNIT.", "QTD", "VAL TOTAL")
        val headerH = 23f
        val rowH = 22f
        var y = TABLE_TOP

        var x = LEFT
        headers.forEachIndexed { i, h ->
            canvas.drawRect(x, y, x + widths[i], y + headerH, fillPaint(dark))
            canvas.drawRect(x, y, x + widths[i], y + headerH, strokePaint(0.8f))
            drawCenteredText(
                canvas,
                h,
                x,
                y,
                x + widths[i],
                y + headerH,
                textPaint(7.4f, Color.WHITE, true, Paint.Align.CENTER)
            )
            x += widths[i]
        }
        y += headerH

        pageItems.forEach { item ->
            x = LEFT
            widths.forEach {
                canvas.drawRect(x, y, x + it, y + rowH, strokePaint(0.65f))
                x += it
            }

            drawCellFit(canvas, item.productCode.ifBlank { "-" }, LEFT, y, widths[0], rowH, 7.2f, Paint.Align.CENTER)
            drawCellFit(canvas, item.productName, LEFT + widths[0], y, widths[1], rowH, 7.2f, Paint.Align.LEFT)
            drawCellFit(canvas, item.unitPrice.money(), LEFT + widths[0] + widths[1], y, widths[2], rowH, 7.2f, Paint.Align.CENTER)
            drawCellFit(canvas, item.qty.toString(), LEFT + widths[0] + widths[1] + widths[2], y, widths[3], rowH, 7.2f, Paint.Align.CENTER)
            drawCellFit(canvas, lineTotal(item).money(), RIGHT - widths[4], y, widths[4], rowH, 7.2f, Paint.Align.CENTER)
            y += rowH
        }

        val footerH = 21f
        x = LEFT
        widths.forEach {
            canvas.drawRect(x, y, x + it, y + footerH, strokePaint(0.75f))
            x += it
        }
        val pageText =
            if (pageCount == 1) allItemCount.toString() + " item(ns)"
            else allItemCount.toString() + " item(ns) • pág. " + (pageIndex + 1) + "/" + pageCount
        drawCellFit(canvas, pageText, LEFT, y, widths[0] + widths[1], footerH, 7.2f, Paint.Align.LEFT, true)
        drawCellFit(canvas, pageSubtotal.money(), RIGHT - widths[4], y, widths[4], footerH, 8f, Paint.Align.CENTER, true)
        return y + footerH
    }

    private fun drawSummary(
        canvas: Canvas,
        top: Float,
        remaining: Double,
        status: String,
        subtotal: Double,
        discount: Double,
        total: Double
    ) {
        val h = 50f
        canvas.drawRect(LEFT, top, RIGHT, top + h, fillPaint(summary))
        canvas.drawRect(LEFT, top, RIGHT, top + h, strokePaint(0.9f))

        val widths = floatArrayOf(104f, 122f, 111f, 119f, 91f)
        val labels = arrayOf("Restante/Fiado", "Status", "Sub Total", "Total dos descontos", "Total Final")
        val values = arrayOf(remaining.money(), status, subtotal.money(), discount.money(), total.money())

        var x = LEFT
        widths.forEachIndexed { i, w ->
            if (i > 0) line(canvas, x, top + 8f, x, top + h - 8f, 0.7f)
            drawCenteredText(
                canvas,
                labels[i],
                x,
                top + 7f,
                x + w,
                top + 25f,
                textPaint(6.7f, ink, false, Paint.Align.CENTER)
            )
            val valueColor = if (i == 3 && discount > 0.0) discountRed else ink
            drawCenteredText(
                canvas,
                values[i],
                x,
                top + 22f,
                x + w,
                top + h - 4f,
                textPaint(if (i == 4) 9.6f else 9f, valueColor, true, Paint.Align.CENTER)
            )
            x += w
        }
    }

    private fun drawPaymentAndFooter(
        canvas: Canvas,
        company: Company,
        client: Client?,
        fallbackClientName: String,
        payment: String,
        installments: Int,
        cardFeePercent: Double,
        passCardFee: Boolean,
        notes: String
    ) {
        val paymentText = buildString {
            append("Forma de pagamento: ")
            append(payment)
            if (payment == "Cartão") {
                append(" • ")
                append(installments.coerceAtLeast(1))
                append("x")
                if (cardFeePercent > 0.0) {
                    append(" • tarifa ")
                    append(String.format(Locale("pt", "BR"), "%.2f%%", cardFeePercent))
                    append(if (passCardFee) " repassada ao cliente" else " absorvida pela empresa")
                }
            }
        }
        drawFitText(canvas, paymentText, LEFT, 638f, RIGHT - LEFT, textPaint(7.4f, ink, true), 6.2f)

        if (notes.isNotBlank()) {
            drawFitText(
                canvas,
                "Observações: " + notes.replace("\n", " "),
                LEFT,
                652f,
                RIGHT - LEFT,
                textPaint(7f, ink),
                5.8f
            )
        }

        drawCenteredText(
            canvas,
            "Oferecemos os melhores produtos com os melhores preços. Agradecemos sua preferência.",
            LEFT,
            679f,
            RIGHT,
            700f,
            textPaint(7.2f, ink, false, Paint.Align.CENTER)
        )

        val leftX1 = 55f
        val leftX2 = 282f
        val rightX1 = 321f
        val rightX2 = 550f
        val sigY = 754f
        line(canvas, leftX1, sigY, leftX2, sigY, 0.9f)
        line(canvas, rightX1, sigY, rightX2, sigY, 0.9f)

        val clientName = client?.name?.takeIf { it.isNotBlank() } ?: fallbackClientName
        drawFitText(
            canvas,
            clientName.uppercase(Locale("pt", "BR")),
            leftX1,
            sigY + 15f,
            leftX2 - leftX1,
            textPaint(6.7f, ink, false, Paint.Align.CENTER),
            5.3f,
            centerX = (leftX1 + leftX2) / 2f
        )

        val cpf = client?.cpf.orEmpty()
        if (cpf.isNotBlank()) {
            drawCenteredText(
                canvas,
                "CPF/CNPJ: " + cpf,
                leftX1,
                sigY + 16f,
                leftX2,
                sigY + 34f,
                textPaint(6.3f, ink, false, Paint.Align.CENTER)
            )
        }

        drawFitText(
            canvas,
            company.name.ifBlank { "Lotus Produtos para Estética" },
            rightX1,
            sigY + 15f,
            rightX2 - rightX1,
            textPaint(6.7f, ink, false, Paint.Align.CENTER),
            5.3f,
            centerX = (rightX1 + rightX2) / 2f
        )

        val seller = company.sellerName
        if (seller.isNotBlank()) {
            drawCenteredText(
                canvas,
                "Vendedor: " + seller,
                rightX1,
                sigY + 16f,
                rightX2,
                sigY + 34f,
                textPaint(6.1f, Color.rgb(80, 80, 80), false, Paint.Align.CENTER)
            )
        }

        val footer = textPaint(5.8f, Color.rgb(120, 120, 120), false, Paint.Align.CENTER)
        canvas.drawText("Documento gerado pelo Sistema Lotus.", PAGE_W / 2f, 818f, footer)
    }

    private fun sectionTitle(canvas: Canvas, title: String, top: Float, bottom: Float) {
        canvas.drawRect(LEFT, top, RIGHT, bottom, fillPaint(light))
        line(canvas, LEFT, top, RIGHT, top, 0.9f)
        line(canvas, LEFT, bottom, RIGHT, bottom, 0.9f)
        drawCenteredText(
            canvas,
            title,
            LEFT,
            top,
            RIGHT,
            bottom,
            textPaint(8.4f, ink, true, Paint.Align.CENTER)
        )
    }

    private fun metaCell(
        canvas: Canvas,
        x: Float,
        top: Float,
        width: Float,
        bottom: Float,
        label: String,
        value: String
    ) {
        val labelPaint = textPaint(6.8f, ink)
        canvas.drawText(label, x + 7f, top + 15f, labelPaint)
        drawFitText(
            canvas,
            value,
            x + 44f,
            top + 15f,
            width - 50f,
            textPaint(7.1f, ink, true),
            5.6f
        )
    }

    private fun clientAddress(client: Client?): String {
        if (client == null) return ""
        val streetPart = listOf(client.street, client.number, client.complement)
            .filter { it.isNotBlank() }
            .joinToString(", ")
        val cityPart = listOf(client.district, client.city, client.state)
            .filter { it.isNotBlank() }
            .joinToString(" - ")
        return listOf(
            streetPart,
            cityPart,
            if (client.cep.isNotBlank()) "CEP " + client.cep else ""
        ).filter { it.isNotBlank() }.joinToString(" • ")
    }

    private fun lineTotal(item: QuoteItem): Double {
        val discountFactor = 1.0 - item.itemDiscountPercent.coerceIn(0.0, 100.0) / 100.0
        return item.qty * item.unitPrice * discountFactor
    }

    private fun compactNumber(id: Long): String {
        val s = id.toString()
        return if (s.length <= 8) s else s.takeLast(8)
    }

    private fun loadLogo(context: Context, company: Company): Bitmap? {
        if (company.logoUri.isNotBlank()) {
            try {
                context.contentResolver.openInputStream(Uri.parse(company.logoUri))?.use {
                    BitmapFactory.decodeStream(it)?.let { bitmap -> return bitmap }
                }
            } catch (_: Exception) {
            }
        }
        return LotusBrand.bitmap()
    }

    private fun drawBitmapFit(canvas: Canvas, bitmap: Bitmap, box: RectF) {
        val scale = min(box.width() / bitmap.width, box.height() / bitmap.height)
        val w = bitmap.width * scale
        val h = bitmap.height * scale
        val left = box.left + (box.width() - w) / 2f
        val top = box.top + (box.height() - h) / 2f
        canvas.drawBitmap(bitmap, null, RectF(left, top, left + w, top + h), Paint(Paint.ANTI_ALIAS_FLAG))
    }

    private fun drawCellFit(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        size: Float,
        align: Paint.Align,
        bold: Boolean = false
    ) {
        val p = textPaint(size, ink, bold, align)
        val centerY = y + h / 2f - (p.fontMetrics.ascent + p.fontMetrics.descent) / 2f
        when (align) {
            Paint.Align.LEFT -> drawFitText(canvas, text, x + 6f, centerY, w - 12f, p, 5.5f)
            Paint.Align.CENTER -> drawFitText(canvas, text, x, centerY, w, p, 5.5f, centerX = x + w / 2f)
            Paint.Align.RIGHT -> drawFitText(canvas, text, x + w - 6f, centerY, w - 12f, p, 5.5f)
        }
    }

    private fun drawCenteredText(
        canvas: Canvas,
        text: String,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        paint: Paint
    ) {
        val y = (top + bottom) / 2f - (paint.fontMetrics.ascent + paint.fontMetrics.descent) / 2f
        paint.textAlign = Paint.Align.CENTER
        drawFitText(canvas, text, left, y, right - left, paint, 5.5f, centerX = (left + right) / 2f)
    }

    private fun drawFitText(
        canvas: Canvas,
        text: String,
        x: Float,
        baseline: Float,
        maxWidth: Float,
        paint: Paint,
        minSize: Float,
        centerX: Float? = null
    ) {
        val safe = text.ifBlank { "-" }.replace("\n", " ")
        val original = paint.textSize
        while (paint.measureText(safe) > maxWidth && paint.textSize > minSize) {
            paint.textSize -= 0.25f
        }
        if (centerX != null) {
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(safe, centerX, baseline, paint)
        } else {
            canvas.drawText(safe, x, baseline, paint)
        }
        paint.textSize = original
    }

    private fun textPaint(
        size: Float,
        color: Int,
        bold: Boolean = false,
        align: Paint.Align = Paint.Align.LEFT
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = size
        this.color = color
        this.textAlign = align
        this.typeface = Typeface.create(Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun fillPaint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    private fun strokePaint(width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = border
        style = Paint.Style.STROKE
        strokeWidth = width
    }

    private fun line(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, width: Float) {
        canvas.drawLine(x1, y1, x2, y2, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = border
            strokeWidth = width
        })
    }

    private fun share(context: Context, file: File, subject: String) {
        shareFile(context, file, "application/pdf", subject, "Enviar PDF")
    }

    private fun shareFile(context: Context, file: File, mimeType: String, subject: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }
}
