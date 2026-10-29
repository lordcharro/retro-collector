# RetroCollector — Current State

## 🔄 Current Loop Position
- **Status**: `UNIFIED`
- **Last Completed Phase**: `Phase 02: Firestore Security & Authentication Hardening` ([`02-01-SUMMARY.md`](file:///Users/ivolopes/development/physical%20disk%20follow/.paul/phases/02-firestore-security-hardening/02-01-SUMMARY.md))
- **Active Plan**: `None (Loop Closed)`
- **Current Branch**: `feature/paul-governance`

---

## 🏛️ Accumulated Architectural Context
- **Clean Architecture & Konsist Governance**:
  - Enforced by `CleanArchitectureKonsistTest`: `..domain..` has zero external dependencies (`domain.dependsOnNothing()`), `..domain.repository..` contains interfaces only, and all `UseCase` classes reside in `..domain.usecase..` with `operator fun invoke`.
- **Storage & Cloud Sync Boundaries**:
  - `AppSettings` (`firebaseProjectId`, `firebaseApiKey`, AI keys, language, theme) is **strictly local** (`~/.retrocollector/settings.json` on Desktop, `SharedPreferences` on Android) and blocked by `firestore.rules` from ever syncing to Cloud Firestore.
  - Cloud Firestore (`retrocollector-72e97`) is in **Production Mode**, exclusively serving `/games/{gameId}` and `/chat_threads/{messageId}` gated by `isAuthenticated()` (`request.auth != null`) and inline schema validation (`isValidGame`, `isValidChatMessage` — 2 function calls per request to respect Firestore's 20-call limit).
  - `FirebaseAuthService` manages Anonymous Auth token acquisition, in-memory caching, and refresh via Firebase REST APIs, while `FirestoreService` injects `Authorization: Bearer <idToken>` on all 6 endpoints and logs HTTP error responses.

---

## 🎯 Immediate Next Action
Run `/paul-plan` for **Phase 01** (Self-Hosted `curl-cffi` Scraper Proxy Microservice) or **Phase 03** (Firestore Real-Time Full Sync & Conflict Resolution).
