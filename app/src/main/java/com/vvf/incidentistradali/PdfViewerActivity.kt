package com.vvf.incidentistradali

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.pdf.PdfRenderer
import java.io.File
import java.util.concurrent.Executors

/**
 * Viewer PDF integrato basato su PdfRenderer (nessuna dipendenza esterna, offline).
 * Renderizza le pagine del manuale in una lista verticale con lazy loading,
 * saltando direttamente alla pagina richiesta.
 */
class PdfViewerActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PAGINA_PDF = "pagina_pdf" // 1-based
    }

    private var rendererRef: PdfRenderer? = null
    private val bitmaps = mutableMapOf<Int, Bitmap>()
    private var numPagine = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val paginaRichiesta = intent.getIntExtra(EXTRA_PAGINA_PDF, 1)
        val pdfFile = ensurePdfInCache()
        setContent {
            MaterialTheme {
                ViewerContent(pdfFile, paginaRichiesta)
            }
        }
    }

    private fun ensurePdfInCache(): File {
        val f = File(cacheDir, "manuale.pdf")
        if (!f.exists() || f.length() == 0L) {
            assets.open("data/manuale.pdf").use { input ->
                f.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return f
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ViewerContent(pdfFile: File, paginaIniziale: Int) {
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        var caricato by remember { mutableStateOf(false) }
        var paginaCorrente by remember { mutableStateOf(paginaIniziale) }
        val ctx = LocalContext.current

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val fd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(fd)
                    rendererRef = renderer
                    numPagine = renderer.pageCount
                } catch (e: Exception) { }
            }
            caricato = true
            // salta alla pagina richiesta (paginaIniziale è 1-based; la lista include tutte le pagine)
            listState.scrollToItem(paginaIniziale - 1)
        }

        // aggiorna indicatore pagina durante lo scroll
        val visibleItem by remember { derivedStateOf { listState.firstVisibleItemIndex + 1 } }
        LaunchedEffect(visibleItem) { paginaCorrente = visibleItem }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Manuale — pag. ${paginaCorrente - 10}/${numPagine - 10}", fontSize = 15.sp) },
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
            if (!caricato) {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFF2A2A30)),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    itemsIndexed(List(numPagine) { it }) { _, pageIdx ->
                        PaginaItem(pageIdx, paginaIniziale - 1)
                    }
                }
            }
        }
    }

    @Composable
    fun PaginaItem(pageIdx: Int, initialIdx: Int) {
        var bmp by remember(pageIdx) { mutableStateOf(bitmaps[pageIdx]) }
        LaunchedEffect(pageIdx) {
            if (bmp == null) {
                withContext(Dispatchers.IO) {
                    val b = renderPage(pageIdx)
                    if (b != null) {
                        bitmaps[pageIdx] = b
                        bmp = b
                    }
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            if (bmp != null) {
                Image(
                    bitmap = bmp!!.asImageBitmap(),
                    contentDescription = "Pagina ${pageIdx + 1}",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color(0xFF3A3A40)),
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

    private fun renderPage(index: Int): Bitmap? {
        return try {
            val renderer = rendererRef ?: return null
            synchronized(renderer) {
                renderer.openPage(index).use { page ->
                    val targetW = 1080
                    val ratio = page.height.toFloat() / page.width.toFloat()
                    val targetH = (targetW * ratio).toInt()
                    val bmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.RGB_565)
                    bmp.eraseColor(AndroidColor.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bmp
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroy() {
        rendererRef?.close()
        rendererRef = null
        bitmaps.clear()
        super.onDestroy()
    }
}
