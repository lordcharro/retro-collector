# RetroCollector — TODO & Roadmap

> Living document tracking what is missing, broken, or planned.
> See also the [schema.md](schema.md) for the Firestore data model reference.

---

## 🔴 Critical / Broken

- [ ] **macOS app name shows "java" in the menu bar and Dock**
  - Root cause: the `packageName` in `compose.desktop.application` is set but the JVM process name defaults to `java` when running via `./gradlew :composeApp:run`.
  - Fix: set `-Dapple.awt.application.name=RetroCollector` in the JVM args in `Main.kt`, or build the native `.app` bundle via `./gradlew :composeApp:createDistributable` and run from there.
  - File: [`composeApp/build.gradle.kts`](composeApp/build.gradle.kts), [`Main.kt`](composeApp/src/desktopMain/kotlin/com/retrocollector/app/Main.kt)

- [ ] **Firestore access is unauthenticated (relies on "test mode")**
  - `FirestoreService` hits the REST API with no auth token; Firestore test mode expires after 30 days and is publicly writable.
  - Fix: implement proper [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started) (see `schema.md`) and/or add Firebase Auth so only the owner can read/write.
  - File: [`FirestoreService.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/firestore/FirestoreService.kt)

---

## 🟠 High Priority

- [ ] **App Icon — replace with a lighter, modern design**
  - Current icon (`app_icon.png`, `app_icon.icns`) is a placeholder.
  - Needed assets:
    - `app_icon.icns` (macOS, multi-size)
    - `app_icon.png` (desktop/Wasm, 512×512)
    - Android adaptive icon set: `ic_launcher_foreground.xml` + `ic_launcher_background.xml` (or PNGs) in all mipmap densities (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) plus `ic_launcher_round` variants
  - The Android manifest already references `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`, so only the asset files need replacing.
  - Suggestion: a clean retro controller or cartridge silhouette on a dark zinc background.

- [ ] **Firestore: full sync integration (Phase 4 from progress notes)**
  - The `FirestoreService` is implemented and wired up, but sync is not automatic — the user has to manually trigger it.
  - Missing:
    - [ ] Auto-sync on app start (pull remote → merge local)
    - [ ] Real-time listener / polling to push local changes immediately on save/delete
    - [ ] Conflict resolution strategy when the same game is edited on two devices
  - File: [`FirestoreService.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/firestore/FirestoreService.kt), [`GameRepositoryImpl.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/repository/GameRepositoryImpl.kt)

- [ ] **Internationalisation (i18n) — translate the app UI**
  - All strings are currently in Portuguese (`TextKeys.kt` constants).
  - The `TextKeys` object was designed with i18n in mind but no locale switching is implemented.
  - Languages to consider: **English** (primary), **French**, **German** (important for the CH market).
  - Approach: replace `TextKeys` string constants with a proper `StringResource` system using Compose Multiplatform's `composeResources` (already set up in the build) and add `.xml` string files per locale.
  - File: [`TextKeys.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/presentation/text/TextKeys.kt)

---

## 🟡 Medium Priority

- [ ] **Settings persistence on Desktop and Wasm**
  - Android uses `SharedPreferences` (working).
  - Desktop uses `DesktopSettingsLocalDataSource` — verify it correctly persists to a file (e.g. `~/.retrocollector/settings.json`) across app restarts.
  - Wasm/browser: settings are likely lost on page reload; consider `localStorage` via JS interop.

- [ ] **Release build & signing**
  - `isMinifyEnabled = false` in release config — consider enabling ProGuard/R8 for Android release builds.
  - No signing config defined in `build.gradle.kts`; a keystore needs to be set up for Google Play distribution.
  - macOS: build a proper notarised `.dmg` for distribution outside developer mode.

- [ ] **Android CameraX barcode scanner**
  - `CAMERA` permission is declared in `AndroidManifest.xml` but no barcode scanning is implemented.
  - Plan: integrate `androidx.camera:camera-core` + `com.google.mlkit:barcode-scanning` in `androidMain` to auto-fill the product code field in the Quick Scan dialog.

- [ ] **Wasm / Web: settings and local storage**
  - The Wasm target currently has no persistent settings — every browser reload starts fresh.
  - Fix: use `kotlinx.browser.localStorage` (available in `wasmJsMain`) to persist the `AppSettings` JSON.

- [ ] **Error handling UX**
  - Network errors from Gemini and Firestore bubble up as plain text in a snackbar.
  - Add structured error types and user-friendly recovery actions (e.g. "Retry", "Check API key").

- [ ] **Dark / Light theme toggle**
  - App is permanently dark (`RetroTactileTheme` hard-codes dark mode).
  - Consider respecting the system theme, or adding a toggle in Settings.

---

## 🟢 Nice to Have / Backlog

- [ ] **Price alert / opportunity notifications**
  - Monitor new listings on Ricardo.ch for Wishlist games below the target CHF price and send a push notification (Android) or desktop notification (macOS).

- [ ] **Export collection (CSV / PDF)**
  - Allow exporting the full game catalogue with values for insurance or collector sharing.

- [ ] **Collection value chart**
  - A simple line or bar chart showing total collection value over time (requires timestamped price snapshots).

- [ ] **Offline-first architecture**
  - Currently, if Firestore is not configured, data lives only in memory (lost on restart on desktop/wasm).
  - Add a local SQLite/Room store (Android) or SQLite via `SQLDelight` for a proper offline-first baseline before syncing to Firestore.

- [ ] **Share sheet on Android — pre-fill title from OCR**
  - When a listing URL is shared from Ricardo.ch, the scraper extracts the title and price.
  - Enhancement: if a *photo* is shared instead of a URL, pass it directly to the Gemini multimodal endpoint for OCR/identification.

- [ ] **Wasm Synology Docker: HTTPS / reverse proxy docs**
  - The current `nginx.conf` serves over plain HTTP on port 8085.
  - Add documentation or a second Docker Compose profile for HTTPS with a self-signed cert or Let's Encrypt via a reverse proxy.

- [ ] **Unit tests beyond architecture validation**
  - Only Konsist architecture tests exist.
  - Add unit tests for: `GeminiRemoteDataSource` JSON parsing, `ListingScraper` URL slug extraction, `PriceFormatter`, and `DashboardViewModel` state transitions.

- [ ] **GitHub Actions CI pipeline**
  - Add a `.github/workflows/ci.yml` that runs `./gradlew detekt test` on every push/PR so linting and architecture tests are enforced automatically.
