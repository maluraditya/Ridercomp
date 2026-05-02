package com.ridercomp.app.components

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceOutputManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("VoiceOutputManager", "The Language specified is not supported!")
            } else {
                _isReady.value = true
                // Make the voice sound slightly slower and lower pitch to be more calming/empathetic
                tts?.setSpeechRate(0.9f)
                tts?.setPitch(0.95f)
            }
        } else {
            Log.e("VoiceOutputManager", "Initilization Failed!")
        }
    }

    fun speak(text: String) {
        if (_isReady.value) {
            _isSpeaking.value = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "RiderCompanionTTS")
            // A more sophisticated implementation would use UtteranceProgressListener 
            // to accurately track when speaking starts and stops.
            _isSpeaking.value = false
        } else {
            Log.w("VoiceOutputManager", "TTS not ready yet")
        }
    }

    fun stop() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        _isSpeaking.value = false
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
    }
}
