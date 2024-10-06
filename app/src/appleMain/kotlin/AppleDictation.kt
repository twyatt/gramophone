package com.traviswyatt.qd

import com.juul.khronicle.Log
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import platform.AVFAudio.AVAudioEngine
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerDelegateProtocol
import platform.darwin.NSObject

class AppleDictation : Dictation {

    override val isAvailable = MutableStateFlow(false)
    private val task = MutableStateFlow<SFSpeechRecognitionTask?>(null)
    override val isDictating = task.map { it != null }
    override val transcript = MutableStateFlow("")

    private val recognizer = SFSpeechRecognizer(NSLocale.currentLocale).apply {
        delegate = object : NSObject(), SFSpeechRecognizerDelegateProtocol {
            override fun speechRecognizer(
                speechRecognizer: SFSpeechRecognizer,
                availabilityDidChange: Boolean
            ) {
                Log.info { "Available: $availabilityDidChange" }
                isAvailable.value = availabilityDidChange
            }
        }
    }

    private val engine = AVAudioEngine()

    @OptIn(ExperimentalForeignApi::class)
    override fun start() {
        cancel() // Cancel any in-progress dictation.

        val request = SFSpeechAudioBufferRecognitionRequest()
        request.shouldReportPartialResults = true
        task.value = recognizer.recognitionTaskWithRequest(request) { result, error ->
            if (error != null) {
                Log.error { error.description ?: "Unknown error" }
//                transcript.value = "⚠️ Error: ${error.description}"
                engine.inputNode.removeTapOnBus(0u)
                return@recognitionTaskWithRequest
            }

            if (result!!.final) {
                engine.inputNode.removeTapOnBus(0u)
            } else {
                val transcript = result.bestTranscription.formattedString
                Log.info { "Transcript: $transcript" }
                this.transcript.value = transcript
            }
        }

        engine.inputNode.installTapOnBus(
            0u,
            3200u,
            engine.inputNode.outputFormatForBus(0u),
        ) { buffer, _ ->
            request.appendAudioPCMBuffer(buffer!!)
        }

        engine.prepare()
        engine.startAndReturnError(null)
        Log.info { "Done!" }
    }

    override fun toggle() {
        TODO("Not yet implemented")
    }

    override fun cancel() {
        task.value?.cancel()
        task.value = null
        engine.inputNode.removeTapOnBus(0u)
    }
}
