package com.vvf.incidentistradali

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.pdf.PdfRenderer
import android.util.LruCache
import androidx.core.content.FileProvider
import java.io.File

/**
 * Viewer PDF integrato basato su PdfRenderer (offline, nessuna dipendenza esterna).
 * - copia del PDF in cache verificata per dimensione (ripara copie corrotte)
 * - LruCache per le pagine renderizzate (memoria limitata)
 * - errore visibile con fallback al visualizzatore esterno
 */
class PdfViewerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PAGINA_PDF = "pagina_pdf" // 1-based
        private const val PDF_ASSET = "data/manuale.pdf"
        private const val EXPECTED_SIZE = 12726948L
    }

    private var rendererRef: PdfRenderer? = null
    private val pageCache = LruCache<Int, Bitmap>(24 * 1024 * 1024) // 24 MB di bitmap
    private var numPagine = 0
    private var erroreApertura: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val paginaRichiesta = intent.getIntExtra(EXTRA_PAGINA_PDF, 1)
        setContent {
            MaterialTheme {
                ViewerContent(paginaRichiesta)
            }
        }
    }

    private fun ensurePdfInCache(): File {
        val f = File(cacheDir, "manuale.pdf")
        if (!f.exists() || f.length() != EXPECTED_SIZE) {
            f.delete()
            assets.open(PDF_ASSET).use { input ->
                f.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return f
    }

    private fun openRenderer(): String? {
        return try {
            val file = ensurePdfInCache()
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)
            rendererRef = renderer
            numPagine = renderer.pageCount
            null
        } catch (e: Exception) {
            "Impossibile aprire il PDF: ${e.message}"
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ViewerContent(paginaIniziale: Int) {
        val listState = rememberLazyListState()
        var stato by remember { mutableStateOf("caricamento") } // caricamento | pronto | errore
        var errorMsg by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            val err = withContext(Dispatchers.IO) { openRenderer() }
            if (err == null) {
                stato = "pronto"
                if (paginaIniziale > 1 && paginaIniziale <= numPagine) {
                    listState.scrollToItem(paginaIniziale - 1)
                }
            } else {
                errorMsg = err
                stato = "errore"
            }
        }

        val visibleItem by remember { derivedStateOf { listState.firstVisibleItemIndex + 1 } }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (stato == "pronto") "Manuale — pag. ${visibleItem - 10}" else "Manuale",
                            fontSize = 15.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) { Icon(Icons.Filled.ArrowBack, "indietro") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF202060),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            when (stato) {
                "caricamento" -> {
                    Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(10.dp))
                            Text("Apertura manuale…", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
                "errore" -> {
                    Column(
                        Modifier.padding(padding).fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("⚠️", fontSize = 40.sp)
                        Spacer(Modifier.height(10.dp))
                        Text(errorMsg, fontSize = 14.sp, color = Color.Gray)
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = { apriConViewerEsterno() }) {
                            Text("Apri con visualizzatore PDF esterno")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { finish() }) { Text("Chiudi") }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFF2A2A30)),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        itemsIndexed(List(numPagine) { it }) { _, pageIdx ->
                            PaginaItem(pageIdx)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PaginaItem(pageIdx: Int) {
        var bmp by remember(pageIdx) { mutableStateOf<Bitmap?>(pageCache.get(pageIdx)) }
        var failed by remember(pageIdx) { mutableStateOf(false) }
        var errorMsg by remember(pageIdx) { mutableStateOf<String?>(null) }
        var retry by remember(pageIdx) { mutableStateOf(0) }
        LaunchedEffect(pageIdx, retry) {
            if (bmp == null) {
                val b = withContext(Dispatchers.IO) { renderPage(pageIdx) }
                if (b != null) {
                    pageCache.put(pageIdx, b)
                    bmp = b
                } else {
                    errorMsg = lastRenderError
                    failed = true
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                bmp != null -> Image(
                    bitmap = bmp!!.asImageBitmap(),
                    contentDescription = "Pagina ${pageIdx + 1}",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
                failed -> Column(
                    Modifier.fillMaxWidth().background(Color(0xFF3A3A40)).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Errore nel rendering della pag. ${pageIdx + 1}", color = Color.White, fontSize = 13.sp)
                    if (errorMsg != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(errorMsg!!, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { failed = false; retry++ }) {
                        Text("Riprova", fontSize = 12.sp)
                    }
                }
                else -> Box(
                    Modifier.fillMaxWidth().height(400.dp).background(Color(0xFF3A3A40)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("pag. ${pageIdx + 1}", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            }
        }
    }

    private var lastRenderError: String? = null

    private fun renderPage(index: Int): Bitmap? {
        val renderer = rendererRef ?: return null
        return try {
            synchronized(renderer) {
                renderer.openPage(index).use { page ->
                    val targetW = 720
                    val ratio = page.height.toFloat() / page.width.toFloat()
                    val targetH = (targetW * ratio).toInt()
                    val bmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(AndroidColor.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bmp
                }
            }
        } catch (e: Exception) {
            lastRenderError = "${e.javaClass.simpleName}: ${e.message}"
            null
        }
    }

    private fun apriConViewerEsterno() {
        try {
            val file = ensurePdfInCache()
            val uri = FileProvider.getUriForFile(this, "com.vvf.incidentistradali.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            finish()
        }
    }

    override fun onDestroy() {
        rendererRef?.close()
        rendererRef = null
        pageCache.evictAll()
        super.onDestroy()
    }
}

/**
 * Slide a schermo intero con pinch-zoom e doppio tap per chiudere.
 */
@Composable
fun FullscreenSlide(bmp: Bitmap, onDismiss: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    Dialog(onDismissRequest = onDismiss) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xEE101014))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 6f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        } else {
                            offsetX = 0f; offsetY = 0f
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { onDismiss() },
                        onTap = { }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Slide ingrandita",
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                contentScale = ContentScale.FillWidth
            )
            Text(
                "pizzica per zoom · doppio tap per chiudere",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            )
        }
    }
}
