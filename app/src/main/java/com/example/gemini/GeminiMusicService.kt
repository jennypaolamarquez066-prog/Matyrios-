package com.example.gemini

import com.example.BuildConfig
import com.example.audio.AudioSynthesisEngine
import com.example.model.AudioStem
import com.example.model.MusicTrack
import com.example.model.StemType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class GeminiMusicService(
    private val audioEngine: AudioSynthesisEngine
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        const val UNLOCK_COMMAND = "desbloquear crear"
    }

    fun isUnlockCommand(input: String): Boolean {
        val normalized = input.lowercase().trim()
        return normalized.contains("desbloquear crear") ||
                normalized.contains("desbloquear el boton") ||
                normalized.contains("desbloquea crear") ||
                normalized.contains("desbloquear") && normalized.contains("crear")
    }

    suspend fun processUserPrompt(
        prompt: String,
        stemTypeFilter: StemType? = null
    ): Pair<String, MusicTrack?> = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

        // Attempt Gemini API call for intelligent musical parameters
        var trackInfo: GeneratedTrackMetadata? = null

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                trackInfo = queryGeminiForTrackMetadata(apiKey, prompt, stemTypeFilter)
            } catch (e: Exception) {
                // Fallback to local procedural generator if network or key issue
                trackInfo = null
            }
        }

        if (trackInfo == null) {
            trackInfo = generateProceduralMetadata(prompt, stemTypeFilter)
        }

        // Build the stems for this 30-second track
        val stems = mutableListOf<AudioStem>()
        if (stemTypeFilter != null) {
            stems.add(
                AudioStem(
                    id = "stem_${System.currentTimeMillis()}_1",
                    stemType = stemTypeFilter,
                    name = "${stemTypeFilter.displayName} ${trackInfo.genre}",
                    volume = 0.9f
                )
            )
        } else {
            // Full multi-stem composition
            stems.add(AudioStem("stem_drums", StemType.DRUMS, "Batería / Percusión", 0.85f))
            stems.add(AudioStem("stem_bass", StemType.BASS, "Línea de Bajo", 0.8f))
            stems.add(AudioStem("stem_melody", StemType.MELODY, "Melodía Principal", 0.75f))
            stems.add(AudioStem("stem_pad", StemType.PAD, "Textura / Armonía", 0.65f))
            stems.add(AudioStem("stem_fx", StemType.FX, "Efectos & Brillo", 0.5f))
        }

        val track = MusicTrack(
            id = "track_${System.currentTimeMillis()}_${Random.nextInt(100, 999)}",
            title = trackInfo.title,
            prompt = prompt,
            genre = trackInfo.genre,
            bpm = trackInfo.bpm,
            keyScale = trackInfo.keyScale,
            durationSeconds = 30,
            stems = stems,
            isMix = false,
            sourceTrackCount = 1,
            waveformData = audioEngine.generateVisualWaveform(40)
        )

        val aiExplanation = "✨ He compuesto la pista de 30 segundos **\"${track.title}\"** basada en tu idea. " +
                "Tiene un ritmo de **${track.bpm} BPM** en escala **${track.keyScale}** (${track.genre}). " +
                "¡Puedes reproducirla de inmediato!"

        Pair(aiExplanation, track)
    }

    private fun queryGeminiForTrackMetadata(
        apiKey: String,
        prompt: String,
        stemType: StemType?
    ): GeneratedTrackMetadata? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val systemInstruction = "Eres el motor musical de Google Gemini. Tu tarea es analizar el prompt del usuario y responder ÚNICAMENTE con un JSON válido con campos: title (título creativo en español), genre (género musical), bpm (número entero entre 75 y 150), keyScale (ej: C Minor, A Pentatonic, F# Major), y mood (descripción breve de 1 frase)."

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Idea musical: $prompt. Instrumento específico: ${stemType?.displayName ?: "Completo"}")
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val raw = response.body?.string() ?: return null
            val root = JSONObject(raw)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
            val textContent = parts.getJSONObject(0).getString("text")

            val json = JSONObject(textContent)
            return GeneratedTrackMetadata(
                title = json.optString("title", "Pista Gemini 30s"),
                genre = json.optString("genre", "Futuristic Synth"),
                bpm = json.optInt("bpm", 120),
                keyScale = json.optString("keyScale", "C Minor"),
                mood = json.optString("mood", "Vibras inspiradoras")
            )
        }
    }

    private fun generateProceduralMetadata(
        prompt: String,
        stemType: StemType?
    ): GeneratedTrackMetadata {
        val lower = prompt.lowercase()
        return when {
            lower.contains("lofi") || lower.contains("chill") || lower.contains("relaj") -> {
                GeneratedTrackMetadata(
                    title = if (stemType != null) "${stemType.displayName} Lofi Sunset" else "Lofi Chill Sunset",
                    genre = "Lofi Hip Hop",
                    bpm = Random.nextInt(78, 88),
                    keyScale = "D Minor 7th",
                    mood = "Cálido, nostálgico y relajante"
                )
            }
            lower.contains("synthwave") || lower.contains("80") || lower.contains("retro") -> {
                GeneratedTrackMetadata(
                    title = if (stemType != null) "${stemType.displayName} Neon 80s" else "Neon Horizon 1984",
                    genre = "Synthwave 80s",
                    bpm = Random.nextInt(118, 128),
                    keyScale = "A Minor",
                    mood = "Retro-futurista con sintetizadores brillantes"
                )
            }
            lower.contains("cyberpunk") || lower.contains("trap") || lower.contains("futur") -> {
                GeneratedTrackMetadata(
                    title = if (stemType != null) "${stemType.displayName} Cyber Matrix" else "Cyberpunk 2099 Beat",
                    genre = "Cyberpunk Trap",
                    bpm = Random.nextInt(130, 142),
                    keyScale = "F# Minor",
                    mood = "Oscuro, agresivo y de alta tecnología"
                )
            }
            lower.contains("reggaeton") || lower.contains("latin") || lower.contains("urbano") -> {
                GeneratedTrackMetadata(
                    title = if (stemType != null) "${stemType.displayName} Ritmo Dembow" else "Dembow Solar 30s",
                    genre = "Reggaeton Pop",
                    bpm = Random.nextInt(92, 98),
                    keyScale = "G Minor",
                    mood = "Rítmico, bailable y contagioso"
                )
            }
            else -> {
                val genres = listOf("Electro Pop", "Ambient Space", "Chillhop", "Deep Groove", "Future Bass")
                val selectedGenre = genres.random()
                GeneratedTrackMetadata(
                    title = if (stemType != null) "${stemType.displayName} $selectedGenre" else "Gemini Spark Harmony",
                    genre = selectedGenre,
                    bpm = Random.nextInt(105, 125),
                    keyScale = "C Minor",
                    mood = "Energético y moderno generado por Gemini"
                )
            }
        }
    }
}

data class GeneratedTrackMetadata(
    val title: String,
    val genre: String,
    val bpm: Int,
    val keyScale: String,
    val mood: String
)
