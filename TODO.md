# RetroCollector — TODO & Roadmap

> Living document tracking what is missing, broken, or planned.
> See also the [firestore.md](firestore.md) for the Firestore data model reference.

---

## 🔴 Critical / Broken

- [x] **macOS app name shows "java" in the menu bar and Dock**
  - Configured `nativeDistributions.macOS.dockName = "RetroCollector"`, `bundleID`, and icon assets in [`composeApp/build.gradle.kts`](composeApp/build.gradle.kts).
  - Note: Raw `./gradlew :composeApp:run` launches unbundled `/bin/java` directly, which the macOS kernel/Dock identifies as "java". Launching the packaged native bundle via `./gradlew :composeApp:runDistributable` or `make run-app` reads `Info.plist` and displays "RetroCollector" with proper icon and menu bar branding.
  - File: [`composeApp/build.gradle.kts`](composeApp/build.gradle.kts), [`Main.kt`](composeApp/src/desktopMain/kotlin/com/retrocollector/app/Main.kt), [`Makefile`](Makefile)

- [ ] **Firestore access is unauthenticated (relies on "test mode")**
  - `FirestoreService` hits the REST API with no auth token; Firestore test mode expires after 30 days and is publicly writable.
  - Fix: implement proper [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started) (see `schema.md`) and/or add Firebase Auth so only the owner can read/write.
  - File: [`FirestoreService.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/firestore/FirestoreService.kt)

---

## 🟠 High Priority

- [x] **App Icon — replace with a lighter, modern design**
  - Modernized retro controller logo and icon set across all supported targets:
    - `app_icon.icns` (macOS, multi-size)
    - `app_icon.png` (desktop/Wasm, 512×512)
    - Android launcher icon set in all mipmap densities (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) plus `ic_launcher_round` variants
    - Compose Multiplatform drawable resource (`app_icon.png`)

- [ ] **Self-Hosted `curl-cffi` Scraper Proxy Microservice (Long-term unlimited scraping)**
  - Standalone, containerized microservice using Python (FastAPI + `curl-cffi`) or Go to scrape Swiss marketplaces (Ricardo.ch, Tutti.ch, Anibis.ch) with zero recurring API costs.
  - Features:
    - Impersonates Chrome TLS/HTTP2 fingerprints (JA3/JA4) to seamlessly bypass Cloudflare WAF.
    - Resolves short numeric Ricardo links (`/a/1318136906` -> `/a/mass-effect-3-ps3-1318136906/`) and extracts high-res cover photos, condition, and price in CHF.
    - Deployment blueprints for Fly.io, Railway, and Synology Docker (LAN/Tailscale).
    - Client endpoint in RetroCollector configured via **Settings > Custom Proxy**.
  - File: [`TODO.md`](TODO.md), [`ListingScraper.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/scraper/ListingScraper.kt)

- [ ] **Firestore: full sync integration (Phase 4 from progress notes)**
  - The `FirestoreService` is implemented and wired up, but sync is not automatic — the user has to manually trigger it.
  - Missing:
    - [ ] Auto-sync on app start (pull remote → merge local)
    - [ ] Real-time listener / polling to push local changes immediately on save/delete
    - [ ] Conflict resolution strategy when the same game is edited on two devices
  - File: [`FirestoreService.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/firestore/FirestoreService.kt), [`GameRepositoryImpl.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/repository/GameRepositoryImpl.kt)

- [x] **Firestore: sync `chat_threads` collection**
  - Chat history persistence and synchronization across sessions and devices.
  - Implemented:
    - [x] `saveChatMessage(projectId, message: ChatMessage)` in `FirestoreService`
    - [x] `getChatMessages(projectId)` in `FirestoreService`
    - [x] Automatic background sync and save on message creation in `GameRepositoryImpl`
  - File: [`FirestoreService.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/firestore/FirestoreService.kt), [`GameRepositoryImpl.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/repository/GameRepositoryImpl.kt)

- [x] **Delete individual chat messages**
  - Users can remove specific messages from a game's chat history.
  - Implemented:
    - [x] Long-press gesture and contextual menu trigger (`⋮`) on chat bubbles in the dossier UI
    - [x] Tactile confirmation dialog (`TactileConfirmDialog`) to prevent accidental deletions
    - [x] `deleteChatMessage(projectId, messageId)` in `FirestoreService` (`DELETE` to the `chat_threads/{messageId}` endpoint)
    - [x] Immediate optimistic removal from in-memory state in `DashboardViewModel` with toast notification
  - File: [`DeletableChatBubble.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/presentation/components/DeletableChatBubble.kt), [`GeminiChatBubble.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/presentation/components/GeminiChatBubble.kt), [`FirestoreService.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/firestore/FirestoreService.kt), [`DashboardViewModel.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/dashboard/presentation/viewmodel/DashboardViewModel.kt)

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

- [ ] **AI-generated game description and cover image**
  - Games added manually or via CSV enrichment currently have no description text or cover image.
  - Missing:
    - [ ] Extend `GeminiRemoteDataSource` to return a short game description (2–3 sentences on gameplay, genre, and why it's a PAL collector pick) as part of the structured verdict
    - [ ] Fetch a cover image: either ask Gemini to return a known image URL, or query a public API (e.g. [IGDB](https://www.igdb.com/api) or [TheGamesDB](https://thegamesdb.net/)) using the title + platform
    - [ ] Store the result in `GameItem.coverImageUrl` and display it in the dossier header and game list card
  - File: [`GeminiRemoteDataSource.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/datasource/GeminiRemoteDataSource.kt), [`GameItem.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/domain/model/GameItem.kt)

- [ ] **Error handling UX**
  - Network errors from Gemini and Firestore bubble up as plain text in a snackbar.
  - Add structured error types and user-friendly recovery actions (e.g. "Retry", "Check API key").

- [x] **Dark / Light theme toggle**
  - App supports Dark, Light, and System theme modes selectable via Settings (`ThemeMode`).

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
  - Added:
    - [x] `ListingScraperProxyTest.kt` — Scrape.do query generation (`render=true`, `super=true`), custom proxy endpoint mapping, marketplace URL pattern matching, and slug/title extraction.
  - Remaining:
    - [ ] `GeminiRemoteDataSource` JSON parsing and error recovery
    - [ ] `PriceFormatter` currency calculations
    - [ ] `DashboardViewModel` MVI state transitions

- [x] **GitHub Actions CI pipeline**
  - Added `.github/workflows/ci.yml` running detekt lint checks, unit tests, and multiplatform build validation on every push/PR.
