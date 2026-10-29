# RetroCollector — Strategic Roadmap

Living roadmap tracking phased development milestones adhering to Clean Architecture.
Synchronized with [`TODO.md`](file:///Users/ivolopes/development/physical%20disk%20follow/TODO.md) and current project state.

---

## 🗺️ Phases Overview

### 🟠 Phase 01: Self-Hosted `curl-cffi` Scraper Proxy & Marketplace Engine
- **Goal**: Unlimited, zero-recurring-cost scraping for Swiss marketplaces (Ricardo.ch, Tutti.ch, Anibis.ch) bypassing Cloudflare WAF.
- **Key Deliverables**:
  - Standalone containerized microservice using Python (FastAPI + `curl-cffi`) impersonating Chrome TLS/JA3/JA4 fingerprints.
  - Deployment blueprints (Fly.io, Railway, Synology Container Manager).
  - Integration with `ListingScraper` adapter and Settings proxy toggle (`super=true` residential fallback).
  - Automated unit and integration tests for URL resolution, slug extraction, and CHF price parsing.

### ✅ Phase 02: Firestore Security & Authentication Hardening (COMPLETED)
- **Goal**: Close the 30-day "test mode" vulnerability and restrict access to authenticated clients.
- **Key Deliverables**:
  - [x] Implemented and deployed hardened `firestore.rules` (inline CEL evaluation, <80 expressions, 2 function calls) adhering to the rules audit checklist.
  - [x] Integrated multiplatform `FirebaseAuthService` and Bearer token handling into `FirestoreService`.
  - [x] Maintained strict storage separation: Firestore exclusively handles `games` and `chat_threads`; `AppSettings` remains strictly local.

### 🟠 Phase 03: Firestore Real-Time Full Sync & Conflict Resolution
- **Goal**: Full automatic two-way data synchronization between local state and Cloud Firestore.
- **Key Deliverables**:
  - Auto-sync on app launch (pull remote snapshot → merge with local catalog).
  - Real-time snapshot listener / reactive push pipeline on game creation, edition, and deletion.
  - Deterministic conflict resolution strategy for concurrent multi-device updates.

### 🟡 Phase 04: Multiplatform Storage & Local Persistence (Wasm & Desktop)
- **Goal**: Eliminate state loss on browser refresh and guarantee persistent desktop settings.
- **Key Deliverables**:
  - Wasm persistent settings bridge using `kotlinx.browser.localStorage` in `wasmJsMain`.
  - Validate and harden `DesktopSettingsLocalDataSource` (`~/.retrocollector/settings.json`) across application lifecycles.
  - Enforce local-only boundary (strict prohibition of `AppSettings` syncing to Firestore).

### 🟡 Phase 05: Error Handling UX & Network Resilience
- **Goal**: Upgrade plain-text snackbars to structured, actionable recovery workflows.
- **Key Deliverables**:
  - Domain error hierarchy (`DomainError`, `NetworkError`, `AiQuotaExceeded`, `AuthenticationError`).
  - Tactile UI banners and recovery dialogs with "Retry", "Check API Key", and "Switch Provider" actions.
  - Unit tests covering JSON decoding edge cases in `GeminiRemoteDataSource`.

### 🟡 Phase 06: Mobile Native Integration (CameraX Barcode Scanner & Share Sheet)
- **Goal**: Streamline game acquisition and serial scanning on Android devices.
- **Key Deliverables**:
  - Android `CameraX` (`androidx.camera:camera-core`) + Google ML Kit Barcode scanning in `androidMain`.
  - Auto-fill product/serial codes directly into the Quick Scan dialog.
  - Android Share Menu photo receiver: pass shared listing images directly into Gemini multimodal OCR.

### 🟢 Phase 07: Offline-First Architecture & Local Database Engine
- **Goal**: Provide complete offline capability so collection and dossier data persist without an active connection.
- **Key Deliverables**:
  - Multiplatform local database engine (SQLDelight or Room Multiplatform) in `composeApp/src/commonMain`.
  - Single Source of Truth (SSOT) pattern: UI reads from local DB, sync engine synchronizes with Firestore in background.

### 🟢 Phase 08: Collector Workstation Tools (Export, Value Analytics & Price Alerts)
- **Goal**: Advanced collector utility features for insurance, sharing, and market surveillance.
- **Key Deliverables**:
  - Collection export engine: formatted CSV and printable PDF dossiers.
  - Collection financial value history line/bar charts with timestamped snapshots.
  - Background Swiss market price watcher notifying users when wishlist titles hit target CHF thresholds.

### 🟡 Phase 09: Production Hardening, Distribution & Release Packaging
- **Goal**: Deliver ready-to-distribute native bundles and secure production configurations.
- **Key Deliverables**:
  - Android ProGuard / R8 code shrinking rules and release keystore signing.
  - macOS signed and notarized `.dmg` bundle for external distribution.
  - Synology Container Manager Docker Compose configuration for HTTPS / reverse proxy.
