package com.lotusdistribuidora.app

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay

private val Rose=Color(0xFFC05C79)
private val DeepRose=Color(0xFFA74363)
private val Plum=Color(0xFF743653)
private val Lilac=Color(0xFFF7E7EF)
private val Blush=Color(0xFFFCE8ED)
private val Champagne=Color(0xFFE6BE92)
private val Ivory=Color(0xFFFFF7F3)
private val Ink=Color(0xFF322730)
private val Muted=Color(0xFF876C78)
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
        topBar={
            if(section==Section.DASH) LotusPremiumHeader(store)
            else LotusSectionHeader(section.title){section=Section.DASH}
        },
        bottomBar={
            LotusBottomBar(
                section=section,
                onSection={section=it},
                onQuick={quickActions=true},
                onMore={moreMenu=true}
            )
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
private fun LotusBottomBar(
    section:Section,
    onSection:(Section)->Unit,
    onQuick:()->Unit,
    onMore:()->Unit
){
    Box(
        Modifier.fillMaxWidth().background(
            Brush.horizontalGradient(listOf(Color(0xFFC7667E),Color(0xFFA74966)))
        ).navigationBarsPadding().padding(horizontal=8.dp,vertical=4.dp)
    ){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceAround,verticalAlignment=Alignment.CenterVertically){
            BottomNavItem("Início",Icons.Default.Home,section==Section.DASH){onSection(Section.DASH)}
            BottomNavItem("Clientes",Icons.Default.People,section==Section.CLIENTS){onSection(Section.CLIENTS)}
            Column(
                Modifier.clickable{onQuick()}.padding(horizontal=5.dp),
                horizontalAlignment=Alignment.CenterHorizontally
            ){
                Surface(color=Color.White,shape=CircleShape,shadowElevation=5.dp){
                    Box(Modifier.size(44.dp),contentAlignment=Alignment.Center){
                        Icon(Icons.Default.Add,null,tint=DeepRose,modifier=Modifier.size(25.dp))
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text("Novo",fontSize=9.sp,color=Color.White,fontWeight=FontWeight.SemiBold)
            }
            BottomNavItem("Estoque",Icons.Default.Inventory2,section==Section.PRODUCTS){onSection(Section.PRODUCTS)}
            BottomNavItem("Mais",Icons.Default.MoreHoriz,section==Section.QUOTES||section==Section.SALES||section==Section.COMPANY){onMore()}
        }
    }
}

@Composable
private fun BottomNavItem(label:String,icon:androidx.compose.ui.graphics.vector.ImageVector,selected:Boolean,onClick:()->Unit){
    Column(
        Modifier.clickable{onClick()}.padding(horizontal=7.dp,vertical=3.dp),
        horizontalAlignment=Alignment.CenterHorizontally
    ){
        Surface(
            color=if(selected)Color.White.copy(alpha=.18f) else Color.Transparent,
            shape=RoundedCornerShape(14.dp)
        ){
            Icon(icon,null,tint=Color.White,modifier=Modifier.padding(horizontal=10.dp,vertical=5.dp).size(20.dp))
        }
        Text(label,fontSize=9.sp,color=Color.White,fontWeight=if(selected)FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun LotusPremiumHeader(store:LotusStore){
    Surface(color=Ivory,shadowElevation=0.dp){
        Box(
            Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=2.dp),
            contentAlignment=Alignment.Center
        ){
            IconButton(onClick={},modifier=Modifier.align(Alignment.CenterStart)){
                Icon(Icons.Default.Menu,contentDescription="Menu",tint=Plum)
            }
            LotusLogoLockup()
            IconButton(onClick={},modifier=Modifier.align(Alignment.CenterEnd)){
                Icon(Icons.Default.NotificationsNone,contentDescription="Notificações",tint=Rose)
            }
        }
    }
}

@Composable
private fun LotusLogoLockup(){
    Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.padding(top=4.dp,bottom=4.dp)){
        Image(
            painter=painterResource(R.drawable.lotus_mark),
            contentDescription="Lotus Distribuidora",
            modifier=Modifier.width(67.dp).height(58.dp),
            contentScale=ContentScale.Fit
        )
        Text(
            "LOTUS",
            color=Color(0xFFC98F68),
            fontSize=17.sp,
            fontWeight=FontWeight.Medium,
            letterSpacing=.32.em,
            textAlign=TextAlign.Center
        )
        Text(
            "DISTRIBUIDORA",
            color=Color(0xFF14243C),
            fontSize=8.sp,
            fontWeight=FontWeight.Bold,
            letterSpacing=.20.em
        )
        Text(
            "PRODUTOS PARA ESTÉTICA PROFISSIONAL",
            color=Color(0xFF14243C),
            fontSize=5.5.sp,
            fontWeight=FontWeight.Medium,
            letterSpacing=.06.em
        )
    }
}

@Composable
private fun LotusSectionHeader(title:String,onBack:()->Unit){
    Box(
        Modifier.fillMaxWidth().background(
            Brush.horizontalGradient(listOf(Color(0xFFC05C79),Color(0xFFA74363)))
        ).padding(horizontal=8.dp,vertical=6.dp),
        contentAlignment=Alignment.Center
    ){
        IconButton(onClick=onBack,modifier=Modifier.align(Alignment.CenterStart)){
            Icon(Icons.Default.ArrowBack,contentDescription="Voltar",tint=Color.White)
        }
        Text(title,color=Color.White,fontWeight=FontWeight.Bold,fontSize=18.sp)
        IconButton(onClick={},modifier=Modifier.align(Alignment.CenterEnd)){
            Icon(Icons.Default.MoreVert,contentDescription="Mais",tint=Color.White)
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
    val gross=monthSales.sumOf{it.chargedTotal}
    val net=monthSales.sumOf{it.netRevenue}
    val cogs=monthSales.sumOf{s->s.items.sumOf{it.qty*it.unitCostSnapshot}}
    val profit=net-cogs
    val low=store.products.count{it.stock<=it.minStock}
    val pendingQuotes=store.quotes.count{!it.finalized}
    val sellerFirst=store.company.sellerName.trim().substringBefore(" ").takeIf{it.isNotBlank()} ?: "Tuanny"
    val monthName=java.text.SimpleDateFormat("MMMM yyyy",Locale("pt","BR")).format(now.time)
        .replaceFirstChar{if(it.isLowerCase())it.titlecase(Locale("pt","BR")) else it.toString()}

    Box(Modifier.fillMaxSize()){
        Icon(
            painter=painterResource(R.drawable.lotus_watermark),
            contentDescription=null,
            tint=Rose,
            modifier=Modifier.size(230.dp).align(Alignment.TopEnd).offset(x=60.dp,y=(-12).dp).alpha(.055f)
        )
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal=16.dp,vertical=8.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)
    ){
        item{
            Text("Olá, $sellerFirst!",fontSize=24.sp,fontWeight=FontWeight.Bold,color=Plum)
            Text("Seja bem-vinda!",fontSize=14.sp,color=Rose)
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){
                Surface(
                    color=Color.White,
                    shape=RoundedCornerShape(18.dp),
                    shadowElevation=1.dp,
                    modifier=Modifier.width(238.dp)
                ){
                    Row(
                        Modifier.padding(horizontal=14.dp,vertical=9.dp),
                        verticalAlignment=Alignment.CenterVertically,
                        horizontalArrangement=Arrangement.Center
                    ){
                        Text(monthName,fontWeight=FontWeight.SemiBold,color=Plum,fontSize=13.sp)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.KeyboardArrowDown,null,tint=Rose,modifier=Modifier.size(17.dp))
                    }
                }
            }
        }
        item{
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                GradientMetricCard(
                    label="Vendas do mês",
                    value=gross.money(),
                    caption="${count} venda(s)",
                    modifier=Modifier.weight(1f)
                )
                SoftMetricCard(
                    label="Lucro líquido",
                    value=profit.money(),
                    caption="resultado do mês",
                    modifier=Modifier.weight(1f)
                )
            }
        }
        item{
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                SmallStatusCard("Orçamentos","$pendingQuotes pendentes",Icons.Default.ReceiptLong,Modifier.weight(1f)){onNavigate(Section.QUOTES)}
                SmallStatusCard("Estoque baixo","$low produtos",Icons.Default.Inventory2,Modifier.weight(1f)){onNavigate(Section.PRODUCTS)}
            }
        }
        item{
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.spacedBy(8.dp)
            ){
                CompactQuickTile("Orçamento",Icons.Default.ReceiptLong,Modifier.weight(1f)){onNavigate(Section.QUOTES)}
                CompactQuickTile("Venda",Icons.Default.ShoppingBag,Modifier.weight(1f)){onNavigate(Section.QUOTES)}
                CompactQuickTile("Cliente",Icons.Default.People,Modifier.weight(1f)){onNavigate(Section.CLIENTS)}
                CompactQuickTile("Produto",Icons.Default.Inventory2,Modifier.weight(1f)){onNavigate(Section.PRODUCTS)}
            }
        }
        item{
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Text("Vendas recentes",Modifier.weight(1f),fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
                TextButton(onClick={onNavigate(Section.SALES)}){Text("Ver todas")}
            }
        }
        if(monthSales.isEmpty()){
            item{DataCard{Text("Nenhuma venda registrada neste mês.",color=Muted,fontSize=13.sp)}}
        }else{
            items(monthSales.take(5)){sale->
                DataCard{
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                        Surface(color=Blush,shape=CircleShape){
                            Icon(Icons.Default.ReceiptLong,null,tint=Rose,modifier=Modifier.padding(10.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)){
                            Text(sale.clientName.ifBlank{"Cliente"},fontWeight=FontWeight.SemiBold,color=Ink)
                            Text(java.text.SimpleDateFormat("dd/MM/yyyy",Locale("pt","BR")).format(java.util.Date(sale.createdAt)),fontSize=11.sp,color=Muted)
                        }
                        Column(horizontalAlignment=Alignment.End){
                            Text(sale.chargedTotal.money(),fontWeight=FontWeight.Bold,color=Plum)
                            Surface(color=Color(0xFFE7F3ED),shape=RoundedCornerShape(50)){
                                Text("Venda",Modifier.padding(horizontal=9.dp,vertical=3.dp),fontSize=10.sp,color=Success,fontWeight=FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
    }

}

@Composable
private fun GradientMetricCard(label:String,value:String,caption:String,modifier:Modifier=Modifier){
    Card(modifier,shape=RoundedCornerShape(24.dp),elevation=CardDefaults.cardElevation(3.dp)){
        Box(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(Color(0xFFB85D79),Color(0xFF94405F)))
            ).padding(16.dp)
        ){
            Column{
                Text(label,fontSize=12.sp,color=Color.White.copy(alpha=.9f))
                Spacer(Modifier.height(6.dp))
                Text(value,fontSize=21.sp,fontWeight=FontWeight.Bold,color=Color.White)
                Spacer(Modifier.height(3.dp))
                Text(caption,fontSize=11.sp,color=Color.White.copy(alpha=.82f))
            }
        }
    }
}

@Composable
private fun SoftMetricCard(label:String,value:String,caption:String,modifier:Modifier=Modifier){
    Card(
        modifier,
        shape=RoundedCornerShape(24.dp),
        colors=CardDefaults.cardColors(containerColor=Color(0xFFFFF0F4)),
        elevation=CardDefaults.cardElevation(2.dp)
    ){
        Column(Modifier.padding(16.dp)){
            Text(label,fontSize=12.sp,color=Muted)
            Spacer(Modifier.height(6.dp))
            Text(value,fontSize=21.sp,fontWeight=FontWeight.Bold,color=Plum)
            Spacer(Modifier.height(3.dp))
            Text(caption,fontSize=11.sp,color=Rose)
        }
    }
}

@Composable
private fun CompactQuickTile(label:String,icon:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier=Modifier,onClick:()->Unit){
    Card(
        onClick=onClick,
        modifier=modifier.height(88.dp),
        shape=RoundedCornerShape(18.dp),
        colors=CardDefaults.cardColors(containerColor=Color.White),
        elevation=CardDefaults.cardElevation(2.dp)
    ){
        Column(
            Modifier.fillMaxSize().padding(vertical=9.dp,horizontal=3.dp),
            horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.Center
        ){
            Surface(color=Blush,shape=RoundedCornerShape(12.dp)){
                Icon(icon,null,tint=DeepRose,modifier=Modifier.padding(7.dp).size(18.dp))
            }
            Spacer(Modifier.height(5.dp))
            Text(label,fontSize=9.5.sp,fontWeight=FontWeight.SemiBold,color=Plum,maxLines=1)
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
    Card(
        onClick=onClick,
        modifier=modifier.height(76.dp),
        shape=RoundedCornerShape(20.dp),
        colors=CardDefaults.cardColors(containerColor=Color.White),
        elevation=CardDefaults.cardElevation(2.dp)
    ){
        Row(Modifier.fillMaxSize().padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically){
            Surface(color=Blush,shape=RoundedCornerShape(13.dp)){
                Icon(icon,null,tint=Rose,modifier=Modifier.padding(8.dp).size(20.dp))
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)){
                Text(label,fontSize=11.sp,color=Muted,maxLines=1)
                Spacer(Modifier.height(2.dp))
                Text(value,fontWeight=FontWeight.Bold,color=Plum,fontSize=13.sp,maxLines=1)
            }
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
    var tab by remember{mutableStateOf("Dados")}
    fun clear(){id=0;supplier="";name="";sku="";stock="";minStock="";cost="";price="";tab="Dados"}
    val profit=price.num()-cost.num()
    val margin=if(cost.num()>0)profit/cost.num()*100 else 0.0
    val stockCapital=store.products.sumOf{it.stock*it.cost}
    val low=store.products.count{it.stock<=it.minStock}
    val filtered=store.products.filter{
        search.isBlank() || it.name.contains(search,true) || it.supplier.contains(search,true) || it.sku.contains(search,true)
    }.sortedBy{it.name}
    @Suppress("UNUSED_EXPRESSION") version

    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{FormCard{
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                listOf("Dados","Estoque","Preços","Fornecedor").forEach{t->
                    FilterChip(
                        selected=tab==t,
                        onClick={tab=t},
                        label={Text(t,fontSize=10.sp)},
                        colors=FilterChipDefaults.filterChipColors(
                            selectedContainerColor=Blush,
                            selectedLabelColor=DeepRose
                        )
                    )
                }
            }
            when(tab){
                "Dados"->{
                    Row(verticalAlignment=Alignment.CenterVertically){
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp)){
                            Field("Nome do produto",name){name=it}
                            Field("Código / SKU",sku){sku=it}
                        }
                        Spacer(Modifier.width(10.dp))
                        Surface(color=Blush,shape=RoundedCornerShape(20.dp)){
                            Icon(Icons.Default.Inventory2,null,tint=DeepRose,modifier=Modifier.padding(22.dp).size(38.dp))
                        }
                    }
                }
                "Estoque"->{
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                        Field("Quantidade em estoque",stock,Modifier.weight(1f),KeyboardType.Number){stock=it}
                        Field("Estoque mínimo",minStock,Modifier.weight(1f),KeyboardType.Number){minStock=it}
                    }
                    Surface(color=Color(0xFFFFF6F7),shape=RoundedCornerShape(16.dp)){
                        Text("O estoque só é baixado quando a venda é finalizada.",Modifier.padding(12.dp),fontSize=12.sp,color=Muted)
                    }
                }
                "Preços"->{
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                        Field("Custo de aquisição",cost,Modifier.weight(1f),KeyboardType.Decimal){cost=it}
                        Field("Preço de revenda",price,Modifier.weight(1f),KeyboardType.Decimal){price=it}
                    }
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                        Surface(Modifier.weight(1f),color=Color.White,shape=RoundedCornerShape(16.dp),shadowElevation=1.dp){
                            Column(Modifier.padding(12.dp)){
                                Text("Margem de lucro",fontSize=11.sp,color=Muted)
                                Text(String.format(Locale("pt","BR"),"%.1f%%",margin),fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
                            }
                        }
                        Surface(Modifier.weight(1f),color=Color.White,shape=RoundedCornerShape(16.dp),shadowElevation=1.dp){
                            Column(Modifier.padding(12.dp)){
                                Text("Lucro por unidade",fontSize=11.sp,color=Muted)
                                Text(profit.money(),fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
                            }
                        }
                    }
                }
                else->{
                    Field("Fornecedor",supplier){supplier=it}
                    Surface(color=Champagne.copy(alpha=.22f),shape=RoundedCornerShape(16.dp)){
                        Text("O fornecedor ficará vinculado ao produto para filtros, compras e análise de margem.",Modifier.padding(12.dp),fontSize=12.sp,color=Muted)
                    }
                }
            }
            Button(
                onClick={
                    if(name.isNotBlank()){
                        store.upsertProduct(Product(if(id==0L)System.currentTimeMillis() else id,supplier.trim(),name.trim(),sku.trim(),stock.intNum(),minStock.intNum(),cost.num(),price.num()))
                        version++;clear()
                    }
                },
                modifier=Modifier.fillMaxWidth(),
                shape=RoundedCornerShape(16.dp)
            ){Text(if(id==0L)"Salvar produto" else "Salvar alterações")}
        }}
        item{
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                PremiumMetricCard("Estoque financeiro",stockCapital.money(),"capital em produtos",Blush,Plum,Modifier.weight(1f))
                PremiumMetricCard("Estoque baixo",low.toString(),"produto(s)",Color.White,Plum,Modifier.weight(1f))
            }
        }
        item{Field("Buscar produto, fornecedor ou SKU",search){search=it}}
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
                TextButton(onClick={
                    id=p.id;supplier=p.supplier;name=p.name;sku=p.sku;stock=p.stock.toString();minStock=p.minStock.toString()
                    cost=p.cost.toString().replace('.',',');price=p.price.toString().replace('.',',');tab="Dados"
                }){Text("Editar")}
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
    var cepStatus by remember{mutableStateOf("")}
    val cepDigits=cep.filter{it.isDigit()}
    LaunchedEffect(cepDigits){
        if(cepDigits.length==8){
            cepStatus="Buscando CEP..."
            delay(250)
            val result=CepService.lookup(cepDigits)
            if(result!=null){
                if(result.street.isNotBlank())street=result.street
                if(result.district.isNotBlank())district=result.district
                if(result.city.isNotBlank())city=result.city
                if(result.state.isNotBlank())state=result.state
                if(complement.isBlank()&&result.complement.isNotBlank())complement=result.complement
                cepStatus="Endereço preenchido automaticamente"
            }else{
                cepStatus="CEP não encontrado"
            }
        }else if(cepDigits.length<8){
            cepStatus=""
        }
    }
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
            if(cepStatus.isNotBlank())Text(cepStatus,fontSize=11.sp,color=if(cepStatus.startsWith("Endereço"))Success else Rose)
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
    var notes by remember{mutableStateOf("")}
    var msg by remember{mutableStateOf("")}

    var pendingFinalize by remember{mutableStateOf<Quote?>(null)}
    var checkoutPayment by remember{mutableStateOf("Pix")}
    var checkoutInstallments by remember{mutableStateOf("2")}
    var checkoutPaid by remember{mutableStateOf("")}
    var checkoutReceived by remember{mutableStateOf("")}

    @Suppress("UNUSED_EXPRESSION") version

    fun clear(){
        editId=0;createdAt=0;clientId=0;clientName="";qItems.clear()
        discountType="R$";discountValue="";payment="Pix";installments="1"
        cardFee="";notes="";msg=""
    }

    fun currentQuote()=Quote(
        if(editId==0L)System.currentTimeMillis() else editId,
        clientId,
        clientName,
        qItems.map{it.copy()}.toMutableList(),
        discountType,
        discountValue.num(),
        payment,
        if(payment=="Crédito parcelado")installments.intNum().coerceAtLeast(2) else 1,
        0.0,
        false,
        notes,
        if(createdAt==0L)System.currentTimeMillis() else createdAt,
        false
    )

    val calc=calculateQuote(currentQuote())
    val pending=store.quotes.count{!it.finalized}
    val closed=store.quotes.count{it.finalized}

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)
    ){
        item{
            Text(if(editId==0L)"Novo orçamento" else "Editar orçamento",fontSize=20.sp,fontWeight=FontWeight.Bold,color=Plum)
            Text("Monte, ajuste e gere PDF ou JPG antes de finalizar.",fontSize=12.sp,color=Muted)
        }
        item{
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                PremiumMetricCard("Pendentes",pending.toString(),"editáveis",Blush,Plum,Modifier.weight(1f))
                PremiumMetricCard("Finalizados",closed.toString(),"convertidos",Color.White,Plum,Modifier.weight(1f))
            }
        }
        item{FormCard{
            Text("Dados do orçamento",fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)
            Selector("Selecionar cliente",clientName,store.clients.map{it.name}){i->
                store.clients.getOrNull(i)?.let{clientId=it.id;clientName=it.name}
            }

            Text("Produtos",fontWeight=FontWeight.Bold,color=Plum)
            Selector("Adicionar produto","",store.products.map{it.name+" • estoque "+it.stock}){i->
                store.products.getOrNull(i)?.let{p->
                    val ix=qItems.indexOfFirst{it.productId==p.id}
                    if(ix>=0){
                        val old=qItems[ix]
                        qItems[ix]=old.copy(qty=old.qty+1)
                    }else{
                        qItems.add(QuoteItem(p.id,p.name,1,p.price,0.0,p.cost,p.sku))
                    }
                }
            }

            qItems.forEachIndexed{i,item->
                Surface(color=Blush,shape=RoundedCornerShape(18.dp)){
                    Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement=Arrangement.SpaceBetween,
                            verticalAlignment=Alignment.CenterVertically
                        ){
                            Column(Modifier.weight(1f)){
                                Text(item.productName,fontWeight=FontWeight.Bold,color=Ink)
                                if(item.productCode.isNotBlank())Text("SKU ${item.productCode}",fontSize=11.sp,color=Muted)
                            }
                            TextButton(onClick={qItems.removeAt(i)}){Text("Remover")}
                        }
                        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                            Field("Qtd.",item.qty.toString(),Modifier.weight(.7f),KeyboardType.Number){v->
                                qItems[i]=item.copy(qty=v.intNum().coerceAtLeast(0))
                            }
                            Field("Preço",item.unitPrice.toString().replace('.',','),Modifier.weight(1f),KeyboardType.Decimal){v->
                                qItems[i]=item.copy(unitPrice=v.num())
                            }
                            Field("Desc. %",item.itemDiscountPercent.toString().replace('.',','),Modifier.weight(1f),KeyboardType.Decimal){v->
                                qItems[i]=item.copy(itemDiscountPercent=v.num())
                            }
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

            Text("Condição de pagamento prevista",fontWeight=FontWeight.Bold,color=Plum)
            Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
                FilterChip(
                    selected=payment=="Pix",
                    onClick={payment="Pix";installments="1";cardFee=""},
                    label={Text("Pix")}
                )
                FilterChip(
                    selected=payment=="Crédito à vista",
                    onClick={payment="Crédito à vista";installments="1"},
                    label={Text("Crédito à vista")}
                )
                FilterChip(
                    selected=payment=="Crédito parcelado",
                    onClick={payment="Crédito parcelado";if(installments.intNum()<2)installments="2"},
                    label={Text("Crédito parcelado")}
                )
            }

            if(isCreditPayment(payment)){
                if(payment=="Crédito parcelado"){
                    Field("Parcelas",installments,keyboard=KeyboardType.Number){installments=it}
                }
                Text(
                    "A taxa será calculada automaticamente na finalização, usando o valor pago pelo cliente e o valor líquido recebido.",
                    fontSize=11.sp,
                    color=Muted
                )
            }

            Field("Observações",notes,singleLine=false){notes=it}

            Surface(color=Lilac,shape=RoundedCornerShape(20.dp)){
                Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                    PremiumLine("Subtotal",calc.subtotal.money())
                    PremiumLine("Descontos",(calc.itemDiscounts+calc.generalDiscount).money())
                    HorizontalDivider(color=Color(0xFFD9CBD3))
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                        Text("TOTAL CLIENTE",fontSize=16.sp,fontWeight=FontWeight.Bold,color=Plum)
                        Text(calc.charged.money(),fontSize=22.sp,fontWeight=FontWeight.Bold,color=DeepRose)
                    }
                }
            }

            if(msg.isNotBlank()){
                Text(
                    msg,
                    color=if(msg.startsWith("Venda")||msg.startsWith("Rascunho")||msg.startsWith("Recibo"))Success else MaterialTheme.colorScheme.error,
                    fontWeight=FontWeight.SemiBold
                )
            }

            Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){
                OutlinedButton(
                    onClick={
                        if(clientId==0L||qItems.isEmpty())msg="Selecione cliente e pelo menos um produto."
                        else{
                            val q=currentQuote()
                            store.upsertQuote(q)
                            editId=q.id
                            createdAt=q.createdAt
                            version++
                            msg="Rascunho salvo."
                        }
                    },
                    modifier=Modifier.weight(1f),
                    shape=RoundedCornerShape(14.dp)
                ){Text("Salvar",fontSize=11.sp)}

                Button(
                    onClick={
                        if(clientId==0L||qItems.isEmpty())msg="Selecione cliente e pelo menos um produto."
                        else{
                            val q=currentQuote()
                            store.upsertQuote(q)
                            editId=q.id
                            createdAt=q.createdAt
                            version++
                            PdfUtil.shareQuote(context,store.company,store.clients.firstOrNull{it.id==q.clientId},q)
                        }
                    },
                    modifier=Modifier.weight(1f),
                    shape=RoundedCornerShape(14.dp)
                ){Text("PDF",fontSize=11.sp)}

                Button(
                    onClick={
                        if(clientId==0L||qItems.isEmpty())msg="Selecione cliente e pelo menos um produto."
                        else{
                            val q=currentQuote()
                            store.upsertQuote(q)
                            editId=q.id
                            createdAt=q.createdAt
                            version++
                            PdfUtil.shareQuoteJpg(context,store.company,store.clients.firstOrNull{it.id==q.clientId},q)
                        }
                    },
                    modifier=Modifier.weight(1f),
                    shape=RoundedCornerShape(14.dp)
                ){Text("JPG",fontSize=11.sp)}
            }
        }}

        item{Text("Orçamentos salvos",fontWeight=FontWeight.Bold,color=Plum,fontSize=18.sp)}

        items(store.quotes){q->DataCard{
            val qc=calculateQuote(q)
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Surface(
                    color=if(q.finalized)Color(0xFFE7F3ED) else Blush,
                    shape=RoundedCornerShape(14.dp)
                ){
                    Icon(
                        Icons.Default.ReceiptLong,
                        null,
                        tint=if(q.finalized)Success else Rose,
                        modifier=Modifier.padding(9.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)){
                    Text("#"+q.id.toString().takeLast(6)+" • "+q.clientName,fontWeight=FontWeight.Bold,color=Ink)
                    Text(
                        qc.charged.money()+" • "+q.payment+
                            (if(q.payment=="Crédito parcelado" || (q.payment=="Cartão"&&q.installments>1))" "+q.installments+"x" else ""),
                        color=Rose,
                        fontSize=12.sp
                    )
                    Text(
                        if(q.finalized)"Venda finalizada" else "Editável",
                        color=if(q.finalized)Success else Plum,
                        fontWeight=FontWeight.SemiBold,
                        fontSize=12.sp
                    )
                }
            }

            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)){
                if(!q.finalized)TextButton(onClick={
                    editId=q.id
                    createdAt=q.createdAt
                    clientId=q.clientId
                    clientName=q.clientName
                    qItems.clear()
                    qItems.addAll(q.items.map{it.copy()})
                    discountType=q.discountType
                    discountValue=q.discountValue.toString().replace('.',',')
                    payment=when{
                        q.payment=="Cartão"&&q.installments>1->"Crédito parcelado"
                        q.payment=="Cartão"->"Crédito à vista"
                        else->q.payment
                    }
                    installments=q.installments.toString()
                    cardFee=""
                    notes=q.notes
                    msg=""
                }){Text("Editar")}

                TextButton(onClick={
                    PdfUtil.shareQuote(context,store.company,store.clients.firstOrNull{it.id==q.clientId},q)
                }){Text("PDF")}

                TextButton(onClick={
                    PdfUtil.shareQuoteJpg(context,store.company,store.clients.firstOrNull{it.id==q.clientId},q)
                }){Text("JPG")}

                if(!q.finalized)TextButton(onClick={
                    pendingFinalize=q
                    checkoutPayment=when{
                        q.payment=="Cartão"&&q.installments>1->"Crédito parcelado"
                        q.payment=="Cartão"->"Crédito à vista"
                        q.payment=="Crédito à vista"||q.payment=="Crédito parcelado"->q.payment
                        else->"Pix"
                    }
                    checkoutInstallments=(if(q.installments<2)2 else q.installments).toString()
                    val quoteTotal=calculateQuote(q).charged
                    checkoutPaid=String.format(Locale("pt","BR"),"%.2f",quoteTotal)
                    checkoutReceived=if(isCreditPayment(q.payment)&&q.cardFeePercent>0.0){
                        String.format(Locale("pt","BR"),"%.2f",calculateQuote(q).netRevenue)
                    }else ""
                }){Text("Finalizar venda")}
            }
        }}
    }

    pendingFinalize?.let{original->
        val normalizedInstallments=if(checkoutPayment=="Crédito parcelado")checkoutInstallments.intNum().coerceAtLeast(2) else 1
        val quoteTotal=calculateQuote(original).charged
        val actualPaid=if(isCreditPayment(checkoutPayment))checkoutPaid.num() else quoteTotal
        val actualReceived=if(isCreditPayment(checkoutPayment))checkoutReceived.num() else actualPaid
        val settlement=calculateCardSettlement(actualPaid,actualReceived)
        val settlementValid=!isCreditPayment(checkoutPayment) || (
            checkoutPaid.isNotBlank() &&
            checkoutReceived.isNotBlank() &&
            actualPaid>0.0 &&
            actualReceived>=0.0 &&
            actualReceived<=actualPaid
        )
        val preview=original.copy(
            payment=checkoutPayment,
            installments=normalizedInstallments,
            cardFeePercent=settlement.feePercent,
            passCardFee=false,
            finalized=false
        )

        AlertDialog(
            onDismissRequest={pendingFinalize=null},
            title={Text("Finalizar venda",color=Plum,fontWeight=FontWeight.Bold)},
            text={
                Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
                    Text("Como o cliente pagou?",fontWeight=FontWeight.SemiBold,color=Ink)

                    FilterChip(
                        selected=checkoutPayment=="Pix",
                        onClick={checkoutPayment="Pix";checkoutInstallments="1";checkoutPaid="";checkoutReceived=""},
                        label={Text("Pix")}
                    )
                    FilterChip(
                        selected=checkoutPayment=="Crédito à vista",
                        onClick={checkoutPayment="Crédito à vista";checkoutInstallments="1"},
                        label={Text("Crédito à vista")}
                    )
                    FilterChip(
                        selected=checkoutPayment=="Crédito parcelado",
                        onClick={checkoutPayment="Crédito parcelado";if(checkoutInstallments.intNum()<2)checkoutInstallments="2"},
                        label={Text("Crédito parcelado")}
                    )

                    if(checkoutPayment=="Crédito parcelado"){
                        Field("Número de parcelas",checkoutInstallments,keyboard=KeyboardType.Number){checkoutInstallments=it}
                    }

                    if(isCreditPayment(checkoutPayment)){
                        Field("Valor pago pelo cliente",checkoutPaid,keyboard=KeyboardType.Decimal){checkoutPaid=it}
                        Field("Valor líquido recebido",checkoutReceived,keyboard=KeyboardType.Decimal){checkoutReceived=it}

                        if(checkoutReceived.isNotBlank() && actualReceived>actualPaid){
                            Text(
                                "O valor líquido recebido não pode ser maior que o valor pago pelo cliente.",
                                fontSize=11.sp,
                                color=MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Surface(color=Blush,shape=RoundedCornerShape(18.dp)){
                        Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
                            PremiumLine("Valor pago pelo cliente",(if(isCreditPayment(checkoutPayment))settlement.paid else quoteTotal).money())
                            if(isCreditPayment(checkoutPayment)){
                                PremiumLine("Valor líquido recebido",settlement.received.money())
                                PremiumLine("Taxa do cartão",settlement.feeValue.money())
                                PremiumLine(
                                    "Percentual da taxa",
                                    String.format(Locale("pt","BR"),"%.2f%%",settlement.feePercent)
                                )
                            }else{
                                PremiumLine("Valor líquido recebido",quoteTotal.money())
                            }
                        }
                    }

                    if(isCreditPayment(checkoutPayment)){
                        Text(
                            "Exemplo: pagou R$ 500,00 e você recebeu R$ 450,00 → taxa R$ 50,00 → 10,00%.",
                            fontSize=11.sp,
                            color=Muted
                        )
                    }
                }
            },
            confirmButton={
                Button(
                    enabled=settlementValid,
                    onClick={
                        val finalQuote=preview.copy()
                        store.upsertQuote(finalQuote)
                        val error=store.finalizeQuote(
                            finalQuote,
                            actualPaid=if(isCreditPayment(checkoutPayment))settlement.paid else quoteTotal,
                            actualNetReceived=if(isCreditPayment(checkoutPayment))settlement.received else quoteTotal
                        )
                        version++
                        if(error!=null){
                            msg=error
                        }else{
                            msg="Venda finalizada. Recibo gerado com o valor pago pelo cliente."
                            val sale=store.sales.firstOrNull{it.quoteId==finalQuote.id}
                            if(sale!=null){
                                PdfUtil.shareSale(
                                    context,
                                    store.company,
                                    store.clients.firstOrNull{it.id==sale.clientId},
                                    sale
                                )
                            }
                        }
                        pendingFinalize=null
                    }
                ){
                    Text("Confirmar e gerar recibo")
                }
            },
            dismissButton={
                TextButton(onClick={pendingFinalize=null}){Text("Cancelar")}
            }
        )
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
                    Text(
                        sale.payment+
                            (if(sale.payment=="Crédito parcelado" || (sale.payment=="Cartão"&&sale.installments>1))" • "+sale.installments+"x" else "")+
                            (if(isCreditPayment(sale.payment)&&sale.cardFeePercent>0.0)" • taxa "+String.format(Locale("pt","BR"),"%.2f%%",sale.cardFeePercent) else ""),
                        fontSize=12.sp,
                        color=Muted
                    )
                }
                Surface(color=Color(0xFFE7F3ED),shape=RoundedCornerShape(50)){
                    Text(profit.money(),Modifier.padding(horizontal=10.dp,vertical=5.dp),fontSize=11.sp,color=Success,fontWeight=FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(6.dp))
            Column(verticalArrangement=Arrangement.spacedBy(5.dp)){
                PremiumLine("Pago pelo cliente",sale.chargedTotal.money())
                PremiumLine("Taxa do cartão",sale.cardFeeValue.money())
                PremiumLine("Líquido recebido",sale.netRevenue.money())
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
                    LotusLogoLockup()
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
