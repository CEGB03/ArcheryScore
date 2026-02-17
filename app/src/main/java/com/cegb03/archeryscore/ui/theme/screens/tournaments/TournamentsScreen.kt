package com.cegb03.archeryscore.ui.theme.screens.tournaments

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cegb03.archeryscore.data.model.Tournament
import com.cegb03.archeryscore.viewmodel.TournamentViewModel

@Composable
fun TournamentsScreen(modifier: Modifier = Modifier) {
    Log.d("ArcheryScore_Debug", "🎯 TournamentsScreen - Composable iniciado")
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Próximos Torneos", "Torneos Participados")

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTabIndex) {
                0 -> ProximosTorneosTab()
                1 -> TorneosParticipadosTab()
            }
        }
    }
    Log.d("ArcheryScore_Debug", "✅ TournamentsScreen - Renderizado completo")
}

@Composable
fun ProximosTorneosTab(viewModel: TournamentViewModel = hiltViewModel()) {
    Log.d("ArcheryScore_Debug", "🎯 ProximosTorneosTab - Composable iniciado")
    val tournaments by viewModel.tournaments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    var detailType by rememberSaveable { mutableStateOf(TournamentDetailType.NONE) }
    var detailUrl by rememberSaveable { mutableStateOf("") }
    
    // Cargar torneos cuando el composable se monta
    LaunchedEffect(Unit) {
        Log.d("ArcheryScore_Debug", "🔄 ProximosTorneosTab - LaunchedEffect ejecutado, llamando loadTournaments()")
        viewModel.loadTournaments()
        Log.d("ArcheryScore_Debug", "✅ ProximosTorneosTab - loadTournaments() llamado")
    }
    
    when (detailType) {
        TournamentDetailType.PARTICIPANTS -> {
            ParticipantsScreen(
                url = detailUrl,
                onClose = { detailType = TournamentDetailType.NONE }
            )
        }
        TournamentDetailType.INVITATION -> {
            InvitationScreen(
                url = detailUrl,
                onClose = { detailType = TournamentDetailType.NONE }
            )
        }
        TournamentDetailType.NONE -> {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando torneos de FATARCO...")
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reintentar")
                        }
                    }
                }
                tournaments.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No hay torneos disponibles")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tournaments) { tournament ->
                            TournamentCard(
                                tournament = tournament,
                                onOpenParticipants = {
                                    detailUrl = it
                                    detailType = TournamentDetailType.PARTICIPANTS
                                },
                                onOpenInvitation = {
                                    detailUrl = it
                                    detailType = TournamentDetailType.INVITATION
                                }
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun TournamentCard(
    tournament: Tournament,
    onOpenParticipants: (String) -> Unit,
    onOpenInvitation: (String) -> Unit
) {
    val participantsUrl = tournament.participantsUrl
    val invitationUrl = tournament.invitationUrl
    val isClickable = !tournament.isSuspended && participantsUrl != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if (isClickable && participantsUrl != null) {
                onOpenParticipants(participantsUrl)
            }
        },
        enabled = isClickable,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tournament.clubName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${tournament.tournamentType} - ${tournament.region}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${tournament.date} - ${tournament.modality}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tournament.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        tournament.isSuspended -> MaterialTheme.colorScheme.error
                        tournament.status.contains("Cerradas") -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Participantes: ${tournament.participants}/${tournament.capacity}",
                    style = MaterialTheme.typography.bodySmall
                )

                if (!tournament.isSuspended && (participantsUrl != null || invitationUrl != null)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (participantsUrl != null) {
                            OutlinedButton(onClick = { onOpenParticipants(participantsUrl) }) {
                                Text("Participantes")
                            }
                        }
                        if (invitationUrl != null) {
                            Button(onClick = { onOpenInvitation(invitationUrl) }) {
                                Text("Invitación")
                            }
                        }
                    }
                }
            }
            
            if (tournament.imageUrl != null) {
                AsyncImage(
                    model = tournament.imageUrl,
                    contentDescription = "Logo del club",
                    modifier = Modifier.size(60.dp)
                )
            }
        }
    }
}
private enum class TournamentDetailType {
    NONE,
    PARTICIPANTS,
    INVITATION,
}

@Composable
fun TorneosParticipadosTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Torneos Participados")
    }
}