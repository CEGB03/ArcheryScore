package com.cegb03.archeryscore.ui.theme.screens.trainings

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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

@Composable
fun TrainingsScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val trainings by viewModel.trainings.collectAsState()
    val trainingDetail by viewModel.trainingDetail.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

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
            },
            onAddEnd = {
                viewModel.addNewEnd()
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
                contentPadding = PaddingValues(16.dp),
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
    onConfirmEnd: (Long, String, Int) -> Unit,
    onAddEnd: () -> Unit = { }
) {
    Log.d("ArcheryScore_Debug", "TrainingDetailScreen called - detail: ${detail != null}, training: ${detail?.training != null}, ends: ${detail?.ends?.size ?: 0}")
    val training = detail?.training
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planilla") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
                        onAddEnd = onAddEnd
                    )
                }
                1 -> {
                    Log.d("ArcheryScore_Debug", "Rendering TrainingStatsTab")
                    TrainingStatsTab(detail = detail)
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
    onAddEnd: () -> Unit = { }
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
    val pagerState = rememberPagerState(pageCount = { totalEnds })

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header con información del entrenamiento
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = training.archerName?.takeIf { it.isNotBlank() } ?: "Entrenamiento",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Distancia: ${training.distanceMeters} m")
                        Text("Categoria: ${training.category}")
                        Text("Blanco: ${training.targetType}")
                        Text("Flechas por tanda: ${training.arrowsPerEnd}")
                        Text("Tandas: ${training.endsCount}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sistema: ${if (safePuntajeSystem == "X_TO_M") "X a M" else "11 a M"} ($safeTargetZoneCount zonas)")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Clima:")
                        Text(text = "Viento: ${training.windSpeed ?: "-"} ${training.windSpeedUnit ?: ""}")
                        Text(text = "Direccion: ${formatWindDirection(training.windDirectionDegrees)}")
                        Text(text = "Cielo: ${training.skyCondition ?: "-"}")
                    }
                }
            }
        }

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
            
            var scoresInput by remember(end.id) {
                mutableStateOf(normalizeScoresText(end.scoresText))
            }
            var showErrorDialog by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            var realTimeError by remember { mutableStateOf<String?>(null) }
            val isConfirmed = end.confirmedAt != null

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Tanda ${end.endNumber}", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = scoresInput,
                                onValueChange = { newValue ->
                                    scoresInput = newValue
                                    // Validación en tiempo real
                                    val (isValid, error) = validateScoreInput(newValue, safeTargetZoneCount, safePuntajeSystem)
                                    realTimeError = if (!isValid && newValue.isNotBlank()) error else null
                                },
                                label = { Text("Tiradas (ej: 10, 9, X, M)") },
                                enabled = !isConfirmed,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                isError = realTimeError != null
                            )
                            if (realTimeError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = realTimeError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (isConfirmed) "✓ Confirmada" else "⊙ Pendiente")
                                Button(
                                    onClick = {
                                        val (isValid, error) = validateScoreInput(scoresInput, safeTargetZoneCount, safePuntajeSystem)
                                        if (!isValid) {
                                            errorMessage = error
                                            showErrorDialog = true
                                        } else {
                                            val parsed = parseScores(scoresInput)
                                            if (parsed.isNotEmpty()) {
                                                val sorted = sortScores(parsed)
                                                val sortedText = formatScores(sorted)
                                                scoresInput = sortedText
                                                val total = parsed.sumOf { it.score }
                                                onConfirmEnd(end.id, sortedText, total)
                                            }
                                        }
                                    },
                                    enabled = !isConfirmed && scoresInput.isNotBlank()
                                ) {
                                    Text("Confirmar")
                                }
                            }
                            if (end.totalScore != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Puntajes:", style = MaterialTheme.typography.labelMedium)
                                    Text(scoresInput, style = MaterialTheme.typography.labelMedium)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text("Total: ${end.totalScore}", style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                    }
                }
            }

            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = {
                        scoresInput = ""
                        showErrorDialog = false
                    },
                    title = { Text("Error en los puntajes") },
                    text = { Text(errorMessage ?: "Error desconocido") },
                    confirmButton = {
                        TextButton(onClick = {
                            scoresInput = ""
                            showErrorDialog = false
                        }) {
                            Text("Aceptar")
                        }
                    }
                )
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
            Button(
                onClick = {
                    Log.d("ArcheryScore_Debug", "Add tanda button clicked")
                    onAddEnd()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Agregar tanda")
            }
        }
    }
}

@Composable
private fun TrainingStatsTab(detail: TrainingWithEnds) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
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
    }
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
