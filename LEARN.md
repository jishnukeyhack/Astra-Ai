# 📚 LEARN.md - Astra AI Secretary App

Welcome to the **Astra AI Secretary App** learning guide! This document will help you understand the project structure, technologies used, and how to contribute effectively to this GirlScript Summer of Code (GSSoC) project.

## 🎯 Project Overview

**Astra AI** is an AI-powered personal secretary Android application designed to assist users with various tasks through voice and text interactions. It utilizes a local LLM (LLaMA 3 via Ollama) to provide intelligent responses and features memory-based context and retrieval-augmented generation (RAG) capabilities.

### Key Features
- 🎤 Voice and text input support
- 🧠 Intelligent responses using LLaMA 3
- 💾 Memory management for context-aware interactions
- 💬 User-friendly chat interface
- ⚙️ Settings management for user preferences
- 🔊 Text-to-Speech and Speech Recognition
- 🎯 Wake word detection with background listening

## 📱 Technology Stack

- **Platform**: Android
- **Language**: Kotlin
- **Build System**: Gradle with Kotlin DSL
- **Architecture**: Modern Android Architecture Components

## 📁 Project Structure

### Root Level Files

- **`build.gradle.kts`** - Main project build configuration using Kotlin DSL
- **`settings.gradle.kts`** - Gradle settings and module configuration
- **`gradle.properties`** - Project-wide Gradle properties and build optimizations
- **`gradlew` / `gradlew.bat`** - Gradle wrapper scripts for Unix/Windows
- **`local.properties`** - Local development properties (SDK paths, API keys)
- **`secrets.properties`** - Secure configuration file for sensitive data
- **`secrets.properties.template`** - Template for required secret configurations
- **`LICENSE`** - Project license information
- **`README.md`** - Main project documentation

### Core Directories

#### 📱 `/app` Directory
The main Android application module containing all source code and resources.

- **`build.gradle.kts`** - App-specific build configuration and dependencies
- **`src/main/`** - Primary source code directory
  - **`AndroidManifest.xml`** - App permissions, components, and configuration
  - **`kotlin/`** - All Kotlin source code organized by packages
  - **`res/`** - Android resources (layouts, strings, colors, etc.)
- **`src/test/`** - Unit test files
- **`build/`** - Generated build artifacts and intermediate files

## 🏗️ Detailed Source Code Architecture

### Main Application Structure (`src/main/kotlin/com/example/aisecretary/`)

#### 🎯 Core Application
- **`MainActivity.kt`** - Main entry point and navigation host
- **`SecretaryApplication.kt`** - Application class for global initialization

#### 💬 UI Layer (`ui/`)
- **`chat/`** - Chat interface components
  - `ChatFragment.kt` - Main chat screen UI
  - `ChatViewModel.kt` - Chat logic and state management
  - `MessageAdapter.kt` - RecyclerView adapter for messages
- **`settings/`** - Settings and preferences
  - `SettingsFragment.kt` - Settings screen UI
  - `SettingsViewModel.kt` - Settings logic and preferences
- **`memory/`** - Memory management interface
  - `MemoryFragment.kt` - Memory visualization screen
  - `MemoryViewModel.kt` - Memory operations logic
  - `MemoryAdapter.kt` - Adapter for memory items display

#### 📊 Data Layer (`data/`)
- **`model/`** - Data models and entities
  - `Message.kt` - Chat message data structure
  - `ConversationContext.kt` - Context for AI conversations
- **`repository/`** - Data access abstraction
  - `ChatRepository.kt` - Chat data operations
  - `VoiceRepository.kt` - Voice processing operations
- **`local/`** - Local data storage
  - `database/` - Room database components
    - `AppDatabase.kt` - Main database configuration
    - `dao/MessageDao.kt` - Message data access object
  - `preferences/UserPreferences.kt` - Shared preferences wrapper

#### 🤖 AI Components (`ai/`)
- **`llm/`** - Large Language Model integration
  - `LlamaClient.kt` - LLaMA 3 API client
  - `OllamaService.kt` - Ollama service integration
- **`voice/`** - Voice processing
  - `SpeechRecognizer.kt` - Speech-to-text functionality
  - `TextToSpeech.kt` - Text-to-speech functionality
- **`memory/`** - AI memory system
  - `ConversationMemory.kt` - Context memory management
  - `MemoryManager.kt` - Memory operations and cleanup
- **`rag/`** - Retrieval Augmented Generation
  - `DocumentStore.kt` - Document storage for RAG
  - `Retriever.kt` - Information retrieval logic
  - `VectorStore.kt` - Vector embeddings storage

#### 🔧 Dependency Injection (`di/`)
- **`AppModule.kt`** - Dagger/Hilt module for dependency injection

### 🎨 Resources Directory (`src/main/res/`)

#### 📱 UI Resources
- **`layout/`** - XML layout files for activities and fragments
  - `activity_main.xml` - Main activity layout
  - `fragment_chat.xml` - Chat screen layout
  - `fragment_settings.xml` - Settings screen layout
- **`drawable/`** - Vector drawables, images, and drawable resources
- **`mipmap-anydpi-v26/`** - App icons for different screen densities

#### 🧭 Navigation
- **`navigation/`** - Navigation component graphs
  - `nav_graph.xml` - App navigation flow

#### 🎨 Styling & Content
- **`values/`** - App-wide values and configurations
  - `colors.xml` - Color palette definitions
  - `strings.xml` - Text strings and localizations
  - `themes.xml` - Material Design themes and styles
- **`menu/`** - Menu definitions for navigation and options

### 🧪 Testing (`src/test/`)
- **`kotlin/com/example/aisecretary/`** - Unit tests
  - `LlmClientTest.kt` - Tests for LLM integration

## 🛠️ Technical Setup

### Prerequisites
- **Android Studio**: Latest stable version (Arctic Fox or newer)
- **JDK**: Java 17
- **Android SDK**: API level 21 (minimum) to 34 (target)
- **Kotlin**: 1.9.0 or newer
- **Gradle**: 8.0 or newer

### Key Dependencies
- **AndroidX Core & UI**: Material Design, ConstraintLayout, AppCompat
- **Architecture Components**: Lifecycle, ViewModel, Room Database
- **Networking**: Retrofit2 with Gson converter
- **Async Operations**: Kotlin Coroutines
- **Navigation**: Navigation Component
- **Image Loading**: Glide
- **Testing**: JUnit, Mockito, Robolectric, Espresso

### Configuration Files
- **`secrets.properties`**: Store sensitive configuration (API keys, URLs)
  - `OLLAMA_BASE_URL`: Local Ollama server endpoint
  - `LLAMA_MODEL_NAME`: LLaMA model identifier
- **`local.properties`**: SDK paths and local development settings

## 🚀 Getting Started for Contributors

### 1. Environment Setup
```bash
# Clone the repository
git clone https://github.com/A-Akhil/Astra-Ai.git
cd Astra-Ai

# Copy secrets template and configure
cp secrets.properties.template secrets.properties
# Edit secrets.properties with your configuration
```

### 2. Project Structure Understanding
- Start with `MainActivity.kt` to understand app flow
- Explore `ChatFragment.kt` for UI components
- Check `ChatViewModel.kt` for business logic
- Review `LlamaClient.kt` for AI integration

### 3. Development Workflow
1. **Pick an issue** from the GSSoC issue tracker
2. **Create a feature branch**: `git checkout -b feature/your-feature`
3. **Make changes** following the established patterns
4. **Test your changes** using unit and integration tests
5. **Submit a PR** with clear description and testing evidence

### 4. Code Style Guidelines
- Follow **Kotlin coding conventions**
- Use **MVVM architecture** for new features
- Implement **dependency injection** for new components
- Add **unit tests** for business logic
- Use **meaningful variable and function names**

## 🎯 Areas for Contribution

### 🟢 Beginner-Friendly
- UI improvements and bug fixes
- Documentation updates
- Test case additions
- Resource optimizations (strings, colors, layouts)

### 🟡 Intermediate
- New chat features and customizations
- Voice processing enhancements
- Database schema improvements
- Performance optimizations

### 🔴 Advanced
- AI model integration improvements
- Memory system enhancements
- RAG implementation features
- Architecture component additions

## 📋 App Permissions & Configuration

### Required Permissions
- **INTERNET**: For API calls to Ollama server
- **RECORD_AUDIO**: For voice input functionality
- **READ/WRITE_EXTERNAL_STORAGE**: For file operations and caching

### Application Configuration
- **Package**: `com.example.aisecretary`
- **Target SDK**: 34 (Android 14)
- **Minimum SDK**: 21 (Android 5.0)
- **Application Class**: `SecretaryApplication` for global initialization

## 🔧 Building & Running

### Development Build
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

### Configuration Setup
1. Copy `secrets.properties.template` to `secrets.properties`
2. Configure your Ollama server URL and model:
   ```properties
   OLLAMA_BASE_URL=http://your-server:11434
   LLAMA_MODEL_NAME=llama3:8b
   ```

## 📚 Learning Resources

### Android Development
- [Android Developer Documentation](https://developer.android.com/)
- [Kotlin Programming Language](https://kotlinlang.org/)
- [Modern Android Development](https://developer.android.com/modern-android-development)

### Architecture Patterns
- [MVVM Architecture](https://developer.android.com/topic/architecture)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Navigation Component](https://developer.android.com/guide/navigation)

### AI Integration
- [Ollama Documentation](https://ollama.com/)
- [LLaMA Model Information](https://ai.meta.com/blog/meta-llama-3/)

## 🔧 Troubleshooting

### Common Issues
**Build Issues:**
- Ensure Java 17 is installed and selected
- Check Android SDK installation
- Verify `local.properties` has correct SDK path
- Make sure `secrets.properties` exists with valid configuration

**Runtime Issues:**
- Check Ollama server is running and accessible
- Verify network permissions are granted
- Ensure microphone permissions for voice features
- Check device API level compatibility (minimum API 21)

**Development Environment:**
- Use Android Studio Arctic Fox or newer
- Enable Kotlin plugin
- Install Android SDK Tools and Platform Tools
- Configure proper emulator or physical device

### Getting Help
1. Check existing GitHub issues first
2. Search the project documentation
3. Ask in GSSoC Discord community
4. Create a detailed issue with logs and steps to reproduce

## 🧪 Testing Guidelines

### Unit Tests
- Located in `src/test/kotlin/`
- Run with `./gradlew test`
- Cover business logic and data operations
- Mock external dependencies

### Integration Tests
- Test complete user flows
- Verify AI integration works correctly
- Test voice processing functionality
- Validate database operations

### Testing Best Practices
- Write tests before implementing features (TDD)
- Use descriptive test names
- Test both success and failure scenarios
- Mock network calls and external services

## 📊 Project Status & Roadmap

### Current Version: 1.0
- ✅ Core chat functionality
- ✅ LLaMA 3 integration via Ollama
- ✅ Voice input/output
- ✅ Memory management system
- ✅ Settings and preferences
- ✅ Room database integration

### Upcoming Features
- 🔄 Enhanced RAG capabilities
- 🔄 Improved memory visualization
- 🔄 Advanced voice commands
- 🔄 Customizable AI personalities
- 🔄 Offline mode capabilities

### GSSoC Contribution Opportunities
- **UI/UX Improvements**: Enhance the chat interface and memory visualization
- **AI Features**: Improve memory management and RAG implementation
- **Performance**: Optimize database operations and memory usage
- **Testing**: Expand test coverage and add automated testing
- **Documentation**: Improve code comments and user guides
- **Accessibility**: Add accessibility features for better inclusion

## 🤝 Contributing to GSSoC

### Code of Conduct
Before contributing, please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md). We are committed to providing a welcoming, inclusive, and harassment-free experience for everyone in the GSSoC community.

### Issue Labels
- `good first issue`: Perfect for newcomers
- `hacktoberfest`: Part of Hacktoberfest celebration
- `enhancement`: New features and improvements
- `bug`: Bug fixes needed
- `documentation`: Documentation improvements

### Pull Request Guidelines
1. **Fork** the repository
2. **Create** a descriptive branch name
3. **Follow** the code style guidelines
4. **Add tests** for new functionality
5. **Update documentation** if needed
6. **Reference issues** in your PR description
7. **Ensure compliance** with the Code of Conduct

### Code Review Process
- All PRs require at least one review
- Ensure CI checks pass
- Address reviewer feedback promptly
- Maintain backwards compatibility
- Follow GSSoC community standards

## 📞 Support & Communication

- **GitHub Issues**: For bug reports and feature requests
- **Discussions**: For questions and community support
- **GSSoC Discord**: For real-time communication
- **Email**: gssoc@girlscript.tech for Code of Conduct violations

---

## 🏆 Recognition

This project is part of **GirlScript Summer of Code (GSSoC)** - an initiative to encourage open source contributions and provide learning opportunities for students and developers.

**Happy Coding! 🚀**

---
*Last updated: July 2025*
