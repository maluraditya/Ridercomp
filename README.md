# Rider Companion

A voice-first AI companion for riders. Works offline, feels empathetic, and responds in a natural conversational tone.

## Features

- **Voice Input**: Uses Android's native `SpeechRecognizer` (supports offline on modern devices).
- **Voice Output**: Uses Android's native `TextToSpeech` for offline natural voices.
- **Local Memory**: Powered by Room Database for fast, local-first context storage.
- **AI Empathy Layer**: Pre-processes user input to detect emotional state and prepends empathetic human-like phrases.
- **Rider Context Tracker**: Automatically checks in on the rider every 20 minutes to ensure they are safe and alert.

## Architecture

This project is built using modern Android development practices:
- **Kotlin**
- **Jetpack Compose** for UI
- **Coroutines & Flow** for asynchronous state management
- **Room** for local SQLite storage

The architecture is highly modular:
- `VoiceInputManager`: Handles STT
- `VoiceOutputManager`: Handles TTS
- `MemoryManager`: Handles Room DB transactions
- `AIEngine`: Interface for the LLM. Currently uses `FallbackAIEngine` for simulated logic, which can be swapped with MediaPipe LLM Inference for models like Gemma.
- `ConversationController`: The main orchestrator connecting STT, AI, Memory, and TTS.

## Setup Instructions

1. **Prerequisites**: Ensure you have Android Studio installed (latest stable version).
2. **Open Project**: Select "Open" in Android Studio and navigate to the `Ridercomp` folder.
3. **Gradle Sync**: Android Studio should automatically prompt you to sync Gradle. Wait for this to complete.
4. **Run Application**:
   - Connect an Android device or start an Emulator.
   - Press the Run button (green triangle).
   - *Note*: Ensure the device has speech recognition capabilities and TTS engines installed (most Android devices have Google TTS and STT pre-installed).

## Usage

- Launch the app. Accept the Audio recording permissions.
- Tap the central circle to start listening.
- Speak naturally (e.g., "I'm feeling really tired from this ride").
- The app will respond with empathetic, context-aware audio.
- Leave the app open (or running in background, if services are expanded later) and it will automatically check in every 20 minutes.

## Future Enhancements
- Replace `FallbackAIEngine` with actual local Gemma weights using MediaPipe LLM Inference API.
- Add Foreground Service to allow continuous operation even when the app is in the background.
