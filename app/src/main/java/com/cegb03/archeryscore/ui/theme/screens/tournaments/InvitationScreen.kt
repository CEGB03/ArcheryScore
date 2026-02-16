package com.cegb03.archeryscore.ui.theme.screens.tournaments

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cegb03.archeryscore.data.model.InvitationBlock
import com.cegb03.archeryscore.viewmodel.InvitationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationScreen(
    url: String,
    onClose: () -> Unit,
    viewModel: InvitationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(url) {
        Log.d("ArcheryScore_Debug", "📨 InvitationScreen - load url=$url")
        viewModel.load(url)
    }

    LaunchedEffect(state.isLoading, state.errorMessage, state.html, state.pdfFile) {
        when {
            state.isLoading -> Log.d("ArcheryScore_Debug", "📨 InvitationScreen - loading")
            state.errorMessage != null -> Log.d(
                "ArcheryScore_Debug",
                "📨 InvitationScreen - error=${state.errorMessage}"
            )
            state.pdfFile != null -> Log.d(
                "ArcheryScore_Debug",
                "📨 InvitationScreen - pdfFile=${state.pdfFile?.name}"
            )
            state.html != null -> Log.d(
                "ArcheryScore_Debug",
                "📨 InvitationScreen - html blocks=${state.html?.blocks?.size ?: 0}"
            )
        }
    }

    BackHandler(enabled = true) { onClose() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invitación") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Cargando invitación...")
                }
            }
            state.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.errorMessage ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            state.html != null -> {
                InvitationHtmlContent(
                    title = state.html?.title.orEmpty(),
                    blocks = state.html?.blocks.orEmpty(),
                    modifier = Modifier.padding(innerPadding)
                )
            }
            state.pdfFile != null -> {
                PdfContent(
                    file = state.pdfFile,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun InvitationHtmlContent(
    title: String,
    blocks: List<InvitationBlock>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (title.isNotEmpty()) {
            item {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
            }
        }
        items(blocks) { block ->
            when (block) {
                is InvitationBlock.Heading -> {
                    Text(text = block.text, style = MaterialTheme.typography.titleMedium)
                }
                is InvitationBlock.Paragraph -> {
                    Text(text = block.text, style = MaterialTheme.typography.bodyMedium)
                }
                is InvitationBlock.Table -> {
                    TableBlock(block.rows)
                }
            }
        }
    }
}

@Composable
private fun TableBlock(rows: List<List<String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            rows.forEach { row ->
                Text(text = row.joinToString(" | "), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PdfContent(file: File?, modifier: Modifier = Modifier) {
    if (file == null) return

    var pages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val minScale = 1f
    val maxScale = 3f
    val doubleTapScale = 2f

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
        scale = newScale
        offset = if (newScale == minScale) Offset.Zero else offset + panChange
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val targetWidthPx = with(density) { maxWidth.roundToPx().coerceAtLeast(1) }
        val imageWidth = maxWidth

        LaunchedEffect(file, targetWidthPx) {
            try {
                error = null
                Log.d("ArcheryScore_Debug", "📄 RenderPDF - Starting with file=$file")
                val bitmaps = withContext(Dispatchers.IO) {
                    renderPdf(file, targetWidthPx)
                }
                Log.d("ArcheryScore_Debug", "📄 RenderPDF - Loaded ${bitmaps.size} pages")
                pages = bitmaps
            } catch (e: Exception) {
                Log.e("ArcheryScore_Debug", "📄 RenderPDF - Error", e)
                error = e.message
            }
        }

        if (error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = error ?: "Error al abrir PDF",
                    color = MaterialTheme.colorScheme.error
                )
            }
            return@BoxWithConstraints
        }

        val listState = rememberLazyListState()

        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        tonalElevation = 1.dp,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.clickable {
                            val newScale = if (scale < doubleTapScale) doubleTapScale else minScale
                            scale = newScale
                            offset = Offset.Zero
                        }
                    ) {
                        Text(
                            text = "${(scale * 100).toInt()}%",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                val newScale = if (scale < doubleTapScale) doubleTapScale else minScale
                                scale = newScale
                                offset = Offset.Zero
                            }
                        )
                    }
                    .transformable(state = transformableState)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                        clip = true
                    },
                state = listState,
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(pages) { index, bitmap ->
                    val aspect = bitmap.height.toFloat() / bitmap.width.toFloat()
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f / aspect)
                    )
                }
            }
        }
    }
}

private fun renderPdf(file: File?, targetWidthPx: Int): List<Bitmap> {
    val bitmaps = mutableListOf<Bitmap>()
    if (file == null) {
        Log.e("ArcheryScore_Debug", "📄 renderPdf - file is null")
        return bitmaps
    }
    
    val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = PdfRenderer(descriptor)
    
    Log.d("ArcheryScore_Debug", "📄 renderPdf - PDF has ${renderer.pageCount} pages")

    for (i in 0 until renderer.pageCount) {
        val page = renderer.openPage(i)
        val scale = targetWidthPx.toFloat() / page.width.toFloat()
        val targetHeightPx = (page.height * scale).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(targetWidthPx, targetHeightPx, Bitmap.Config.ARGB_8888)
        val matrix = android.graphics.Matrix().apply { setScale(scale, scale) }
        page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        bitmaps.add(bitmap)
    }

    renderer.close()
    descriptor.close()
    return bitmaps
}
