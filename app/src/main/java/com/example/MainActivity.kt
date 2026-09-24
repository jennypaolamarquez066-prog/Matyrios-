package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.dialogs.ManualEditorDialog
import com.example.ui.screens.ChatStudioScreen
import com.example.ui.screens.LibraryHistoryScreen
import com.example.ui.screens.MixerScreen
import com.example.ui.theme.GeminiAmber
import com.example.ui.theme.GeminiBackground
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiEmerald
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPrimary
import com.example.ui.theme.GeminiSecondary
import com.example.ui.theme.GeminiSurface
import com.example.ui.theme.GeminiSurfaceCard
import com.example.ui.theme.GeminiSurfaceVariant
import com.example.ui.theme.GeminiTertiary
import com.example.ui.theme.GeminiTextMuted
import com.example.ui.theme.GeminiTextPrimary
import com.example.ui.theme.GeminiTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.GeminiMusicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: GeminiMusicViewModel = viewModel()
                GeminiMusicMainApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiMusicMainApp(viewModel: GeminiMusicViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isCreateUnlocked by viewModel.isCreateUnlocked.collectAsState()
    val selectedTrackIds by viewModel.selectedTrackIds.collectAsState()
    val editingTrack by viewModel.editingTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentTrackId by viewModel.currentTrackId.collectAsState()
    val availableTracks by viewModel.availableTracks.collectAsState()
    val progressSec by viewModel.currentPositionSec.collectAsState()

    val currentPlayingTrack = availableTracks.firstOrNull { it.id == currentTrackId }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = GeminiBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(GeminiPrimary, GeminiSecondary, GeminiPink)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini Spark",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Gemini",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiTextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Music",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiTertiary
                                )
                            }
                            Text(
                                text = "Pistas de 30s & Mezclas IA",
                                style = MaterialTheme.typography.bodySmall,
                                color = GeminiTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Unlock status badge in TopBar
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCreateUnlocked) GeminiEmerald.copy(alpha = 0.15f) else GeminiAmber.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCreateUnlocked) GeminiEmerald.copy(alpha = 0.4f) else GeminiAmber.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable {
                                if (!isCreateUnlocked) {
                                    viewModel.sendUserMessage("Desbloquear crear")
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isCreateUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isCreateUnlocked) GeminiEmerald else GeminiAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCreateUnlocked) "Crear Activo" else "Bloqueado",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCreateUnlocked) GeminiEmerald else GeminiAmber
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GeminiSurface
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                // Mini Player Bar if something is currently playing or selected
                AnimatedVisibility(
                    visible = currentPlayingTrack != null && isPlaying,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    currentPlayingTrack?.let { track ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = GeminiSurfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiPrimary)
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
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(GeminiPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = track.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = GeminiTextPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Reproduciendo 00:${String.format("%02d", progressSec.toInt())} / 00:30",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GeminiTertiary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.stopPlayback() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Pausar",
                                        tint = GeminiPink
                                    )
                                }
                            }
                        }
                    }
                }

                // Main navigation bar
                NavigationBar(
                    containerColor = GeminiSurface,
                    contentColor = GeminiTextPrimary,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Estudio Chat")
                        },
                        label = { Text("Estudio Chat", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = GeminiTertiary,
                            indicatorColor = GeminiPrimary,
                            unselectedIconColor = GeminiTextMuted,
                            unselectedTextColor = GeminiTextMuted
                        ),
                        modifier = Modifier.testTag("nav_studio_chat")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            if (selectedTrackIds.size in 2..5) {
                                BadgedBox(badge = { Badge { Text("${selectedTrackIds.size}") } }) {
                                    Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "Mixer")
                                }
                            } else {
                                Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "Mixer")
                            }
                        },
                        label = { Text("Mesa de Mezclas", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = GeminiPink,
                            indicatorColor = GeminiSecondary,
                            unselectedIconColor = GeminiTextMuted,
                            unselectedTextColor = GeminiTextMuted
                        ),
                        modifier = Modifier.testTag("nav_mixer")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(imageVector = Icons.Default.LibraryMusic, contentDescription = "Biblioteca")
                        },
                        label = { Text("Historial", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = GeminiTertiary,
                            indicatorColor = GeminiPrimary,
                            unselectedIconColor = GeminiTextMuted,
                            unselectedTextColor = GeminiTextMuted
                        ),
                        modifier = Modifier.testTag("nav_library")
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> ChatStudioScreen(
                viewModel = viewModel,
                contentPadding = innerPadding
            )
            1 -> MixerScreen(
                viewModel = viewModel,
                contentPadding = innerPadding
            )
            2 -> LibraryHistoryScreen(
                viewModel = viewModel,
                contentPadding = innerPadding,
                onNavigateToStudio = { selectedTab = 0 }
            )
        }

        // Manual Editor Dialog when requested
        editingTrack?.let { track ->
            ManualEditorDialog(
                track = track,
                viewModel = viewModel,
                onDismiss = { viewModel.closeManualEditor() }
            )
        }
    }
}
