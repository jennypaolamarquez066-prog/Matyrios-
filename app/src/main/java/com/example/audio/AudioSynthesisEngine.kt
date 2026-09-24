package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.AudioStem
import com.example.model.MusicTrack
import com.example.model.StemType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class AudioSynthesisEngine {
    private val sampleRate = 22050
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrackId = MutableStateFlow<String?>(null)
    val currentTrackId: StateFlow<String?> = _currentTrackId.asStateFlow()

    private val _currentPositionSec = MutableStateFlow(0f)
    val currentPositionSec: StateFlow<Float> = _currentPositionSec.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    companion object {
        const val TRACK_DURATION_SECONDS = 30
    }

    /**
     * Synthesizes 30 seconds of audio data for a given track
     */
    fun synthesizeTrack(track: MusicTrack): ShortArray {
        val totalSamples = sampleRate * TRACK_DURATION_SECONDS
        val buffer = FloatArray(totalSamples)

        val activeStems = track.stems.filter { !it.isMuted }
        val hasSolo = track.stems.any { it.isSolo }
        val effectiveStems = if (hasSolo) track.stems.filter { it.isSolo } else activeStems

        val effectiveBpm = track.bpm.coerceIn(60, 200)
        val pitchMultiplier = track.pitchShift.coerceIn(0.5f, 2.0f)
        val bassBoost = track.bassBoost.coerceIn(1.0f, 3.0f)

        // Musical scale frequencies (e.g. C Minor / Pentatonic)
        val baseFreqs = when (track.genre.lowercase()) {
            "lofi", "lofi hip hop" -> floatArrayOf(130.81f, 155.56f, 174.61f, 196.00f, 233.08f, 261.63f)
            "synthwave", "synthwave 80s" -> floatArrayOf(110.00f, 130.81f, 146.83f, 164.81f, 196.00f, 220.00f)
            "cyberpunk", "cyberpunk trap" -> floatArrayOf(98.00f, 116.54f, 130.81f, 146.83f, 174.61f, 196.00f)
            "reggaeton", "reggaeton pop" -> floatArrayOf(123.47f, 146.83f, 164.81f, 185.00f, 220.00f, 246.94f)
            else -> floatArrayOf(130.81f, 146.83f, 164.81f, 174.61f, 196.00f, 220.00f)
        }

        // Generate audio per stem
        for (stem in effectiveStems) {
            val stemVolume = stem.volume.coerceIn(0f, 1f)
            when (stem.stemType) {
                StemType.DRUMS -> synthesizeDrums(buffer, totalSamples, effectiveBpm, stemVolume, track.genre)
                StemType.BASS -> synthesizeBass(buffer, totalSamples, effectiveBpm, stemVolume * bassBoost, baseFreqs, pitchMultiplier)
                StemType.MELODY -> synthesizeMelody(buffer, totalSamples, effectiveBpm, stemVolume, baseFreqs, pitchMultiplier)
                StemType.PAD -> synthesizePad(buffer, totalSamples, effectiveBpm, stemVolume, baseFreqs, pitchMultiplier)
                StemType.FX -> synthesizeFx(buffer, totalSamples, effectiveBpm, stemVolume, track.reverbAmount)
            }
        }

        // Apply simple reverb if configured
        if (track.reverbAmount > 0.05f) {
            applyReverb(buffer, totalSamples, track.reverbAmount)
        }

        // Convert FloatArray to 16-bit PCM ShortArray with normalization
        var maxPeak = 0.0001f
        for (i in 0 until totalSamples) {
            val absVal = kotlin.math.abs(buffer[i])
            if (absVal > maxPeak) maxPeak = absVal
        }

        val normFactor = if (maxPeak > 0.95f) 0.95f / maxPeak else 0.85f
        val outPcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val sample = (buffer[i] * normFactor).coerceIn(-1.0f, 1.0f)
            outPcm[i] = (sample * 32767).toInt().toShort()
        }

        return outPcm
    }

    /**
     * Mixes 2 to 5 tracks together into a single 30-second balanced master track
     */
    fun mixTracks(tracks: List<MusicTrack>, mixTitle: String = "Gemini Mix"): MusicTrack {
        require(tracks.size in 2..5) { "Debe haber entre 2 y 5 canciones para mezclar." }

        // Blend BPMs (average or dominant)
        val averageBpm = tracks.map { it.bpm }.average().toInt()
        val primaryGenre = tracks.first().genre

        // Combine unique stems from all source tracks with balanced gain
        val combinedStems = mutableListOf<AudioStem>()
        val stemTypesPresent = mutableSetOf<StemType>()

        var stemIdx = 1
        for (t in tracks) {
            for (s in t.stems) {
                // Adjust volume proportionally to number of tracks to prevent clipping
                val adjustedVolume = (s.volume * (1.1f / tracks.size)).coerceIn(0.2f, 0.9f)
                combinedStems.add(
                    s.copy(
                        id = "mix_stem_${stemIdx++}",
                        name = "${t.title}: ${s.name}",
                        volume = adjustedVolume
                    )
                )
                stemTypesPresent.add(s.stemType)
            }
        }

        // Generate synthetic waveform preview
        val waveform = generateVisualWaveform()

        return MusicTrack(
            id = "mix_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}",
            title = mixTitle,
            prompt = "Mix híbrido de ${tracks.size} pistas: " + tracks.joinToString(", ") { it.title },
            genre = "$primaryGenre Fusion",
            bpm = averageBpm,
            keyScale = tracks.first().keyScale,
            durationSeconds = TRACK_DURATION_SECONDS,
            stems = combinedStems,
            isMix = true,
            sourceTrackCount = tracks.size,
            waveformData = waveform
        )
    }

    private fun synthesizeDrums(buffer: FloatArray, totalSamples: Int, bpm: Int, volume: Float, genre: String) {
        val beatSamples = (sampleRate * 60f / bpm).toInt()
        val stepSamples = beatSamples / 4 // 16th note steps

        var step = 0
        var sampleIdx = 0
        val isReggaeton = genre.contains("reggaeton", ignoreCase = true)

        while (sampleIdx < totalSamples) {
            val stepInBar = step % 16

            // Kick drum: on beats 0, 4, 8, 12 (4-on-the-floor) or reggaeton dembow
            val isKick = if (isReggaeton) {
                stepInBar == 0 || stepInBar == 4 || stepInBar == 8 || stepInBar == 12
            } else {
                stepInBar == 0 || stepInBar == 8 || (stepInBar == 14 && step % 32 == 14)
            }

            if (isKick) {
                val kickLen = min((sampleRate * 0.2f).toInt(), totalSamples - sampleIdx)
                for (k in 0 until kickLen) {
                    val progress = k.toFloat() / kickLen
                    val freq = 130f * (1f - progress * 0.7f) + 40f
                    val env = exp(-progress * 9f)
                    val sample = sin(2 * PI * freq * k / sampleRate).toFloat() * env * 0.8f * volume
                    buffer[sampleIdx + k] += sample
                }
            }

            // Snare drum: on steps 4, 12 (standard) or dembow snare (steps 3, 7, 11, 15)
            val isSnare = if (isReggaeton) {
                stepInBar == 3 || stepInBar == 7 || stepInBar == 10 || stepInBar == 14
            } else {
                stepInBar == 4 || stepInBar == 12
            }

            if (isSnare) {
                val snareLen = min((sampleRate * 0.18f).toInt(), totalSamples - sampleIdx)
                for (s in 0 until snareLen) {
                    val progress = s.toFloat() / snareLen
                    val noise = (Random.nextFloat() * 2f - 1f) * 0.6f
                    val tone = sin(2 * PI * 180f * s / sampleRate).toFloat() * 0.4f
                    val env = exp(-progress * 11f)
                    val sample = (noise + tone) * env * 0.65f * volume
                    buffer[sampleIdx + s] += sample
                }
            }

            // Hi-hats: on even steps (8th notes or 16th notes)
            if (stepInBar % 2 == 0) {
                val hatLen = min((sampleRate * 0.05f).toInt(), totalSamples - sampleIdx)
                val accent = if (stepInBar % 4 == 0) 0.5f else 0.3f
                for (h in 0 until hatLen) {
                    val progress = h.toFloat() / hatLen
                    val noise = (Random.nextFloat() * 2f - 1f)
                    val env = exp(-progress * 28f)
                    buffer[sampleIdx + h] += noise * env * accent * volume
                }
            }

            sampleIdx += stepSamples
            step++
        }
    }

    private fun synthesizeBass(
        buffer: FloatArray,
        totalSamples: Int,
        bpm: Int,
        volume: Float,
        freqs: FloatArray,
        pitchMult: Float
    ) {
        val beatSamples = (sampleRate * 60f / bpm).toInt()
        val barSamples = beatSamples * 4
        var pos = 0
        var bar = 0

        while (pos < totalSamples) {
            val rootFreq = freqs[bar % freqs.size] * 0.5f * pitchMult
            val noteDuration = (beatSamples * 0.85f).toInt()

            for (b in 0 until 4) {
                val noteStart = pos + b * beatSamples
                if (noteStart >= totalSamples) break
                val len = min(noteDuration, totalSamples - noteStart)

                val currentFreq = if (b == 3) rootFreq * 1.25f else rootFreq

                for (i in 0 until len) {
                    val t = i.toFloat() / sampleRate
                    val progress = i.toFloat() / len
                    val env = exp(-progress * 3.5f)
                    // Sub bass sine + subtle 2nd harmonic
                    val fundamental = sin(2 * PI * currentFreq * t).toFloat()
                    val harmonic = sin(2 * PI * currentFreq * 2 * t).toFloat() * 0.25f
                    buffer[noteStart + i] += (fundamental + harmonic) * env * 0.7f * volume
                }
            }

            pos += barSamples
            bar++
        }
    }

    private fun synthesizeMelody(
        buffer: FloatArray,
        totalSamples: Int,
        bpm: Int,
        volume: Float,
        freqs: FloatArray,
        pitchMult: Float
    ) {
        val stepSamples = (sampleRate * 60f / bpm / 2).toInt() // 8th note arpeggio
        var pos = 0
        var step = 0

        val melodyNotes = freqs.map { it * 2f * pitchMult }.toFloatArray()

        while (pos < totalSamples) {
            val noteIndex = (step * 3 + (step / 8)) % melodyNotes.size
            val noteFreq = melodyNotes[noteIndex]
            val noteLen = min((stepSamples * 0.9f).toInt(), totalSamples - pos)

            for (i in 0 until noteLen) {
                val t = i.toFloat() / sampleRate
                val progress = i.toFloat() / noteLen
                val env = exp(-progress * 5.0f)
                // Pluck synth
                val sine = sin(2 * PI * noteFreq * t).toFloat()
                val overtone = sin(2 * PI * noteFreq * 3 * t).toFloat() * 0.18f
                buffer[pos + i] += (sine + overtone) * env * 0.45f * volume
            }

            pos += stepSamples
            step++
        }
    }

    private fun synthesizePad(
        buffer: FloatArray,
        totalSamples: Int,
        bpm: Int,
        volume: Float,
        freqs: FloatArray,
        pitchMult: Float
    ) {
        val barSamples = (sampleRate * 60f / bpm * 4).toInt()
        var pos = 0
        var bar = 0

        while (pos < totalSamples) {
            val chordRoot = freqs[bar % freqs.size] * pitchMult
            val chordThird = chordRoot * 1.25f
            val chordFifth = chordRoot * 1.5f

            val len = min(barSamples, totalSamples - pos)

            for (i in 0 until len) {
                val t = i.toFloat() / sampleRate
                val progress = i.toFloat() / len
                // Smooth fade in / fade out envelope
                val env = sin(PI * progress).toFloat()
                val wave1 = sin(2 * PI * chordRoot * t).toFloat()
                val wave2 = sin(2 * PI * chordThird * t).toFloat() * 0.8f
                val wave3 = sin(2 * PI * chordFifth * t).toFloat() * 0.7f
                buffer[pos + i] += (wave1 + wave2 + wave3) * env * 0.22f * volume
            }

            pos += barSamples
            bar++
        }
    }

    private fun synthesizeFx(buffer: FloatArray, totalSamples: Int, bpm: Int, volume: Float, reverb: Float) {
        // Futuristic sweep every 8 bars
        val sweepInterval = (sampleRate * 60f / bpm * 8).toInt()
        var pos = 0

        while (pos < totalSamples) {
            val sweepLen = min((sampleRate * 1.5f).toInt(), totalSamples - pos)
            for (i in 0 until sweepLen) {
                val progress = i.toFloat() / sweepLen
                val freq = 300f + progress * 2400f
                val t = i.toFloat() / sampleRate
                val env = exp(-progress * 2.5f) * sin(PI * progress).toFloat()
                val sample = sin(2 * PI * freq * t).toFloat() * env * 0.25f * volume
                buffer[pos + i] += sample
            }
            pos += sweepInterval
        }
    }

    private fun applyReverb(buffer: FloatArray, totalSamples: Int, amount: Float) {
        val delaySamples = (sampleRate * 0.12f).toInt()
        val decay = amount.coerceIn(0.1f, 0.7f)
        for (i in delaySamples until totalSamples) {
            buffer[i] += buffer[i - delaySamples] * decay
        }
    }

    /**
     * Plays the audio track in a background coroutine
     */
    fun playTrack(track: MusicTrack, onProgress: (Float) -> Unit = {}) {
        stopPlayback()

        playbackJob = scope.launch {
            _currentTrackId.value = track.id
            _isPlaying.value = true

            val pcmData = synthesizeTrack(track)
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val trackInstance = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(max(bufferSize, pcmData.size * 2))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = trackInstance
            trackInstance.play()

            val chunkSize = 2048
            var offset = 0
            val totalSamples = pcmData.size

            while (isActive && offset < totalSamples && _isPlaying.value) {
                val toWrite = min(chunkSize, totalSamples - offset)
                trackInstance.write(pcmData, offset, toWrite)

                // Update current position
                val currentSec = (offset.toFloat() / sampleRate).coerceIn(0f, TRACK_DURATION_SECONDS.toFloat())
                _currentPositionSec.value = currentSec
                onProgress(currentSec)

                // Peak amplitude for UI wave bounce
                var chunkPeak = 0f
                for (c in 0 until toWrite) {
                    val amp = kotlin.math.abs(pcmData[offset + c].toFloat()) / 32768f
                    if (amp > chunkPeak) chunkPeak = amp
                }
                _amplitude.value = chunkPeak

                offset += toWrite
            }

            // Loop or complete
            if (_isPlaying.value && offset >= totalSamples) {
                _currentPositionSec.value = TRACK_DURATION_SECONDS.toFloat()
                delay(300)
                _isPlaying.value = false
                _currentPositionSec.value = 0f
            }

            try {
                trackInstance.stop()
                trackInstance.release()
            } catch (_: Exception) {}
        }
    }

    fun stopPlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        _amplitude.value = 0f
    }

    fun generateVisualWaveform(pointCount: Int = 40): List<Float> {
        val list = mutableListOf<Float>()
        var prev = 0.4f
        for (i in 0 until pointCount) {
            val target = Random.nextFloat() * 0.75f + 0.15f
            prev = prev * 0.4f + target * 0.6f
            list.add(prev)
        }
        return list
    }
}
