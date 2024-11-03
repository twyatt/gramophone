package com.traviswyatt.gramophone

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH
import android.speech.RecognizerIntent.EXTRA_LANGUAGE
import android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL
import android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
import android.speech.SpeechRecognizer
import android.speech.SpeechRecognizer.RESULTS_RECOGNITION
import com.juul.khronicle.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidDictation(private val commander: Commander) : Dictation {

    private val intent = Intent(ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(EXTRA_LANGUAGE_MODEL, LANGUAGE_MODEL_FREE_FORM)
        putExtra(EXTRA_LANGUAGE, "en-US")
    }

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(applicationContext).apply {
        setRecognitionListener(object : RecognitionListenerStub() {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.debug { "onReadyForSpeech" }
                _isDictating.value = true
            }

            override fun onBeginningOfSpeech() {
                Log.debug { "onBeginningOfSpeech" }
                _isDictating.value = true
            }

            override fun onEndOfSpeech() {
                _isDictating.value = false
            }

            override fun onError(error: Int) {
                _isDictating.value = false
                Log.error { "RecognitionListener error $error" }
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (text != null) {
                    if (!commander.handle(text)) {
                        transcript.value = text
                    }
                } else {
                    Log.error { "Empty transcription results" }
                }
            }
        })
    }

    private val _isAvailable = MutableStateFlow(SpeechRecognizer.isRecognitionAvailable(applicationContext))
    override val isAvailable = _isAvailable.asStateFlow()

    private val _isDictating = MutableStateFlow(false)
    override val isDictating = _isDictating.asStateFlow()

    override fun start() {
        recognizer.startListening(intent)
    }

    override fun cancel() {
        recognizer.cancel()
    }
}
