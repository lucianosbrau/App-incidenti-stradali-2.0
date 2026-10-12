package com.vvf.incidentistradali

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.gestures.detectTapGestures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.pdf.PdfRenderer
import java.io.File

/**
 * Viewer PDF integrato basato su PdfRenderer (offline, nessuna dipendenza esterna).
 * Renderizza le pagine con lazy loading e saltando direttamente alla pagina richiesta.
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
        var rendererPronto by remember { mutableStateOf(false) }
        var paginaCorrente by remember { mutableStateOf(paginaIniziale) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val fd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                    rendererRef = PdfRenderer(fd)
                    numPagine = rendererRef?.pageCount ?: 0
                } catch (e: Exception) { }
            }
            rendererPronto = true
            if (paginaIniziale > 1) listState.scrollToItem(paginaIniziale - 1)
        }

        val visibleItem by remember { derivedStateOf { listState.firstVisibleItemIndex + 1 } }
        LaunchedEffect(visibleItem) { paginaCorrente = visibleItem }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Manuale — pag. ${paginaCorrente - 10}", fontSize = 15.sp) },
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
            if (!rendererPronto) {
                Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(10.dp))
                        Text("Apertura manuale…", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFF2A2A30)),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    itemsIndexed(List(numPagine) { it }) { _, pageIdx ->
                        PaginaItem(pageIdx, rendererPronto)
                    }
                }
            }
        }
    }

    @Composable
    fun PaginaItem(pageIdx: Int, rendererPronto: Boolean) {
        var bmp by remember(pageIdx) { mutableStateOf<Bitmap?>(bitmaps[pageIdx]) }
        // riprova il rendering quando il renderer diventa pronto
        LaunchedEffect(pageIdx, rendererPronto) {
            if (bmp == null && rendererPronto) {
                val b = withContext(Dispatchers.IO) { renderPage(pageIdx) }
                if (b != null) {
                    bitmaps[pageIdx] = b
                    bmp = b
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
        val renderer = rendererRef ?: return null
        return try {
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
