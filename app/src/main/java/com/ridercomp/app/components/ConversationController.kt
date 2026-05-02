package com.ridercomp.app.components

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConversationController(
    private val context: Context,
    private val voiceInputManager: VoiceInputManager,
    private val voiceOutputManager: VoiceOutputManager,
    private val memoryManager: MemoryManager,
    private val aiEngine: AIEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private var checkInJob: Job? = null
    
    // Check-in interval: 20 minutes (in milliseconds)
    private val CHECK_IN_INTERVAL = 20L * 60L * 1000L

    init {
        // Listen to voice input results
        viewModelScope.launch {
            voiceInputManager.spokenText.collect { text ->
                if (text.isNotEmpty()) {
                    handleUserInput(text)
                }
            }
        }
        
        viewModelScope.launch {
            voiceInputManager.isListening.collect { listening ->
                _uiState.value = _uiState.value.copy(isListening = listening)
            }
        }

        viewModelScope.launch {
            voiceOutputManager.isSpeaking.collect { speaking ->
                _uiState.value = _uiState.value.copy(isSpeaking = speaking)
            }
        }

        startRiderContextTracker()
    }

    private fun handleUserInput(userInput: String) {
        viewModelScope.launch {
            // Update UI
            _uiState.value = _uiState.value.copy(lastUserInput = userInput, isProcessing = true)
            
            // Get Context
            val history = memoryManager.getRecentContext()
            val userName = memoryManager.getUserName()

            // Generate AI Response
            val aiResponse = aiEngine.generateResponse(userInput, history, userName)

            // Save to Memory
            memoryManager.saveConversation(userInput, aiResponse)

            // Speak Response
            voiceOutputManager.speak(aiResponse)
            
            // Update UI
            _uiState.value = _uiState.value.copy(lastAiResponse = aiResponse, isProcessing = false)

            // Reset check-in timer since user just interacted
            resetRiderContextTracker()
        }
    }

    fun toggleListening() {
        if (_uiState.value.isListening) {
            voiceInputManager.stopListening()
        } else {
            voiceOutputManager.stop()
            voiceInputManager.startListening()
        }
    }

    private fun startRiderContextTracker() {
        checkInJob = viewModelScope.launch {
            while (true) {
                delay(CHECK_IN_INTERVAL)
                triggerCheckIn()
            }
        }
    }

    private fun resetRiderContextTracker() {
        checkInJob?.cancel()
        startRiderContextTracker()
    }

    private fun triggerCheckIn() {
        viewModelScope.launch {
            val checkInMsg = "You have been riding for a while. Feeling okay?"
            voiceOutputManager.speak(checkInMsg)
            _uiState.value = _uiState.value.copy(lastAiResponse = checkInMsg)
            // Optionally auto-start listening for a response
            // delay(3000)
            // voiceInputManager.startListening()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceInputManager.destroy()
        voiceOutputManager.destroy()
        checkInJob?.cancel()
    }
}

data class UIState(
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val isSpeaking: Boolean = false,
    val lastUserInput: String = "",
    val lastAiResponse: String = ""
)
