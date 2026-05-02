package com.ridercomp.app.components

import kotlinx.coroutines.delay

interface AIEngine {
    suspend fun generateResponse(
        userInput: String,
        contextHistory: List<ConversationLog>,
        userName: String
    ): String
}

/**
 * A fallback implementation that uses rules and lightweight simulated logic.
 * In a real application, this would use MediaPipe LLM Inference Task for Gemma.
 */
class FallbackAIEngine : AIEngine {

    override suspend fun generateResponse(
        userInput: String,
        contextHistory: List<ConversationLog>,
        userName: String
    ): String {
        // Simulate inference delay
        delay(800)

        val lowerInput = userInput.lowercase()
        
        // 1. Empathy Layer: Detect emotional tone
        val empathyPrefix = detectEmotion(lowerInput)

        // 2. Generate Core Response
        val coreResponse = when {
            lowerInput.contains("hello") || lowerInput.contains("hi") -> 
                "Hey $userName! Ready for the ride?"
            lowerInput.contains("weather") -> 
                "It looks pretty clear ahead. Keep an eye out for crosswinds, though."
            lowerInput.contains("how long") || lowerInput.contains("distance") -> 
                "You've been riding for a while. Make sure to stay hydrated."
            lowerInput.contains("tired") || lowerInput.contains("exhausted") -> 
                "Take a break soon. There's a rest stop a few miles ahead."
            lowerInput.contains("lost") || lowerInput.contains("where am i") -> 
                "Don't worry. Pull over when it's safe, and we can check the map."
            else -> 
                "I hear you. Let's keep rolling safely."
        }

        return if (empathyPrefix.isNotEmpty()) "$empathyPrefix $coreResponse" else coreResponse
    }

    private fun detectEmotion(input: String): String {
        return when {
            input.contains("tired") || input.contains("exhausted") || input.contains("sleepy") -> 
                "That sounds tiring."
            input.contains("stressed") || input.contains("annoyed") || input.contains("angry") -> 
                "I'm here with you. Take a deep breath."
            input.contains("bored") -> 
                "Long stretches can get dull. Want to talk or stay quiet for a bit?"
            input.contains("sad") || input.contains("down") -> 
                "I'm sorry you're feeling that way. Remember I'm right here."
            else -> ""
        }
    }
}
