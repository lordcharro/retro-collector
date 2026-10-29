# RetroCollector — Project Governance & Architecture

## 🎯 Project Overview
RetroCollector is a multiplatform Swiss / PAL retrogaming tracking workstation built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform (CMP)**. It supports macOS Desktop, Android, and WebAssembly (Wasm).

---

## 🏛️ Clean Architecture Governance

RetroCollector strictly enforces **Clean Architecture** and the **Dependency Rule** across its multiplatform codebase:

```
  ┌────────────────────────────────────────────────────────┐
  │                      Presentation                      │
  │        (Compose Multiplatform UI, ViewModels, UDF)     │
  └───────────────────────────┬────────────────────────────┘
                              │
                              ▼
  ┌────────────────────────────────────────────────────────┐
  │                         Domain                         │
  │  (Pure Entities, Value Objects, Use Cases, Repositories)│
  └───────────────────────────▲────────────────────────────┘
                              │
  ┌───────────────────────────┴────────────────────────────┐
  │                     Data & Adapters                    │
  │ (Repository Impl, Firestore, Ktor, Local DataSources)  │
  └────────────────────────────────────────────────────────┘
```

### 1. Domain Layer (`..domain..`)
- **Strict Purity**: Zero external platform or framework dependencies. Written purely in standard Kotlin within `commonMain`.
- **Entities & Value Objects** (`..domain.model..`):
  - `Game`, `GameLanguageSafety`, `ConsolePlatform`, `MarketplaceOffer`, `ChatMessage`, `AppSettings`, etc.
  - Immutable data classes representing core business concepts.
- **Repository Contracts** (`..domain.repository..`):
  - Must ONLY contain interfaces (enforced via Konsist).
  - Concrete classes or storage-specific annotations are strictly forbidden.
- **Use Cases** (`..domain.usecase..`):
  - Encapsulate single, cohesive business workflows (e.g., `DiscoverGamesUseCase`, `GetDashboardGamesUseCase`).
  - Class name MUST end with `UseCase` and expose an `operator fun invoke(...)` function.
  - Must never import data sources, HTTP clients, or framework drivers directly.

### 2. Adapters & Data Layer (`..data..`)
- **Implementations** (`..data.repository..`):
  - Implements domain repository interfaces (e.g., `GameRepositoryImpl`, `SettingsRepositoryImpl`).
  - Orchestrates local data sources and remote APIs, performing mapping between data transfer objects and domain models.
- **Data Sources & Gateways**:
  - Remote: `FirestoreService` (REST API), `ListingScraper` (Scrape.do & curl-cffi proxy), Multi-AI clients (Gemini, Anthropic, OpenAI).
  - Local: `DesktopSettingsLocalDataSource`, `AndroidSettingsLocalDataSource`.
  - Frameworks: Ktor Client, `kotlinx.serialization`, `kotlinx.coroutines`.

### 3. Presentation Layer (`..presentation..`)
- **State & Flow**:
  - Follows Unidirectional Data Flow (UDF) with immutable state models (`UiState`).
  - ViewModels must end with `ViewModel` and reside inside `..presentation..`.
- **UI Components**:
  - Compose Multiplatform UI components, tactile design tokens, responsive adaptive layouts.
  - Screen composables observe ViewModels and dispatch user intents.

---

## 💾 Storage & Boundary Invariants

1. **Application Settings (`AppSettings`) are strictly local**:
   - Persisted only via `SharedPreferences` (Android) or local file storage (Desktop `~/.retrocollector/settings.json`, Wasm `localStorage`).
   - **CRITICAL**: Do NOT save, sync, or create schemas for `AppSettings` in Cloud Firestore.
2. **Cloud Firestore Boundaries**:
   - Exclusively dedicated to `games` and `chat_threads` collections.
3. **Platform Source Set Purity (`commonMain`)**:
   - Keep `commonMain` 100% platform-agnostic. No `android.*`, `java.*`, or CocoaTouch SDK imports.
   - Platform abstractions use interfaces injected via Koin or minimal `expect`/`actual` declarations.

---

## 🌿 Git & Workflow Governance

1. **Branching Strategy**:
   - **NEVER edit directly on `develop` or `main` branches.**
   - Always create and work inside a separate feature or fix branch (e.g. `feature/...` or `fix/...`).
   - Merge into `develop` or `main` only when explicitly instructed by the user.
2. **Architecture Verification**:
   - Automated architecture integrity verified via `CleanArchitectureKonsistTest`.
3. **Living Backlog**:
   - The detailed technical backlog and completed tasks are tracked in [`TODO.md`](file:///Users/ivolopes/development/physical%20disk%20follow/TODO.md).
   - Each phase in [`.paul/ROADMAP.md`](file:///Users/ivolopes/development/physical%20disk%20follow/.paul/ROADMAP.md) derives directly from the prioritized items in `TODO.md`.

