package com.traviswyatt.qd

import com.juul.khronicle.Log
import kotlin.time.Duration.Companion.seconds
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.LAZY
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioEngine
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognitionTaskHintDictation
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerDelegateProtocol
import platform.darwin.NSObject

/**
 * How long after the last transcription was received to consider speaking complete.
 *
 * This should be long enough to not timeout while someone is speaking a long work, but not too long
 * that it feels like a long delay after someone is done speaking.
 */
private val SpeechTimeout = 5.seconds

class AppleDictation(
    private val scope: CoroutineScope,
    private val commander: Commander,
) : Dictation {

    override val isAvailable = MutableStateFlow(false)
    private val task = MutableStateFlow<SFSpeechRecognitionTask?>(null)
    override val isDictating = task.map { it != null }

    private val recognizer = SFSpeechRecognizer(NSLocale.currentLocale).apply {
        delegate = object : NSObject(), SFSpeechRecognizerDelegateProtocol {
            override fun speechRecognizer(
                speechRecognizer: SFSpeechRecognizer,
                availabilityDidChange: Boolean
            ) {
                Log.info { "SFSpeechRecognizer availabilityDidChange: $availabilityDidChange" }
                isAvailable.value = availabilityDidChange
            }
        }
    }

    private val engine = AVAudioEngine()

    override fun start() {
        @OptIn(FlowPreview::class)
        val job = scope.launch(start = LAZY) {
            transcript.timeout(SpeechTimeout).catch {
                if (it is TimeoutCancellationException) task.value?.cancel()
            }.collect {}
        }

        val request = SFSpeechAudioBufferRecognitionRequest().apply {
            addsPunctuation = false
            shouldReportPartialResults = true
            taskHint = SFSpeechRecognitionTaskHintDictation
        }

        var lastTranscription: String? = null
        task.value = recognizer.recognitionTaskWithRequest(request) { result, error ->
            val transcription = result?.bestTranscription?.formattedString?.takeUnless { it.isEmpty() }
            if (transcription != null) {
                job.start()
                lastTranscription = result.bestTranscription.formattedString
            }
            val isFinal = result?.final == true

            Log.verbose {
                "transcription: $transcription, isFinal: $isFinal"
            }

            if (error != null || isFinal) {
                Log.verbose { "Cancel" }
                job.cancel()
                engine.stop()
                engine.inputNode.removeTapOnBus(0u)
                task.value = null

                (transcription ?: lastTranscription)?.let {
                    if (!commander.handle(it)) {
                        transcript.value = it
                    }
                }
            }
        }

        engine.inputNode.installTapOnBus(
            0u,
            1024u,
            engine.inputNode.outputFormatForBus(0u),
        ) { buffer, _ ->
            request.appendAudioPCMBuffer(buffer!!)
        }

        engine.prepare()

        @OptIn(ExperimentalForeignApi::class)
        engine.startAndReturnError(null)
    }

    override fun toggle() {
        if (task.value != null) {
            cancel()
        } else {
            start()
        }
    }

    override fun cancel() {
        task.value?.cancel()
    }
}
