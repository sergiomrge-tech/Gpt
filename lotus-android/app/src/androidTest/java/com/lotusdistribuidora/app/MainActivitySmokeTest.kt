package com.lotusdistribuidora.app

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {
    @Test
    fun appLaunchesWithoutCrash() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                check(!activity.isFinishing) { "MainActivity finished during startup" }
            }
        }
    }

    @Test
    fun officialBrandVectorExists() {
        val context=ApplicationProvider.getApplicationContext<Context>()
        val drawable=androidx.core.content.ContextCompat.getDrawable(context,R.drawable.lotus_mark)
        check(drawable!=null) { "Official Lotus vector mark was not packaged" }
    }

    @Test
    fun quotationJpgIsGenerated() {
        val context=ApplicationProvider.getApplicationContext<Context>()
        val company=Company(name="Lotus Distribuidora",sellerName="Tuanny")
        val client=Client(
            id=1L,name="Cliente Teste",phone="11999999999",cpf="12345678900",
            email="cliente@teste.com",profession="Esteticista",cep="01001000",
            street="Praça da Sé",number="100",complement="",district="Sé",
            city="São Paulo",state="SP",notes=""
        )
        val quote=Quote(
            id=123L,clientId=1L,clientName=client.name,
            items=mutableListOf(
                QuoteItem(1L,"Produto Teste",2,150.0,0.0,80.0,"LT-001")
            ),
            discountType="R$",discountValue=10.0,payment="Pix",installments=1,
            cardFeePercent=0.0,passCardFee=false,notes="Teste de geração",
            createdAt=System.currentTimeMillis(),finalized=false
        )
        val file=PdfUtil.createQuoteJpg(context,company,client,quote)
        check(file.exists() && file.length()>10000){"JPG was not generated"}
        val bytes=file.inputStream().use{it.readNBytes(2)}
        check(bytes.size==2 && bytes[0].toInt()==0xFF-256 && bytes[1].toInt()==0xD8-256){"Invalid JPEG header"}
    }

    @Test
    fun creditFeeKeepsCustomerTotalAndCalculatesNet() {
        val quote=Quote(
            id=500L,clientId=1L,clientName="Cliente",
            items=mutableListOf(QuoteItem(1L,"Produto",1,500.0,0.0,300.0,"P500")),
            discountType="R$",discountValue=0.0,
            payment="Crédito parcelado",installments=3,
            cardFeePercent=10.0,passCardFee=false,notes="",
            createdAt=System.currentTimeMillis(),finalized=false
        )
        val calc=calculateQuote(quote)
        check(kotlin.math.abs(calc.charged-500.0)<0.001){"Customer total changed: ${calc.charged}"}
        check(kotlin.math.abs(calc.cardFee-50.0)<0.001){"Unexpected card fee: ${calc.cardFee}"}
        check(kotlin.math.abs(calc.netRevenue-450.0)<0.001){"Unexpected net revenue: ${calc.netRevenue}"}
    }

    @Test
    fun cepLookupFillsKnownAddress() = runBlocking {
        val address=CepService.lookup("01001000")
        check(address!=null){"CEP lookup returned null"}
        check(address.city.equals("São Paulo",ignoreCase=true)){"Unexpected city: ${address.city}"}
        check(address.state.equals("SP",ignoreCase=true)){"Unexpected state: ${address.state}"}
        check(address.street.isNotBlank()){"Street was not filled"}
    }
}
