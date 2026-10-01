package com.lotusdistribuidora.app

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfUtil {
    private val dateFmt=SimpleDateFormat("dd/MM/yyyy HH:mm",Locale("pt","BR"))

    fun shareQuote(context:Context, company:Company, quote:Quote){
        val calc=calculateQuote(quote)
        val file=createPdf(context,"orcamento_lotus_"+quote.id+".pdf","ORÇAMENTO",company,quote.clientName,quote.items,quote.payment,quote.installments,calc.subtotal,calc.itemDiscounts+calc.generalDiscount,calc.charged,quote.notes,quote.createdAt)
        share(context,file,"Orçamento - "+company.name)
    }

    fun shareSale(context:Context, company:Company, sale:Sale){
        val subtotal=sale.items.sumOf{it.qty*it.unitPrice}
        val file=createPdf(context,"recibo_lotus_"+sale.id+".pdf","RECIBO DE VENDA",company,sale.clientName,sale.items,sale.payment,sale.installments,subtotal,sale.discountAmount,sale.chargedTotal,sale.notes,sale.createdAt)
        share(context,file,"Recibo - "+company.name)
    }

    private fun createPdf(
        context:Context,fileName:String,title:String,company:Company,clientName:String,items:List<QuoteItem>,
        payment:String,installments:Int,subtotal:Double,discount:Double,total:Double,notes:String,date:Long
    ):File{
        val dir=File(context.cacheDir,"docs").apply{mkdirs()}
        val file=File(dir,fileName)
        val pdf=PdfDocument()
        val page=pdf.startPage(PdfDocument.PageInfo.Builder(595,842,1).create())
        val c=page.canvas
        val dark=Paint().apply{color=android.graphics.Color.rgb(75,46,68);textSize=20f;typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD)}
        val normal=Paint().apply{color=android.graphics.Color.rgb(55,45,52);textSize=10f}
        val small=Paint().apply{color=android.graphics.Color.rgb(120,90,108);textSize=9f}
        val bold=Paint(normal).apply{typeface=Typeface.create(Typeface.DEFAULT,Typeface.BOLD)}
        val line=Paint().apply{color=android.graphics.Color.rgb(225,205,217);strokeWidth=1f}
        var y=48f
        c.drawText(company.name.ifBlank{"Lotus Distribuidora"},36f,y,dark); y+=18f
        c.drawText(listOf(company.document,company.phone,company.email).filter{it.isNotBlank()}.joinToString(" • "),36f,y,small); y+=28f
        c.drawText(title,36f,y,dark); y+=18f
        c.drawText("Data: "+dateFmt.format(Date(date)),36f,y,normal); y+=15f
        c.drawText("Cliente: "+clientName,36f,y,bold); y+=16f
        if(company.sellerName.isNotBlank()){c.drawText("Vendedor: "+company.sellerName+"  "+company.sellerPhone,36f,y,normal);y+=16f}
        c.drawLine(36f,y,559f,y,line); y+=18f
        c.drawText("Produto",36f,y,bold); c.drawText("Qtd.",330f,y,bold); c.drawText("Unit.",380f,y,bold); c.drawText("Desc.",455f,y,bold); y+=14f
        items.forEach{
            if(y>690f)return@forEach
            c.drawText(it.productName.take(42),36f,y,normal)
            c.drawText(it.qty.toString(),334f,y,normal)
            c.drawText(it.unitPrice.money(),380f,y,normal)
            c.drawText(String.format(Locale("pt","BR"),"%.1f%%",it.itemDiscountPercent),455f,y,normal)
            y+=15f
        }
        y+=8f;c.drawLine(36f,y,559f,y,line);y+=20f
        c.drawText("Subtotal:",330f,y,bold);c.drawText(subtotal.money(),445f,y,normal);y+=16f
        c.drawText("Descontos:",330f,y,bold);c.drawText(discount.money(),445f,y,normal);y+=16f
        c.drawText("TOTAL:",330f,y,dark);c.drawText(total.money(),445f,y,dark);y+=26f
        c.drawText("Pagamento: "+payment+(if(payment=="Cartão")" em "+installments+"x" else ""),36f,y,bold);y+=16f
        if(company.pixKey.isNotBlank()&&payment=="Pix"){c.drawText("Chave Pix: "+company.pixKey,36f,y,normal);y+=16f}
        if(notes.isNotBlank()){c.drawText("Observações: "+notes.take(85),36f,y,normal);y+=16f}
        c.drawText("Documento gerado pelo Sistema Lotus.",36f,800f,small)
        pdf.finishPage(page)
        FileOutputStream(file).use{pdf.writeTo(it)}
        pdf.close()
        return file
    }

    private fun share(context:Context,file:File,subject:String){
        val uri=FileProvider.getUriForFile(context,context.packageName+".fileprovider",file)
        val intent=Intent(Intent.ACTION_SEND).apply{
            type="application/pdf"
            putExtra(Intent.EXTRA_STREAM,uri)
            putExtra(Intent.EXTRA_SUBJECT,subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent,"Enviar PDF"))
    }
}
