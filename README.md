# AIHub

AIHub is a modern, unified Android application that lets you seamlessly interact with multiple leading AI models—including ChatGPT, Gemini, Claude, and Perplexity—from a single interface. Built purely with native Android tools and without relying on expensive API keys, AIHub leverages intelligent WebViews to query multiple AIs simultaneously, compare their responses side-by-side, and save your chat history locally.

## ✨ Features

*   **Multi-Model Interface**: Query ChatGPT, Google Gemini, Anthropic Claude, and Perplexity AI all at once.
*   **Side-by-Side Comparison**: Easily glance through responses from different AI engines to get the most comprehensive answer.
*   **Zero API Keys Required**: AIHub acts as an intelligent wrapper around the web interfaces of these tools, meaning you don't need to pay for API access or manage developer keys (Note: Requires logging into the respective web platforms initially).
*   **Local History**: All your prompts and interactions are securely saved on your device using a local Room Database.
*   **Modern UI/UX**: Built entirely with Jetpack Compose following Material Design 3 guidelines for a clean, responsive, and intuitive user experience.
*   **Dark/Light Mode**: Full support for system-level dark and light themes.

## 🛠️ Tech Stack

*   **Language**: [Kotlin](https://kotlinlang.org/)
*   **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **Architecture**: MVVM (Model-View-ViewModel) + StateFlow
*   **Local Storage**: [Room Database](https://developer.android.com/training/data-storage/room)
*   **Asynchrony**: Kotlin Coroutines & Flows
*   **Integration**: Advanced WebView manipulation and JavaScript injection

## 🚀 Getting Started

### Prerequisites

*   Android Studio (latest version recommended)
*   An Android device or emulator running Android API level 24 or higher.

### Installation

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/yourusername/AIHub.git
    cd AIHub
    ```

2.  **Open in Android Studio**:
    *   Launch Android Studio.
    *   Select **Open an existing Project**.
    *   Navigate to the cloned directory and select it.

3.  **Build and Run**:
    *   Wait for Gradle to finish syncing.
    *   Click the **Run** button (`Shift + F10`) to compile and launch the application on your device or emulator.

### Initial Setup

When you first open a specific AI platform tab (e.g., ChatGPT or Claude) within the app, you will need to log in to your existing account on that platform. The app uses secure WebViews, meaning your login credentials are handled directly by the official websites. Once logged in, the app will retain your session.

## 📱 Screenshots

*(Consider placing your app screenshots here. e.g.,)*
<!--
![Home Screen](docs/images/home.png)
![Comparison View](docs/images/compare.png)
![Settings](docs/images/settings.png)
-->

## 🏗️ Architecture

AIHub follows a robust MVVM architecture:
*   **UI Layer**: Jetpack Compose screens (`MainScreen`, `EnginesTab`) that observe state.
*   **ViewModel**: `MainViewModel` manages UI state (`UiState`), formatting data, and handling user intents.
*   **Repository Layer**: `ChatRepository` abstracts the data sources.
*   **Data Layer**: Room database (`AppDatabase`, `ChatDao`) for local persistence.
*   **WebView Layer**: `WebViewManager` handles the lifecycle and logic for isolated AI WebViews, along with JavaScript injection adapters (`GeminiAdapter`, `ClaudeAdapter`, etc.) to automatically populate and send prompts.

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1.  Fork the project.
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.

## 📝 License

Distributed under the MIT License. See `LICENSE` for more information.

---
*Disclaimer: AIHub is not officially affiliated with OpenAI, Google, Anthropic, or Perplexity. It is a client application utilizing rendering of public web interfaces.*
