package com.cegb03.archeryscore.ui.theme.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cegb03.archeryscore.data.local.training.TrainingEndEntity
import com.cegb03.archeryscore.data.local.training.TrainingType
import com.cegb03.archeryscore.data.local.training.TrainingWithSeries
import com.cegb03.archeryscore.ui.theme.ScoreBlack
import com.cegb03.archeryscore.ui.theme.ScoreBlue
import com.cegb03.archeryscore.ui.theme.ScoreRed
import com.cegb03.archeryscore.ui.theme.ScoreWhite
import com.cegb03.archeryscore.ui.theme.ScoreYellow
import com.cegb03.archeryscore.viewmodel.TrainingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun HomeStatsScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingsViewModel = hiltViewModel()
) {
    val trainings by viewModel.trainings.collectAsState()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showIndividual by rememberSaveable { mutableStateOf(true) }
    var showGroup by rememberSaveable { mutableStateOf(true) }

    val trainingTypeFilter = if (selectedTabIndex == 0) TrainingType.TRAINING else TrainingType.TOURNAMENT
    val filteredTrainings = remember(trainings, trainingTypeFilter, showGroup, showIndividual) {
        trainings.filter { training ->
            training.training.trainingType == trainingTypeFilter &&
                ((training.training.isGroup && showGroup) || (!training.training.isGroup && showIndividual))
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Entrenamientos") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Torneos") }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showIndividual,
                onClick = { showIndividual = !showIndividual },
                label = { Text("Individual") }
            )
            FilterChip(
                selected = showGroup,
                onClick = { showGroup = !showGroup },
                label = { Text("Grupal") }
            )
        }

        if (!showGroup && !showIndividual) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Activa al menos un filtro")
            }
        } else if (filteredTrainings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay registros")
            }
        } else {
            StatsContent(trainings = filteredTrainings)
        }
    }
}

@Composable
private fun StatsContent(trainings: List<TrainingWithSeries>) {
    val summary = remember(trainings) { calculateAggregateStats(trainings) }
    val targetGroups = remember(trainings) { buildTargetGroups(trainings) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Resumen general", style = MaterialTheme.typography.titleMedium)
        }

        item {
            SummaryCard(summary = summary)
        }

        item {
            Text("Por registro", style = MaterialTheme.typography.titleMedium)
        }

        items(trainings, key = { it.training.id }) { training ->
            TrainingSummaryCard(training = training)
        }

        item {
            Text("Distribucion por blanco", style = MaterialTheme.typography.titleMedium)
        }

        items(targetGroups, key = { it.key }) { group ->
            TargetGroupCard(group = group)
        }
    }
}

@Composable
private fun SummaryCard(summary: AggregateStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Series: ${summary.totalSeries}")
                Text("Tandas: ${summary.confirmedEnds}/${summary.totalEnds}")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Flechas: ${summary.scoreStats.totalArrows}")
                Text("Puntuacion: ${summary.scoreStats.totalScore}")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Promedio: ${formatAvg(summary.scoreStats.average)}")
                Text("X: ${summary.scoreStats.xCount}")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Text("10: ${summary.scoreStats.tenCount}")
                Text("9: ${summary.scoreStats.nineCount}")
                Text("M: ${summary.scoreStats.missCount}")
            }
        }
    }
}

@Composable
private fun TrainingSummaryCard(training: TrainingWithSeries) {
    val stats = remember(training) { calculateTrainingStats(training) }
    val date = remember(training.training.createdAt) {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.format(Date(training.training.createdAt))
    }
    val name = training.training.archerName?.takeIf { it.isNotBlank() } ?: "Registro"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Fecha: $date", style = MaterialTheme.typography.bodySmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Series: ${stats.seriesCount}")
                Text("Tandas: ${stats.confirmedEnds}/${stats.totalEnds}")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Flechas: ${stats.scoreStats.totalArrows}")
                Text("Puntuacion: ${stats.scoreStats.totalScore}")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Promedio: ${formatAvg(stats.scoreStats.average)}")
                Text("X: ${stats.scoreStats.xCount}")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Text("10: ${stats.scoreStats.tenCount}")
                Text("9: ${stats.scoreStats.nineCount}")
                Text("M: ${stats.scoreStats.missCount}")
            }
        }
    }
}

@Composable
private fun TargetGroupCard(group: TargetGroupStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "${group.targetType} - ${group.distanceMeters}m - ${group.targetZoneCount} zonas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            ScorePieChart(
                slices = group.slices,
                total = group.totalShots
            )
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
                        val hit = findSliceIndex(offset, chartSize, slices, total)
                        selectedIndex = hit
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
                if (isSelected) {
                    drawArc(
                        color = Color.Black.copy(alpha = 0.15f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        size = Size(sliceRadius * 2f, sliceRadius * 2f),
                        topLeft = Offset(
                            (size.width - sliceRadius * 2f) / 2f,
                            (size.height - sliceRadius * 2f) / 2f
                        ),
                        style = Stroke(width = 4f)
                    )
                }
                startAngle += sweep
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            slices.forEachIndexed { index, slice ->
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
                fontSize = 12.sp,
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
    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
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

private fun buildTargetGroups(trainings: List<TrainingWithSeries>): List<TargetGroupStats> {
    val grouped = linkedMapOf<String, MutableList<ParsedScore>>()

    trainings.forEach { training ->
        training.series.forEach { seriesWithEnds ->
            val series = seriesWithEnds.series
            val key = "${series.targetType}|${series.targetZoneCount}|${series.distanceMeters}"
            val scores = seriesWithEnds.ends.flatMap { parseScores(it.scoresText.orEmpty()) }
            grouped.getOrPut(key) { mutableListOf() }.addAll(scores)
        }
    }

    return grouped.map { (key, scores) ->
        val parts = key.split("|")
        val targetType = parts.getOrNull(0).orEmpty()
        val zoneCount = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val distance = parts.getOrNull(2)?.toIntOrNull() ?: 0
        val slices = buildSlices(scores)
        TargetGroupStats(
            key = key,
            targetType = targetType,
            targetZoneCount = zoneCount,
            distanceMeters = distance,
            slices = slices,
            totalShots = scores.size
        )
    }.sortedWith(compareBy({ it.targetType }, { it.distanceMeters }, { it.targetZoneCount }))
}

private fun buildSlices(scores: List<ParsedScore>): List<ScoreSlice> {
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
        ScoreSlice("Amarillo", ScoreYellow, counts["Amarillo"] ?: 0),
        ScoreSlice("Rojo", ScoreRed, counts["Rojo"] ?: 0),
        ScoreSlice("Azul", ScoreBlue, counts["Azul"] ?: 0),
        ScoreSlice("Negro", ScoreBlack, counts["Negro"] ?: 0),
        ScoreSlice("Blanco", ScoreWhite, counts["Blanco"] ?: 0)
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

private fun calculateAggregateStats(trainings: List<TrainingWithSeries>): AggregateStats {
    val allEnds = trainings.flatMap { it.series.flatMap { series -> series.ends } }
    val scoreStats = calculateScoreStats(allEnds)
    val totalSeries = trainings.sumOf { it.series.size }
    val totalEnds = trainings.sumOf { training -> training.series.sumOf { it.series.endsCount } }
    val confirmedEnds = allEnds.count { it.confirmedAt != null }
    return AggregateStats(
        totalSeries = totalSeries,
        totalEnds = totalEnds,
        confirmedEnds = confirmedEnds,
        scoreStats = scoreStats
    )
}

private fun calculateTrainingStats(training: TrainingWithSeries): TrainingStats {
    val ends = training.series.flatMap { it.ends }
    val scoreStats = calculateScoreStats(ends)
    val totalEnds = training.series.sumOf { it.series.endsCount }
    val confirmedEnds = ends.count { it.confirmedAt != null }
    return TrainingStats(
        seriesCount = training.series.size,
        totalEnds = totalEnds,
        confirmedEnds = confirmedEnds,
        scoreStats = scoreStats
    )
}

private fun calculateScoreStats(ends: List<TrainingEndEntity>): ScoreStats {
    val parsedScores = ends.flatMap { parseScores(it.scoresText.orEmpty()) }
    val totalArrows = parsedScores.size
    val totalScore = parsedScores.sumOf { it.score }
    val average = if (totalArrows == 0) 0.0 else totalScore.toDouble() / totalArrows
    val xCount = parsedScores.count { it.isX }
    val tenCount = parsedScores.count { it.score == 10 && !it.isX }
    val nineCount = parsedScores.count { it.score == 9 }
    val missCount = parsedScores.count { it.isMiss }

    return ScoreStats(
        totalArrows = totalArrows,
        totalScore = totalScore,
        average = average,
        xCount = xCount,
        tenCount = tenCount,
        nineCount = nineCount,
        missCount = missCount
    )
}

private fun formatAvg(value: Double): String = "%.2f".format(value)

private fun formatPercent(count: Int, total: Int): String {
    if (total == 0) return "0%"
    val percent = count.toDouble() / total * 100.0
    return "%.1f%%".format(percent)
}

private data class ScoreStats(
    val totalArrows: Int,
    val totalScore: Int,
    val average: Double,
    val xCount: Int,
    val tenCount: Int,
    val nineCount: Int,
    val missCount: Int
)

private data class TrainingStats(
    val seriesCount: Int,
    val totalEnds: Int,
    val confirmedEnds: Int,
    val scoreStats: ScoreStats
)

private data class AggregateStats(
    val totalSeries: Int,
    val totalEnds: Int,
    val confirmedEnds: Int,
    val scoreStats: ScoreStats
)

private data class TargetGroupStats(
    val key: String,
    val targetType: String,
    val targetZoneCount: Int,
    val distanceMeters: Int,
    val slices: List<ScoreSlice>,
    val totalShots: Int
)

private data class ScoreSlice(
    val label: String,
    val color: Color,
    val count: Int
)

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
