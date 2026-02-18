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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import com.cegb03.archeryscore.data.local.training.TrainingEntity
import com.cegb03.archeryscore.data.local.training.TrainingWithEnds
import com.cegb03.archeryscore.data.model.WeatherSnapshot
import com.cegb03.archeryscore.util.getCurrentLocation
import com.cegb03.archeryscore.viewmodel.AuthViewModel
import com.cegb03.archeryscore.viewmodel.TrainingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

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
        CreateTrainingDialog(
            isLoggedIn = isLoggedIn,
            onDismiss = { showCreateDialog = false },
            onCreate = { form ->
                viewModel.createTraining(
                    archerName = form.archerName,
                    distanceMeters = form.distanceMeters,
                    category = form.category,
                    targetType = form.targetType,
                    arrowsPerEnd = form.arrowsPerEnd,
                    endsCount = form.endsCount,
                    weather = form.weather,
                    locationLat = form.locationLat,
                    locationLon = form.locationLon,
                    weatherSource = form.weatherSource,
                    targetZoneCount = form.targetZoneCount,
                    puntajeSystem = form.puntajeSystem
                )
                showCreateDialog = false
            },
            onFetchWeather = { lat, lon -> viewModel.fetchWeather(lat, lon) }
        )
    }
}

@Composable
private fun TrainingsList(
    trainings: List<TrainingEntity>,
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
                items(trainings) { training ->
                    TrainingCard(training = training, onClick = { onSelect(training.id) })
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
private fun TrainingCard(training: TrainingEntity, onClick: () -> Unit) {
    val date = remember(training.createdAt) {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.format(Date(training.createdAt))
    }

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
            Text(
                text = "Distancia: ${training.distanceMeters} m",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Categoria: ${training.category}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainingDetailScreen(
    detail: TrainingWithEnds?,
    onBack: () -> Unit,
    onConfirmEnd: (Long, String, Int) -> Unit
) {
    Log.d("ArcheryScore_Debug", "TrainingDetailScreen called - detail: ${detail != null}, training: ${detail?.training != null}, ends: ${detail?.ends?.size ?: 0}")
    val training = detail?.training
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var targetSheetPage by rememberSaveable { mutableIntStateOf(-1) } // Página destino para la planilla

    var showInfoMenu by remember { mutableStateOf(false) }

    // Manejar el back button del sistema en la planilla
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
                            if (training != null) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .width(300.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Información del entrenamiento", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Distancia: ${training.distanceMeters} m", style = MaterialTheme.typography.labelSmall)
                                    Text("Categoría: ${training.category}", style = MaterialTheme.typography.labelSmall)
                                    Text("Blanco: ${training.targetType}", style = MaterialTheme.typography.labelSmall)
                                    Text("Flechas por tanda: ${training.arrowsPerEnd}", style = MaterialTheme.typography.labelSmall)
                                    Text("Tandas: ${training.endsCount}", style = MaterialTheme.typography.labelSmall)
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Sistema: ${if (training.puntajeSystem == "X_TO_M") "X a M" else "11 a M"} (${training.targetZoneCount} zonas)", style = MaterialTheme.typography.labelSmall)
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Clima:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text("Viento: ${training.windSpeed ?: "-"} ${training.windSpeedUnit ?: ""}", style = MaterialTheme.typography.labelSmall)
                                    Text("Dirección: ${formatWindDirection(training.windDirectionDegrees)}", style = MaterialTheme.typography.labelSmall)
                                    Text("Cielo: ${training.skyCondition ?: "-"}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (training == null || detail == null) {
            Log.w("ArcheryScore_Debug", "Training or detail is null")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Cargando...")
            }
            return@Scaffold
        }

        // Validar que detail.ends no esté vacío
        if (detail.ends.isEmpty()) {
            Log.e("ArcheryScore_Debug", "detail.ends is empty for training ID: ${training.id}")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: No hay tandas. Por favor, crea un nuevo entrenamiento.")
            }
            return@Scaffold
        }
        
        Log.d("ArcheryScore_Debug", "Rendering tabs for training ${training.id} with ${detail.ends.size} ends")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Planilla") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Estadisticas") }
                )
            }

            when (selectedTabIndex) {
                0 -> {
                    Log.d("ArcheryScore_Debug", "Rendering TrainingSheetTab")
                    TrainingSheetTab(
                        training = training,
                        detail = detail,
                        targetZoneCount = training.targetZoneCount,
                        puntajeSystem = training.puntajeSystem,
                        onConfirmEnd = onConfirmEnd,
                        onSwitchToStats = { selectedTabIndex = 1 },
                        targetPage = targetSheetPage,
                        onPageChanged = { targetSheetPage = -1 } // Resetear después de usarlo
                    )
                }
                1 -> {
                    Log.d("ArcheryScore_Debug", "Rendering TrainingStatsTab")
                    TrainingStatsTab(
                        detail = detail,
                        onSwitchToPlanilla = { 
                            targetSheetPage = detail.ends.size - 1 // Ir a la última tanda
                            selectedTabIndex = 0 
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrainingSheetTab(
    training: TrainingEntity,
    detail: TrainingWithEnds,
    targetZoneCount: Int,
    puntajeSystem: String,
    onConfirmEnd: (Long, String, Int) -> Unit,
    onSwitchToStats: () -> Unit = {},
    targetPage: Int = -1,
    onPageChanged: () -> Unit = {}
) {
    Log.d("ArcheryScore_Debug", "TrainingSheetTab - training: ${training.id}, ends: ${detail.ends.size}, zones: $targetZoneCount, system: $puntajeSystem")
    
    // Validar que detail.ends no esté vacío
    if (detail.ends.isEmpty()) {
        Log.e("ArcheryScore_Debug", "detail.ends is empty!")
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay tandas disponibles")
        }
        return
    }

    // Asegurar valores válidos
    val safeTargetZoneCount = if (targetZoneCount in listOf(6, 10)) targetZoneCount else 10
    val safePuntajeSystem = if (puntajeSystem in listOf("X_TO_M", "11_TO_M")) puntajeSystem else "X_TO_M"
    
    Log.d("ArcheryScore_Debug", "Safe values - zones: $safeTargetZoneCount, system: $safePuntajeSystem")
    
    // Usar el número actual de tandas para pageCount
    val totalEnds = detail.ends.size
    Log.d("ArcheryScore_Debug", "Creating pagerState with totalEnds: $totalEnds")
    val pagerState = rememberPagerState(
        pageCount = { totalEnds },
        initialPage = if (targetPage >= 0 && targetPage < totalEnds) targetPage else 0
    )

    // Estado para controlar el swipe
    var hasTriggeredSwitch by remember { mutableStateOf(false) }

    // Navegar a targetPage si está configurado
    LaunchedEffect(targetPage) {
        if (targetPage >= 0 && targetPage < totalEnds) {
            pagerState.scrollToPage(targetPage)
            onPageChanged()
            hasTriggeredSwitch = false // Resetear al cambiar de página
        }
    }

    // Resetear el flag cuando cambiamos de página
    LaunchedEffect(pagerState.settledPage) {
        hasTriggeredSwitch = false
    }

    // Detectar intento de swipe más allá de la última página
    LaunchedEffect(pagerState) {
        snapshotFlow { 
            Triple(
                pagerState.settledPage, 
                pagerState.currentPageOffsetFraction,
                pagerState.isScrollInProgress
            ) 
        }.collect { (settledPage, offsetFraction, isScrolling) ->
            // Solo detectar si estamos ASENTADOS en la última página e intentamos ir más allá
            if (!hasTriggeredSwitch &&
                settledPage == totalEnds - 1 && 
                isScrolling &&
                offsetFraction < -0.5f) {
                
                Log.d("ArcheryScore_Debug", "Detectado swipe al final (settledPage: $settledPage, offset: $offsetFraction), cambiando a Estadísticas")
                hasTriggeredSwitch = true
                onSwitchToStats()
            }
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
            Log.d("ArcheryScore_Debug", "Rendering page $pageIndex of ${detail.ends.size}")
            val end = detail.ends.getOrNull(pageIndex)
            
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
            val validScores = remember(safeTargetZoneCount, safePuntajeSystem) {
                getValidScores(safeTargetZoneCount, safePuntajeSystem)
            }
            
            // Estado para drag & drop
            var draggedScore by remember { mutableStateOf<String?>(null) }
            var dragOffset by remember { mutableStateOf(Offset.Zero) }
            var dropAreaBounds by remember { mutableStateOf<Pair<Offset, IntSize>?>(null) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título de la tanda
                Text(
                    text = "Tanda ${end.endNumber}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isConfirmed) "✓ Confirmada" else "${existingScores.size}/${training.arrowsPerEnd} flechas",
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
                                        if (draggedScore != null && !isConfirmed && existingScores.size < training.arrowsPerEnd) {
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
                                    .background(
                                        color = if (existingScores.size >= training.arrowsPerEnd)
                                            MaterialTheme.colorScheme.surfaceVariant
                                        else
                                            MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .pointerInput(score) {
                                        detectDragGestures(
                                            onDragStart = {
                                                if (existingScores.size < training.arrowsPerEnd) {
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
                                                            if (existingScores.size < training.arrowsPerEnd) {
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
                                    .clickable(enabled = existingScores.size < training.arrowsPerEnd) {
                                        existingScores.add(score)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = score,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (existingScores.size >= training.arrowsPerEnd)
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.onPrimary
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
                    enabled = existingScores.size == training.arrowsPerEnd && !isConfirmed
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
                repeat(detail.ends.size) { index ->
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
            Text("Tanda ${pagerState.currentPage + 1} de ${detail.ends.size}")
        }
    }
}

@Composable
private fun TrainingStatsTab(
    detail: TrainingWithEnds,
    onSwitchToPlanilla: () -> Unit = {}
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
            // Estadísticas por tanda
            item {
                Text("Por tanda", style = MaterialTheme.typography.titleMedium)
            }

            items(detail.ends) { end ->
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

            // Separador visual
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Estadísticas generales
            item {
                Text("Estadísticas generales", style = MaterialTheme.typography.titleMedium)
            }

            item {
                val parsedScores = detail.ends.flatMap { parseScores(it.scoresText.orEmpty()) }
                val totalArrows = parsedScores.size
                val totalScore = parsedScores.sumOf { it.score }
                val average = if (totalArrows == 0) 0.0 else totalScore.toDouble() / totalArrows
                val xCount = parsedScores.count { it.isX }
                val tenCount = parsedScores.count { it.score == 10 && !it.isX }
                val nineCount = parsedScores.count { it.score == 9 }
                val missCount = parsedScores.count { it.isMiss }
                val confirmedEnds = detail.ends.count { it.confirmedAt != null }

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

private data class TrainingFormData(
    val archerName: String?,
    val distanceMeters: Int,
    val category: String,
    val targetType: String,
    val arrowsPerEnd: Int,
    val endsCount: Int,
    val weather: WeatherSnapshot?,
    val locationLat: Double?,
    val locationLon: Double?,
    val weatherSource: String?,
    val targetZoneCount: Int = 10,
    val puntajeSystem: String = "X_TO_M"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTrainingDialog(
    isLoggedIn: Boolean,
    onDismiss: () -> Unit,
    onCreate: (TrainingFormData) -> Unit,
    onFetchWeather: suspend (Double, Double) -> WeatherSnapshot?
) {
    val context = LocalContext.current

    var archerName by remember { mutableStateOf("") }
    var distanceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf("") }
    var arrowsPerEndText by remember { mutableStateOf("6") }
    var endsCountText by remember { mutableStateOf("6") }

    var windSpeedText by remember { mutableStateOf("") }
    var windDirectionText by remember { mutableStateOf("") }
    var windUnit by remember { mutableStateOf("") }
    var skyCondition by remember { mutableStateOf("") }

    var targetZoneCount by remember { mutableStateOf("10") }
    var puntajeSystem by remember { mutableStateOf("X_TO_M") }

    var weatherSnapshot by remember { mutableStateOf<WeatherSnapshot?>(null) }
    var locationLat by remember { mutableStateOf<Double?>(null) }
    var locationLon by remember { mutableStateOf<Double?>(null) }

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showTargetMenu by remember { mutableStateOf(false) }
    var showWindUnitMenu by remember { mutableStateOf(false) }
    var showSkyMenu by remember { mutableStateOf(false) }
    var showZoneCountMenu by remember { mutableStateOf(false) }
    var showPuntajeSystemMenu by remember { mutableStateOf(false) }

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

    LaunchedEffect(permissionGranted.value) {
        if (permissionGranted.value) {
            val location = getCurrentLocation(context)
            if (location != null) {
                locationLat = location.latitude
                locationLon = location.longitude
                weatherSnapshot = onFetchWeather(location.latitude, location.longitude)
                if (weatherSnapshot != null) {
                    windSpeedText = weatherSnapshot?.windSpeed?.toString().orEmpty()
                    windDirectionText = weatherSnapshot?.windDirectionDegrees?.toString().orEmpty()
                    windUnit = weatherSnapshot?.windSpeedUnit.orEmpty()
                    skyCondition = weatherSnapshot?.skyCondition.orEmpty()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val distance = distanceText.toIntOrNull()
                val arrowsPerEnd = arrowsPerEndText.toIntOrNull()
                val endsCount = endsCountText.toIntOrNull()

                if (!isLoggedIn && archerName.isBlank()) {
                    validationError = "Nombre obligatorio"
                    return@TextButton
                }
                if (distance == null || distance <= 0) {
                    validationError = "Distancia invalida"
                    return@TextButton
                }
                if (category.isBlank()) {
                    validationError = "Categoria obligatoria"
                    return@TextButton
                }
                if (targetType.isBlank()) {
                    validationError = "Tipo de blanco obligatorio"
                    return@TextButton
                }
                if (arrowsPerEnd == null || arrowsPerEnd <= 0) {
                    validationError = "Flechas por tanda invalido"
                    return@TextButton
                }
                if (endsCount == null || endsCount <= 0) {
                    validationError = "Cantidad de tandas invalida"
                    return@TextButton
                }

                val windSpeed = windSpeedText.toDoubleOrNull()
                val windDirection = windDirectionText.toIntOrNull()
                if (windSpeed == null || windDirection == null || skyCondition.isBlank()) {
                    validationError = "Completa los datos de clima"
                    return@TextButton
                }

                val weather = WeatherSnapshot(
                    windSpeed = windSpeed,
                    windSpeedUnit = windUnit.ifBlank { null },
                    windDirectionDegrees = windDirection,
                    skyCondition = skyCondition
                )

                val weatherSource = if (weatherSnapshot != null) "forecast" else "manual"

                onCreate(
                    TrainingFormData(
                        archerName = if (isLoggedIn) null else archerName,
                        distanceMeters = distance,
                        category = category,
                        targetType = targetType,
                        arrowsPerEnd = arrowsPerEnd,
                        endsCount = endsCount,
                        weather = weather,
                        locationLat = locationLat,
                        locationLon = locationLon,
                        weatherSource = weatherSource,
                        targetZoneCount = targetZoneCount.toIntOrNull() ?: 10,
                        puntajeSystem = puntajeSystem
                    )
                )
            }) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Nuevo entrenamiento") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isLoggedIn) {
                    item {
                        OutlinedTextField(
                            value = archerName,
                            onValueChange = { archerName = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = distanceText,
                        onValueChange = { distanceText = it },
                        label = { Text("Distancia (m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = showCategoryMenu,
                        onExpandedChange = { showCategoryMenu = !showCategoryMenu }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
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
                                        category = option
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = showTargetMenu,
                        onExpandedChange = { showTargetMenu = !showTargetMenu }
                    ) {
                        OutlinedTextField(
                            value = targetType,
                            onValueChange = { targetType = it },
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
                                        targetType = option
                                        showTargetMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = showZoneCountMenu,
                        onExpandedChange = { showZoneCountMenu = !showZoneCountMenu }
                    ) {
                        OutlinedTextField(
                            value = targetZoneCount,
                            onValueChange = { targetZoneCount = it },
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
                                        targetZoneCount = option
                                        showZoneCountMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = showPuntajeSystemMenu,
                        onExpandedChange = { showPuntajeSystemMenu = !showPuntajeSystemMenu }
                    ) {
                        OutlinedTextField(
                            value = if (puntajeSystem == "X_TO_M") "X a M" else "11 a M",
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
                                        puntajeSystem = value
                                        showPuntajeSystemMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = arrowsPerEndText,
                            onValueChange = { arrowsPerEndText = it },
                            label = { Text("Flechas/tanda") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endsCountText,
                            onValueChange = { endsCountText = it },
                            label = { Text("Tandas") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Clima")
                        TextButton(onClick = {
                            if (permissionGranted.value) {
                                permissionGranted.value = true
                            } else {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }) {
                            Text(if (permissionGranted.value) "Actualizar" else "Usar ubicacion")
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = windSpeedText,
                            onValueChange = { windSpeedText = it },
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
                                value = windUnit,
                                onValueChange = { windUnit = it },
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
                                            windUnit = option
                                            showWindUnitMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = windDirectionText,
                        onValueChange = { windDirectionText = it },
                        label = { Text("Direccion (grados)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = showSkyMenu,
                        onExpandedChange = { showSkyMenu = !showSkyMenu }
                    ) {
                        OutlinedTextField(
                            value = skyCondition,
                            onValueChange = { skyCondition = it },
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
                                        skyCondition = option
                                        showSkyMenu = false
                                    }
                                )
                            }
                        }
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
