package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TrackCard
import com.example.ui.theme.GeminiBackground
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiPrimary
import com.example.ui.theme.GeminiSecondary
import com.example.ui.theme.GeminiSurfaceCard
import com.example.ui.theme.GeminiSurfaceVariant
import com.example.ui.theme.GeminiTertiary
import com.example.ui.theme.GeminiTextMuted
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.viewmodel.GeminiMusicViewModel

@Composable
fun LibraryHistoryScreen(
    viewModel: GeminiMusicViewModel,
    contentPadding: PaddingValues,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tracks by viewModel.availableTracks.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTrackId by viewModel.currentTrackId.collectAsState()
    val progressSec by viewModel.currentPositionSec.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()
    val selectedTrackIds by viewModel.selectedTrackIds.collectAsState()

    var filterType by remember { mutableStateOf("ALL") }

    val filteredTracks = when (filterType) {
        "MIX" -> tracks.filter { it.isMix }
        "TRACKS" -> tracks.filter { !it.isMix }
        else -> tracks
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeminiBackground)
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GeminiSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = null,
                        tint = GeminiTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Historial y Creaciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GeminiTextPrimary
                    )
                    Text(
                        text = "${tracks.size} pistas y canciones de 30s",
                        style = MaterialTheme.typography.bodySmall,
                        color = GeminiTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = filterType == "ALL",
                    onClick = { filterType = "ALL" },
                    label = { Text("Todas (${tracks.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GeminiPrimary.copy(alpha = 0.25f),
                        selectedLabelColor = GeminiPrimary
                    )
                )
            }
            item {
                FilterChip(
                    selected = filterType == "TRACKS",
                    onClick = { filterType = "TRACKS" },
                    label = { Text("Pistas Individuales (${tracks.count { !it.isMix }})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GeminiPrimary.copy(alpha = 0.25f),
                        selectedLabelColor = GeminiPrimary
                    )
                )
            }
            item {
                FilterChip(
                    selected = filterType == "MIX",
                    onClick = { filterType = "MIX" },
                    label = { Text("Mixes (${tracks.count { it.isMix }})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GeminiSecondary.copy(alpha = 0.25f),
                        selectedLabelColor = GeminiSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = GeminiTextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay canciones en este filtro",
                        style = MaterialTheme.typography.titleMedium,
                        color = GeminiTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Crea nuevas pistas en el Estudio o desbloquea el modo mix.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GeminiTextMuted
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToStudio,
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ir al Estudio Gemini")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredTracks, key = { it.id }) { track ->
                    TrackCard(
                        track = track,
                        isPlaying = isPlaying && currentTrackId == track.id,
                        progressSec = if (currentTrackId == track.id) progressSec else 0f,
                        amplitude = if (currentTrackId == track.id) amplitude else 0f,
                        isSelectedForMix = selectedTrackIds.contains(track.id),
                        onPlayToggle = { viewModel.playTrack(track) },
                        onEditClick = { viewModel.openManualEditor(track) },
                        onToggleSelectForMix = { viewModel.toggleTrackSelectionForMix(track.id) },
                        onDeleteClick = { viewModel.deleteTrack(track.id) }
                    )
                }
            }
        }
    }
}
