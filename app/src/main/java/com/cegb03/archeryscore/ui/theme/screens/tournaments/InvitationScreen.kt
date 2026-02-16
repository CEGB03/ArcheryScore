package com.cegb03.archeryscore.ui.theme.screens.tournaments

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
        viewModel.load(url)
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

    LaunchedEffect(file) {
        try {
            val bitmaps = withContext(Dispatchers.IO) {
                renderPdf(file)
            }
            pages = bitmaps
        } catch (e: Exception) {
            error = e.message
        }
    }

    if (error != null) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = error ?: "Error al abrir PDF", color = MaterialTheme.colorScheme.error)
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pages) { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun renderPdf(file: File): List<Bitmap> {
    val bitmaps = mutableListOf<Bitmap>()
    val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = PdfRenderer(descriptor)

    for (i in 0 until renderer.pageCount) {
        val page = renderer.openPage(i)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        bitmaps.add(bitmap)
    }

    renderer.close()
    descriptor.close()
    return bitmaps
}
