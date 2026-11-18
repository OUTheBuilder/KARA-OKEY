# Kara-okay Clone — v4

This is the editable reconstruction project based on the supplied `base.apk` reference.

## Current features
- Reconstructed Compose navigation shell and major app sections.
- Bundled reference assets/native libraries retained where available.
- Offline/Online connection-mode toggle.
- Online karaoke search using YouTube's mobile search page inside an Android WebView.
- Online mode preference persists across app restarts.
- Offline mode keeps the local-library UI available and disables online search.
- GitHub Actions workflow builds a debug APK with Gradle 9.1.0, Java 21 and Android API 37.

## Build
The included GitHub Actions workflow is the recommended build path. It produces:
`app/build/outputs/apk/debug/app-debug.apk`

## Important
This remains a reconstruction from an APK, not the original source project. Some original behavior that depends on proprietary/internal services or compiled implementation details still needs to be recreated and verified.
