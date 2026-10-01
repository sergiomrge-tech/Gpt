package com.lotusdistribuidora.app

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale

private val Rose=Color(0xFFB65C7A)
private val DeepRose=Color(0xFF9D4567)
private val Plum=Color(0xFF6F3D5E)
private val Lilac=Color(0xFFF0E5F4)
private val Blush=Color(0xFFFFEEF3)
private val Champagne=Color(0xFFF2DFC0)
private val Ivory=Color(0xFFFFF9FB)
private val Ink=Color(0xFF322730)
private val Muted=Color(0xFF806B77)
private val Success=Color(0xFF477A67)

class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        val store=LotusStore(this)
        setContent{LotusTheme{LotusApp(store)}}
    }
}

@Composable
private fun LotusTheme(content: @Composable () -> Unit){
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
    var quickActions by remember{mutableStateOf(false)}
    var moreMenu by remember{mutableStateOf(false)}

    Scaffold(
        containerColor=Ivory,
        topBar={ LotusPremiumHeader(store) },
        bottomBar={
            NavigationBar(
                containerColor=Color.White,
                tonalElevation=8.dp
            ){
                NavigationBarItem(
                    selected=section==Section.DASH,
                    onClick={section=Section.DASH},
                    icon={Icon(Icons.Default.Home,null)},
                    label={Text("Início")}
                )
                NavigationBarItem(
                    selected=section==Section.CLIENTS,
                    onClick={section=Section.CLIENTS},
                    icon={Icon(Icons.Default.People,null)},
                    label={Text("Clientes")}
                )
                NavigationBarItem(
                    selected=false,
                    onClick={quickActions=true},
                    icon={Box(Modifier.size(38.dp).clip(CircleShape).background(Rose),contentAlignment=Alignment.Center){Icon(Icons.Default.Add,null,tint=Color.White)}},
                    label={Text("Novo")}
                )
                NavigationBarItem(
                    selected=section==Section.PRODUCTS,
                    onClick={section=Section.PRODUCTS},
                    icon={Icon(Icons.Default.Inventory2,null)},
                    label={Text("Estoque")}
                )
                NavigationBarItem(
                    selected=section==Section.SALES || section==Section.COMPANY || section==Section.QUOTES,
                    onClick={moreMenu=true},
                    icon={Icon(Icons.Default.MoreHoriz,null)},
                    label={Text("Mais")}
                )
            }
        }
    ){pad->
        Box(Modifier.padding(pad).fillMaxSize()){
            when(section){
                Section.DASH->DashboardScreen(store,onNavigate={section=it})
                Section.PRODUCTS->ProductsScreen(store)
                Section.CLIENTS->ClientsScreen(store)
                Section.QUOTES->QuotesScreen(store)
                Section.SALES->SalesScreen(store)
                Section.COMPANY->CompanyScreen(store)
            }
        }
    }

    if(quickActions){
        AlertDialog(
            onDismissRequest={quickActions=false},
            title={Text("Criar novo",color=Plum,fontWeight=FontWeight.Bold)},
            text={
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
                    PremiumActionButton("Novo orçamento",Icons.Default.ReceiptLong){quickActions=false;section=Section.QUOTES}
                    PremiumActionButton("Nova venda",Icons.Default.ShoppingBag){quickActions=false;section=Section.QUOTES}
                    PremiumActionButton("Novo cliente",Icons.Default.People){quickActions=false;section=Section.CLIENTS}
                    PremiumActionButton("Novo produto",Icons.Default.Inventory2){quickActions=false;section=Section.PRODUCTS}
                }
            },
            confirmButton={}
        )
    }

    if(moreMenu){
        AlertDialog(
            onDismissRequest={moreMenu=false},
            title={Text("Mais opções",color=Plum,fontWeight=FontWeight.Bold)},
            text={
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
                    PremiumActionButton("Orçamentos",Icons.Default.ReceiptLong){moreMenu=false;section=Section.QUOTES}
                    PremiumActionButton("Vendas",Icons.Default.ShoppingBag){moreMenu=false;section=Section.SALES}
                    PremiumActionButton("Dados da empresa",Icons.Default.Settings){moreMenu=false;section=Section.COMPANY}
                }
            },
            confirmButton={}
        )
    }
}

@Composable
private fun LotusPremiumHeader(store:LotusStore){
    Surface(color=Ivory,shadowElevation=1.dp){
        Row(
            Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=10.dp),
            verticalAlignment=Alignment.CenterVertically
        ){
            LotusLogoImage(Modifier.width(104.dp).height(58.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)){
                Text("Lotus Distribuidora",fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
                Text("Gestão comercial e financeira",fontSize=11.sp,color=Rose)
            }
        }
    }
}

@Composable
private fun LotusLogoImage(modifier:Modifier=Modifier){
    val image=remember{LotusBrand.bitmap()?.asImageBitmap()}
    if(image!=null){
        Image(
            bitmap=image,
            contentDescription="Lotus Distribuidora",
            modifier=modifier,
            contentScale=ContentScale.Fit
        )
    }else{
        Surface(modifier=modifier,shape=RoundedCornerShape(16.dp),color=Blush){
            Box(contentAlignment=Alignment.Center){
                Text("LOTUS",fontWeight=FontWeight.Bold,color=Plum)
            }
        }
    }
}

@Composable
private fun PremiumActionButton(label:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit){
    OutlinedButton(
        onClick=onClick,
        modifier=Modifier.fillMaxWidth(),
        shape=RoundedCornerShape(16.dp),
        colors=ButtonDefaults.outlinedButtonColors(contentColor=Plum)
    ){
        Icon(icon,null)
        Spacer(Modifier.width(10.dp))
        Text(label,Modifier.weight(1f))
    }
}

@Composable
private fun SectionTitle(title:String,subtitle:String){
    Text(title,fontSize=25.sp,fontWeight=FontWeight.Bold,color=Plum)
    Text(subtitle,fontSize=13.sp,color=Muted)
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun FormCard(content:@Composable ColumnScope.()->Unit){
    Card(shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(3.dp)){
        Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)
    }
}

@Composable
private fun DataCard(content:@Composable ColumnScope.()->Unit){
    Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(2.dp)){
        Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp),content=content)
    }
}

@Composable
private fun Field(label:String,value:String,modifier:Modifier=Modifier.fillMaxWidth(),keyboard:KeyboardType=KeyboardType.Text,singleLine:Boolean=true,onChange:(String)->Unit){
    OutlinedTextField(
        value=value,
        onValueChange=onChange,
        label={Text(label)},
        modifier=modifier,
        shape=RoundedCornerShape(18.dp),
        singleLine=singleLine,
        keyboardOptions=KeyboardOptions(keyboardType=keyboard),
        colors=OutlinedTextFieldDefaults.colors(
            focusedBorderColor=Rose,
            focusedLabelColor=Rose,
            cursorColor=Rose,
            unfocusedBorderColor=Color(0xFFE4D6DE)
        )
    )
}

@Composable
private fun Metric(label:String,value:String,modifier:Modifier=Modifier){
    Card(modifier,shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(2.dp)){
        Column(Modifier.padding(14.dp)){Text(label,fontSize=12.sp,color=Color(0xFF806B77));Spacer(Modifier.height(4.dp));Text(value,fontSize=19.sp,fontWeight=FontWeight.Bold,color=Plum)}
    }
}

@Composable
private fun DashboardScreen(store:LotusStore,onNavigate:(Section)->Unit){
    val now=Calendar.getInstance()
    val monthSales=store.sales.filter{
        val c=Calendar.getInstance().apply{timeInMillis=it.createdAt}
        c.get(Calendar.MONTH)==now.get(Calendar.MONTH)&&c.get(Calendar.YEAR)==now.get(Calendar.YEAR)
    }
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
    val pendingQuotes=store.quotes.count{!it.finalized}
    val soldByProduct=monthSales.flatMap{it.items}.groupBy{it.productName}.mapValues{e->e.value.sumOf{it.qty}}
    val topProduct=soldByProduct.maxByOrNull{it.value}?.let{it.key+" ("+it.value+")"}?:"—"
    val sellerFirst=store.company.sellerName.trim().substringBefore(" ").takeIf{it.isNotBlank()}
    val monthName=java.text.SimpleDateFormat("MMMM yyyy",Locale("pt","BR")).format(now.time)
        .replaceFirstChar{if(it.isLowerCase())it.titlecase(Locale("pt","BR")) else it.toString()}

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal=16.dp,vertical=14.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)
    ){
        item{
            Text(
                if(sellerFirst!=null)"Olá, $sellerFirst!" else "Olá! Seja bem-vinda.",
                fontSize=25.sp,fontWeight=FontWeight.Bold,color=Plum
            )
            Text("Acompanhe a Lotus em um só lugar",fontSize=13.sp,color=Muted)
            Spacer(Modifier.height(12.dp))
            Surface(color=Color.White,shape=RoundedCornerShape(18.dp),shadowElevation=2.dp){
                Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){
                    Text("📅",fontSize=18.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(monthName,Modifier.weight(1f),fontWeight=FontWeight.SemiBold,color=Plum)
                    Text("Visão mensal",fontSize=11.sp,color=Rose)
                }
            }
        }
        item{
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                PremiumMetricCard("Vendas do mês",gross.money(),"${count} venda(s)",DeepRose,Color.White,Modifier.weight(1f))
                PremiumMetricCard("Lucro líquido",profit.money(),String.format(Locale("pt","BR"),"%.1f%% margem",margin),Blush,Plum,Modifier.weight(1f))
            }
        }
        item{
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                SmallStatusCard("Orçamentos","$pendingQuotes pendente(s)",Icons.Default.ReceiptLong,Modifier.weight(1f)){onNavigate(Section.QUOTES)}
                SmallStatusCard("Estoque baixo","$low produto(s)",Icons.Default.Inventory2,Modifier.weight(1f)){onNavigate(Section.PRODUCTS)}
            }
        }
        item{
            Text("Acesso rápido",fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                QuickTile("Novo orçamento",Icons.Default.ReceiptLong,Modifier.weight(1f)){onNavigate(Section.QUOTES)}
                QuickTile("Nova venda",Icons.Default.ShoppingBag,Modifier.weight(1f)){onNavigate(Section.QUOTES)}
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                QuickTile("Cliente",Icons.Default.People,Modifier.weight(1f)){onNavigate(Section.CLIENTS)}
                QuickTile("Produto",Icons.Default.Inventory2,Modifier.weight(1f)){onNavigate(Section.PRODUCTS)}
            }
        }
        item{
            Text("Resumo financeiro",fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
            Spacer(Modifier.height(6.dp))
            DataCard{
                PremiumLine("Faturamento líquido",net.money())
                PremiumLine("CMV",cogs.money())
                PremiumLine("Taxas de cartão",fees.money())
                PremiumLine("Descontos",discounts.money())
                PremiumLine("Ticket médio",ticket.money())
                PremiumLine("Capital em estoque",stockCapital.money())
                PremiumLine("Produtos vendidos",qty.toString())
                PremiumLine("Mais vendido",topProduct)
            }
        }
        if(monthSales.isNotEmpty()){
            item{
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                    Text("Vendas recentes",Modifier.weight(1f),fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
                    TextButton(onClick={onNavigate(Section.SALES)}){Text("Ver todas")}
                }
            }
            items(monthSales.take(4)){sale->
                DataCard{
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                        Surface(color=Blush,shape=CircleShape){
                            Icon(Icons.Default.ShoppingBag,null,tint=Rose,modifier=Modifier.padding(10.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)){
                            Text(sale.clientName.ifBlank{"Cliente"},fontWeight=FontWeight.SemiBold,color=Ink)
                            Text(sale.chargedTotal.money(),fontSize=13.sp,color=Rose)
                        }
                        Surface(color=Color(0xFFE7F3ED),shape=RoundedCornerShape(50)){
                            Text("Venda",Modifier.padding(horizontal=10.dp,vertical=5.dp),fontSize=11.sp,color=Success,fontWeight=FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumMetricCard(label:String,value:String,caption:String,bg:Color,fg:Color,modifier:Modifier=Modifier){
    Card(modifier,shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=bg),elevation=CardDefaults.cardElevation(3.dp)){
        Column(Modifier.padding(16.dp)){
            Text(label,fontSize=12.sp,color=fg.copy(alpha=.85f))
            Spacer(Modifier.height(6.dp))
            Text(value,fontSize=21.sp,fontWeight=FontWeight.Bold,color=fg)
            Spacer(Modifier.height(4.dp))
            Text(caption,fontSize=11.sp,color=fg.copy(alpha=.78f))
        }
    }
}

@Composable
private fun SmallStatusCard(label:String,value:String,icon:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier=Modifier,onClick:()->Unit){
    Card(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(2.dp)){
        Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){
            Surface(color=Blush,shape=RoundedCornerShape(14.dp)){Icon(icon,null,tint=Rose,modifier=Modifier.padding(9.dp))}
            Spacer(Modifier.width(10.dp))
            Column{Text(label,fontSize=12.sp,color=Muted);Text(value,fontWeight=FontWeight.Bold,color=Plum)}
        }
    }
}

@Composable
private fun QuickTile(label:String,icon:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier=Modifier,onClick:()->Unit){
    Card(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(2.dp)){
        Column(Modifier.padding(14.dp),horizontalAlignment=Alignment.CenterHorizontally){
            Surface(color=Lilac,shape=RoundedCornerShape(16.dp)){Icon(icon,null,tint=Plum,modifier=Modifier.padding(10.dp))}
            Spacer(Modifier.height(8.dp))
            Text(label,fontSize=12.sp,fontWeight=FontWeight.SemiBold,color=Plum)
        }
    }
}

@Composable
private fun PremiumLine(label:String,value:String){
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
        Text(label,Modifier.weight(1f),fontSize=13.sp,color=Muted)
        Text(value,fontWeight=FontWeight.SemiBold,color=Plum,fontSize=13.sp)
    }
}

@Composable
private fun ProductsScreen(store:LotusStore){
    var version by remember{mutableIntStateOf(0)}
    var id by remember{mutableLongStateOf(0L)}
    var supplier by remember{mutableStateOf("")};var name by remember{mutableStateOf("")};var sku by remember{mutableStateOf("")}
    var stock by remember{mutableStateOf("")};var minStock by remember{mutableStateOf("")};var cost by remember{mutableStateOf("")};var price by remember{mutableStateOf("")}
    var search by remember{mutableStateOf("")}
    fun clear(){id=0;supplier="";name="";sku="";stock="";minStock="";cost="";price=""}
    val profit=price.num()-cost.num()
    val margin=if(price.num()>0)profit/price.num()*100 else 0.0
    val stockCapital=store.products.sumOf{it.stock*it.cost}
    val low=store.products.count{it.stock<=it.minStock}
    val filtered=store.products.filter{
        search.isBlank() || it.name.contains(search,true) || it.supplier.contains(search,true) || it.sku.contains(search,true)
    }.sortedBy{it.name}
    @Suppress("UNUSED_EXPRESSION") version

    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{SectionTitle("Produtos e estoque","Controle de fornecedor, custo, revenda e margem")}
        item{
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                PremiumMetricCard("Produtos",store.products.size.toString(),"cadastrados",Blush,Plum,Modifier.weight(1f))
                PremiumMetricCard("Estoque",stockCapital.money(),"${low} baixo(s)",Color.White,Plum,Modifier.weight(1f))
            }
        }
        item{
            Field("Buscar produto, fornecedor ou SKU",search){search=it}
        }
        item{FormCard{
            Text(if(id==0L)"Cadastrar produto" else "Editar produto",fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
            Field("Fornecedor",supplier){supplier=it}
            Field("Produto",name){name=it}
            Field("Código / SKU",sku){sku=it}
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Field("Estoque",stock,Modifier.weight(1f),KeyboardType.Number){stock=it}
                Field("Estoque mínimo",minStock,Modifier.weight(1f),KeyboardType.Number){minStock=it}
            }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Field("Valor pago",cost,Modifier.weight(1f),KeyboardType.Decimal){cost=it}
                Field("Preço de revenda",price,Modifier.weight(1f),KeyboardType.Decimal){price=it}
            }
            Surface(color=Lilac,shape=RoundedCornerShape(18.dp)){
                Row(Modifier.fillMaxWidth().padding(14.dp),horizontalArrangement=Arrangement.SpaceBetween){
                    Column{Text("Lucro por unidade",fontSize=11.sp,color=Muted);Text(profit.money(),fontWeight=FontWeight.Bold,color=Plum)}
                    Column(horizontalAlignment=Alignment.End){Text("Margem",fontSize=11.sp,color=Muted);Text(String.format(Locale("pt","BR"),"%.1f%%",margin),color=Rose,fontWeight=FontWeight.Bold)}
                }
            }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button(
                    onClick={
                        if(name.isNotBlank()){
                            store.upsertProduct(Product(if(id==0L)System.currentTimeMillis() else id,supplier.trim(),name.trim(),sku.trim(),stock.intNum(),minStock.intNum(),cost.num(),price.num()))
                            version++;clear()
                        }
                    },
                    modifier=Modifier.weight(1f),
                    shape=RoundedCornerShape(16.dp)
                ){Text(if(id==0L)"Salvar produto" else "Salvar alterações")}
                if(id!=0L)OutlinedButton(onClick={clear()},shape=RoundedCornerShape(16.dp)){Text("Cancelar")}
            }
        }}
        item{
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Text("Produtos cadastrados",Modifier.weight(1f),fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
                Surface(color=Blush,shape=RoundedCornerShape(50)){Text("${filtered.size}",Modifier.padding(horizontal=10.dp,vertical=4.dp),fontSize=12.sp,color=Rose,fontWeight=FontWeight.Bold)}
            }
        }
        items(filtered){p->DataCard{
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Surface(color=if(p.stock<=p.minStock)Color(0xFFFFE8EA) else Blush,shape=RoundedCornerShape(16.dp)){
                    Icon(Icons.Default.Inventory2,null,tint=if(p.stock<=p.minStock)Color(0xFFB64752) else Rose,modifier=Modifier.padding(10.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)){
                    Text(p.name,fontWeight=FontWeight.Bold,color=Ink)
                    Text(p.supplier.ifBlank{"Fornecedor não informado"},color=Rose,fontSize=12.sp)
                    Text("Estoque ${p.stock} • Custo ${p.cost.money()} • Venda ${p.price.money()}",fontSize=12.sp,color=Muted)
                    Text("Lucro/unid. ${(p.price-p.cost).money()}",color=Success,fontWeight=FontWeight.SemiBold,fontSize=12.sp)
                }
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
    var search by remember{mutableStateOf("")}
    fun clear(){id=0;name="";phone="";cpf="";email="";profession="";cep="";street="";number="";complement="";district="";city="";state="";notes=""}
    val filtered=store.clients.filter{
        search.isBlank() || it.name.contains(search,true) || it.phone.contains(search,true) || it.profession.contains(search,true) || it.city.contains(search,true)
    }.sortedBy{it.name}
    @Suppress("UNUSED_EXPRESSION") version

    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{SectionTitle("Clientes","Cadastro completo e histórico comercial")}
        item{
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                PremiumMetricCard("Clientes",store.clients.size.toString(),"cadastrados",Blush,Plum,Modifier.weight(1f))
                PremiumMetricCard("Com vendas",store.sales.map{it.clientId}.distinct().size.toString(),"clientes ativos",Color.White,Plum,Modifier.weight(1f))
            }
        }
        item{Field("Buscar cliente, telefone, profissão ou cidade",search){search=it}}
        item{FormCard{
            Text(if(id==0L)"Novo cliente" else "Editar cliente",fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
            Field("Nome completo",name){name=it}
            Field("Telefone / WhatsApp",phone){phone=it}
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Field("CPF",cpf,Modifier.weight(1f)){cpf=it}
                Field("Profissão",profession,Modifier.weight(1f)){profession=it}
            }
            Field("E-mail",email,keyboard=KeyboardType.Email){email=it}
            Text("Endereço",fontWeight=FontWeight.SemiBold,color=Plum)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Field("CEP",cep,Modifier.weight(1f)){cep=it}
                Field("Número",number,Modifier.weight(.65f)){number=it}
            }
            Field("Logradouro",street){street=it}
            Field("Complemento",complement){complement=it}
            Field("Bairro",district){district=it}
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Field("Cidade",city,Modifier.weight(1f)){city=it}
                Field("UF",state,Modifier.width(90.dp)){state=it.take(2).uppercase()}
            }
            Field("Observações",notes,singleLine=false){notes=it}
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button(
                    onClick={
                        if(name.isNotBlank()){
                            store.upsertClient(Client(if(id==0L)System.currentTimeMillis() else id,name,phone,cpf,email,profession,cep,street,number,complement,district,city,state,notes))
                            version++;clear()
                        }
                    },
                    modifier=Modifier.weight(1f),
                    shape=RoundedCornerShape(16.dp)
                ){Text(if(id==0L)"Salvar cliente" else "Salvar alterações")}
                if(id!=0L)OutlinedButton(onClick={clear()},shape=RoundedCornerShape(16.dp)){Text("Cancelar")}
            }
        }}
        item{
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Text("Clientes cadastrados",Modifier.weight(1f),fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
                Surface(color=Blush,shape=RoundedCornerShape(50)){Text("${filtered.size}",Modifier.padding(horizontal=10.dp,vertical=4.dp),fontSize=12.sp,color=Rose,fontWeight=FontWeight.Bold)}
            }
        }
        items(filtered){c->DataCard{
            val clientSales=store.sales.filter{it.clientId==c.id}
            val spent=clientSales.sumOf{it.chargedTotal}
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Surface(color=Lilac,shape=CircleShape){
                    Text(c.name.trim().take(1).uppercase().ifBlank{"C"},Modifier.padding(12.dp),fontWeight=FontWeight.Bold,color=Plum)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)){
                    Text(c.name,fontWeight=FontWeight.Bold,color=Ink)
                    Text(c.phone.ifBlank{"Telefone não informado"},color=Rose,fontSize=12.sp)
                    Text(listOf(c.profession,c.city,c.state).filter{it.isNotBlank()}.joinToString(" • "),fontSize=12.sp,color=Muted)
                    if(clientSales.isNotEmpty())Text("${clientSales.size} compra(s) • ${spent.money()}",fontSize=12.sp,color=Success,fontWeight=FontWeight.SemiBold)
                }
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
    val pending=store.quotes.count{!it.finalized}
    val closed=store.quotes.count{it.finalized}

    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{SectionTitle(if(editId==0L)"Novo orçamento" else "Editar orçamento","Monte, ajuste e envie antes de finalizar a venda")}
        item{
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                PremiumMetricCard("Pendentes",pending.toString(),"editáveis",Blush,Plum,Modifier.weight(1f))
                PremiumMetricCard("Finalizados",closed.toString(),"convertidos",Color.White,Plum,Modifier.weight(1f))
            }
        }
        item{FormCard{
            Text("Dados do orçamento",fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
            Selector("Selecionar cliente",clientName,store.clients.map{it.name}){i->store.clients.getOrNull(i)?.let{clientId=it.id;clientName=it.name}}
            Text("Produtos",fontWeight=FontWeight.Bold,color=Plum)
            Selector("Adicionar produto","",store.products.map{it.name+" • estoque "+it.stock}){i->
                store.products.getOrNull(i)?.let{p->
                    val ix=qItems.indexOfFirst{it.productId==p.id}
                    if(ix>=0){val old=qItems[ix];qItems[ix]=old.copy(qty=old.qty+1)}
                    else qItems.add(QuoteItem(p.id,p.name,1,p.price,0.0,p.cost,p.sku))
                }
            }
            qItems.forEachIndexed{i,item->
                Surface(color=Blush,shape=RoundedCornerShape(18.dp)){
                    Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                            Column(Modifier.weight(1f)){
                                Text(item.productName,fontWeight=FontWeight.Bold,color=Ink)
                                if(item.productCode.isNotBlank())Text("SKU ${item.productCode}",fontSize=11.sp,color=Muted)
                            }
                            TextButton(onClick={qItems.removeAt(i)}){Text("Remover")}
                        }
                        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                            Field("Qtd.",item.qty.toString(),Modifier.weight(.7f),KeyboardType.Number){v->qItems[i]=item.copy(qty=v.intNum().coerceAtLeast(0))}
                            Field("Preço",item.unitPrice.toString().replace('.',','),Modifier.weight(1f),KeyboardType.Decimal){v->qItems[i]=item.copy(unitPrice=v.num())}
                            Field("Desc. %",item.itemDiscountPercent.toString().replace('.',','),Modifier.weight(1f),KeyboardType.Decimal){v->qItems[i]=item.copy(itemDiscountPercent=v.num())}
                        }
                    }
                }
            }
            Text("Desconto geral",fontWeight=FontWeight.Bold,color=Plum)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){
                FilterChip(selected=discountType=="R$",onClick={discountType="R$"},label={Text("R$")})
                FilterChip(selected=discountType=="%",onClick={discountType="%"},label={Text("%")})
                Field(if(discountType=="%")"Percentual" else "Valor",discountValue,Modifier.weight(1f),KeyboardType.Decimal){discountValue=it}
            }
            Text("Pagamento",fontWeight=FontWeight.Bold,color=Plum)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                FilterChip(selected=payment=="Pix",onClick={payment="Pix"},label={Text("Pix")})
                FilterChip(selected=payment=="Cartão",onClick={payment="Cartão"},label={Text("Cartão")})
            }
            if(payment=="Cartão"){
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    Field("Parcelas",installments,Modifier.weight(1f),KeyboardType.Number){installments=it}
                    Field("Tarifa %",cardFee,Modifier.weight(1f),KeyboardType.Decimal){cardFee=it}
                }
                Row(verticalAlignment=Alignment.CenterVertically){
                    Switch(checked=passFee,onCheckedChange={passFee=it})
                    Spacer(Modifier.width(8.dp))
                    Text("Repassar tarifa ao cliente",color=Muted)
                }
            }
            Field("Observações",notes,singleLine=false){notes=it}
            Surface(color=Lilac,shape=RoundedCornerShape(20.dp)){
                Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                    PremiumLine("Subtotal",calc.subtotal.money())
                    PremiumLine("Descontos",(calc.itemDiscounts+calc.generalDiscount).money())
                    if(payment=="Cartão")PremiumLine("Tarifa cartão",calc.cardFee.money())
                    HorizontalDivider(color=Color(0xFFD9CBD3))
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                        Text("TOTAL",fontSize=18.sp,fontWeight=FontWeight.Bold,color=Plum)
                        Text(calc.charged.money(),fontSize=22.sp,fontWeight=FontWeight.Bold,color=DeepRose)
                    }
                }
            }
            if(msg.isNotBlank())Text(msg,color=if(msg.startsWith("Salvo")||msg.startsWith("Venda"))Success else MaterialTheme.colorScheme.error,fontWeight=FontWeight.SemiBold)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button(
                    onClick={
                        if(clientId==0L||qItems.isEmpty())msg="Selecione cliente e pelo menos um produto."
                        else{
                            val q=currentQuote();store.upsertQuote(q);editId=q.id;createdAt=q.createdAt;version++;msg="Salvo. Você pode continuar editando."
                        }
                    },
                    modifier=Modifier.weight(1f),
                    shape=RoundedCornerShape(16.dp)
                ){Text("Salvar orçamento")}
                if(editId!=0L)OutlinedButton(onClick={clear()},shape=RoundedCornerShape(16.dp)){Text("Novo")}
            }
        }}
        item{Text("Orçamentos salvos",fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)}
        items(store.quotes){q->DataCard{
            val qc=calculateQuote(q)
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Surface(color=if(q.finalized)Color(0xFFE7F3ED) else Blush,shape=RoundedCornerShape(14.dp)){
                    Icon(Icons.Default.ReceiptLong,null,tint=if(q.finalized)Success else Rose,modifier=Modifier.padding(9.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)){
                    Text("#"+q.id.toString().takeLast(6)+" • "+q.clientName,fontWeight=FontWeight.Bold,color=Ink)
                    Text(qc.charged.money()+" • "+q.payment+(if(q.payment=="Cartão")" "+q.installments+"x" else ""),color=Rose,fontSize=12.sp)
                    Text(if(q.finalized)"Venda finalizada" else "Editável",color=if(q.finalized)Success else Plum,fontWeight=FontWeight.SemiBold,fontSize=12.sp)
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)){
                if(!q.finalized)TextButton(onClick={editId=q.id;createdAt=q.createdAt;clientId=q.clientId;clientName=q.clientName;qItems.clear();qItems.addAll(q.items.map{it.copy()});discountType=q.discountType;discountValue=q.discountValue.toString().replace('.',',');payment=q.payment;installments=q.installments.toString();cardFee=q.cardFeePercent.toString().replace('.',',');passFee=q.passCardFee;notes=q.notes;msg=""}){Text("Editar")}
                TextButton(onClick={PdfUtil.shareQuote(context,store.company,store.clients.firstOrNull{it.id==q.clientId},q)}){Text("Gerar PDF")}
                if(!q.finalized)TextButton(onClick={val err=store.finalizeQuote(q);version++;msg=err?:"Venda finalizada e estoque baixado."}){Text("Finalizar venda")}
            }
        }}
    }
}

@Composable
private fun SalesScreen(store:LotusStore){
    val context=androidx.compose.ui.platform.LocalContext.current
    val now=Calendar.getInstance()
    val monthSales=store.sales.filter{
        val c=Calendar.getInstance().apply{timeInMillis=it.createdAt}
        c.get(Calendar.MONTH)==now.get(Calendar.MONTH)&&c.get(Calendar.YEAR)==now.get(Calendar.YEAR)
    }
    val monthGross=monthSales.sumOf{it.chargedTotal}
    val monthProfit=monthSales.sumOf{s->s.netRevenue-s.items.sumOf{it.qty*it.unitCostSnapshot}}

    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{SectionTitle("Vendas","Histórico, lucro e recibos profissionais")}
        item{
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                PremiumMetricCard("Vendas no mês",monthSales.size.toString(),monthGross.money(),Blush,Plum,Modifier.weight(1f))
                PremiumMetricCard("Lucro no mês",monthProfit.money(),"líquido",Color.White,Plum,Modifier.weight(1f))
            }
        }
        if(store.sales.isEmpty())item{DataCard{Text("Nenhuma venda finalizada ainda.",color=Rose)}}
        items(store.sales){sale->DataCard{
            val cogs=sale.items.sumOf{it.qty*it.unitCostSnapshot}
            val profit=sale.netRevenue-cogs
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Surface(color=Blush,shape=RoundedCornerShape(14.dp)){
                    Icon(Icons.Default.ShoppingBag,null,tint=Rose,modifier=Modifier.padding(9.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)){
                    Text("#"+sale.id.toString().takeLast(6)+" • "+sale.clientName,fontWeight=FontWeight.Bold,color=Ink)
                    Text("Total ${sale.chargedTotal.money()}",color=Plum,fontWeight=FontWeight.SemiBold)
                    Text(sale.payment+(if(sale.payment=="Cartão")" • "+sale.installments+"x • taxa "+sale.cardFeePercent+"%" else ""),fontSize=12.sp,color=Muted)
                }
                Surface(color=Color(0xFFE7F3ED),shape=RoundedCornerShape(50)){
                    Text(profit.money(),Modifier.padding(horizontal=10.dp,vertical=5.dp),fontSize=11.sp,color=Success,fontWeight=FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                PremiumLine("Líquido",sale.netRevenue.money())
                PremiumLine("CMV",cogs.money())
            }
            TextButton(onClick={PdfUtil.shareSale(context,store.company,store.clients.firstOrNull{it.id==sale.clientId},sale)}){Text("Gerar / enviar recibo PDF")}
        }}
    }
}

@Composable
private fun CompanyScreen(store:LotusStore){
    val context=androidx.compose.ui.platform.LocalContext.current
    var version by remember{mutableIntStateOf(0)}
    var company by remember(version){mutableStateOf(store.company.copy())}
    var msg by remember{mutableStateOf("")}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->
        if(uri!=null){
            try{context.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)}catch(_:Exception){}
            company=company.copy(logoUri=uri.toString())
        }
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{SectionTitle("Dados da empresa","Identidade usada automaticamente nos PDFs e vendas")}
        item{
            Card(shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(containerColor=Blush),elevation=CardDefaults.cardElevation(2.dp)){
                Column(Modifier.fillMaxWidth().padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    LotusLogoImage(Modifier.width(180.dp).height(95.dp))
                    Text(company.name.ifBlank{"Lotus Distribuidora"},fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
                    Text("Produtos para estética profissional",fontSize=12.sp,color=Rose)
                }
            }
        }
        item{FormCard{
            Text("Dados institucionais",fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
            Field("Nome da empresa",company.name){company=company.copy(name=it)}
            Field("CNPJ / documento",company.document){company=company.copy(document=it)}
            Field("Telefone / WhatsApp",company.phone){company=company.copy(phone=it)}
            Field("E-mail",company.email,keyboard=KeyboardType.Email){company=company.copy(email=it)}
            Field("Endereço",company.address){company=company.copy(address=it)}
            HorizontalDivider()
            Text("Vendedor responsável",fontWeight=FontWeight.Bold,color=Plum)
            Field("Nome do vendedor",company.sellerName){company=company.copy(sellerName=it)}
            Field("Contato do vendedor",company.sellerPhone){company=company.copy(sellerPhone=it)}
            Field("Chave Pix",company.pixKey){company=company.copy(pixKey=it)}
            OutlinedButton(onClick={picker.launch(arrayOf("image/*"))},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)){
                Text(if(company.logoUri.isBlank())"Usar outra logo" else "Trocar logo personalizada")
            }
            Button(
                onClick={store.saveCompany(company);version++;msg="Dados salvos."},
                modifier=Modifier.fillMaxWidth(),
                shape=RoundedCornerShape(16.dp)
            ){Text("Salvar dados da empresa")}
            if(msg.isNotBlank())Text(msg,color=Success,fontWeight=FontWeight.SemiBold)
        }}
        item{
            DataCard{
                Text("Padrão dos documentos",fontWeight=FontWeight.Bold,color=Plum)
                Text("Orçamentos e recibos usam o logo oficial, layout A4, tabela com linhas, dados do cliente e totais alinhados.",fontSize=13.sp,color=Muted)
            }
        }
    }
}
