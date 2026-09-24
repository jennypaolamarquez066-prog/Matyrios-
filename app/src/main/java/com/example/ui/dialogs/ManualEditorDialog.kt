package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.MusicTrack
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiBackground
import com.example.ui.theme.GeminiBorder
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
fun ManualEditorDialog(
    track: MusicTrack,
    viewModel: GeminiMusicViewModel,
    onDismiss: () -> Unit
) {
    val bpm by viewModel.editingBpm.collectAsState()
    val pitch by viewModel.editingPitch.collectAsState()
    val bassBoost by viewModel.editingBassBoost.collectAsState()
    val reverb by viewModel.editingReverb.collectAsState()
    val stems by viewModel.editingStems.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, GeminiBorder, RoundedCornerShape(24.dp)),
            color = GeminiBackground
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
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
                                .background(GeminiSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Editor Manual",
                                tint = GeminiTertiary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Editor Manual de Música",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GeminiTextPrimary
                            )
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = GeminiTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = GeminiTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GeminiSurfaceCard)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🛠️ Herramientas manuales fáciles: Modifica el ritmo, ecualiza los instrumentos y agrega efectos a tu gusto sin depender de la IA.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GeminiTertiary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 1: BPM & Tempo
                Card(
                    colors = CardDefaults.cardColors(containerColor = GeminiSurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = GeminiAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ritmo y Velocidad",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GeminiTextPrimary
                                )
                            }
                            Text(
                                text = "$bpm BPM",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GeminiAmber
                            )
                        }

                        Slider(
                            value = bpm.toFloat(),
                            onValueChange = { viewModel.updateEditorBpm(it.toInt()) },
                            valueRange = 60f..180f,
                            colors = SliderDefaults.colors(
                                thumbColor = GeminiAmber,
                                activeTrackColor = GeminiAmber,
                                inactiveTrackColor = GeminiSurfaceVariant
                            ),
                            modifier = Modifier.testTag("bpm_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(
                                80 to "🌙 Lento (80)",
                                110 to "🎧 Medio (110)",
                                128 to "⚡ Club (128)",
                                145 to "🚀 Rápido (145)"
                            ).forEach { (presetBpm, label) ->
                                FilterChip(
                                    selected = bpm == presetBpm,
                                    onClick = { viewModel.updateEditorBpm(presetBpm) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GeminiAmber.copy(alpha = 0.2f),
                                        selectedLabelColor = GeminiAmber
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Stems / Instrument Channels
                Card(
                    colors = CardDefaults.cardColors(containerColor = GeminiSurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = GeminiPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Canales de Instrumentos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = GeminiTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        stems.forEach { stem ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GeminiSurfaceVariant.copy(alpha = 0.5f))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${stem.stemType.icon} ${stem.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (stem.isMuted) GeminiTextMuted else GeminiTextPrimary
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Solo button
                                        FilterChip(
                                            selected = stem.isSolo,
                                            onClick = { viewModel.toggleStemSolo(stem.id) },
                                            label = { Text("Solo", fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = GeminiSecondary,
                                                selectedLabelColor = Color.White
                                            )
                                        )

                                        // Mute button
                                        FilterChip(
                                            selected = stem.isMuted,
                                            onClick = { viewModel.toggleStemMute(stem.id) },
                                            label = { Text(if (stem.isMuted) "Silenciado" else "Mute", fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = GeminiPink,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Slider(
                                        value = stem.volume,
                                        onValueChange = { viewModel.updateStemVolume(stem.id, it) },
                                        valueRange = 0f..1f,
                                        enabled = !stem.isMuted,
                                        modifier = Modifier.weight(1f),
                                        colors = SliderDefaults.colors(
                                            thumbColor = GeminiTertiary,
                                            activeTrackColor = GeminiTertiary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${(stem.volume * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GeminiTextSecondary,
                                        modifier = Modifier.width(36.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Pitch & Tone
                Card(
                    colors = CardDefaults.cardColors(containerColor = GeminiSurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tono y Afinación",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = GeminiTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(
                                0.85f to "🎸 Grave / Profundo",
                                1.0f to "✨ Estándar",
                                1.25f to "🎹 Agudo / Brillante"
                            ).forEach { (pVal, pLabel) ->
                                FilterChip(
                                    selected = pitch == pVal,
                                    onClick = { viewModel.updateEditorPitch(pVal) },
                                    label = { Text(pLabel, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GeminiPrimary.copy(alpha = 0.3f),
                                        selectedLabelColor = GeminiPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Effects (Bass Boost & Reverb)
                Card(
                    colors = CardDefaults.cardColors(containerColor = GeminiSurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Efectos Intuitivos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = GeminiTextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bass Boost
                        Text(
                            text = "Potenciador de Bajos (Bass Boost): ${(bassBoost * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = GeminiTextSecondary
                        )
                        Slider(
                            value = bassBoost,
                            onValueChange = { viewModel.updateEditorBassBoost(it) },
                            valueRange = 1.0f..2.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = GeminiPink,
                                activeTrackColor = GeminiPink
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Reverb / Eco
                        Text(
                            text = "Eco y Espacio (Reverb): ${(reverb * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = GeminiTextSecondary
                        )
                        Slider(
                            value = reverb,
                            onValueChange = { viewModel.updateEditorReverb(it) },
                            valueRange = 0f..0.7f,
                            colors = SliderDefaults.colors(
                                thumbColor = GeminiSecondary,
                                activeTrackColor = GeminiSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Live Preview Button
                Button(
                    onClick = {
                        if (isPlaying) viewModel.stopPlayback() else viewModel.previewEditedTrack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("preview_edit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) GeminiPink else GeminiSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPlaying) "Detener Escucha" else "▶️ Escuchar Cambios en Vivo (30s)",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons: Save changes / Save as new / Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.saveManualEdits(saveAsNewCopy = true) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_as_new_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Crear Copia", fontSize = 13.sp)
                    }

                    Button(
                        onClick = { viewModel.saveManualEdits(saveAsNewCopy = false) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_changes_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
