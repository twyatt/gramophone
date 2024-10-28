package com.traviswyatt.gramophone

import com.juul.khronicle.Log
import platform.Speech.SFSpeechRecognitionResult
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognitionTaskDelegateProtocol
import platform.Speech.SFTranscription
import platform.darwin.NSObject

val SFSpeechRecognitionTaskDelegateProtocolLogger: SFSpeechRecognitionTaskDelegateProtocol =
    object : NSObject(), SFSpeechRecognitionTaskDelegateProtocol {
        override fun speechRecognitionDidDetectSpeech(task: SFSpeechRecognitionTask) {
            Log.verbose {
                "speechRecognitionDidDetectSpeech task.state: ${task.state}"
            }
        }

        override fun speechRecognitionTask(
            task: SFSpeechRecognitionTask,
            didFinishRecognition: SFSpeechRecognitionResult,
        ) {
            Log.verbose {
                "speechRecognitionTask task.state: ${task.state}, didFinishRecognition: $didFinishRecognition"
            }
        }

        override fun speechRecognitionTask(
            task: SFSpeechRecognitionTask,
            didFinishSuccessfully: Boolean,
        ) {
            Log.verbose {
                "speechRecognitionTask task.state: ${task.state}, didFinishSuccessfully: $didFinishSuccessfully"
            }
        }

        override fun speechRecognitionTask(
            task: SFSpeechRecognitionTask,
            didHypothesizeTranscription: SFTranscription,
        ) {
            Log.verbose {
                "speechRecognitionTask task.state: ${task.state}, didHypothesizeTranscription: $didHypothesizeTranscription"
            }
        }

        override fun speechRecognitionTaskFinishedReadingAudio(task: SFSpeechRecognitionTask) {
            Log.verbose {
                "speechRecognitionTaskFinishedReadingAudio task.state: ${task.state}"
            }
        }

        override fun speechRecognitionTaskWasCancelled(task: SFSpeechRecognitionTask) {
            Log.verbose {
                "speechRecognitionTaskWasCancelled task.state: ${task.state}"
            }
        }
    }
