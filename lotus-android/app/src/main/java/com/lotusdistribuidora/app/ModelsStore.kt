package com.lotusdistribuidora.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

val brl: NumberFormat = NumberFormat.getCurrencyInstance(Locale("pt","BR"))
fun Double.money(): String = brl.format(this)
fun String.num(): Double = replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
fun String.intNum(): Int = toIntOrNull() ?: 0

data class Product(val id: Long,var supplier: String,var name: String,var sku: String,var stock: Int,var minStock: Int,var cost: Double,var price: Double)
data class Client(val id: Long,var name: String,var phone: String,var cpf: String,var email: String,var profession: String,var cep: String,var street: String,var number: String,var complement: String,var district: String,var city: String,var state: String,var notes: String)
data class QuoteItem(val productId: Long,val productName: String,var qty: Int,var unitPrice: Double,var itemDiscountPercent: Double,val unitCostSnapshot: Double)
data class Quote(val id: Long,var clientId: Long,var clientName: String,var items: MutableList<QuoteItem>,var discountType: String,var discountValue: Double,var payment: String,var installments: Int,var cardFeePercent: Double,var passCardFee: Boolean,var notes: String,var createdAt: Long,var finalized: Boolean)
data class Sale(val id: Long,val quoteId: Long,val clientId: Long,val clientName: String,val items: MutableList<QuoteItem>,val payment: String,val installments: Int,val cardFeePercent: Double,val passCardFee: Boolean,val notes: String,val createdAt: Long,val chargedTotal: Double,val cardFeeValue: Double,val netRevenue: Double,val discountAmount: Double)
data class Company(var name: String="Lotus Distribuidora",var document: String="",var phone: String="",var email: String="",var address: String="",var sellerName: String="",var sellerPhone: String="",var pixKey: String="",var logoUri: String="")
data class QuoteCalc(val subtotal: Double,val itemDiscounts: Double,val generalDiscount: Double,val charged: Double,val cardFee: Double,val netRevenue: Double,val cogs: Double)

fun calculateQuote(q: Quote): QuoteCalc {
    val raw=q.items.sumOf{it.qty*it.unitPrice}
    val afterItem=q.items.sumOf{it.qty*it.unitPrice*(1.0-it.itemDiscountPercent.coerceIn(0.0,100.0)/100.0)}
    val itemDiscounts=raw-afterItem
    val general=if(q.discountType=="%") afterItem*q.discountValue.coerceIn(0.0,100.0)/100.0 else q.discountValue.coerceAtLeast(0.0).coerceAtMost(afterItem)
    val base=max(0.0,afterItem-general)
    val rate=if(q.payment=="Cartão") q.cardFeePercent.coerceIn(0.0,99.0)/100.0 else 0.0
    val charged=if(q.payment=="Cartão"&&q.passCardFee&&rate>0.0) base/(1.0-rate) else base
    val fee=charged*rate
    val net=charged-fee
    val cogs=q.items.sumOf{it.qty*it.unitCostSnapshot}
    return QuoteCalc(raw,itemDiscounts,general,charged,fee,net,cogs)
}

class LotusStore(private val context: Context) {
    private val prefs=context.getSharedPreferences("lotus_store_v1",Context.MODE_PRIVATE)
    var products: List<Product> = loadProducts(); private set
    var clients: List<Client> = loadClients(); private set
    var quotes: List<Quote> = loadQuotes(); private set
    var sales: List<Sale> = loadSales(); private set
    var company: Company = loadCompany(); private set

    fun upsertProduct(p:Product){val m=products.toMutableList();val i=m.indexOfFirst{it.id==p.id};if(i>=0)m[i]=p else m.add(p);products=m;saveProducts()}
    fun upsertClient(c:Client){val m=clients.toMutableList();val i=m.indexOfFirst{it.id==c.id};if(i>=0)m[i]=c else m.add(c);clients=m;saveClients()}
    fun upsertQuote(q:Quote){val m=quotes.toMutableList();val i=m.indexOfFirst{it.id==q.id};if(i>=0)m[i]=q else m.add(0,q);quotes=m;saveQuotes()}
    fun saveCompany(c:Company){company=c.copy();prefs.edit().putString("company",companyJson(company).toString()).apply()}

    fun finalizeQuote(q:Quote):String?{
        if(q.finalized)return "Venda já finalizada."
        q.items.forEach{item->
            val p=products.firstOrNull{it.id==item.productId}?:return "Produto não encontrado: "+item.productName
            if(item.qty<=0)return "Quantidade inválida: "+item.productName
            if(p.stock<item.qty)return "Estoque insuficiente: "+item.productName
        }
        val calc=calculateQuote(q)
        products=products.map{p->val item=q.items.firstOrNull{it.productId==p.id};if(item==null)p else p.copy(stock=p.stock-item.qty)}
        q.finalized=true
        upsertQuote(q)
        val sale=Sale(System.currentTimeMillis(),q.id,q.clientId,q.clientName,q.items.map{it.copy()}.toMutableList(),q.payment,q.installments,q.cardFeePercent,q.passCardFee,q.notes,System.currentTimeMillis(),calc.charged,calc.cardFee,calc.netRevenue,calc.itemDiscounts+calc.generalDiscount)
        sales=listOf(sale)+sales
        saveProducts();saveQuotes();saveSales()
        return null
    }

    private fun saveProducts()=prefs.edit().putString("products",JSONArray().apply{products.forEach{put(productJson(it))}}.toString()).apply()
    private fun saveClients()=prefs.edit().putString("clients",JSONArray().apply{clients.forEach{put(clientJson(it))}}.toString()).apply()
    private fun saveQuotes()=prefs.edit().putString("quotes",JSONArray().apply{quotes.forEach{put(quoteJson(it))}}.toString()).apply()
    private fun saveSales()=prefs.edit().putString("sales",JSONArray().apply{sales.forEach{put(saleJson(it))}}.toString()).apply()

    private fun loadProducts()=readArray("products"){o->Product(o.getLong("id"),o.optString("supplier"),o.optString("name"),o.optString("sku"),o.optInt("stock"),o.optInt("minStock"),o.optDouble("cost"),o.optDouble("price"))}
    private fun loadClients()=readArray("clients"){o->Client(o.getLong("id"),o.optString("name"),o.optString("phone"),o.optString("cpf"),o.optString("email"),o.optString("profession"),o.optString("cep"),o.optString("street"),o.optString("number"),o.optString("complement"),o.optString("district"),o.optString("city"),o.optString("state"),o.optString("notes"))}
    private fun loadQuotes()=readArray("quotes"){quoteFromJson(it)}
    private fun loadSales()=readArray("sales"){saleFromJson(it)}
    private fun loadCompany()=try{prefs.getString("company",null)?.let{companyFromJson(JSONObject(it))}?:Company()}catch(_:Exception){Company()}
    private fun <T> readArray(key:String,f:(JSONObject)->T):List<T> = try{val a=JSONArray(prefs.getString(key,"[]")?:"[]");(0 until a.length()).map{f(a.getJSONObject(it))}}catch(_:Exception){emptyList()}

    private fun itemJson(i:QuoteItem)=JSONObject().put("productId",i.productId).put("productName",i.productName).put("qty",i.qty).put("unitPrice",i.unitPrice).put("itemDiscountPercent",i.itemDiscountPercent).put("unitCostSnapshot",i.unitCostSnapshot)
    private fun itemFrom(o:JSONObject)=QuoteItem(o.getLong("productId"),o.optString("productName"),o.optInt("qty"),o.optDouble("unitPrice"),o.optDouble("itemDiscountPercent"),o.optDouble("unitCostSnapshot"))
    private fun productJson(p:Product)=JSONObject().put("id",p.id).put("supplier",p.supplier).put("name",p.name).put("sku",p.sku).put("stock",p.stock).put("minStock",p.minStock).put("cost",p.cost).put("price",p.price)
    private fun clientJson(c:Client)=JSONObject().put("id",c.id).put("name",c.name).put("phone",c.phone).put("cpf",c.cpf).put("email",c.email).put("profession",c.profession).put("cep",c.cep).put("street",c.street).put("number",c.number).put("complement",c.complement).put("district",c.district).put("city",c.city).put("state",c.state).put("notes",c.notes)
    private fun quoteJson(q:Quote)=JSONObject().put("id",q.id).put("clientId",q.clientId).put("clientName",q.clientName).put("items",JSONArray().apply{q.items.forEach{put(itemJson(it))}}).put("discountType",q.discountType).put("discountValue",q.discountValue).put("payment",q.payment).put("installments",q.installments).put("cardFeePercent",q.cardFeePercent).put("passCardFee",q.passCardFee).put("notes",q.notes).put("createdAt",q.createdAt).put("finalized",q.finalized)
    private fun quoteFromJson(o:JSONObject):Quote{val a=o.optJSONArray("items")?:JSONArray();return Quote(o.getLong("id"),o.optLong("clientId"),o.optString("clientName"),(0 until a.length()).map{itemFrom(a.getJSONObject(it))}.toMutableList(),o.optString("discountType","R$"),o.optDouble("discountValue"),o.optString("payment","Pix"),o.optInt("installments",1),o.optDouble("cardFeePercent"),o.optBoolean("passCardFee"),o.optString("notes"),o.optLong("createdAt"),o.optBoolean("finalized"))}
    private fun saleJson(s:Sale)=JSONObject().put("id",s.id).put("quoteId",s.quoteId).put("clientId",s.clientId).put("clientName",s.clientName).put("items",JSONArray().apply{s.items.forEach{put(itemJson(it))}}).put("payment",s.payment).put("installments",s.installments).put("cardFeePercent",s.cardFeePercent).put("passCardFee",s.passCardFee).put("notes",s.notes).put("createdAt",s.createdAt).put("chargedTotal",s.chargedTotal).put("cardFeeValue",s.cardFeeValue).put("netRevenue",s.netRevenue).put("discountAmount",s.discountAmount)
    private fun saleFromJson(o:JSONObject):Sale{val a=o.optJSONArray("items")?:JSONArray();return Sale(o.getLong("id"),o.optLong("quoteId"),o.optLong("clientId"),o.optString("clientName"),(0 until a.length()).map{itemFrom(a.getJSONObject(it))}.toMutableList(),o.optString("payment"),o.optInt("installments",1),o.optDouble("cardFeePercent"),o.optBoolean("passCardFee"),o.optString("notes"),o.optLong("createdAt"),o.optDouble("chargedTotal"),o.optDouble("cardFeeValue"),o.optDouble("netRevenue"),o.optDouble("discountAmount"))}
    private fun companyJson(c:Company)=JSONObject().put("name",c.name).put("document",c.document).put("phone",c.phone).put("email",c.email).put("address",c.address).put("sellerName",c.sellerName).put("sellerPhone",c.sellerPhone).put("pixKey",c.pixKey).put("logoUri",c.logoUri)
    private fun companyFromJson(o:JSONObject)=Company(o.optString("name","Lotus Distribuidora"),o.optString("document"),o.optString("phone"),o.optString("email"),o.optString("address"),o.optString("sellerName"),o.optString("sellerPhone"),o.optString("pixKey"),o.optString("logoUri"))
}
