# 🤖 Agent Development Log

This document tracks significant changes, features, and architectural decisions made during the development of **CryptoSy** - an Android cryptocurrency tracking application with AI-powered chat interface.

---

## 📱 Project Overview

**CryptoSy** is an Android application built with Kotlin and Jetpack Compose that provides real-time cryptocurrency market updates through an AI-powered chat interface. The app integrates with an MCP (Model Context Protocol) server to deliver market data, news, and insights.

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: Clean Architecture with MVVM pattern
- **DI**: Hilt (configured, but using manual DI via AppContainer)
- **Networking**: Ktor Client
- **Serialization**: Kotlinx Serialization
- **Logging**: Timber
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36

---

## 🏗️ Architecture

### Clean Architecture Layers

1. **Presentation Layer** (`ui/`)
   - Jetpack Compose UI components
   - ChatScreen with LazyColumn for message display
   - Custom composables for crypto data visualization:
     - `FearGreedGauge` - Fear & Greed Index visualization
     - `PricesCard` - Cryptocurrency price display
     - `NewsCard` - News items display
     - `CoinInfoCard` - Detailed coin information
   - `ChatViewModel` - Manages UI state and handles user interactions

2. **Domain Layer** (`domain/`)
   - Use Cases:
     - `SendMessageUseCase` - Sends chat messages to MCP server
     - `GetSummaryUseCase` - Fetches periodic market summaries
     - `LoadToolsUseCase` - Loads available MCP tools
   - Domain Models (`Model.kt`):
     - `ChatItem` - Represents chat messages
     - `ChatPayload` - Sealed class for different content types (Text, Prices, FearGreed, News, Coin)
     - `ToolItem` - Represents available MCP tools

3. **Data Layer** (`data/`)
   - `ChatRepository` interface with `ChatRepositoryImpl`
   - `McpApi` - Ktor-based API client for MCP server
   - DTOs for API communication
   - `ChatCache` - Local message persistence using DataStore

### Key Design Patterns
- **Repository Pattern**: Abstracts data sources
- **Use Case Pattern**: Encapsulates business logic
- **Sealed Classes**: Type-safe payload handling
- **State Flow**: Reactive UI updates
- **Dependency Injection**: Manual DI via `AppContainer`

---

## 🚀 Core Features

### 1. AI Chat Interface
- Real-time chat with MCP-powered AI assistant
- Message history with caching
- Support for multiple content types:
  - Text messages
  - Cryptocurrency prices
  - Fear & Greed Index
  - News feeds
  - Coin information

### 2. Foreground Service
- `CryptoUpdateService` - Background service for periodic market updates
- Runs every 60 seconds when enabled
- Displays notification with last update time
- Emits updates via `SharedFlow` to ViewModel
- User-controlled start/stop via UI toggle

### 3. Tools Dialog
- Displays available MCP tools
- Pre-fills chat input with sample queries
- Quick access to common operations

### 4. Rich Data Visualization
- **Fear & Greed Gauge**: Animated gauge visualization with color-coded sentiment
- **Price Cards**: Real-time crypto prices with 1h change indicators
- **News Cards**: Formatted news items with timestamps
- **Coin Info**: Detailed cryptocurrency information with market cap

---

## 📝 Initial Commit (2025-11-20)

### Commit: `37e6821` - "git ignore"

This represents the initial project setup with complete application structure:

#### Application Setup
- Created `CryptoSy` Application class
- Notification channel setup for foreground service
- Timber logging initialization
- Manual DI container (`AppContainer`)

#### UI Implementation
- Material 3 theme with custom colors
- Dark theme support
- ChatScreen with auto-scrolling
- Input panel with send button and loading indicator
- Service control button in top bar
- Tools dialog for quick access

#### Data Layer
- Ktor HTTP client configuration with:
  - Content negotiation (JSON)
  - Logging
  - Android engine
  - 30-second timeout
- MCP API endpoints:
  - `/tools` - Get available tools
  - `/chat` - Send messages
  - `/summary` - Get market summary
- Local caching with DataStore

#### Domain Layer
- Clean separation of concerns
- Use cases for each major operation
- Rich domain models with sealed classes

#### Background Service
- Periodic updates every 60 seconds
- Foreground service with notification
- SharedFlow for reactive updates
- Error handling and logging

#### Build Configuration
- Gradle Kotlin DSL
- Version catalog for dependencies
- ProGuard configuration for release builds
- Build config fields for MCP base URL
- Java 21 compatibility

---

## 🔧 Configuration

### MCP Server
- **Base URL**: `http://10.0.2.2:8080` (Android emulator localhost)
- Configurable via `BuildConfig.MCP_BASE_URL`

### Update Interval
- **Default**: 60 seconds (1 minute)
- Configurable in `CryptoUpdateService.UPDATE_INTERVAL_MS`

### Notification Channel
- **ID**: `crypto_updates_channel`
- **Importance**: LOW
- **Category**: Service

---

## 📦 Dependencies

### Core Libraries
- `androidx.core:core-ktx`
- `androidx.lifecycle:lifecycle-runtime-ktx`
- `androidx.activity:activity-compose`

### Compose
- Compose BOM (Bill of Materials)
- Material 3
- Material Icons Extended
- ViewModel Compose integration

### Networking
- Ktor Client (Android engine)
- Content negotiation
- Kotlinx Serialization

### Utilities
- Timber (logging)
- DataStore Preferences
- Kotlinx Coroutines

---

## 🎯 Future Considerations

Based on the current architecture, potential areas for enhancement:

1. **Hilt Integration**: Currently configured but not used - could migrate from manual DI
2. **Navigation**: Navigation Compose is included but not implemented yet
3. **Testing**: Test dependencies are configured but no tests written yet
4. **Customizable Update Intervals**: Allow users to configure service update frequency
5. **Notification Actions**: Add actions to notification for quick interactions
6. **Offline Support**: Enhanced caching for offline viewing
7. **User Preferences**: Settings screen for API endpoint and other configurations

---

## 📄 Project Structure

```
app/src/main/kotlin/dev/kamikaze/cryptosy/
├── CryptoSy.kt                          # Application class
├── MainActivity.kt                       # Single activity
├── ChatViewModel.kt                      # Main ViewModel
├── data/
│   ├── cache/
│   │   └── ChatCache.kt                 # Local storage
│   ├── dto/
│   │   ├── ChatRequestDto.kt
│   │   └── ChatResponseDto.kt
│   ├── remote/
│   │   └── McpApi.kt                    # Ktor API client
│   └── repository/
│       ├── ChatRepository.kt
│       └── ChatRepositoryImpl.kt
├── di/
│   └── AppContainer.kt                  # Manual DI
├── domain/
│   ├── model/
│   │   └── Model.kt                     # Domain models
│   └── usecase/
│       ├── GetSummaryUseCase.kt
│       ├── LoadToolsUseCase.kt
│       └── SendMessageUseCase.kt
├── service/
│   └── CryptoUpdateService.kt           # Foreground service
└── ui/
    ├── ChatEvent.kt
    ├── ChatMessage.kt
    ├── ChatScreen.kt
    ├── ChatUiState.kt
    ├── CoinInfoCard.kt
    ├── FearGreedGauge.kt
    ├── NewsCard.kt
    ├── PricesCard.kt
    ├── theme/
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    └── utils/
        └── ToolsDialog.kt
```

---

## 🔍 Code Quality

- **Logging**: Comprehensive Timber logging throughout
- **Error Handling**: Result types for API calls with proper error propagation
- **Type Safety**: Sealed classes for type-safe payload handling
- **Coroutines**: Proper scope management and cancellation
- **UI State**: Immutable state with StateFlow
- **Resource Management**: Proper lifecycle management for service and coroutines

---

*Last Updated: 2025-11-20*

*Built with ❤️ using Kotlin & Jetpack Compose*
