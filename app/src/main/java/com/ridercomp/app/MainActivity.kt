package com.ridercomp.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ridercomp.app.components.ConversationController
import com.ridercomp.app.components.FallbackAIEngine
import com.ridercomp.app.components.MemoryManager
import com.ridercomp.app.components.VoiceInputManager
import com.ridercomp.app.components.VoiceOutputManager

class MainActivity : ComponentActivity() {

    private lateinit var controller: ConversationController

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Handle permission denial gracefully
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAudioPermission()

        // Initialize dependencies
        val voiceInputManager = VoiceInputManager(this)
        val voiceOutputManager = VoiceOutputManager(this)
        val memoryManager = MemoryManager(this)
        val aiEngine = FallbackAIEngine()

        controller = ConversationController(
            context = this,
            voiceInputManager = voiceInputManager,
            voiceOutputManager = voiceOutputManager,
            memoryManager = memoryManager,
            aiEngine = aiEngine
        )

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212) // Dark theme
                ) {
                    RiderCompanionApp(controller)
                }
            }
        }
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

@Composable
fun RiderCompanionApp(controller: ConversationController) {
    val uiState by controller.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Central visual indicator
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(
                    when {
                        uiState.isListening -> Color(0xFFE91E63) // Pink/Red for listening
                        uiState.isProcessing -> Color(0xFFFFC107) // Amber for processing
                        uiState.isSpeaking -> Color(0xFF4CAF50) // Green for speaking
                        else -> Color(0xFF333333) // Gray for idle
                    }
                )
                .clickable { controller.toggleListening() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when {
                    uiState.isListening -> "Listening..."
                    uiState.isProcessing -> "Thinking..."
                    uiState.isSpeaking -> "Speaking..."
                    else -> "Tap to Speak"
                },
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Transcript
        if (uiState.lastUserInput.isNotEmpty()) {
            Text(
                text = "You: ${uiState.lastUserInput}",
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        if (uiState.lastAiResponse.isNotEmpty()) {
            Text(
                text = "AI: ${uiState.lastAiResponse}",
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
