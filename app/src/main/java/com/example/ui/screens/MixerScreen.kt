package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TrackCard
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiBackground
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiEmerald
import com.example.ui.theme.GeminiPink
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
fun MixerScreen(
    viewModel: GeminiMusicViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val tracks by viewModel.availableTracks.collectAsState()
    val selectedIds by viewModel.selectedTrackIds.collectAsState()
    val isCreateUnlocked by viewModel.isCreateUnlocked.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTrackId by viewModel.currentTrackId.collectAsState()
    val progressSec by viewModel.currentPositionSec.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()

    val selectedCount = selectedIds.size
    val canMix = selectedCount in 2..5

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeminiBackground)
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Mixer Header Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = GeminiSurfaceCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GeminiBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(GeminiPink, GeminiSecondary))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mesa de Mezclas Gemini",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GeminiTextPrimary
                            )
                            Text(
                                text = "Combina de 2 a 5 pistas en una canción de 30s",
                                style = MaterialTheme.typography.bodySmall,
                                color = GeminiTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Counter badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (canMix) GeminiEmerald.copy(alpha = 0.2f) else GeminiSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (canMix) GeminiEmerald else GeminiBorder
                        )
                    ) {
                        Text(
                            text = "$selectedCount / 5 seleccionadas",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (canMix) GeminiEmerald else GeminiTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rule reminder
                Text(
                    text = when {
                        selectedCount == 0 -> "👉 Selecciona abajo al menos 2 canciones para mezclarlas con IA."
                        selectedCount == 1 -> "👉 Te falta 1 canción más. Se requiere de 2 a 5 canciones para el Mix."
                        selectedCount in 2..5 -> "✨ ¡Listo! Pulsa \"Crear Mix\" para que Gemini ensamble armónicamente las $selectedCount pistas."
                        else -> "⚠️ Máximo 5 canciones permitidas por Mix."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (canMix) GeminiTertiary else GeminiTextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action Button: Unlocked or Locked
                if (!isCreateUnlocked) {
                    Button(
                        onClick = { viewModel.unlockCreateMode() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiAmber),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Desbloquear Crear (Toca para activar)", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                } else {
                    Button(
                        onClick = { viewModel.createMixFromSelected() },
                        enabled = canMix && !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("mixer_screen_create_mix_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canMix) GeminiPink else GeminiSurfaceVariant,
                            disabledContainerColor = GeminiSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Mezclando y sincronizando...")
                        } else {
                            Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (canMix) "🎛️ Crear Mix ($selectedCount pistas de 30s)" else "Selecciona entre 2 y 5 pistas",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tracks to Select
        Text(
            text = "Pistas disponibles en tu estudio:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = GeminiTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(tracks, key = { it.id }) { track ->
                val isSelected = selectedIds.contains(track.id)

                TrackCard(
                    track = track,
                    isPlaying = isPlaying && currentTrackId == track.id,
                    progressSec = if (currentTrackId == track.id) progressSec else 0f,
                    amplitude = if (currentTrackId == track.id) amplitude else 0f,
                    isSelectedForMix = isSelected,
                    onPlayToggle = { viewModel.playTrack(track) },
                    onEditClick = { viewModel.openManualEditor(track) },
                    onToggleSelectForMix = { viewModel.toggleTrackSelectionForMix(track.id) }
                )
            }
        }
    }
}
