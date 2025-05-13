# AI Secretary App

## Overview
The AI Secretary App is an AI-powered personal secretary application designed to assist users with various tasks through voice and text interactions. It utilizes a local LLM (LLaMA 3 via Ollama) to provide intelligent responses and has memory-based context and retrieval-augmented generation (RAG) capabilities.

## Features
- Voice and text input support
- Intelligent responses using LLaMA 3
- Memory management for context-aware interactions
- User-friendly chat interface
- Settings management for user preferences

## Project Structure
```
ai-secretary-app
├── app
│   ├── src
│   │   ├── main
│   │   │   ├── kotlin
│   │   │   │   └── com
│   │   │   │       └── example
│   │   │   │           └── aisecretary
│   │   │   │               ├── MainActivity.kt
│   │   │   │               ├── SecretaryApplication.kt
│   │   │   │               ├── ui
│   │   │   │               │   ├── chat
│   │   │   │               │   │   ├── ChatFragment.kt
│   │   │   │               │   │   ├── ChatViewModel.kt
│   │   │   │               │   │   └── MessageAdapter.kt
│   │   │   │               │   └── settings
│   │   │   │               │       ├── SettingsFragment.kt
│   │   │   │               │       └── SettingsViewModel.kt
│   │   │   │               ├── data
│   │   │   │               │   ├── model
│   │   │   │               │   │   ├── Message.kt
│   │   │   │               │   │   └── ConversationContext.kt
│   │   │   │               │   ├── repository
│   │   │   │               │   │   ├── ChatRepository.kt
│   │   │   │               │   │   └── VoiceRepository.kt
│   │   │   │               │   └── local
│   │   │   │               │       ├── database
│   │   │   │               │       │   ├── AppDatabase.kt
│   │   │   │               │       │   └── dao
│   │   │   │               │       │       └── MessageDao.kt
│   │   │   │               │       └── preferences
│   │   │   │               │           └── UserPreferences.kt
│   │   │   │               ├── ai
│   │   │   │               │   ├── llm
│   │   │   │               │   │   ├── LlamaClient.kt
│   │   │   │               │   │   └── OllamaService.kt
│   │   │   │               │   ├── voice
│   │   │   │               │   │   ├── SpeechRecognizer.kt
│   │   │   │               │   │   └── TextToSpeech.kt
│   │   │   │               │   ├── memory
│   │   │   │               │   │   ├── ConversationMemory.kt
│   │   │   │               │   │   └── MemoryManager.kt
│   │   │   │               │   └── rag
│   │   │   │               │       ├── DocumentStore.kt
│   │   │   │               │       ├── Retriever.kt
│   │   │   │               │       └── VectorStore.kt
│   │   │   │               └── di
│   │   │   │                   └── AppModule.kt
│   │   │   ├── res
│   │   │   │   ├── layout
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── fragment_chat.xml
│   │   │   │   │   └── fragment_settings.xml
│   │   │   │   ├── values
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── navigation
│   │   │   │       └── nav_graph.xml
│   │   │   └── AndroidManifest.xml
│   │   └── test
│   │       └── kotlin
│   │           └── com
│   │               └── example
│   │                   └── aisecretary
│   │                       └── LlmClientTest.kt
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Setup Instructions
1. Clone the repository:
   ```
   git clone https://github.com/A-Akhil/Astra-Ai.git
   ```
2. Open the project in your preferred IDE.
3. Ensure you have the necessary SDKs and dependencies installed.
4. Build and run the application on an Android device or emulator.

## Usage
- Launch the application and interact with the AI Secretary using voice or text.
- Access settings to customize your preferences.
- The app will remember previous interactions to provide context-aware responses.

## Contributing
Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

[![Contributors](https://contrib.rocks/image?repo=A-Akhil/CertiMaster)](https://github.com/A-Akhil/CertiMaster/graphs/contributors)

## License
This project is licensed under the MIT License. See the LICENSE file for details.

<div align="center">

## Please support the development by donating.

[![BuyMeACoffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/aakhil)

</div>
