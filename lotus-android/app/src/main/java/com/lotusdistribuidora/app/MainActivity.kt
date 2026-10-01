package com.lotusdistribuidora.app

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale

private val Rose=Color(0xFFB65C7A)
private val Plum=Color(0xFF6F3D5E)
private val Lilac=Color(0xFFEBDDF1)
private val Champagne=Color(0xFFF2DFC0)
private val Ivory=Color(0xFFFFF8FB)
private val Ink=Color(0xFF322730)
private val Success=Color(0xFF477A67)

class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        val store=LotusStore(this)
        setContent{LotusTheme{LotusApp(store)}}
    }
}

@Composable
private fun LotusTheme(content:@Composable()->Unit){
    MaterialTheme(
        colorScheme=lightColorScheme(primary=Rose,onPrimary=Color.White,secondary=Plum,tertiary=Champagne,background=Ivory,surface=Color.White,onSurface=Ink,outline=Color(0xFFD9CBD3)),
        content=content
    )
}

private enum class Section(val title:String){DASH("Painel"),PRODUCTS("Produtos"),CLIENTS("Clientes"),QUOTES("Orçamentos"),SALES("Vendas"),COMPANY("Empresa")}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LotusApp(store:LotusStore){
    var section by remember{mutableStateOf(Section.DASH)}
    Scaffold(
        containerColor=Ivory,
        topBar={
            Column(Modifier.background(Ivory)){
                TopAppBar(
                    title={Column{Text("Lotus Distribuidora",fontWeight=FontWeight.Bold,color=Plum);Text("Gestão comercial e financeira",fontSize=12.sp,color=Rose)}},
                    colors=TopAppBarDefaults.topAppBarColors(containerColor=Ivory)
                )
                ScrollableTabRow(selectedTabIndex=section.ordinal,edgePadding=10.dp,containerColor=Ivory,contentColor=Rose){
                    Section.entries.forEach{s->Tab(selected=s==section,onClick={section=s},text={Text(s.title)})}
                }
            }
        }
    ){pad->
        Box(Modifier.padding(pad).fillMaxSize()){
            when(section){
                Section.DASH->DashboardScreen(store)
                Section.PRODUCTS->ProductsScreen(store)
                Section.CLIENTS->ClientsScreen(store)
                Section.QUOTES->QuotesScreen(store)
                Section.SALES->SalesScreen(store)
                Section.COMPANY->CompanyScreen(store)
            }
        }
    }
}

@Composable
private fun SectionTitle(title:String,subtitle:String){
    Text(title,fontSize=26.sp,fontWeight=FontWeight.Bold,color=Plum)
    Text(subtitle,fontSize=14.sp,color=Rose)
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun FormCard(content:@Composable ColumnScope.()->Unit){
    Card(shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(2.dp)){
        Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)
    }
}

@Composable
private fun DataCard(content:@Composable ColumnScope.()->Unit){
    Card(shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(1.dp)){
        Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp),content=content)
    }
}

@Composable
private fun Field(label:String,value:String,modifier:Modifier=Modifier.fillMaxWidth(),keyboard:KeyboardType=KeyboardType.Text,singleLine:Boolean=true,onChange:(String)->Unit){
    OutlinedTextField(value=value,onValueChange=onChange,label={Text(label)},modifier=modifier,shape=RoundedCornerShape(16.dp),singleLine=singleLine,keyboardOptions=KeyboardOptions(keyboardType=keyboard))
}

@Composable
private fun Metric(label:String,value:String,modifier:Modifier=Modifier){
    Card(modifier,shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(2.dp)){
        Column(Modifier.padding(14.dp)){Text(label,fontSize=12.sp,color=Color(0xFF806B77));Spacer(Modifier.height(4.dp));Text(value,fontSize=19.sp,fontWeight=FontWeight.Bold,color=Plum)}
    }
}

@Composable
private fun DashboardScreen(store:LotusStore){
    val now=Calendar.getInstance()
    val monthSales=store.sales.filter{val c=Calendar.getInstance().apply{timeInMillis=it.createdAt};c.get(Calendar.MONTH)==now.get(Calendar.MONTH)&&c.get(Calendar.YEAR)==now.get(Calendar.YEAR)}
    val count=monthSales.size
    val qty=monthSales.sumOf{s->s.items.sumOf{it.qty}}
    val gross=monthSales.sumOf{it.chargedTotal}
    val net=monthSales.sumOf{it.netRevenue}
    val fees=monthSales.sumOf{it.cardFeeValue}
    val discounts=monthSales.sumOf{it.discountAmount}
    val cogs=monthSales.sumOf{s->s.items.sumOf{it.qty*it.unitCostSnapshot}}
    val profit=net-cogs
    val ticket=if(count>0)gross/count else 0.0
    val margin=if(net>0)profit/net*100 else 0.0
    val stockCapital=store.products.sumOf{it.stock*it.cost}
    val low=store.products.count{it.stock<=it.minStock}
    val soldByProduct=monthSales.flatMap{it.items}.groupBy{it.productName}.mapValues{e->e.value.sumOf{it.qty}}
    val topProduct=soldByProduct.maxByOrNull{it.value}?.let{it.key+" ("+it.value+")"}?:"—"

    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{SectionTitle("Visão do mês","Resultados calculados automaticamente pelas vendas finalizadas")}
        item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Vendas",count.toString(),Modifier.weight(1f));Metric("Produtos vendidos",qty.toString(),Modifier.weight(1f))}}
        item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Faturamento bruto",gross.money(),Modifier.weight(1f));Metric("Faturamento líquido",net.money(),Modifier.weight(1f))}}
        item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("CMV",cogs.money(),Modifier.weight(1f));Metric("Lucro líquido",profit.money(),Modifier.weight(1f))}}
        item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Taxas cartão",fees.money(),Modifier.weight(1f));Metric("Descontos",discounts.money(),Modifier.weight(1f))}}
        item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Ticket médio",ticket.money(),Modifier.weight(1f));Metric("Margem líquida",String.format(Locale("pt","BR"),"%.1f%%",margin),Modifier.weight(1f))}}
        item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Capital em estoque",stockCapital.money(),Modifier.weight(1f));Metric("Estoque baixo",low.toString(),Modifier.weight(1f))}}
        item{Metric("Produto mais vendido",topProduct,Modifier.fillMaxWidth())}
    }
}

@Composable
private fun ProductsScreen(store:LotusStore){
    var version by remember{mutableIntStateOf(0)}
    var id by remember{mutableLongStateOf(0L)}
    var supplier by remember{mutableStateOf("")};var name by remember{mutableStateOf("")};var sku by remember{mutableStateOf("")}
    var stock by remember{mutableStateOf("")};var minStock by remember{mutableStateOf("")};var cost by remember{mutableStateOf("")};var price by remember{mutableStateOf("")}
    fun clear(){id=0;supplier="";name="";sku="";stock="";minStock="";cost="";price=""}
    val profit=price.num()-cost.num()
    val margin=if(price.num()>0)profit/price.num()*100 else 0.0
    @Suppress("UNUSED_EXPRESSION") version

    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{SectionTitle("Produtos e estoque","Fornecedor, custo, revenda e lucro no mesmo cadastro")}
        item{FormCard{
            Field("Fornecedor",supplier){supplier=it};Field("Produto",name){name=it};Field("Código / SKU",sku){sku=it}
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Field("Estoque",stock,Modifier.weight(1f),KeyboardType.Number){stock=it};Field("Estoque mínimo",minStock,Modifier.weight(1f),KeyboardType.Number){minStock=it}}
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Field("Valor pago",cost,Modifier.weight(1f),KeyboardType.Decimal){cost=it};Field("Preço de revenda",price,Modifier.weight(1f),KeyboardType.Decimal){price=it}}
            Surface(color=Lilac.copy(alpha=.55f),shape=RoundedCornerShape(16.dp)){Row(Modifier.fillMaxWidth().padding(12.dp),horizontalArrangement=Arrangement.SpaceBetween){Text("Lucro/unid. "+profit.money(),fontWeight=FontWeight.SemiBold,color=Plum);Text(String.format(Locale("pt","BR"),"%.1f%%",margin),color=Rose,fontWeight=FontWeight.Bold)}}
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button(onClick={if(name.isNotBlank()){store.upsertProduct(Product(if(id==0L)System.currentTimeMillis() else id,supplier.trim(),name.trim(),sku.trim(),stock.intNum(),minStock.intNum(),cost.num(),price.num()));version++;clear()}},modifier=Modifier.weight(1f)){Text(if(id==0L)"Cadastrar produto" else "Salvar alterações")}
                if(id!=0L)OutlinedButton(onClick={clear()}){Text("Cancelar")}
            }
        }}
        item{Text("Produtos cadastrados",fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)}
        items(store.products.sortedBy{it.name}){p->DataCard{
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Column(Modifier.weight(1f)){Text(p.name,fontWeight=FontWeight.Bold);Text(p.supplier.ifBlank{"Fornecedor não informado"},color=Rose,fontSize=13.sp);Text("Estoque "+p.stock+" • Custo "+p.cost.money()+" • Venda "+p.price.money(),fontSize=13.sp);Text("Lucro/unid. "+(p.price-p.cost).money(),color=Success,fontWeight=FontWeight.SemiBold)}
                TextButton(onClick={id=p.id;supplier=p.supplier;name=p.name;sku=p.sku;stock=p.stock.toString();minStock=p.minStock.toString();cost=p.cost.toString().replace('.',',');price=p.price.toString().replace('.',',')}){Text("Editar")}
            }
        }}
    }
}

@Composable
private fun ClientsScreen(store:LotusStore){
    var version by remember{mutableIntStateOf(0)}
    var id by remember{mutableLongStateOf(0L)}
    var name by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")};var cpf by remember{mutableStateOf("")};var email by remember{mutableStateOf("")};var profession by remember{mutableStateOf("")}
    var cep by remember{mutableStateOf("")};var street by remember{mutableStateOf("")};var number by remember{mutableStateOf("")};var complement by remember{mutableStateOf("")};var district by remember{mutableStateOf("")};var city by remember{mutableStateOf("")};var state by remember{mutableStateOf("")};var notes by remember{mutableStateOf("")}
    fun clear(){id=0;name="";phone="";cpf="";email="";profession="";cep="";street="";number="";complement="";district="";city="";state="";notes=""}
    @Suppress("UNUSED_EXPRESSION") version

    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{SectionTitle("Clientes","Dados completos para orçamento, venda e relacionamento")}
        item{FormCard{
            Field("Nome completo",name){name=it};Field("Telefone / WhatsApp",phone){phone=it};Field("CPF",cpf){cpf=it};Field("E-mail",email,keyboard=KeyboardType.Email){email=it};Field("Profissão",profession){profession=it}
            Text("Endereço",fontWeight=FontWeight.SemiBold,color=Plum);Field("CEP",cep){cep=it};Field("Logradouro",street){street=it}
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Field("Número",number,Modifier.weight(1f)){number=it};Field("Complemento",complement,Modifier.weight(1f)){complement=it}}
            Field("Bairro",district){district=it};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Field("Cidade",city,Modifier.weight(1f)){city=it};Field("UF",state,Modifier.width(90.dp)){state=it.take(2).uppercase()}}
            Field("Observações",notes,singleLine=false){notes=it}
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button(onClick={if(name.isNotBlank()){store.upsertClient(Client(if(id==0L)System.currentTimeMillis() else id,name,phone,cpf,email,profession,cep,street,number,complement,district,city,state,notes));version++;clear()}},modifier=Modifier.weight(1f)){Text(if(id==0L)"Cadastrar cliente" else "Salvar alterações")}
                if(id!=0L)OutlinedButton(onClick={clear()}){Text("Cancelar")}
            }
        }}
        item{Text("Clientes cadastrados",fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)}
        items(store.clients.sortedBy{it.name}){c->DataCard{
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Column(Modifier.weight(1f)){Text(c.name,fontWeight=FontWeight.Bold);Text(c.phone,color=Rose);Text(listOf(c.profession,c.city,c.state).filter{it.isNotBlank()}.joinToString(" • "),fontSize=13.sp)}
                TextButton(onClick={id=c.id;name=c.name;phone=c.phone;cpf=c.cpf;email=c.email;profession=c.profession;cep=c.cep;street=c.street;number=c.number;complement=c.complement;district=c.district;city=c.city;state=c.state;notes=c.notes}){Text("Editar")}
            }
        }}
    }
}

@Composable
private fun Selector(label:String,selected:String,options:List<String>,onSelect:(Int)->Unit){
    var open by remember{mutableStateOf(false)}
    Box{
        OutlinedButton(onClick={open=true},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)){Text(if(selected.isBlank())label else selected)}
        DropdownMenu(expanded=open,onDismissRequest={open=false}){options.forEachIndexed{i,s->DropdownMenuItem(text={Text(s)},onClick={open=false;onSelect(i)})}}
    }
}

@Composable
private fun QuotesScreen(store:LotusStore){
    val context=androidx.compose.ui.platform.LocalContext.current
    var version by remember{mutableIntStateOf(0)}
    var editId by remember{mutableLongStateOf(0L)}
    var createdAt by remember{mutableLongStateOf(0L)}
    var clientId by remember{mutableLongStateOf(0L)}
    var clientName by remember{mutableStateOf("")}
    val qItems=remember{mutableStateListOf<QuoteItem>()}
    var discountType by remember{mutableStateOf("R$")}
    var discountValue by remember{mutableStateOf("")}
    var payment by remember{mutableStateOf("Pix")}
    var installments by remember{mutableStateOf("1")}
    var cardFee by remember{mutableStateOf("")}
    var passFee by remember{mutableStateOf(false)}
    var notes by remember{mutableStateOf("")}
    var msg by remember{mutableStateOf("")}
    @Suppress("UNUSED_EXPRESSION") version

    fun clear(){editId=0;createdAt=0;clientId=0;clientName="";qItems.clear();discountType="R$";discountValue="";payment="Pix";installments="1";cardFee="";passFee=false;notes="";msg=""}
    fun currentQuote()=Quote(if(editId==0L)System.currentTimeMillis() else editId,clientId,clientName,qItems.map{it.copy()}.toMutableList(),discountType,discountValue.num(),payment,installments.intNum().coerceAtLeast(1),cardFee.num(),passFee,notes,if(createdAt==0L)System.currentTimeMillis() else createdAt,false)
    val calc=calculateQuote(currentQuote())

    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{SectionTitle(if(editId==0L)"Novo orçamento" else "Editar orçamento","Pode ser alterado livremente até a venda ser finalizada")}
        item{FormCard{
            Selector("Selecionar cliente",clientName,store.clients.map{it.name}){i->store.clients.getOrNull(i)?.let{clientId=it.id;clientName=it.name}}
            Text("Produtos",fontWeight=FontWeight.Bold,color=Plum)
            Selector("Adicionar produto","",store.products.map{it.name+" • estoque "+it.stock}){i->
                store.products.getOrNull(i)?.let{p->
                    val ix=qItems.indexOfFirst{it.productId==p.id}
                    if(ix>=0){val old=qItems[ix];qItems[ix]=old.copy(qty=old.qty+1)}
                    else qItems.add(QuoteItem(p.id,p.name,1,p.price,0.0,p.cost))
                }
            }
            qItems.forEachIndexed{i,item->
                Surface(color=Ivory,shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(item.productName,fontWeight=FontWeight.Bold);TextButton(onClick={qItems.removeAt(i)}){Text("Remover")}}
                    Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                        Field("Qtd.",item.qty.toString(),Modifier.weight(.7f),KeyboardType.Number){v->qItems[i]=item.copy(qty=v.intNum().coerceAtLeast(0))}
                        Field("Preço",item.unitPrice.toString().replace('.',','),Modifier.weight(1f),KeyboardType.Decimal){v->qItems[i]=item.copy(unitPrice=v.num())}
                        Field("Desc. %",item.itemDiscountPercent.toString().replace('.',','),Modifier.weight(1f),KeyboardType.Decimal){v->qItems[i]=item.copy(itemDiscountPercent=v.num())}
                    }
                }}
            }
            Text("Desconto geral",fontWeight=FontWeight.Bold,color=Plum)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){
                FilterChip(selected=discountType=="R$",onClick={discountType="R$"},label={Text("R$")})
                FilterChip(selected=discountType=="%",onClick={discountType="%"},label={Text("%")})
                Field(if(discountType=="%")"Percentual" else "Valor",discountValue,Modifier.weight(1f),KeyboardType.Decimal){discountValue=it}
            }
            Text("Pagamento",fontWeight=FontWeight.Bold,color=Plum)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(selected=payment=="Pix",onClick={payment="Pix"},label={Text("Pix")});FilterChip(selected=payment=="Cartão",onClick={payment="Cartão"},label={Text("Cartão")})}
            if(payment=="Cartão"){
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Field("Parcelas",installments,Modifier.weight(1f),KeyboardType.Number){installments=it};Field("Tarifa %",cardFee,Modifier.weight(1f),KeyboardType.Decimal){cardFee=it}}
                Row(verticalAlignment=Alignment.CenterVertically){Switch(checked=passFee,onCheckedChange={passFee=it});Spacer(Modifier.width(8.dp));Text("Repassar tarifa ao cliente")}
            }
            Field("Observações",notes,singleLine=false){notes=it}
            Surface(color=Lilac.copy(alpha=.55f),shape=RoundedCornerShape(18.dp)){Column(Modifier.fillMaxWidth().padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
                Text("Subtotal: "+calc.subtotal.money())
                Text("Descontos: "+(calc.itemDiscounts+calc.generalDiscount).money(),color=Rose)
                if(payment=="Cartão")Text("Tarifa cartão: "+calc.cardFee.money())
                Text("TOTAL: "+calc.charged.money(),fontSize=20.sp,fontWeight=FontWeight.Bold,color=Plum)
            }}
            if(msg.isNotBlank())Text(msg,color=if(msg.startsWith("Salvo")||msg.startsWith("Venda"))Success else MaterialTheme.colorScheme.error,fontWeight=FontWeight.SemiBold)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button(onClick={if(clientId==0L||qItems.isEmpty())msg="Selecione cliente e pelo menos um produto." else{val q=currentQuote();store.upsertQuote(q);editId=q.id;createdAt=q.createdAt;version++;msg="Salvo. Você pode continuar editando."}},modifier=Modifier.weight(1f)){Text("Salvar orçamento")}
                if(editId!=0L)OutlinedButton(onClick={clear()}){Text("Novo")}
            }
        }}
        item{Text("Orçamentos",fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)}
        items(store.quotes){q->DataCard{
            val qc=calculateQuote(q)
            Text("#"+q.id.toString().takeLast(6)+" • "+q.clientName,fontWeight=FontWeight.Bold)
            Text(qc.charged.money()+" • "+q.payment+(if(q.payment=="Cartão")" "+q.installments+"x" else ""),color=Rose)
            Text(if(q.finalized)"Venda finalizada" else "Editável",color=if(q.finalized)Success else Plum,fontWeight=FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)){
                if(!q.finalized)TextButton(onClick={editId=q.id;createdAt=q.createdAt;clientId=q.clientId;clientName=q.clientName;qItems.clear();qItems.addAll(q.items.map{it.copy()});discountType=q.discountType;discountValue=q.discountValue.toString().replace('.',',');payment=q.payment;installments=q.installments.toString();cardFee=q.cardFeePercent.toString().replace('.',',');passFee=q.passCardFee;notes=q.notes;msg=""}){Text("Editar")}
                TextButton(onClick={PdfUtil.shareQuote(context,store.company,q)}){Text("PDF")}
                if(!q.finalized)TextButton(onClick={val err=store.finalizeQuote(q);version++;msg=err?:"Venda finalizada e estoque baixado."}){Text("Finalizar venda")}
            }
        }}
    }
}

@Composable
private fun SalesScreen(store:LotusStore){
    val context=androidx.compose.ui.platform.LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{SectionTitle("Vendas","Histórico com valores, custos, taxas e recibos")}
        if(store.sales.isEmpty())item{DataCard{Text("Nenhuma venda finalizada ainda.",color=Rose)}}
        items(store.sales){s->DataCard{
            val cogs=s.items.sumOf{it.qty*it.unitCostSnapshot}
            val profit=s.netRevenue-cogs
            Text("#"+s.id.toString().takeLast(6)+" • "+s.clientName,fontWeight=FontWeight.Bold)
            Text("Total cobrado: "+s.chargedTotal.money(),color=Plum,fontWeight=FontWeight.SemiBold)
            Text("Líquido: "+s.netRevenue.money()+" • CMV: "+cogs.money())
            Text("Lucro: "+profit.money(),color=if(profit>=0)Success else MaterialTheme.colorScheme.error,fontWeight=FontWeight.Bold)
            Text(s.payment+(if(s.payment=="Cartão")" • "+s.installments+"x • taxa "+s.cardFeePercent+"%" else ""),fontSize=13.sp,color=Rose)
            TextButton(onClick={PdfUtil.shareSale(context,store.company,s)}){Text("Gerar / enviar recibo PDF")}
        }}
    }
}

@Composable
private fun CompanyScreen(store:LotusStore){
    val context=androidx.compose.ui.platform.LocalContext.current
    var version by remember{mutableIntStateOf(0)}
    var c by remember(version){mutableStateOf(store.company.copy())}
    var msg by remember{mutableStateOf("")}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
        if(uri!=null){try{context.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){};c=c.copy(logoUri=uri.toString())}
    }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        item{SectionTitle("Dados da empresa","Informações usadas automaticamente em orçamentos e recibos")}
        item{FormCard{
            Field("Nome da empresa",c.name){c=c.copy(name=it)}
            Field("CNPJ / documento",c.document){c=c.copy(document=it)}
            Field("Telefone / WhatsApp",c.phone){c=c.copy(phone=it)}
            Field("E-mail",c.email,keyboard=KeyboardType.Email){c=c.copy(email=it)}
            Field("Endereço",c.address){c=c.copy(address=it)}
            HorizontalDivider()
            Text("Vendedor responsável",fontWeight=FontWeight.Bold,color=Plum)
            Field("Nome do vendedor",c.sellerName){c=c.copy(sellerName=it)}
            Field("Contato do vendedor",c.sellerPhone){c=c.copy(sellerPhone=it)}
            Field("Chave Pix",c.pixKey){c=c.copy(pixKey=it)}
            OutlinedButton(onClick={picker.launch(arrayOf("image/*"))},modifier=Modifier.fillMaxWidth()){Text(if(c.logoUri.isBlank())"Selecionar logo da Lotus" else "Trocar logo selecionada")}
            Button(onClick={store.saveCompany(c);version++;msg="Dados salvos."},modifier=Modifier.fillMaxWidth()){Text("Salvar dados da empresa")}
            if(msg.isNotBlank())Text(msg,color=Success,fontWeight=FontWeight.SemiBold)
        }}
    }
}
