<div align="center">

<br />

# 🤖 Omni AI Hub

### _One prompt. Every AI. Instantly._

A native Android app that aggregates **ChatGPT**, **Gemini**, **Claude**, and **Perplexity** into a single, elegant interface — letting you compare, contrast, and explore AI responses side-by-side without switching apps.

<br />

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

<br />

</div>

---

## ✨ Features

| Feature | Description |
|---|---|
| 🚀 **Simultaneous Prompting** | Send one prompt to all AI engines at the same time |
| 🔍 **Side-by-Side Compare** | View and compare responses from all engines on a single screen |
| 🌐 **Native WebView Engines** | Full browser sessions for ChatGPT, Gemini, Claude & Perplexity |
| 🎙️ **Voice Input** | Dictate prompts using your device's speech recognition |
| 📜 **Chat History** | Persistent local history with swipe-to-delete and session drill-down |
| 👤 **Profile & Stats** | Track your usage — prompts sent, models used, and more |
| ⚙️ **Engine Management** | Toggle engines on/off and link accounts directly from the app |
| 🔄 **Background Preloading** | WebViews are silently preloaded so responses arrive instantly |
| 🖥️ **High Refresh Rate** | Automatically targets maximum display refresh rate on supported devices |
| 💾 **Room Database** | Fully offline history and session management powered by Room |

---

## 📸 App Structure

```
Omni AI Hub
├── 🏠  Home        — Send a prompt to all active models, view live responses
├── 📊  Compare     — Side-by-side response comparison with one-tap share
├── 📜  History     — Browse, expand, and delete past sessions
├── ⚙️  Settings    — Manage your profile & link AI engine accounts
├── 👤  Profile     — View usage stats and active engines
└── 🌐  Engines     — Full WebView browser for each AI platform
```

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| **Language** | Kotlin 2.2.10 |
| **UI Framework** | Jetpack Compose + Material3 |
| **Architecture** | MVVM (ViewModel + StateFlow) |
| **Local Database** | Room 2.7.0 |
| **Async** | Kotlin Coroutines + Flow |
| **WebView** | Android WebView (singleton pool) |
| **Networking** | OkHttp + Retrofit + Moshi |
| **DI / Prefs** | SharedPreferences + DataStore |
| **Build System** | Gradle 9.1.1 with Version Catalogs |
| **Minimum SDK** | Android 6.0 (API 23) |

---

## 🚀 Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (Hedgehog or newer recommended)
- Android device or emulator running **API 23+**
- Active accounts on any of: [ChatGPT](https://chatgpt.com), [Gemini](https://gemini.google.com), [Claude](https://claude.ai), [Perplexity](https://perplexity.ai)

### Setup

**1. Clone the repository**

```bash
git clone https://github.com/KarthikSai06/Ai-Playground-.git
cd Ai-Playground-
git checkout new-updated
```

**2. Open in Android Studio**

Select **File → Open** and choose the cloned project directory. Allow Android Studio to sync Gradle dependencies.

**3. Configure your API key**

Create a `.env` file in the root of the project:

```env
GEMINI_API_KEY=your_gemini_api_key_here
```

> See `.env.example` for reference. You can get a free Gemini API key from [Google AI Studio](https://aistudio.google.com).

**4. Configure signing (for local builds)**

Remove the following line from `app/build.gradle.kts` before building:

```kts
signingConfig = signingConfigs.getByName("debugConfig")
```

**5. Run the app**

Connect a device or start an emulator, then press **▶ Run** in Android Studio.

---

## 🔗 Linking AI Accounts

Because Omni AI Hub uses real WebViews to communicate with each AI platform, **you must sign in** to each service within the app for prompts to work:

1. Open the app and navigate to **Settings → Linked Engines**
2. Tap **"Open in Browser"** next to each platform
3. Sign in using your existing account credentials
4. Return to **Home** and start sending prompts

Your sessions are persisted in the WebView's cookie store — you only need to do this once per platform.

---

## 📁 Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                  # Entry point, edge-to-edge + refresh rate setup
├── database/
│   ├── AppDatabase.kt               # Room database definition
│   └── ChatDao.kt                   # Data access object for chat sessions
├── models/
│   └── RoomModels.kt                # Room entities (ChatSession, ChatMessage)
├── repository/
│   └── ChatRepository.kt            # Data layer between ViewModel and Room
├── ui/
│   ├── MainScreen.kt                # All composable screens (Home, Compare, History...)
│   └── theme/                       # Material3 theme: colors, typography, shapes
├── viewmodel/
│   └── MainViewModel.kt             # App state, prompt dispatch, session management
└── webview/
    ├── AiPlatform.kt                # Enum of supported AI platforms + brand colors
    ├── AiAdapter.kt                 # Base adapter interface for all platforms
    ├── WebViewManager.kt            # Singleton WebView pool + prompt injection
    ├── ChatGptAdapter.kt            # ChatGPT-specific injection logic
    ├── GeminiAdapter.kt             # Gemini-specific injection logic
    ├── ClaudeAdapter.kt             # Claude-specific injection logic
    └── PerplexityAdapter.kt         # Perplexity-specific injection logic
```

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

1. Fork the repo and create your branch: `git checkout -b feature/my-feature`
2. Make your changes and commit: `git commit -m 'feat: add my feature'`
3. Push to your branch: `git push origin feature/my-feature`
4. Open a Pull Request against `new-updated`

Please follow [Conventional Commits](https://www.conventionalcommits.org/) for commit messages.

---

## ⚠️ Disclaimer

Omni AI Hub uses WebViews to interact with third-party AI services. Usage of these services is subject to each provider's **Terms of Service**. This app is intended for personal productivity and research. The author is not affiliated with OpenAI, Google, Anthropic, or Perplexity.

---

## 📄 License

```
MIT License — Copyright (c) 2025 KarthikSai06

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, subject to the conditions of the MIT License.
```

---

<div align="center">

Made with ❤️ by [KarthikSai06](https://github.com/KarthikSai06)

⭐ **Star this repo** if you find it useful!

</div>
