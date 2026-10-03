# RetroCollector — AI Agent Guidelines & Rules

## 🌿 Git & Branching Strategy
- **NEVER edit directly on `develop` or `main` branches.**
- **Always create and work inside a separate feature or fix branch** (e.g. `feature/...` or `fix/...`).
- Only switch to and merge into `develop` or `main` when explicitly instructed by the user.

## 💾 Architecture & Storage Boundaries
- **Application Settings (`AppSettings`) are strictly local**:
  - Persisted only via `SharedPreferences` (Android) or local file storage (Desktop).
  - Do NOT save, sync, or create schemas for `AppSettings` in Cloud Firestore.
- **Cloud Firestore**:
  - Exclusively dedicated to `games` and `chat_threads` collections.

## 🎨 Compose & UI Standards
- Follow `@compose-skill` principles: Unidirectional Data Flow (UDF), immutable state models, and tactical theme palette tokens.
