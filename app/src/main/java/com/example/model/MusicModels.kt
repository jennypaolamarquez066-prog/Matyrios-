package com.example.model

enum class StemType(val displayName: String, val icon: String) {
    DRUMS("Batería", "🥁"),
    BASS("Bajo", "🎸"),
    MELODY("Melodía", "🎹"),
    PAD("Ambiente / Pads", "✨"),
    FX("Efectos", "⚡")
}

data class AudioStem(
    val id: String,
    val stemType: StemType,
    val name: String,
    var volume: Float = 0.8f,
    var isMuted: Boolean = false,
    var isSolo: Boolean = false,
    val soundPattern: Int = 0
)

data class MusicTrack(
    val id: String,
    val title: String,
    val prompt: String,
    val genre: String,
    var bpm: Int = 120,
    val keyScale: String = "C Minor",
    val durationSeconds: Int = 30,
    val stems: List<AudioStem> = emptyList(),
    val isMix: Boolean = false,
    val sourceTrackCount: Int = 1,
    val waveformData: List<Float> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    var pitchShift: Float = 1.0f,
    var bassBoost: Float = 1.0f,
    var reverbAmount: Float = 0.2f
)

enum class MessageSender {
    USER, GEMINI
}

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachedTrack: MusicTrack? = null,
    val isUnlockAction: Boolean = false,
    val quickActions: List<String> = emptyList()
)
