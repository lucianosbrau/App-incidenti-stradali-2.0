package com.vvf.incidentistradali

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManualeApp()
        }
    }
}

private fun loadSlideImage(path: String): Bitmap? = try {
    App.instance.assets.open("data/$path").use { BitmapFactory.decodeStream(it) }
} catch (e: Exception) {
    null
}

fun parseColor(hex: String): Color = Color(android.graphics.Color.parseColor(hex))

object NavState {
    var lastSearchQuery: String = ""
    var lastSearchTag: String? = null
}

@Composable
fun ManualeApp() {
    val nav = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2200)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
        return
    }

    NavHost(nav, startDestination = "home") {
        composable("home") { HomeScreen(onParte = { nav.navigate("parte/$it") }, onSearch = { nav.navigate("search") }, onScheda = { nav.navigate("scheda/$it") }, onManuale = { nav.navigate("manuale") }) }
        composable("parte/{n}", arguments = listOf(navArgument("n") { type = androidx.navigation.NavType.IntType })) { entry ->
            val n = entry.arguments?.getInt("n") ?: 1
            ParteScreen(n, onScheda = { nav.navigate("scheda/$it") }, onBack = { nav.popBackStack() })
        }
        composable("search") { SearchScreen(onScheda = { nav.navigate("scheda/$it") }, onBack = { nav.popBackStack() }) }
        composable("scheda/{id}", arguments = listOf(navArgument("id") { type = androidx.navigation.NavType.StringType })) { entry ->
            val id = entry.arguments?.getString("id") ?: ""
            val s = Repository.schede.firstOrNull { it.id == id }
            if (s != null) SchedaScreen(s, onBack = { nav.popBackStack() })
        }
        composable("manuale") { ManualeScreen(onBack = { nav.popBackStack() }) }
    }
}

@Composable
fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(Color(0xFF202060)), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.cover_splash),
            contentDescription = "Copertina manuale",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "MANUALE OPERATIVO\nINCIDENTI STRADALI 2.0/2024",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                color = Color.White,
                trackColor = Color(0x33FFFFFF),
                modifier = Modifier
                    .width(180.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.height(8.dp))
            Text("Corpo Nazionale dei Vigili del Fuoco", color = Color.White, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onParte: (Int) -> Unit, onSearch: () -> Unit, onScheda: (String) -> Unit, onManuale: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val risultati = remember(query) { if (query.length >= 2) Repository.search(query) else emptyList() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.padding(16.dp)) {
                    Text("MENU", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { scope.launch { drawerState.close() }; onManuale() }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("\uD83D\uDCD6", fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Text("Cerca nel manuale", fontSize = 16.sp)
                    }
                }
            }
        }
    ) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Incidenti Stradali 2.0", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Filled.Menu, "Menu") }
                },
                actions = {
                    IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, "Ricerca") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF202060), titleContentColor = Color.White, navigationIconContentColor = Color.White, actionIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Cerca manovra, argomento, tag…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = { if (query.isNotEmpty()) IconButton({ query = "" }) { Icon(Icons.Filled.Close, null) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (query.length >= 2) {
                if (risultati.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Nessun risultato per \"$query\"", color = Color.Gray, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn {
                        items(risultati, key = { it.id }) { s ->
                            SchedaRow(s) { onScheda(s.id) }
                        }
                    }
                }
            } else {
                SezioniGrid(onParte)
            }
        }
    }
    }
}

@Composable
fun SezioniGrid(onParte: (Int) -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
        Repository.parti.forEach { p ->
            val color = parseColor(p.coloreHex)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.15f))
                    .border(1.5.dp, color, RoundedCornerShape(14.dp))
                    .clickable { onParte(p.numero) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${p.numero}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(p.titolo, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Text(
                        "${Repository.schedeDiParte(p.numero).size} schede",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "GdL — Manuale Operativo Incidenti Stradali vers. 2.0/2024 CNVVF",
            fontSize = 11.sp, color = Color.Gray
        )
    }
}

@Composable
fun SchedaRow(s: Scheda, onClick: () -> Unit) {
    val color = parseColor(Repository.coloreParte(s.parte))
    val nb = s.blocks.size
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.10f))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.titolo, fontWeight = FontWeight.Medium, fontSize = 15.sp, maxLines = 2, modifier = Modifier.weight(1f, fill = false))
                if (nb > 1) {
                    Spacer(Modifier.width(6.dp))
                    Text("(${nb} slide)", fontSize = 11.sp, color = Color.Gray)
                }
            }
            if (s.paragrafo.isNotBlank()) {
                Text(s.paragrafo, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
            }
            Text(
                "Parte ${s.parte}" + if (s.pagineManuale.isNotBlank()) " · manuale p. ${s.pagineManuale}" else " · slide ${s.primoSlide}",
                fontSize = 11.sp, color = color
            )
        }
        if (s.videoUrl != null) Icon(Icons.Filled.PlayCircle, "video", tint = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParteScreen(n: Int, onScheda: (String) -> Unit, onBack: () -> Unit) {
    val parte = Repository.parti.first { it.numero == n }
    val color = parseColor(parte.coloreHex)
    val schede = Repository.schedeDiParte(n)
    var query by remember { mutableStateOf("") }
    val ql = query.trim().lowercase()
    val filtered = if (ql.length >= 2) schede.filter { s ->
        s.titolo.lowercase().contains(ql) || s.blocks.any { b -> b.testo.lowercase().contains(ql) || b.paragrafo.lowercase().contains(ql) }
    } else schede

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PARTE ${n} — ${parte.titolo}", fontSize = 16.sp, maxLines = 2) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "indietro") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = color, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Filtra in questa parte…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn {
                items(filtered, key = { it.id }) { s ->
                    SchedaRow(s) { onScheda(s.id) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onScheda: (String) -> Unit, onBack: () -> Unit) {
    var query by remember { mutableStateOf(NavState.lastSearchQuery) }
    var tagSelezionato by remember { mutableStateOf<String?>(NavState.lastSearchTag) }
    val risultati = when {
        tagSelezionato != null -> Repository.searchByTag(tagSelezionato!!)
        query.length >= 2 -> Repository.search(query)
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ricerca e tag") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "indietro") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF202060), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; tagSelezionato = null; NavState.lastSearchQuery = it; NavState.lastSearchTag = null },
                placeholder = { Text("Cerca in tutto il manuale…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = { if (query.isNotEmpty()) IconButton({ query = "" }) { Icon(Icons.Filled.Close, null) } },
                modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp)
            )
            Text("Tag principali", Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.SemiBold)
            FlowRowTags(tagSelezionato) { tagSelezionato = if (tagSelezionato == it) null else it; query = ""; NavState.lastSearchTag = tagSelezionato; NavState.lastSearchQuery = "" }
            Text(
                if (risultati.isEmpty()) "Nessun risultato" else "${risultati.size} risultati",
                Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                fontSize = 12.sp, color = Color.Gray
            )
            LazyColumn {
                items(risultati, key = { it.id }) { s ->
                    SchedaRow(s) { onScheda(s.id) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualeScreen(onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    val focusManager = LocalFocusManager.current
    val risultati = remember(query) { ManualeRepository.search(query) }
    val ricercaAttiva = query.trim().length >= 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cerca nel manuale", fontSize = 16.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "indietro") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF202060), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Cerca una parola nel manuale PDF…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = { if (query.isNotEmpty()) IconButton({ query = "" }) { Icon(Icons.Filled.Close, null) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn {
                if (!ricercaAttiva) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Text("PARTI DEL MANUALE", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        ManualeRepository.parti.forEach { p ->
                            val color = parseColor(p.coloreHex)
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color.copy(alpha = 0.10f))
                                    .clickable { ManualeRepository.apriPdf(ctx, p.pagStampata) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("PARTE ${p.n} — ${p.titolo}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text("inizia a pag. ${p.pagStampata}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
                }
                if (ricercaAttiva) {
                    item {
                        Text(
                            if (risultati.isEmpty()) "Nessuna pagina contiene \"$query\"" else "${risultati.size} pagine con \"$query\"",
                            fontSize = 12.sp, color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
                if (ricercaAttiva) {
                    items(risultati, key = { it.pagStampata }) { r ->
                        val color = if (r.parte in 1..7) parseColor(Repository.coloreParte(r.parte)) else Color(0xFF555555)
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(color.copy(alpha = 0.10f))
                                .clickable { ManualeRepository.apriPdf(ctx, r.pagStampata) }
                                .padding(12.dp)
                        ) {
                            Text(
                                "Manuale p. ${r.pagStampata} — PARTE ${if (r.parte > 0) r.parte else ""} ${r.titoloParte}",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color
                            )
                            Text("…${r.snippet}…", fontSize = 13.sp, color = Color.Gray, maxLines = 3)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlowRowTags(selected: String?, onTag: (String) -> Unit) {
    val tags = Repository.tuttiTag
    Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
        rows(tags.chunked(4)).forEach { rowTags ->
            Row(Modifier.padding(vertical = 2.dp)) {
                rowTags.forEach { tag ->
                    val active = tag == selected
                    Text(
                        tag,
                        Modifier
                            .padding(3.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (active) Color(0xFF202060) else Color(0x14000060))
                            .clickable { onTag(tag) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        color = if (active) Color.White else Color(0xFF303070),
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private fun <T> rows(chunks: List<List<T>>): List<List<T>> = chunks

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedaScreen(s: Scheda, onBack: () -> Unit) {
    val color = parseColor(Repository.coloreParte(s.parte))
    val ctx = LocalContext.current
    var fullscreenSlide by remember { mutableStateOf<Bitmap?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.titolo, fontSize = 16.sp, maxLines = 2) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "indietro") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = color, titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            item {
                Column(Modifier.padding(16.dp)) {
                    if (s.paragrafo.isNotBlank()) {
                        Text(s.paragrafo, fontSize = 14.sp, color = color, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                    }
                    Row {
                        Text(
                            "Parte ${s.parte} · ${Repository.parti.first { it.numero == s.parte }.titolo}",
                            fontSize = 12.sp, color = Color.Gray
                        )
                    }
                    if (s.pagineManuale.isNotBlank()) {
                        Text("Manuale p. ${s.pagineManuale}", fontSize = 12.sp, color = Color.Gray)
                    }
                    if (s.tags.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        TagsInline(s.tags, color)
                    }
                    s.blocks.forEach { b ->
                        if (s.blocks.size > 1) {
                            Spacer(Modifier.height(20.dp))
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Slide ${b.slide}" + if (b.pagineManuale.isNotBlank()) " · manuale p. ${b.pagineManuale}" else "",
                                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        } else if (b.pagineManuale.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Text("Manuale p. ${b.pagineManuale}", fontSize = 12.sp, color = Color.Gray)
                        }
                        b.alert.forEach { a ->
                            AlertBox(a, color)
                            Spacer(Modifier.height(10.dp))
                        }
                        if (b.testo.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(b.testo, fontSize = 15.sp, lineHeight = 22.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        val imgName = "slide_img/slide_%03d.jpg".format(b.slide)
                        val bmp = remember(s.id) { loadSlideImage(imgName) }
                        if (bmp != null) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Slide ${b.slide}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp))
                                    .clickable { fullscreenSlide = bmp },
                                contentScale = ContentScale.FillWidth
                            )
                        }
                    }
                    if (s.videoUrl != null) {
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(s.videoUrl))) },
                            colors = ButtonDefaults.buttonColors(containerColor = color),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.PlayCircle, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Guarda il video della manovra")
                        }
                    }
                }
            }
        }
        if (fullscreenSlide != null) {
        FullscreenSlide(fullscreenSlide!!) { fullscreenSlide = null }
    }
}
}

@Composable
fun TagsInline(tags: List<String>, accent: Color) {
    Column {
        tags.chunked(4).forEach { rowTags ->
            Row(Modifier.padding(vertical = 2.dp)) {
                rowTags.forEach { tag ->
                    Text(
                        tag,
                        Modifier
                            .padding(3.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(accent.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp, color = accent
                    )
                }
            }
        }
    }
}

@Composable
fun AlertBox(a: Alert, accent: Color) {
    val electro = a.tipo == "elettrocuzione"
    val bg = if (electro) Color(0xFFFFF3CD) else Color(0xFFFDE7E7)
    val border = if (electro) Color(0xFFE0A800) else Color(0xFFED2323)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Icon(if (electro) Icons.Filled.Bolt else Icons.Filled.Warning, "alert", tint = border)
        Spacer(Modifier.width(10.dp))
        Column {
            Text("ATTENZIONE!", color = border, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(a.testo, fontSize = 14.sp)
        }
    }
}
