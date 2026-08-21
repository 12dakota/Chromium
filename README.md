# DualEngine Browser (Chromium Blink & Gecko Engine)

An advanced Android dual-engine web browser with Firebase Authentication integration, modern Jetpack Compose UI, tab management, engine inspector, developer tools, and automated GitHub Actions APK builds.

## Features
- **Dual Engine Architecture**: Toggle between Chromium (Blink) and Firefox (Gecko) rendering pipelines and engine behaviors.
- **Firebase Authentication Engine**: Integrated login and profile state sync.
- **Modern Jetpack Compose UI**: Bottom navigation bar, tab overview grid, reader mode, dev tools, and real-time engine telemetry.
- **GitHub Actions CI/CD**: Automatically builds debug APK on push or manual trigger.

## Automated Builds
GitHub Actions workflow builds the APK and uploads it as an artifact on every push to `main`.
