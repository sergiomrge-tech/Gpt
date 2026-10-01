package com.lotusdistribuidora.app

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
}
