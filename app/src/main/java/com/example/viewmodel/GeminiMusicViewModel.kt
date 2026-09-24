package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioSynthesisEngine
import com.example.gemini.GeminiMusicService
import com.example.model.AudioStem
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.model.MusicTrack
import com.example.model.StemType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class GeminiMusicViewModel : ViewModel() {
    val audioEngine = AudioSynthesisEngine()
    val geminiService = GeminiMusicService(audioEngine)

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _availableTracks = MutableStateFlow<List<MusicTrack>>(emptyList())
    val availableTracks: StateFlow<List<MusicTrack>> = _availableTracks.asStateFlow()

    private val _selectedTrackIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedTrackIds: StateFlow<Set<String>> = _selectedTrackIds.asStateFlow()

    // By default, the create/mix button is NOT visible until unlocked with "Desbloquear crear"
    private val _isCreateUnlocked = MutableStateFlow(false)
    val isCreateUnlocked: StateFlow<Boolean> = _isCreateUnlocked.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    val isPlaying: StateFlow<Boolean> = audioEngine.isPlaying
    val currentTrackId: StateFlow<String?> = audioEngine.currentTrackId
    val currentPositionSec: StateFlow<Float> = audioEngine.currentPositionSec
    val amplitude: StateFlow<Float> = audioEngine.amplitude

    // Manual Editor State
    private val _editingTrack = MutableStateFlow<MusicTrack?>(null)
    val editingTrack: StateFlow<MusicTrack?> = _editingTrack.asStateFlow()

    private val _editingBpm = MutableStateFlow(120)
    val editingBpm: StateFlow<Int> = _editingBpm.asStateFlow()

    private val _editingPitch = MutableStateFlow(1.0f)
    val editingPitch: StateFlow<Float> = _editingPitch.asStateFlow()

    private val _editingBassBoost = MutableStateFlow(1.0f)
    val editingBassBoost: StateFlow<Float> = _editingBassBoost.asStateFlow()

    private val _editingReverb = MutableStateFlow(0.2f)
    val editingReverb: StateFlow<Float> = _editingReverb.asStateFlow()

    private val _editingStems = MutableStateFlow<List<AudioStem>>(emptyList())
    val editingStems: StateFlow<List<AudioStem>> = _editingStems.asStateFlow()

    init {
        // Welcome message from Gemini Music
        val welcomeMessage = ChatMessage(
            id = "msg_welcome",
            sender = MessageSender.GEMINI,
            text = "👋 ¡Hola! Soy **Gemini Music**, tu estudio de inteligencia artificial para componer pistas de **30 segundos**.\n\nPuedes pedirme ritmos de cualquier estilo (Lofi, Synthwave, Cyberpunk, Reggaetón, etc.) o stems individuales como batería, bajo y sintetizadores.\n\n🔒 *Nota:* Para activar el botón principal de creación y el mezclador multi-pista, escribe en el chat: **\"Desbloquear crear\"**.",
            quickActions = listOf(
                "Desbloquear crear",
                "Beat Lofi 80 BPM",
                "Synthwave 80s Neón",
                "Bajo Cyberpunk pesado",
                "Reggaeton Dembow 30s"
            )
        )
        _chatMessages.value = listOf(welcomeMessage)

        // Prepopulate with 2 initial inspiring 30s demo tracks so the user has immediate music to test
        val demoTrack1 = MusicTrack(
            id = "demo_1",
            title = "Gemini Cyber Spark",
            prompt = "Cyberpunk synth wave energizante con arpegios y beats 30s",
            genre = "Cyberpunk Trap",
            bpm = 132,
            keyScale = "F# Minor",
            durationSeconds = 30,
            stems = listOf(
                AudioStem("d1_1", StemType.DRUMS, "Batería Hi-Tech", 0.9f),
                AudioStem("d1_2", StemType.BASS, "Sub-Bajo Distorsionado", 0.85f),
                AudioStem("d1_3", StemType.MELODY, "Arpegio Neón", 0.8f),
                AudioStem("d1_4", StemType.PAD, "Textura Espacial", 0.6f)
            ),
            waveformData = audioEngine.generateVisualWaveform(40)
        )

        val demoTrack2 = MusicTrack(
            id = "demo_2",
            title = "Sunset Chill Lofi",
            prompt = "Lofi relajante con acordes cálidos y bajo profundo 30s",
            genre = "Lofi Hip Hop",
            bpm = 84,
            keyScale = "D Minor",
            durationSeconds = 30,
            stems = listOf(
                AudioStem("d2_1", StemType.DRUMS, "Beat Lofi Boom-Bap", 0.8f),
                AudioStem("d2_2", StemType.BASS, "Bajo Acústico Suave", 0.85f),
                AudioStem("d2_3", StemType.MELODY, "Melodía Eléctrica", 0.75f),
                AudioStem("d2_4", StemType.PAD, "Atmósfera Cálida", 0.7f)
            ),
            waveformData = audioEngine.generateVisualWaveform(40)
        )

        _availableTracks.value = listOf(demoTrack1, demoTrack2)
    }

    fun sendUserMessage(text: String, stemFilter: StemType? = null) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val userMsg = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = MessageSender.USER,
            text = trimmed
        )
        _chatMessages.value = _chatMessages.value + userMsg

        // Check if user is asking to unlock the create/mix button
        if (geminiService.isUnlockCommand(trimmed)) {
            _isCreateUnlocked.value = true
            val unlockMsg = ChatMessage(
                id = "gemini_unlock_${System.currentTimeMillis()}",
                sender = MessageSender.GEMINI,
                text = "✨ **¡Botón de Creación y Mezcla Desbloqueado!**\n\nAhora puedes ver y usar el botón inferior:\n• Si no has seleccionado varias pistas, dice **\"Crear Música\"** para generar piezas completas.\n• Si seleccionas entre **2 y 5 pistas**, se transforma automáticamente en **\"Crear Mix\"** para unirlas en una producción de 30 segundos sin esfuerzo.",
                isUnlockAction = true,
                quickActions = listOf("Crear Música", "Seleccionar pistas para Mix", "Lofi Chill 30s")
            )
            _chatMessages.value = _chatMessages.value + unlockMsg
            return
        }

        // Generate track via Gemini service
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val (explanation, newTrack) = geminiService.processUserPrompt(trimmed, stemFilter)
                if (newTrack != null) {
                    _availableTracks.value = listOf(newTrack) + _availableTracks.value
                }
                val geminiMsg = ChatMessage(
                    id = "gemini_${System.currentTimeMillis()}",
                    sender = MessageSender.GEMINI,
                    text = explanation,
                    attachedTrack = newTrack,
                    quickActions = if (!_isCreateUnlocked.value) listOf("Desbloquear crear", "Otro estilo") else listOf("Mezclar esta pista", "Nueva variación")
                )
                _chatMessages.value = _chatMessages.value + geminiMsg
                newTrack?.let { audioEngine.playTrack(it) }
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    id = "gemini_err_${System.currentTimeMillis()}",
                    sender = MessageSender.GEMINI,
                    text = "Hubo un pequeño contratiempo al procesar la música: ${e.localizedMessage}. ¡Inténtalo de nuevo!"
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun unlockCreateMode() {
        _isCreateUnlocked.value = true
        val unlockMsg = ChatMessage(
            id = "gemini_unlock_${System.currentTimeMillis()}",
            sender = MessageSender.GEMINI,
            text = "✨ **¡Acción ejecutada: Desbloquear Crear!**\n\nEl botón de creación ya está visible en la parte inferior. Puedes crear pistas completas o seleccionar de 2 a 5 canciones para mezclarlas automáticamente.",
            isUnlockAction = true
        )
        _chatMessages.value = _chatMessages.value + unlockMsg
    }

    fun toggleTrackSelectionForMix(trackId: String) {
        val current = _selectedTrackIds.value.toMutableSet()
        if (current.contains(trackId)) {
            current.remove(trackId)
        } else {
            if (current.size >= 5) {
                // Maximum 5 tracks for mixing
                val warnMsg = ChatMessage(
                    id = "warn_${System.currentTimeMillis()}",
                    sender = MessageSender.GEMINI,
                    text = "⚠️ Puedes seleccionar un máximo de **5 canciones** para crear un Mix óptimo."
                )
                _chatMessages.value = _chatMessages.value + warnMsg
                return
            }
            current.add(trackId)
        }
        _selectedTrackIds.value = current
    }

    fun clearMixSelection() {
        _selectedTrackIds.value = emptySet()
    }

    fun onMainActionButtonClick() {
        val selectedCount = _selectedTrackIds.value.size
        if (selectedCount in 2..5) {
            createMixFromSelected()
        } else {
            createQuickFullMusic()
        }
    }

    fun createMixFromSelected() {
        val selectedTracks = _availableTracks.value.filter { _selectedTrackIds.value.contains(it.id) }
        if (selectedTracks.size !in 2..5) return

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val mixTitle = "Gemini Mix (${selectedTracks.size} Pistas)"
                val mixedTrack = audioEngine.mixTracks(selectedTracks, mixTitle)

                _availableTracks.value = listOf(mixedTrack) + _availableTracks.value
                _selectedTrackIds.value = emptySet()

                val mixMsg = ChatMessage(
                    id = "mix_msg_${System.currentTimeMillis()}",
                    sender = MessageSender.GEMINI,
                    text = "🎛️ **¡Tu nuevo Mix de ${selectedTracks.size} pistas está terminado!**\n\nGemini ha ensamblado los ritmos, ecualizado los niveles y sincronizado los compases a **${mixedTrack.bpm} BPM** en una sola pieza de 30 segundos perfecta.",
                    attachedTrack = mixedTrack
                )
                _chatMessages.value = _chatMessages.value + mixMsg
                audioEngine.playTrack(mixedTrack)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun createQuickFullMusic(style: String = "Electrónica Gemini 30s") {
        sendUserMessage("Crear una pista musical completa de 30 segundos estilo $style con batería, bajo, melodía y sintetizador")
    }

    fun playTrack(track: MusicTrack) {
        if (audioEngine.currentTrackId.value == track.id && audioEngine.isPlaying.value) {
            audioEngine.stopPlayback()
        } else {
            audioEngine.playTrack(track)
        }
    }

    fun stopPlayback() {
        audioEngine.stopPlayback()
    }

    // Manual Editor Operations
    fun openManualEditor(track: MusicTrack) {
        _editingTrack.value = track
        _editingBpm.value = track.bpm
        _editingPitch.value = track.pitchShift
        _editingBassBoost.value = track.bassBoost
        _editingReverb.value = track.reverbAmount
        _editingStems.value = track.stems.map { it.copy() }
    }

    fun closeManualEditor() {
        _editingTrack.value = null
    }

    fun updateEditorBpm(newBpm: Int) {
        _editingBpm.value = newBpm.coerceIn(60, 180)
    }

    fun updateEditorPitch(pitch: Float) {
        _editingPitch.value = pitch
    }

    fun updateEditorBassBoost(boost: Float) {
        _editingBassBoost.value = boost
    }

    fun updateEditorReverb(reverb: Float) {
        _editingReverb.value = reverb
    }

    fun updateStemVolume(stemId: String, volume: Float) {
        _editingStems.value = _editingStems.value.map {
            if (it.id == stemId) it.copy(volume = volume) else it
        }
    }

    fun toggleStemMute(stemId: String) {
        _editingStems.value = _editingStems.value.map {
            if (it.id == stemId) it.copy(isMuted = !it.isMuted) else it
        }
    }

    fun toggleStemSolo(stemId: String) {
        _editingStems.value = _editingStems.value.map {
            if (it.id == stemId) it.copy(isSolo = !it.isSolo) else it
        }
    }

    fun previewEditedTrack() {
        val current = _editingTrack.value ?: return
        val preview = current.copy(
            bpm = _editingBpm.value,
            pitchShift = _editingPitch.value,
            bassBoost = _editingBassBoost.value,
            reverbAmount = _editingReverb.value,
            stems = _editingStems.value
        )
        audioEngine.playTrack(preview)
    }

    fun saveManualEdits(saveAsNewCopy: Boolean) {
        val current = _editingTrack.value ?: return
        if (saveAsNewCopy) {
            val newTrack = current.copy(
                id = "track_edit_${System.currentTimeMillis()}",
                title = "${current.title} (Editada)",
                bpm = _editingBpm.value,
                pitchShift = _editingPitch.value,
                bassBoost = _editingBassBoost.value,
                reverbAmount = _editingReverb.value,
                stems = _editingStems.value,
                createdAt = System.currentTimeMillis()
            )
            _availableTracks.value = listOf(newTrack) + _availableTracks.value
            val msg = ChatMessage(
                id = "edit_msg_${System.currentTimeMillis()}",
                sender = MessageSender.GEMINI,
                text = "🎚️ Guardaste una copia editada manualmente de **\"${newTrack.title}\"** con tus nuevos ajustes de ecualización y ritmo.",
                attachedTrack = newTrack
            )
            _chatMessages.value = _chatMessages.value + msg
        } else {
            val updated = current.copy(
                bpm = _editingBpm.value,
                pitchShift = _editingPitch.value,
                bassBoost = _editingBassBoost.value,
                reverbAmount = _editingReverb.value,
                stems = _editingStems.value
            )
            _availableTracks.value = _availableTracks.value.map {
                if (it.id == current.id) updated else it
            }
        }
        closeManualEditor()
    }

    fun deleteTrack(trackId: String) {
        if (audioEngine.currentTrackId.value == trackId) {
            audioEngine.stopPlayback()
        }
        _availableTracks.value = _availableTracks.value.filter { it.id != trackId }
        val updatedSelected = _selectedTrackIds.value.toMutableSet().apply { remove(trackId) }
        _selectedTrackIds.value = updatedSelected
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stopPlayback()
    }
}
