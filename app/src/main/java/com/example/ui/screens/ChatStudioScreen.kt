package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.MessageSender
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
fun ChatStudioScreen(
    viewModel: GeminiMusicViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isCreateUnlocked by viewModel.isCreateUnlocked.collectAsState()
    val selectedTrackIds by viewModel.selectedTrackIds.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTrackId by viewModel.currentTrackId.collectAsState()
    val progressSec by viewModel.currentPositionSec.collectAsState()
    val amplitude by viewModel.amplitude.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeminiBackground)
            .padding(contentPadding)
            .imePadding()
    ) {
        // Selection Banner if tracks are selected for mix
        AnimatedVisibility(
            visible = selectedTrackIds.isNotEmpty(),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(GeminiPrimary.copy(alpha = 0.25f), GeminiSecondary.copy(alpha = 0.25f)))
                    )
                    .border(1.dp, GeminiPrimary.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = GeminiTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedTrackIds.size in 2..5) {
                                "${selectedTrackIds.size} canciones seleccionadas para Mix (de 2 a 5)"
                            } else {
                                "${selectedTrackIds.size} canción seleccionada (elige de 2 a 5 para mix)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = GeminiTextPrimary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clearMixSelection() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar selección",
                            tint = GeminiTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageItem(
                    message = message,
                    isPlaying = isPlaying && currentTrackId == message.attachedTrack?.id,
                    progressSec = if (currentTrackId == message.attachedTrack?.id) progressSec else 0f,
                    amplitude = if (currentTrackId == message.attachedTrack?.id) amplitude else 0f,
                    isSelectedForMix = message.attachedTrack?.let { selectedTrackIds.contains(it.id) } ?: false,
                    onPlayToggle = { message.attachedTrack?.let { viewModel.playTrack(it) } },
                    onEditClick = { message.attachedTrack?.let { viewModel.openManualEditor(it) } },
                    onToggleSelectForMix = { message.attachedTrack?.let { viewModel.toggleTrackSelectionForMix(it.id) } },
                    onQuickActionClick = { actionText ->
                        if (actionText.contains("desbloquear", ignoreCase = true)) {
                            viewModel.sendUserMessage("Desbloquear crear")
                        } else {
                            viewModel.sendUserMessage(actionText)
                        }
                    }
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GeminiPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = GeminiTertiary,
                                strokeWidth = 2.dp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Gemini está componiendo y sintetizando tu pista de 30 segundos...",
                            style = MaterialTheme.typography.bodySmall,
                            color = GeminiTertiary
                        )
                    }
                }
            }
        }

        // Hint for unlocking if locked
        if (!isCreateUnlocked) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = GeminiSurfaceCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiAmber.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = GeminiAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dile a Gemini \"Desbloquear crear\" para activar el botón de música y mezcla.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GeminiTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    SuggestionChip(
                        onClick = { viewModel.sendUserMessage("Desbloquear crear") },
                        label = { Text("Desbloquear", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = GeminiAmber.copy(alpha = 0.15f),
                            labelColor = GeminiAmber
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("shortcut_unlock_chip")
                    )
                }
            }
        }

        // Quick suggestions row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = if (!isCreateUnlocked) {
                listOf("Desbloquear crear", "Beat Lofi 80s", "Synthwave Neón", "Bajo Cyberpunk", "Reggaeton Dembow")
            } else {
                listOf("Lofi Chill 30s", "Cyberpunk Arpegio", "Synth Pop 128 BPM", "Ritmo Latino", "Trap Pesado 30s")
            }

            items(suggestions) { tag ->
                SuggestionChip(
                    onClick = { viewModel.sendUserMessage(tag) },
                    label = { Text(tag, fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = GeminiSurfaceCard,
                        labelColor = GeminiTextSecondary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
                )
            }
        }

        // DYNAMIC CONDITIONAL ACTION BAR:
        // As requested:
        // 1. Hidden by default until "Desbloquear crear"
        // 2. If unlocked:
        //    - Shows "Crear Música"
        //    - When 2 to 5 tracks are selected, "Crear Música" disappears and changes to "Crear Mix (X pistas)"
        AnimatedVisibility(
            visible = isCreateUnlocked,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            val isMixMode = selectedTrackIds.size in 2..5

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Button(
                    onClick = { viewModel.onMainActionButtonClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .testTag(if (isMixMode) "create_mix_button" else "create_music_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMixMode) GeminiPink else GeminiPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isMixMode) Icons.Default.GraphicEq else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isMixMode) {
                                "🎛️ Crear Mix (${selectedTrackIds.size} pistas)"
                            } else {
                                "✨ Crear Música (30s)"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Input bottom bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                placeholder = {
                    Text(
                        text = if (!isCreateUnlocked) "Escribe 'Desbloquear crear' o un estilo..." else "Describe el estilo o ritmo para Gemini...",
                        color = GeminiTextMuted,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GeminiSurfaceCard,
                    unfocusedContainerColor = GeminiSurfaceCard,
                    focusedBorderColor = GeminiPrimary,
                    unfocusedBorderColor = GeminiBorder,
                    focusedTextColor = GeminiTextPrimary,
                    unfocusedTextColor = GeminiTextPrimary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendUserMessage(inputText)
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank() && !isGenerating) GeminiPrimary else GeminiSurfaceVariant
                    )
                    .testTag("send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar a Gemini",
                    tint = if (inputText.isNotBlank() && !isGenerating) Color.White else GeminiTextMuted
                )
            }
        }
    }
}

@Composable
fun MessageItem(
    message: ChatMessage,
    isPlaying: Boolean,
    progressSec: Float,
    amplitude: Float,
    isSelectedForMix: Boolean,
    onPlayToggle: () -> Unit,
    onEditClick: () -> Unit,
    onToggleSelectForMix: () -> Unit,
    onQuickActionClick: (String) -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(GeminiPrimary, GeminiSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Gemini",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Card(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUser) GeminiPrimary else GeminiSurfaceCard
                    ),
                    border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isUser) Color.White else GeminiTextPrimary,
                            lineHeight = 20.sp
                        )

                        // If unlocked notification, add celebration tag
                        if (message.isUnlockAction) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = GeminiEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Botón de Creación/Mix desbloqueado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GeminiEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Embedded Track Card if Gemini attached audio
                if (message.attachedTrack != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TrackCard(
                        track = message.attachedTrack,
                        isPlaying = isPlaying,
                        progressSec = progressSec,
                        amplitude = amplitude,
                        isSelectedForMix = isSelectedForMix,
                        onPlayToggle = onPlayToggle,
                        onEditClick = onEditClick,
                        onToggleSelectForMix = onToggleSelectForMix
                    )
                }

                // Quick actions
                if (message.quickActions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(message.quickActions) { action ->
                            SuggestionChip(
                                onClick = { onQuickActionClick(action) },
                                label = { Text(action, fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = GeminiSurfaceVariant,
                                    labelColor = GeminiTertiary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBorder)
                            )
                        }
                    }
                }
            }
        }
    }
}
