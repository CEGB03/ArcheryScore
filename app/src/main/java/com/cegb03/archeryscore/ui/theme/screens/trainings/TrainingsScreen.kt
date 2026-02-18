package com.cegb03.archeryscore.ui.theme.screens.trainings

import android.Manifest
import android.R.attr.enabled
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.cegb03.archeryscore.data.local.training.SeriesEntity
import com.cegb03.archeryscore.data.local.training.TrainingEntity
import com.cegb03.archeryscore.data.local.training.TrainingType
import com.cegb03.archeryscore.data.local.training.TrainingWithSeries
import com.cegb03.archeryscore.data.local.training.SeriesWithEnds
import com.cegb03.archeryscore.data.model.WeatherSnapshot
import com.cegb03.archeryscore.ui.theme.getScoreColor
import com.cegb03.archeryscore.ui.theme.getScoreTextColor
import com.cegb03.archeryscore.util.getCurrentLocation
import com.cegb03.archeryscore.viewmodel.AuthViewModel
import com.cegb03.archeryscore.viewmodel.SeriesFormData
import com.cegb03.archeryscore.viewmodel.TrainingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlinx.coroutines.launch

@Composable
fun TrainingsScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onDetailOpenChanged: (Boolean) -> Unit = {}
) {
    val trainings by viewModel.trainings.collectAsState()
    val trainingDetail by viewModel.trainingDetail.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Notificar cuando se abre o cierra un detalle
    LaunchedEffect(trainingDetail) {
        onDetailOpenChanged(trainingDetail != null)
    }

    Log.d("ArcheryScore_Debug", "Recompose - trainings: ${trainings.size}, trainingDetail: ${trainingDetail != null}, isLoggedIn: $isLoggedIn")

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }

    if (trainingDetail != null) {
        Log.d("ArcheryScore_Debug", "Showing TrainingDetailScreen for training ID: ${trainingDetail?.training?.id}")
        TrainingDetailScreen(
            detail = trainingDetail,
            onBack = { viewModel.selectTraining(null) },
            onConfirmEnd = { endId, scoresText, totalScore ->
                viewModel.confirmEnd(endId, scoresText, totalScore)
            }
        )
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Individual") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Grupal") }
            )
        }

        when (selectedTabIndex) {
            0 -> TrainingsList(
                trainings = trainings,
                onSelect = {
                    Log.d("ArcheryScore_Debug", "Training selected: $it")
                    viewModel.selectTraining(it)
                },
                onAdd = { showCreateDialog = true }
            )
            1 -> GroupTrainingsPlaceholder(onAdd = { showCreateDialog = true })
        }
    }

    if (showCreateDialog) {
        CreateTrainingWithSeriesDialog(
            isLoggedIn = isLoggedIn,
            onDismiss = { showCreateDialog = false },
            onCreate = { archerName, seriesList, trainingType, isGroup ->
                viewModel.createTrainingWithSeries(archerName, seriesList, trainingType, isGroup)
                showCreateDialog = false
            },
            onFetchWeather = { lat, lon -> viewModel.fetchWeather(lat, lon) }
        )
    }
}

@Composable
private fun TrainingsList(
    trainings: List<TrainingWithSeries>,
    onSelect: (Long) -> Unit,
    onAdd: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (trainings.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No hay entrenamientos")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trainings) { trainingWithSeries ->
                    TrainingCard(trainingWithSeries = trainingWithSeries, onClick = { onSelect(trainingWithSeries.training.id) })
                }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo entrenamiento")
        }
    }
}

@Composable
private fun GroupTrainingsPlaceholder(onAdd: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Entrenamientos grupales (pendiente)")
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nuevo entrenamiento")
        }
    }
}

@Composable
private fun TrainingCard(trainingWithSeries: TrainingWithSeries, onClick: () -> Unit) {
    val training = trainingWithSeries.training
    val date = remember(training.createdAt) {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.format(Date(training.createdAt))
    }
    val trainingTypeLabel = if (training.trainingType == TrainingType.TOURNAMENT) "Torneo" else "Entrenamiento"
    val groupLabel = if (training.isGroup) "Grupal" else "Individual"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = training.archerName?.takeIf { it.isNotBlank() } ?: "Entrenamiento",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Fecha: $date", style = MaterialTheme.typography.bodySmall)
            Text(text = "Tipo: $trainingTypeLabel", style = MaterialTheme.typography.bodySmall)
            Text(text = "Modalidad: $groupLabel", style = MaterialTheme.typography.bodySmall)
            
            // Mostrar información de series
            if (trainingWithSeries.series.size == 1) {
                val firstSeries = trainingWithSeries.series.first().series
                Text(
                    text = "Distancia: ${firstSeries.distanceMeters} m",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Categoria: ${firstSeries.category}",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text(
                    text = "${trainingWithSeries.series.size} series",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainingDetailScreen(
    detail: TrainingWithSeries?,
    onBack: () -> Unit,
    onConfirmEnd: (Long, String, Int) -> Unit
) {
    Log.d("ArcheryScore_Debug", "TrainingDetailScreen called - detail: ${detail != null}, training: ${detail?.training != null}, series: ${detail?.series?.size ?: 0}")
    val training = detail?.training
    var selectedMainTabIndex by rememberSaveable { mutableIntStateOf(0) } // 0..N=Series, N+1=Estadísticas
    var selectedSeriesTabIndex by rememberSaveable { mutableIntStateOf(0) } // 0=Planilla, 1=Estadísticas (dentro de serie)
    var targetSheetPage by rememberSaveable { mutableIntStateOf(-1) }

    var showInfoMenu by remember { mutableStateOf(false) }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planilla - ${training?.archerName?.takeIf { it.isNotBlank() } ?: "Entrenamiento"}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showInfoMenu = !showInfoMenu }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Información",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        androidx.compose.material3.DropdownMenu(
                            expanded = showInfoMenu,
                            onDismissRequest = { showInfoMenu = false },
                            modifier = Modifier.wrapContentHeight()
                        ) {
                            if (detail != null && detail.series.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .width(300.dp)
                                        .heightIn(max = 400.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        Text("Información del entrenamiento", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(training?.createdAt ?: 0))}", style = MaterialTheme.typography.labelSmall)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    
                                    items(detail.series) { seriesWithEnds ->
                                        val series = seriesWithEnds.series
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("Serie ${series.seriesNumber}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Distancia: ${series.distanceMeters} m", style = MaterialTheme.typography.labelSmall)
                                                Text("Categoría: ${series.category}", style = MaterialTheme.typography.labelSmall)
                                                Text("Blanco: ${series.targetType}", style = MaterialTheme.typography.labelSmall)
                                                Text("Flechas x tanda: ${series.arrowsPerEnd}", style = MaterialTheme.typography.labelSmall)
                                                Text("Tandas: ${series.endsCount}", style = MaterialTheme.typography.labelSmall)
                                                Text("Sistema: ${if (series.puntajeSystem == "X_TO_M") "X a M" else "11 a M"} (${series.targetZoneCount} zonas)", style = MaterialTheme.typography.labelSmall)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Temperatura: ${series.temperature?.let { "%.1f°C".format(it) } ?: "-"}", style = MaterialTheme.typography.labelSmall)
                                                Text("Viento: ${series.windSpeed ?: "-"} ${series.windSpeedUnit ?: ""}", style = MaterialTheme.typography.labelSmall)
                                                Text("Dirección: ${formatWindDirection(series.windDirectionDegrees)}", style = MaterialTheme.typography.labelSmall)
                                                Text("Cielo: ${series.skyCondition ?: "-"}", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (training == null || detail == null || detail.series.isEmpty()) {
            Log.w("ArcheryScore_Debug", "Training, detail is null or no series")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(if (detail?.series?.isEmpty() == true) "Error: No hay series." else "Cargando...")
            }
            return@Scaffold
        }
        
        Log.d("ArcheryScore_Debug", "Rendering tabs for training ${training.id} with ${detail.series.size} series")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Pestañas principales: Series + Estadísticas
            val numSeries = detail.series.size
            val isGeneralStatsTab = selectedMainTabIndex == numSeries
            
            TabRow(selectedTabIndex = selectedMainTabIndex) {
                // Tabs para cada serie
                detail.series.forEachIndexed { index, seriesWithEnds ->
                    Tab(
                        selected = selectedMainTabIndex == index,
                        onClick = { 
                            selectedMainTabIndex = index
                            selectedSeriesTabIndex = 0 // Reset a Planilla
                        },
                        text = { Text("Serie ${index + 1}") }
                    )
                }
                // Tab para Estadísticas generales
                Tab(
                    selected = isGeneralStatsTab,
                    onClick = { selectedMainTabIndex = numSeries },
                    text = { Text("Estadísticas") }
                )
            }

            // Si estamos en la pestaña de estadísticas generales
            if (isGeneralStatsTab) {
                GeneralStatsTab(
                    detail = detail,
                    onSwitchToSeries = { seriesIndex ->
                        selectedMainTabIndex = seriesIndex
                        selectedSeriesTabIndex = 0
                    }
                )
            } else {
                // Estamos en una serie específica
                val currentSeries = detail.series.getOrNull(selectedMainTabIndex)
                
                if (currentSeries == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Serie no encontrada")
                    }
                    return@Column
                }

                // Pestañas dentro de la serie: Planilla + Estadísticas
                TabRow(selectedTabIndex = selectedSeriesTabIndex) {
                    Tab(
                        selected = selectedSeriesTabIndex == 0,
                        onClick = { selectedSeriesTabIndex = 0 },
                        text = { Text("Planilla") }
                    )
                    Tab(
                        selected = selectedSeriesTabIndex == 1,
                        onClick = { selectedSeriesTabIndex = 1 },
                        text = { Text("Estadísticas") }
                    )
                }

                when (selectedSeriesTabIndex) {
                    0 -> {
                        Log.d("ArcheryScore_Debug", "Rendering SeriesSheetTab for series ${currentSeries.series.id}")
                        SeriesSheetTab(
                            seriesWithEnds = currentSeries,
                            onConfirmEnd = onConfirmEnd,
                            onSwitchToStats = { selectedSeriesTabIndex = 1 },
                            targetPage = targetSheetPage,
                            onPageChanged = { targetSheetPage = -1 }
                        )
                    }
                    1 -> {
                        Log.d("ArcheryScore_Debug", "Rendering SeriesStatsTab for series ${currentSeries.series.id}")
                        SeriesStatsTab(
                            seriesWithEnds = currentSeries,
                            onSwitchToPlanilla = { 
                                targetSheetPage = currentSeries.ends.size - 1
                                selectedSeriesTabIndex = 0 
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesSheetTab(
    seriesWithEnds: SeriesWithEnds,
    onConfirmEnd: (Long, String, Int) -> Unit,
    onSwitchToStats: () -> Unit = {},
    targetPage: Int = -1,
    onPageChanged: () -> Unit = {}
) {
    val series = seriesWithEnds.series
    val ends = seriesWithEnds.ends
    
    Log.d("ArcheryScore_Debug", "SeriesSheetTab - series: ${series.id}, ends: ${ends.size}, zones: ${series.targetZoneCount}, system: ${series.puntajeSystem}")
    
    if (ends.isEmpty()) {
        Log.e("ArcheryScore_Debug", "ends is empty!")
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay tandas disponibles")
        }
        return
    }

    val totalEnds = ends.size
    val pagerState = rememberPagerState(
        pageCount = { totalEnds },
        initialPage = if (targetPage >= 0 && targetPage < totalEnds) targetPage else 0
    )

    LaunchedEffect(targetPage) {
        if (targetPage >= 0 && targetPage < totalEnds) {
            pagerState.scrollToPage(targetPage)
            onPageChanged()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 120.dp)
    ) {
        // HorizontalPager para las tandas (una por página)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pageIndex ->
            Log.d("ArcheryScore_Debug", "Rendering page $pageIndex of ${ends.size}")
            val end = ends.getOrNull(pageIndex)
            
            if (end == null) {
                Log.e("ArcheryScore_Debug", "End is null at pageIndex $pageIndex")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tanda no encontrada")
                }
                return@HorizontalPager
            }
            
            // Parsear los puntajes existentes a una lista mutable
            val existingScores = remember(end.id) { 
                parseScores(end.scoresText ?: "").map { 
                    if (it.isX) "X" else if (it.isMiss) "M" else it.score.toString() 
                }.toMutableStateList()
            }
            
            val isConfirmed = end.confirmedAt != null
            val validScores = remember(series.targetZoneCount, series.puntajeSystem) {
                getValidScores(series.targetZoneCount, series.puntajeSystem)
            }
            
            // Estado para drag & drop
            var draggedScore by remember { mutableStateOf<String?>(null) }
            var dragOffset by remember { mutableStateOf(Offset.Zero) }
            var dropAreaBounds by remember { mutableStateOf<Pair<Offset, IntSize>?>(null) }

            // Estado para controlar el swipe cuando estamos en la última página
            var swipeOffsetX by remember { mutableStateOf(0f) }
            var swipeOffsetY by remember { mutableStateOf(0f) }
            var isHorizontalSwipe by remember { mutableStateOf(false) }
            val isLastPage = pageIndex == totalEnds - 1

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .then(
                        if (isLastPage) {
                            Modifier.pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        swipeOffsetX = 0f
                                        swipeOffsetY = 0f
                                        isHorizontalSwipe = false
                                    },
                                    onDragEnd = {
                                        // Si el swipe fue hacia la izquierda, suficientemente largo y más horizontal que vertical
                                        if (isHorizontalSwipe && swipeOffsetX < -200f) {
                                            Log.d("ArcheryScore_Debug", "Detectado swipe hacia la izquierda en tanda $pageIndex (x: $swipeOffsetX, y: $swipeOffsetY), cambiando a Estadísticas")
                                            onSwitchToStats()
                                        }
                                        swipeOffsetX = 0f
                                        swipeOffsetY = 0f
                                        isHorizontalSwipe = false
                                    },
                                    onDrag = { change, dragAmount ->
                                        swipeOffsetX += dragAmount.x
                                        swipeOffsetY += abs(dragAmount.y)
                                        
                                        // Determinar si es un swipe horizontal después de los primeros movimientos
                                        if (abs(swipeOffsetX) > 30f || swipeOffsetY > 30f) {
                                            if (abs(swipeOffsetX) > swipeOffsetY * 1.5f) {
                                                isHorizontalSwipe = true
                                                change.consume()
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título de la tanda
                Text(
                    text = "Tanda ${end.endNumber}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isConfirmed) "✓ Confirmada" else "${existingScores.size}/${series.arrowsPerEnd} flechas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isConfirmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Área de drop - donde se sueltan los puntajes arrastrados
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .onGloballyPositioned { coordinates ->
                            dropAreaBounds = Pair(
                                coordinates.positionInRoot(),
                                coordinates.size
                            )
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (draggedScore != null) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else 
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        if (draggedScore != null && !isConfirmed && existingScores.size < series.arrowsPerEnd) {
                                            existingScores.add(draggedScore!!)
                                            draggedScore = null
                                            dragOffset = Offset.Zero
                                        }
                                    }
                                )
                            }
                            .padding(16.dp)
                    ) {
                        if (existingScores.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (draggedScore != null) "Suelta aquí" else "Arrastra puntajes aquí",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(existingScores.size) { index ->
                                    val score = existingScores[index]
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = MaterialTheme.shapes.medium
                                            )
                                            .clickable(enabled = !isConfirmed) {
                                                existingScores.removeAt(index)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!isConfirmed) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Eliminar",
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(2.dp)
                                                    .size(14.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                            )
                                        }
                                        Text(
                                            text = score,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Mostrar total en la esquina
                        if (existingScores.isNotEmpty()) {
                            val total = remember(existingScores.toList()) {
                                existingScores.sumOf { score ->
                                    when (score) {
                                        "X" -> 10
                                        "M" -> 0
                                        else -> score.toIntOrNull() ?: 0
                                    }
                                }
                            }
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Text(
                                    text = "$total pts",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }

                // Botones de puntaje arrastrables
                if (!isConfirmed) {
                    Text(
                        text = "Arrastra o toca para agregar:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(validScores.size) { index ->
                            val score = validScores[index]
                            val scoreColor = getScoreColor(score)
                            val scoreTextColor = getScoreTextColor(score)
                            val isDisabled = existingScores.size >= series.arrowsPerEnd
                            
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .graphicsLayer {
                                        if (draggedScore == score) {
                                            translationX = dragOffset.x
                                            translationY = dragOffset.y
                                            alpha = 0.7f
                                            scaleX = 1.2f
                                            scaleY = 1.2f
                                        }
                                    }
                                    .then(
                                        if (score == "M") {
                                            Modifier.border(
                                                width = 2.dp,
                                                color = if (isDisabled) Color.Gray else Color.White,
                                                shape = MaterialTheme.shapes.medium
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .background(
                                        color = if (isDisabled)
                                            MaterialTheme.colorScheme.surfaceVariant
                                        else
                                            scoreColor,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .pointerInput(score) {
                                        detectDragGestures(
                                            onDragStart = {
                                                if (existingScores.size < series.arrowsPerEnd) {
                                                    draggedScore = score
                                                    dragOffset = Offset.Zero
                                                }
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                if (draggedScore == score) {
                                                    dragOffset += dragAmount
                                                }
                                            },
                                            onDragEnd = {
                                                if (draggedScore == score) {
                                                    // Verificar si se soltó en el área de drop
                                                    dropAreaBounds?.let { (dropPos, dropSize) ->
                                                        val finalPos = dragOffset
                                                        if (finalPos.x >= dropPos.x && finalPos.x <= dropPos.x + dropSize.width &&
                                                            finalPos.y >= dropPos.y && finalPos.y <= dropPos.y + dropSize.height) {
                                                            if (existingScores.size < series.arrowsPerEnd) {
                                                                existingScores.add(score)
                                                            }
                                                        }
                                                    }
                                                    draggedScore = null
                                                    dragOffset = Offset.Zero
                                                }
                                            },
                                            onDragCancel = {
                                                draggedScore = null
                                                dragOffset = Offset.Zero
                                            }
                                        )
                                    }
                                    .clickable(enabled = existingScores.size < series.arrowsPerEnd) {
                                        existingScores.add(score)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = score,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDisabled)
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    else
                                        scoreTextColor
                                )
                            }
                        }
                    }
                }

                // Botón confirmar
                Button(
                    onClick = {
                        if (existingScores.isNotEmpty()) {
                            val parsed = existingScores.map { score ->
                                when (score) {
                                    "X" -> ParsedScore(10, isX = true, isMiss = false)
                                    "M" -> ParsedScore(0, isX = false, isMiss = true)
                                    else -> ParsedScore(score.toIntOrNull() ?: 0, isX = false, isMiss = false)
                                }
                            }
                            val sorted = sortScores(parsed)
                            val sortedText = formatScores(sorted)
                            val total = parsed.sumOf { it.score }
                            onConfirmEnd(end.id, sortedText, total)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = existingScores.size == series.arrowsPerEnd && !isConfirmed
                ) {
                    Text(if (isConfirmed) "Confirmada" else "Confirmar tanda")
                }
            }
        }

        // Indicador de página y botón agregar tanda
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Indicador de página (puntos)
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(ends.size) { index ->
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(8.dp)
                            .padding(horizontal = 4.dp)
                            .background(
                                color = if (pagerState.currentPage == index) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                                shape = CircleShape
                            )
                    )
                }
            }
            Text("Tanda ${pagerState.currentPage + 1} de ${ends.size}")
        }
    }
}

@Composable
private fun SeriesStatsTab(
    seriesWithEnds: SeriesWithEnds,
    onSwitchToPlanilla: () -> Unit = {}
) {
    var swipeOffsetX by remember { mutableStateOf(0f) }
    var swipeOffsetY by remember { mutableStateOf(0f) }
    var isHorizontalSwipe by remember { mutableStateOf(false) }

    val ends = seriesWithEnds.ends
    val series = seriesWithEnds.series

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        swipeOffsetX = 0f
                        swipeOffsetY = 0f
                        isHorizontalSwipe = false
                    },
                    onDragEnd = {
                        if (isHorizontalSwipe && swipeOffsetX > 200f) {
                            Log.d("ArcheryScore_Debug", "Detectado swipe hacia la derecha en Estadísticas de serie, volviendo a Planilla")
                            onSwitchToPlanilla()
                        }
                        swipeOffsetX = 0f
                        swipeOffsetY = 0f
                        isHorizontalSwipe = false
                    },
                    onDrag = { change, dragAmount ->
                        swipeOffsetX += dragAmount.x
                        swipeOffsetY += abs(dragAmount.y)
                        if (abs(swipeOffsetX) > 30f || swipeOffsetY > 30f) {
                            if (abs(swipeOffsetX) > swipeOffsetY * 1.5f) {
                                isHorizontalSwipe = true
                                change.consume()
                            }
                        }
                    }
                )
            }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Detalles de cada tanda
            item {
                Text("Estadísticas por tanda", style = MaterialTheme.typography.titleMedium)
            }

            items(ends) { end ->
                if (end.confirmedAt != null) {
                    val endScores = parseScores(end.scoresText.orEmpty())
                    if (endScores.isNotEmpty()) {
                        val endTotal = endScores.sumOf { it.score }
                        val endAverage = endTotal.toDouble() / endScores.size
                        val endXCount = endScores.count { it.isX }
                        val endTenCount = endScores.count { it.score == 10 && !it.isX }
                        val endNineCount = endScores.count { it.score == 9 }
                        val endMissCount = endScores.count { it.isMiss }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Tanda ${end.endNumber}", style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total: $endTotal")
                                    Text("Promedio: ${"%.2f".format(endAverage)}")
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    Text("X: $endXCount")
                                    Text("10: $endTenCount")
                                    Text("9: $endNineCount")
                                    Text("M: $endMissCount")
                                }
                            }
                        }
                    }
                }
            }

            // Separador
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Sumatoria de la serie
            item {
                val parsedScores = ends.flatMap { parseScores(it.scoresText.orEmpty()) }
                val totalArrows = parsedScores.size
                val totalScore = parsedScores.sumOf { it.score }
                val average = if (totalArrows == 0) 0.0 else totalScore.toDouble() / totalArrows
                val xCount = parsedScores.count { it.isX }
                val tenCount = parsedScores.count { it.score == 10 && !it.isX }
                val nineCount = parsedScores.count { it.score == 9 }
                val missCount = parsedScores.count { it.isMiss }
                val confirmedEnds = ends.count { it.confirmedAt != null }

                Text("Sumatoria de la serie", style = MaterialTheme.typography.titleMedium)
            }

            item {
                val parsedScores = ends.flatMap { parseScores(it.scoresText.orEmpty()) }
                val totalArrows = parsedScores.size
                val totalScore = parsedScores.sumOf { it.score }
                val average = if (totalArrows == 0) 0.0 else totalScore.toDouble() / totalArrows
                val xCount = parsedScores.count { it.isX }
                val tenCount = parsedScores.count { it.score == 10 && !it.isX }
                val nineCount = parsedScores.count { it.score == 9 }
                val missCount = parsedScores.count { it.isMiss }
                val confirmedEnds = ends.count { it.confirmedAt != null }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Serie: ${series.distanceMeters}m - ${series.category}")
                            Text("Tandas: $confirmedEnds/${series.endsCount}")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total flechas: $totalArrows")
                            Text("Puntuación: $totalScore")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Promedio: ${"%.2f".format(average)}")
                            Text("X: $xCount")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Text("10: $tenCount")
                            Text("9: $nineCount")
                            Text("M: $missCount")
                        }
                    }
                }
            }

            item {
                Text("Distribucion por color", style = MaterialTheme.typography.titleMedium)
            }

            item {
                val parsedScores = ends.flatMap { parseScores(it.scoresText.orEmpty()) }
                ScorePieChart(
                    slices = buildColorSlices(parsedScores),
                    total = parsedScores.size
                )
            }
        }
    }
}

@Composable
private fun GeneralStatsTab(
    detail: TrainingWithSeries,
    onSwitchToSeries: (Int) -> Unit = {}
) {
    var swipeOffsetX by remember { mutableStateOf(0f) }
    var swipeOffsetY by remember { mutableStateOf(0f) }
    var isHorizontalSwipe by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        swipeOffsetX = 0f
                        swipeOffsetY = 0f
                        isHorizontalSwipe = false
                    },
                    onDragEnd = {
                        // Si hay swipe hacia la derecha, ir a la primera serie
                        if (isHorizontalSwipe && swipeOffsetX > 200f) {
                            Log.d("ArcheryScore_Debug", "Detectado swipe hacia la derecha desde Estadísticas, yendo a Serie 1")
                            onSwitchToSeries(0)
                        }
                        swipeOffsetX = 0f
                        swipeOffsetY = 0f
                        isHorizontalSwipe = false
                    },
                    onDrag = { change, dragAmount ->
                        swipeOffsetX += dragAmount.x
                        swipeOffsetY += abs(dragAmount.y)
                        if (abs(swipeOffsetX) > 30f || swipeOffsetY > 30f) {
                            if (abs(swipeOffsetX) > swipeOffsetY * 1.5f) {
                                isHorizontalSwipe = true
                                change.consume()
                            }
                        }
                    }
                )
            }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Estadísticas por serie", style = MaterialTheme.typography.titleMedium)
            }

            // Estadísticas de cada serie
            items(detail.series) { seriesWithEnds ->
                val series = seriesWithEnds.series
                val ends = seriesWithEnds.ends
                val parsedScores = ends.flatMap { parseScores(it.scoresText.orEmpty()) }
                val totalArrows = parsedScores.size
                val totalScore = parsedScores.sumOf { it.score }
                val average = if (totalArrows == 0) 0.0 else totalScore.toDouble() / totalArrows
                val xCount = parsedScores.count { it.isX }
                val tenCount = parsedScores.count { it.score == 10 && !it.isX }
                val nineCount = parsedScores.count { it.score == 9 }
                val missCount = parsedScores.count { it.isMiss }
                val confirmedEnds = ends.count { it.confirmedAt != null }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSwitchToSeries(detail.series.indexOf(seriesWithEnds)) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Serie ${series.seriesNumber} - ${series.distanceMeters}m", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${series.category}", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${series.targetType}", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tandas: $confirmedEnds/${series.endsCount}")
                            Text("Flechas: $totalArrows")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total: $totalScore")
                            Text("Promedio: ${"%.2f".format(average)}")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Text("X: $xCount")
                            Text("10: $tenCount")
                            Text("9: $nineCount")
                            Text("M: $missCount")
                        }
                    }
                }
            }

            // Separador
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Estadísticas generales totales
            item {
                Text("Estadísticas generales", style = MaterialTheme.typography.titleMedium)
            }

            item {
                val allEnds = detail.series.flatMap { it.ends }
                val parsedScores = allEnds.flatMap { parseScores(it.scoresText.orEmpty()) }
                val totalArrows = parsedScores.size
                val totalScore = parsedScores.sumOf { it.score }
                val average = if (totalArrows == 0) 0.0 else totalScore.toDouble() / totalArrows
                val xCount = parsedScores.count { it.isX }
                val tenCount = parsedScores.count { it.score == 10 && !it.isX }
                val nineCount = parsedScores.count { it.score == 9 }
                val missCount = parsedScores.count { it.isMiss }
                val totalConfirmedEnds = allEnds.count { it.confirmedAt != null }
                val totalEndsRequired = detail.series.sumOf { it.series.endsCount }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Series: ${detail.series.size}")
                            Text("Tandas: $totalConfirmedEnds/$totalEndsRequired")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total flechas: $totalArrows")
                            Text("Puntuación: $totalScore")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Promedio general: ${"%.2f".format(average)}")
                            Text("X: $xCount")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Text("10: $tenCount")
                            Text("9: $nineCount")
                            Text("M: $missCount")
                        }
                    }
                }
            }

            item {
                Text("Distribucion por color", style = MaterialTheme.typography.titleMedium)
            }

            item {
                val parsedScores = detail.series.flatMap { series ->
                    series.ends.flatMap { parseScores(it.scoresText.orEmpty()) }
                }
                ScorePieChart(
                    slices = buildColorSlices(parsedScores),
                    total = parsedScores.size
                )
            }
        }
    }
}

@Composable
private fun TrainingStatsTab(
    detail: TrainingWithSeries,
    onSwitchToPlanilla: () -> Unit = {}
) {
    var swipeOffsetX by remember { mutableStateOf(0f) }
    var swipeOffsetY by remember { mutableStateOf(0f) }
    var isHorizontalSwipe by remember { mutableStateOf(false) }

    // Obtener todos los ends de todas las series
    val allEnds = remember(detail) {
        detail.series.flatMap { it.ends }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        swipeOffsetX = 0f
                        swipeOffsetY = 0f
                        isHorizontalSwipe = false
                    },
                    onDragEnd = {
                        // Si el swipe fue hacia la derecha, suficientemente largo y más horizontal que vertical
                        if (isHorizontalSwipe && swipeOffsetX > 200f) {
                            Log.d("ArcheryScore_Debug", "Detectado swipe horizontal hacia la derecha en Estadísticas (x: $swipeOffsetX, y: $swipeOffsetY), volviendo a Planilla")
                            onSwitchToPlanilla()
                        }
                        swipeOffsetX = 0f
                        swipeOffsetY = 0f
                        isHorizontalSwipe = false
                    },
                    onDrag = { change, dragAmount ->
                        swipeOffsetX += dragAmount.x
                        swipeOffsetY += abs(dragAmount.y)
                        
                        // Determinar si es un swipe horizontal después de los primeros movimientos
                        if (abs(swipeOffsetX) > 30f || swipeOffsetY > 30f) {
                            if (abs(swipeOffsetX) > swipeOffsetY * 1.5f) {
                                isHorizontalSwipe = true
                                change.consume()
                            }
                        }
                    }
                )
            }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Si hay múltiples series, mostrar estadísticas por serie
            if (detail.series.size > 1) {
                detail.series.forEachIndexed { seriesIndex, seriesWithEnds ->
                    item {
                        Text(
                            "Serie ${seriesIndex + 1} - ${seriesWithEnds.series.distanceMeters}m",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    item {
                        val seriesEnds = seriesWithEnds.ends
                        val parsedScores = seriesEnds.flatMap { parseScores(it.scoresText.orEmpty()) }
                        val totalArrows = parsedScores.size
                        val totalScore = parsedScores.sumOf { it.score }
                        val average = if (totalArrows == 0) 0.0 else totalScore.toDouble() / totalArrows
                        val xCount = parsedScores.count { it.isX }
                        val tenCount = parsedScores.count { it.score == 10 && !it.isX }
                        val nineCount = parsedScores.count { it.score == 9 }
                        val missCount = parsedScores.count { it.isMiss }
                        val confirmedEnds = seriesEnds.count { it.confirmedAt != null }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tandas: $confirmedEnds")
                                    Text("Flechas: $totalArrows")
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total: $totalScore")
                                    Text("Promedio: ${"%.2f".format(average)}")
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    Text("X: $xCount")
                                    Text("10: $tenCount")
                                    Text("9: $nineCount")
                                    Text("M: $missCount")
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    Text("Resumen general", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                // Una sola serie: mostrar estadísticas por tanda
                item {
                    Text("Por tanda", style = MaterialTheme.typography.titleMedium)
                }

                items(allEnds) { end ->
                    if (end.confirmedAt != null) {
                        val endScores = parseScores(end.scoresText.orEmpty())
                        if (endScores.isNotEmpty()) {
                            val endTotal = endScores.sumOf { it.score }
                            val endAverage = endTotal.toDouble() / endScores.size
                            val endXCount = endScores.count { it.isX }
                            val endTenCount = endScores.count { it.score == 10 && !it.isX }
                            val endNineCount = endScores.count { it.score == 9 }
                            val endMissCount = endScores.count { it.isMiss }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Tanda ${end.endNumber}", style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total: $endTotal")
                                        Text("Promedio: ${"%.2f".format(endAverage)}")
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Text("X: $endXCount")
                                        Text("10: $endTenCount")
                                        Text("9: $endNineCount")
                                        Text("M: $endMissCount")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Text("Estadísticas generales", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Estadísticas generales (para todas las series)
            item {
                val parsedScores = allEnds.flatMap { parseScores(it.scoresText.orEmpty()) }
                val totalArrows = parsedScores.size
                val totalScore = parsedScores.sumOf { it.score }
                val average = if (totalArrows == 0) 0.0 else totalScore.toDouble() / totalArrows
                val xCount = parsedScores.count { it.isX }
                val tenCount = parsedScores.count { it.score == 10 && !it.isX }
                val nineCount = parsedScores.count { it.score == 9 }
                val missCount = parsedScores.count { it.isMiss }
                val confirmedEnds = allEnds.count { it.confirmedAt != null }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tandas completadas: $confirmedEnds")
                            Text("Total flechas: $totalArrows")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Puntuación total: $totalScore")
                            Text("Promedio: ${"%.2f".format(average)}")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Text("X: $xCount")
                            Text("10: $tenCount")
                            Text("9: $nineCount")
                            Text("M: $missCount")
                        }
                    }
                }
            }
        } // Cierre del LazyColumn
    } // Cierre del Box
}

private data class ParsedScore(
    val score: Int,
    val isX: Boolean,
    val isMiss: Boolean
)

private data class ScoreSlice(
    val label: String,
    val color: Color,
    val count: Int
)

private fun parseScores(input: String): List<ParsedScore> {
    if (input.isBlank()) return emptyList()
    return input
        .split(",", " ", ";")
        .mapNotNull { token ->
            val value = token.trim().uppercase()
            when {
                value == "X" -> ParsedScore(score = 10, isX = true, isMiss = false)
                value == "M" -> ParsedScore(score = 0, isX = false, isMiss = true)
                value.toIntOrNull() != null -> {
                    val score = value.toInt()
                    ParsedScore(score = score, isX = false, isMiss = score == 0)
                }
                else -> null
            }
        }
}

@Composable
private fun ScorePieChart(
    slices: List<ScoreSlice>,
    total: Int,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var chartSize by remember { mutableStateOf(IntSize.Zero) }

    if (total == 0 || slices.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Sin datos")
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .size(180.dp)
                .pointerInput(slices, chartSize) {
                    detectTapGestures { offset ->
                        if (chartSize.width == 0 || chartSize.height == 0) return@detectTapGestures
                        selectedIndex = findSliceIndex(offset, chartSize, slices, total)
                    }
                }
                .padding(4.dp)
        ) {
            chartSize = IntSize(size.width.roundToInt(), size.height.roundToInt())
            var startAngle = -90f
            val radius = min(size.width, size.height) / 2f

            slices.forEachIndexed { index, slice ->
                val sweep = slice.count.toFloat() / total * 360f
                val isSelected = index == selectedIndex
                val sliceRadius = if (isSelected) radius * 1.05f else radius
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    size = Size(sliceRadius * 2f, sliceRadius * 2f),
                    topLeft = Offset(
                        (size.width - sliceRadius * 2f) / 2f,
                        (size.height - sliceRadius * 2f) / 2f
                    )
                )
                startAngle += sweep
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            slices.forEach { slice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(slice.color, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${slice.label}: ${formatPercent(slice.count, total)} (${slice.count})")
                }
            }
        }

        if (selectedIndex in slices.indices) {
            val selected = slices[selectedIndex]
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${selected.label}: ${formatPercent(selected.count, total)} (${selected.count})",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun findSliceIndex(
    offset: Offset,
    size: IntSize,
    slices: List<ScoreSlice>,
    total: Int
): Int {
    val center = Offset(size.width / 2f, size.height / 2f)
    val dx = offset.x - center.x
    val dy = offset.y - center.y
    val distance = sqrt(dx * dx + dy * dy)
    val radius = min(size.width, size.height) / 2f
    if (distance > radius) return -1

    val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    val normalized = (angle + 450f) % 360f

    var start = 0f
    slices.forEachIndexed { index, slice ->
        val sweep = slice.count.toFloat() / total * 360f
        val end = start + sweep
        if (normalized in start..end) return index
        start = end
    }
    return -1
}

private fun buildColorSlices(scores: List<ParsedScore>): List<ScoreSlice> {
    val counts = mutableMapOf(
        "Amarillo" to 0,
        "Rojo" to 0,
        "Azul" to 0,
        "Negro" to 0,
        "Blanco" to 0
    )

    scores.forEach { score ->
        val bucket = scoreToBucket(score)
        counts[bucket] = (counts[bucket] ?: 0) + 1
    }

    return listOf(
        ScoreSlice("Amarillo", getScoreColor("10"), counts["Amarillo"] ?: 0),
        ScoreSlice("Rojo", getScoreColor("8"), counts["Rojo"] ?: 0),
        ScoreSlice("Azul", getScoreColor("6"), counts["Azul"] ?: 0),
        ScoreSlice("Negro", getScoreColor("4"), counts["Negro"] ?: 0),
        ScoreSlice("Blanco", getScoreColor("2"), counts["Blanco"] ?: 0)
    ).filter { it.count > 0 }
}

private fun scoreToBucket(score: ParsedScore): String {
    return when {
        score.isX || score.score >= 9 -> "Amarillo"
        score.score in 7..8 -> "Rojo"
        score.score in 5..6 -> "Azul"
        score.score in 3..4 || score.score == 0 || score.isMiss -> "Negro"
        else -> "Blanco"
    }
}

private fun formatPercent(count: Int, total: Int): String {
    if (total == 0) return "0%"
    val percent = count.toDouble() / total * 100.0
    return "%.1f%%".format(percent)
}

private fun sortScores(scores: List<ParsedScore>): List<ParsedScore> {
    return scores.sortedWith(compareBy { orderKey(it) })
}

private fun orderKey(score: ParsedScore): Int {
    return when {
        score.isX -> 0
        score.isMiss -> 12
        else -> 11 - score.score
    }
}

private fun formatScores(scores: List<ParsedScore>): String {
    return scores.joinToString(", ") { score ->
        when {
            score.isX -> "X"
            score.isMiss -> "M"
            else -> score.score.toString()
        }
    }
}

private fun normalizeScoresText(input: String?): String {
    if (input.isNullOrBlank()) return ""
    val parsed = parseScores(input)
    if (parsed.isEmpty()) return input
    return formatScores(sortScores(parsed))
}

private fun getValidScores(zoneCount: Int, puntajeSystem: String): List<String> {
    val scores = when {
        zoneCount == 6 && puntajeSystem == "X_TO_M" -> listOf("X", "10", "9", "8", "7", "6", "5", "M")
        zoneCount == 6 && puntajeSystem == "11_TO_M" -> listOf("11", "10", "9", "8", "7", "6", "5", "M")
        zoneCount == 10 && puntajeSystem == "X_TO_M" -> listOf("X", "10", "9", "8", "7", "6", "5", "4", "3", "2", "1", "M")
        zoneCount == 10 && puntajeSystem == "11_TO_M" -> listOf("11", "10", "9", "8", "7", "6", "5", "4", "3", "2", "1", "M")
        else -> emptyList()
    }
    return scores
}

private fun validateScoreInput(input: String, zoneCount: Int, puntajeSystem: String): Pair<Boolean, String?> {
    if (input.isBlank()) return Pair(false, "Ingresa los puntajes")
    
    val validScores = getValidScores(zoneCount, puntajeSystem)
    val parsed = parseScores(input)
    
    // Validar sin considerar case
    val invalidScores = mutableListOf<String>()
    input.split(",", " ", ";")
        .map { it.trim().uppercase() }
        .filter { it.isNotEmpty() }
        .forEach { token ->
            if (!validScores.map { it.uppercase() }.contains(token)) {
                invalidScores.add(token)
            }
        }
    
    if (invalidScores.isNotEmpty()) {
        val forbidden = when {
            puntajeSystem == "X_TO_M" && invalidScores.contains("11") -> "No se pueden usar puntajes 11 en sistema X a M"
            puntajeSystem == "11_TO_M" && invalidScores.contains("X") -> "No se pueden usar X en sistema 11 a M"
            zoneCount == 6 && invalidScores.any { !it.matches(Regex("^[0-9]+$|^[XM]|^11$")) } -> "Puntajes inválidos para 6 zonas"
            else -> "Puntajes inválidos: ${invalidScores.take(3).joinToString(", ")}"
        }
        return Pair(false, forbidden)
    }
    
    return Pair(true, null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTrainingWithSeriesDialog(
    isLoggedIn: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String?, List<SeriesFormData>, String, Boolean) -> Unit,
    onFetchWeather: suspend (Double, Double) -> WeatherSnapshot?
) {
    val context = LocalContext.current

    var archerName by remember { mutableStateOf("") }
    var trainingType by remember { mutableStateOf(TrainingType.TRAINING) }
    var isGroup by remember { mutableStateOf(false) }
    var showTrainingTypeMenu by remember { mutableStateOf(false) }
    var seriesList by remember {
        mutableStateOf(listOf(
            SeriesFormState(
                distanceText = "",
                category = "",
                targetType = "",
                arrowsPerEndText = "6",
                endsCountText = "6",
                targetZoneCount = "10",
                puntajeSystem = "X_TO_M",
                temperatureText = "",
                windSpeedText = "",
                windDirectionText = "",
                windUnit = "",
                skyCondition = ""
            )
        ))
    }

    var validationError by remember { mutableStateOf<String?>(null) }

    val permissionGranted = remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted.value = granted
    }

    var locationLat by remember { mutableStateOf<Double?>(null) }
    var locationLon by remember { mutableStateOf<Double?>(null) }

    fun validateAndCreate() {
        if (!isLoggedIn && archerName.isBlank()) {
            validationError = "Nombre obligatorio"
            return
        }

        if (seriesList.isEmpty()) {
            validationError = "Debe agregar al menos una serie"
            return
        }

        val seriesDataList = mutableListOf<SeriesFormData>()

        seriesList.forEachIndexed { index, series ->
            val distance = series.distanceText.toIntOrNull()
            val arrowsPerEnd = series.arrowsPerEndText.toIntOrNull()
            val endsCount = series.endsCountText.toIntOrNull()
            val temperature = series.temperatureText.toDoubleOrNull()
            val windSpeed = series.windSpeedText.toDoubleOrNull()
            val windDirection = series.windDirectionText.toIntOrNull()

            if (distance == null || distance <= 0) {
                validationError = "Serie ${index + 1}: Distancia invalida"
                return
            }
            if (series.category.isBlank()) {
                validationError = "Serie ${index + 1}: Categoria obligatoria"
                return
            }
            if (series.targetType.isBlank()) {
                validationError = "Serie ${index + 1}: Tipo de blanco obligatorio"
                return
            }
            if (arrowsPerEnd == null || arrowsPerEnd <= 0) {
                validationError = "Serie ${index + 1}: Flechas por tanda invalido"
                return
            }
            if (endsCount == null || endsCount <= 0) {
                validationError = "Serie ${index + 1}: Cantidad de tandas invalida"
                return
            }
            if (windSpeed == null || windDirection == null || series.skyCondition.isBlank()) {
                validationError = "Serie ${index + 1}: Completa los datos de clima"
                return
            }

            val weather = WeatherSnapshot(
                temperature = temperature,
                windSpeed = windSpeed,
                windSpeedUnit = series.windUnit.ifBlank { null },
                windDirectionDegrees = windDirection,
                skyCondition = series.skyCondition
            )

            val weatherSource = "manual" // TODO: implement auto-fetch per series

            seriesDataList.add(
                SeriesFormData(
                    distanceMeters = distance,
                    category = series.category,
                    targetType = series.targetType,
                    arrowsPerEnd = arrowsPerEnd,
                    endsCount = endsCount,
                    targetZoneCount = series.targetZoneCount.toIntOrNull() ?: 10,
                    puntajeSystem = series.puntajeSystem,
                    temperature = temperature,
                    weather = weather,
                    locationLat = locationLat,
                    locationLon = locationLon,
                    weatherSource = weatherSource
                )
            )
        }

        onCreate(if (isLoggedIn) null else archerName, seriesDataList, trainingType, isGroup)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { validateAndCreate() }) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Nuevo entrenamiento") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 500.dp)
            ) {
                if (!isLoggedIn) {
                    item {
                        OutlinedTextField(
                            value = archerName,
                            onValueChange = { archerName = it },
                            label = { Text("Nombre del arquero") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = showTrainingTypeMenu,
                        onExpandedChange = { showTrainingTypeMenu = !showTrainingTypeMenu }
                    ) {
                        OutlinedTextField(
                            value = if (trainingType == TrainingType.TOURNAMENT) "Torneo" else "Entrenamiento",
                            onValueChange = { },
                            label = { Text("Tipo de registro") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTrainingTypeMenu) }
                        )
                        ExposedDropdownMenu(
                            expanded = showTrainingTypeMenu,
                            onDismissRequest = { showTrainingTypeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Entrenamiento") },
                                onClick = {
                                    trainingType = TrainingType.TRAINING
                                    showTrainingTypeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Torneo") },
                                onClick = {
                                    trainingType = TrainingType.TOURNAMENT
                                    showTrainingTypeMenu = false
                                }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isGroup,
                            onClick = { isGroup = false },
                            label = { Text("Individual") }
                        )
                        FilterChip(
                            selected = isGroup,
                            onClick = { isGroup = true },
                            label = { Text("Grupal") }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Series", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = {
                            seriesList = seriesList + SeriesFormState(
                                distanceText = "",
                                category = "",
                                targetType = "",
                                arrowsPerEndText = "6",
                                endsCountText = "6",
                                targetZoneCount = "10",
                                puntajeSystem = "X_TO_M",
                                temperatureText = "",
                                windSpeedText = "",
                                windDirectionText = "",
                                windUnit = "",
                                skyCondition = ""
                            )
                        }) {
                            Text("+ Agregar serie")
                        }
                    }
                }

                seriesList.forEachIndexed { index, series ->
                    item {
                        SeriesFormSection(
                            seriesNumber = index+ 1,
                            series = series,
                            onUpdate = { updatedSeries ->
                                seriesList = seriesList.toMutableList().apply {
                                    set(index, updatedSeries)
                                }
                            },
                            onRemove = if (seriesList.size > 1) {
                                {
                                    seriesList = seriesList.toMutableList().apply {
                                        removeAt(index)
                                    }
                                }
                            } else null,
                            permissionGranted = permissionGranted.value,
                            onRequestLocation = {
                                if (permissionGranted.value) {
                                    permissionGranted.value = true
                                } else {
                                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            },
                            onFetchWeather = onFetchWeather,
                            onLocationFetched = { lat, lon ->
                                locationLat = lat
                                locationLon = lon
                            }
                        )
                    }
                }

                if (validationError != null) {
                    item {
                        Text(
                            text = validationError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    )
}

private data class SeriesFormState(
    var distanceText: String,
    var category: String,
    var targetType: String,
    var arrowsPerEndText: String,
    var endsCountText: String,
    var targetZoneCount: String,
    var puntajeSystem: String,
    var temperatureText: String,
    var windSpeedText: String,
    var windDirectionText: String,
    var windUnit: String,
    var skyCondition: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeriesFormSection(
    seriesNumber: Int,
    series: SeriesFormState,
    onUpdate: (SeriesFormState) -> Unit,
    onRemove: (() -> Unit)?,
    permissionGranted: Boolean,
    onRequestLocation: () -> Unit,
    onFetchWeather: suspend (Double, Double) -> WeatherSnapshot?,
    onLocationFetched: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showTargetMenu by remember { mutableStateOf(false) }
    var showWindUnitMenu by remember { mutableStateOf(false) }
    var showSkyMenu by remember { mutableStateOf(false) }
    var showZoneCountMenu by remember { mutableStateOf(false) }
    var showPuntajeSystemMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Serie $seriesNumber", style = MaterialTheme.typography.titleSmall)
                if (onRemove != null) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, "Eliminar serie", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            OutlinedTextField(
                value = series.distanceText,
                onValueChange = { onUpdate(series.copy(distanceText = it)) },
                label = { Text("Distancia (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = !showCategoryMenu }
            ) {
                OutlinedTextField(
                    value = series.category,
                    onValueChange = { onUpdate(series.copy(category = it)) },
                    label = { Text("Categoria") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) }
                )
                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    categoryOptions().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onUpdate(series.copy(category = option))
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = showTargetMenu,
                onExpandedChange = { showTargetMenu = !showTargetMenu }
            ) {
                OutlinedTextField(
                    value = series.targetType,
                    onValueChange = { onUpdate(series.copy(targetType = it)) },
                    label = { Text("Tipo de blanco") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTargetMenu) }
                )
                ExposedDropdownMenu(
                    expanded = showTargetMenu,
                    onDismissRequest = { showTargetMenu = false }
                ) {
                    targetOptions().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onUpdate(series.copy(targetType = option))
                                showTargetMenu = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = showZoneCountMenu,
                onExpandedChange = { showZoneCountMenu = !showZoneCountMenu }
            ) {
                OutlinedTextField(
                    value = series.targetZoneCount,
                    onValueChange = { onUpdate(series.copy(targetZoneCount = it)) },
                    label = { Text("Zonas del blanco") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showZoneCountMenu) }
                )
                ExposedDropdownMenu(
                    expanded = showZoneCountMenu,
                    onDismissRequest = { showZoneCountMenu = false }
                ) {
                    listOf("6", "10").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onUpdate(series.copy(targetZoneCount = option))
                                showZoneCountMenu = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = showPuntajeSystemMenu,
                onExpandedChange = { showPuntajeSystemMenu = !showPuntajeSystemMenu }
            ) {
                OutlinedTextField(
                    value = if (series.puntajeSystem == "X_TO_M") "X a M" else "11 a M",
                    onValueChange = { },
                    label = { Text("Sistema de puntaje") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPuntajeSystemMenu) }
                )
                ExposedDropdownMenu(
                    expanded = showPuntajeSystemMenu,
                    onDismissRequest = { showPuntajeSystemMenu = false }
                ) {
                    listOf(
                        Pair("X_TO_M", "X a M"),
                        Pair("11_TO_M", "11 a M")
                    ).forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onUpdate(series.copy(puntajeSystem = value))
                                showPuntajeSystemMenu = false
                            }
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = series.arrowsPerEndText,
                    onValueChange = { onUpdate(series.copy(arrowsPerEndText = it)) },
                    label = { Text("Flechas/tanda") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = series.endsCountText,
                    onValueChange = { onUpdate(series.copy(endsCountText = it)) },
                    label = { Text("Tandas") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = series.temperatureText,
                onValueChange = { onUpdate(series.copy(temperatureText = it)) },
                label = { Text("Temperatura (°C)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Clima", style = MaterialTheme.typography.labelMedium)
                TextButton(onClick = {
                    scope.launch {
                        onRequestLocation()
                        if (permissionGranted) {
                            val location = getCurrentLocation(context)
                            if (location != null) {
                                onLocationFetched(location.latitude, location.longitude)
                                val weather = onFetchWeather(location.latitude, location.longitude)
                                if (weather != null) {
                                    onUpdate(series.copy(
                                        temperatureText = weather.temperature?.toString().orEmpty(),
                                        windSpeedText = weather.windSpeed?.toString().orEmpty(),
                                        windDirectionText = weather.windDirectionDegrees?.toString().orEmpty(),
                                        windUnit = weather.windSpeedUnit.orEmpty(),
                                        skyCondition = weather.skyCondition.orEmpty()
                                    ))
                                }
                            }
                        }
                    }
                }) {
                    Text(if (permissionGranted) "Actualizar" else "Usar ubicacion")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = series.windSpeedText,
                    onValueChange = { onUpdate(series.copy(windSpeedText = it)) },
                    label = { Text("Viento") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                ExposedDropdownMenuBox(
                    expanded = showWindUnitMenu,
                    onExpandedChange = { showWindUnitMenu = !showWindUnitMenu },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = series.windUnit,
                        onValueChange = { onUpdate(series.copy(windUnit = it)) },
                        label = { Text("Unidad") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showWindUnitMenu) }
                    )
                    ExposedDropdownMenu(
                        expanded = showWindUnitMenu,
                        onDismissRequest = { showWindUnitMenu = false }
                    ) {
                        windUnitOptions().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onUpdate(series.copy(windUnit = option))
                                    showWindUnitMenu = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = series.windDirectionText,
                onValueChange = { onUpdate(series.copy(windDirectionText = it)) },
                label = { Text("Direccion (grados)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = showSkyMenu,
                onExpandedChange = { showSkyMenu = !showSkyMenu }
            ) {
                OutlinedTextField(
                    value = series.skyCondition,
                    onValueChange = { onUpdate(series.copy(skyCondition = it)) },
                    label = { Text("Cielo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSkyMenu) }
                )
                ExposedDropdownMenu(
                    expanded = showSkyMenu,
                    onDismissRequest = { showSkyMenu = false }
                ) {
                    skyOptions().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onUpdate(series.copy(skyCondition = option))
                                showSkyMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun windUnitOptions(): List<String> = listOf("m/s", "km/h", "kn")

private fun skyOptions(): List<String> = listOf(
    "Soleado",
    "Parcialmente nublado",
    "Nublado",
    "Llovizna",
    "Lluvia",
    "Tormenta",
    "Nieve",
    "Niebla"
)

private fun targetOptions(): List<String> = listOf(
    "WA 122 cm",
    "WA 80 cm",
    "WA 60 cm",
    "Triple Spot",
    "3D",
    "Campo",
    "Hunter",
    "Otro"
)

private fun categoryOptions(): List<String> = listOf(
    "Compuesto",
    "Recurvo",
    "Longbow",
    "Raso",
    "Instintivo",
    "Cazador",
    "Adaptado"
)

private fun formatWindDirection(degrees: Int?): String {
    if (degrees == null) return "-"
    val dirs = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSO", "SO", "OSO", "O", "ONO", "NO", "NNO")
    val index = ((degrees % 360) / 22.5).toInt()
    val label = dirs.getOrElse(index) { "N" }
    return "$label ($degrees°)"
}
