package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MusicTrack
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPrimary
import com.example.ui.theme.GeminiSecondary
import com.example.ui.theme.GeminiSurfaceCard
import com.example.ui.theme.GeminiSurfaceVariant
import com.example.ui.theme.GeminiTertiary
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary

@Composable
fun TrackCard(
    track: MusicTrack,
    isPlaying: Boolean,
    progressSec: Float,
    amplitude: Float,
    isSelectedForMix: Boolean,
    onPlayToggle: () -> Unit,
    onEditClick: () -> Unit,
    onToggleSelectForMix: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val progressFraction = (progressSec / 30f).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelectedForMix) 2.dp else 1.dp,
                color = if (isSelectedForMix) GeminiPrimary else GeminiBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = GeminiSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Title, Genre tag, and Selection indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (track.isMix) listOf(GeminiPink, GeminiSecondary)
                                    else listOf(GeminiPrimary, GeminiTertiary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GeminiTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = track.genre,
                                style = MaterialTheme.typography.bodySmall,
                                color = GeminiTertiary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = " • 30s • ${track.bpm} BPM",
                                style = MaterialTheme.typography.bodySmall,
                                color = GeminiTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (onToggleSelectForMix != null) {
                    IconButton(
                        onClick = onToggleSelectForMix,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("select_for_mix_${track.id}")
                    ) {
                        Icon(
                            imageVector = if (isSelectedForMix) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (isSelectedForMix) "Seleccionada para mix" else "Seleccionar para mix",
                            tint = if (isSelectedForMix) GeminiPrimary else GeminiTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Waveform Visualizer
            AudioVisualizerView(
                waveformData = track.waveformData,
                isPlaying = isPlaying,
                progressFraction = if (isPlaying) progressFraction else 0f,
                amplitude = amplitude,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GeminiSurfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isPlaying) String.format("00:%02d", progressSec.toInt()) else "00:00",
                    style = MaterialTheme.typography.bodySmall,
                    color = GeminiTertiary,
                    fontSize = 11.sp
                )
                Text(
                    text = "00:30",
                    style = MaterialTheme.typography.bodySmall,
                    color = GeminiTextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Play/Pause button + "Editar Música" button + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isPlaying) GeminiPink else GeminiPrimary
                        )
                        .clickable(onClick = onPlayToggle)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("play_button_${track.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlaying) "Pausar" else "Escuchar 30s",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // "Editar Música" button - requested by user!
                    AssistChip(
                        onClick = onEditClick,
                        label = {
                            Text(
                                text = "Editar música",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = GeminiSurfaceVariant,
                            labelColor = GeminiTextPrimary,
                            leadingIconContentColor = GeminiTertiary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder),
                        modifier = Modifier.testTag("edit_music_button_${track.id}")
                    )

                    if (onDeleteClick != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar pista",
                                tint = GeminiTextSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
