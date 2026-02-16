package com.cegb03.archeryscore.ui.theme.screens.tournaments

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.Divider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cegb03.archeryscore.data.model.ParticipantRow
import com.cegb03.archeryscore.data.model.ParticipantSummaryItem
import com.cegb03.archeryscore.viewmodel.ParticipantsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantsScreen(
    url: String,
    onClose: () -> Unit,
    viewModel: ParticipantsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(url) {
        viewModel.load(url)
    }

    BackHandler(enabled = true) { onClose() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inscriptos") },
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
                    Text("Cargando inscriptos...")
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
            state.data != null -> {
                val data = state.data
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = data?.clubName.orEmpty(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        data?.totalCount?.let { total ->
                            Text(
                                text = "Total: $total",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (!data?.byClub.isNullOrEmpty()) {
                        item {
                            SummaryTable(
                                title = "Inscriptos",
                                headerLeft = "Denominación",
                                headerRight = "Cant.",
                                rows = data?.byClub.orEmpty(),
                                totalCount = data?.totalCount
                            )
                        }
                    }

                    if (!data?.byCategory.isNullOrEmpty()) {
                        item {
                            SummaryTable(
                                title = "Inscriptos por categoría",
                                headerLeft = "Categoría",
                                headerRight = "Cant.",
                                rows = data?.byCategory.orEmpty(),
                                totalCount = null
                            )
                        }
                    }

                    if (!data?.rows.isNullOrEmpty()) {
                        item {
                            SectionTitle("Listado de inscriptos")
                            ParticipantsHeaderRow()
                        }
                        items(data?.rows.orEmpty()) { row ->
                            ParticipantTableRow(row)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SummaryTable(
    title: String,
    headerLeft: String,
    headerRight: String,
    rows: List<ParticipantSummaryItem>,
    totalCount: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            TableHeaderRow(headerLeft, headerRight)
            rows.forEach { row ->
                TableDataRow(row.label, row.count.toString())
            }
            if (totalCount != null) {
                Divider(modifier = Modifier.padding(vertical = 6.dp))
                TableDataRow("Cantidad de Inscriptos", totalCount.toString(), isBold = true)
            }
        }
    }
}

@Composable
private fun TableHeaderRow(left: String, right: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = left,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = right,
            modifier = Modifier.weight(0.25f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
    Divider(modifier = Modifier.padding(vertical = 6.dp))
}

@Composable
private fun TableDataRow(left: String, right: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = left,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = right,
            modifier = Modifier.weight(0.25f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
    Divider(modifier = Modifier.padding(vertical = 6.dp))
}

@Composable
private fun ParticipantsHeaderRow() {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            ParticipantCell("Nº", 0.8f, true)
            ParticipantCell("DNI", 1.4f, true)
            ParticipantCell("Arquero", 2.4f, true)
            ParticipantCell("Club", 1.2f, true)
            ParticipantCell("F.Nac.", 1.2f, true)
            ParticipantCell("Categoría", 2.0f, true)
            ParticipantCell("Fecha Ingreso", 1.8f, true)
        }
    }
    Divider(modifier = Modifier.padding(vertical = 6.dp))
}

@Composable
private fun ParticipantTableRow(row: ParticipantRow) {
    Row(modifier = Modifier.fillMaxWidth()) {
        ParticipantCell(row.number, 0.8f)
        ParticipantCell(row.dni, 1.4f)
        ParticipantCell(row.name, 2.4f)
        ParticipantCell(row.club, 1.2f)
        ParticipantCell(row.birthDate, 1.2f)
        ParticipantCell(row.category, 2.0f)
        ParticipantCell(row.entryDate, 1.8f)
    }
    Divider(modifier = Modifier.padding(vertical = 6.dp))
}

@Composable
private fun RowScope.ParticipantCell(text: String, weight: Float, isHeader: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = if (isHeader) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
        fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
        maxLines = if (isHeader) 1 else 2,
        overflow = TextOverflow.Ellipsis
    )
}
