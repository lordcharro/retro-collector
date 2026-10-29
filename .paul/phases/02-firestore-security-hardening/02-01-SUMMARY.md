# Phase 02 — Plan 01 Summary: Firestore Security Rules & Production Authentication Hardening

**Phase**: `02-firestore-security-hardening`  
**Plan**: [`02-01-PLAN.md`](file:///Users/ivolopes/development/physical%20disk%20follow/.paul/phases/02-firestore-security-hardening/02-01-PLAN.md)  
**Branch**: `feature/paul-governance`  
**Status**: **UNIFIED & COMPLETE** ✅

---

## 🛠️ What Was Implemented

### 1. `domain/` — Pure Business Entities
- **[`FirebaseAuthToken.kt`](file:///Users/ivolopes/development/physical%20disk%20follow/composeApp/src/commonMain/kotlin/com/retrocollector/app/core/domain/model/FirebaseAuthToken.kt)**: Created pure, zero-dependency domain model representing an authenticated Firebase session token (`idToken`, `refreshToken`, `expiresInSeconds`, `localId`, `expiresAtEpochMs`) with expiration buffer checking (`isExpired()`).
- **[`AppSettings.kt`](file:///Users/ivolopes/development/physical%20disk%20follow/composeApp/src/commonMain/kotlin/com/retrocollector/app/settings/domain/model/AppSettings.kt)**: Added `hasFirebaseAuthConfig` computed helper (`firebaseProjectId.isNotBlank() && firebaseApiKey.isNotBlank()`), keeping `AppSettings` strictly local.

### 2. `use_cases/` — Workflows & Contracts
- **[`IGameRepository.kt`](file:///Users/ivolopes/development/physical%20disk%20follow/composeApp/src/commonMain/kotlin/com/retrocollector/app/core/domain/repository/IGameRepository.kt)**: Extended contract with `suspend fun testFirestoreConnection(projectId: String, apiKey: String = ""): Result<String>`.
- **[`VerifyFirestoreConnectionUseCase.kt`](file:///Users/ivolopes/development/physical%20disk%20follow/composeApp/src/commonMain/kotlin/com/retrocollector/app/settings/domain/usecase/VerifyFirestoreConnectionUseCase.kt)**: Added pure use case orchestrating authenticated Firestore connection verification.

### 3. `adapters/` — Data Sources, Repositories, UI & Security Rules
- **[`firestore.rules`](file:///Users/ivolopes/development/physical%20disk%20follow/firestore.rules)**: Authored, deployed, and verified production Cloud Firestore Security Rules v2:
  - Enforces `isAuthenticated()` (`request.auth != null`) across `/games/{gameId}` and `/chat_threads/{messageId}`.
  - Enforces strict `keys.hasAll(...)` and `keys.hasOnly(...)` field whitelists, 30-platform enum checks, status/language/condition enum checks, payload size bounds, and ID immutability on updates.
  - Explicitly denies all other collections (`match /{document=**} { allow read, write: if false; }`), guaranteeing `AppSettings` can never be written to Firestore.
- **[`FirebaseAuthService.kt`](file:///Users/ivolopes/development/physical%20disk%20follow/composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/firestore/FirebaseAuthService.kt)**: Implemented Ktor-based multiplatform Firebase Auth REST client (`accounts:signUp` anonymous auth + `securetoken.googleapis.com/v1/token` refresh) with `Mutex`-guarded in-memory token caching.
- **[`FirestoreService.kt`](file:///Users/ivolopes/development/physical%20disk%20follow/composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/firestore/FirestoreService.kt)**:
  - Attached `Authorization: Bearer <idToken>` headers to all 6 Firestore operations (`getGames`, `saveGame`, `deleteGame`, `getChatMessages`, `saveChatMessage`, `deleteChatMessage`).
  - Replaced silent non-2xx return values with explicit `Result.failure(...)` and HTTP status/body diagnostics.
- **[`GameRepositoryImpl.kt`](file:///Users/ivolopes/development/physical%20disk%20follow/composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/repository/GameRepositoryImpl.kt)**:
  - Wired `_settings.value.firebaseApiKey` into all `FirestoreService` calls.
  - Hardened `syncFromFirestore()` to merge remote documents with local state and automatically re-sync any local-only games that had not yet been pushed to Firestore.
- **[`SettingsSections.kt`](file:///Users/ivolopes/development/physical%20disk%20follow/composeApp/src/commonMain/kotlin/com/retrocollector/app/settings/presentation/ui/SettingsSections.kt) & [`SettingsDialog.kt`](file:///Users/ivolopes/development/physical%20disk%20follow/composeApp/src/commonMain/kotlin/com/retrocollector/app/settings/presentation/ui/SettingsDialog.kt)**:
  - Added the **Firebase Web API Key (`AIzaSy...`)** field in `SettingsFirebaseSection` with localized labels/hints across EN, PT, DE, and FR (`strings.xml`).
- **Automated Tests**:
  - Added [`FirebaseAuthServiceTest.kt`](file:///Users/ivolopes/development/physical%20disk%20follow/composeApp/src/desktopTest/kotlin/com/retrocollector/app/core/FirebaseAuthServiceTest.kt) and expanded [`FirestoreServiceSerializationTest.kt`](file:///Users/ivolopes/development/physical%20disk%20follow/composeApp/src/desktopTest/kotlin/com/retrocollector/app/core/FirestoreServiceSerializationTest.kt).

---

## 🔄 Deviations & Runtime Resolutions

1. **Firebase Console Unused Function Compiler Warning (`8c0e4e6`)**:
   - The initial `firestore.rules` included an optional `isOwner(uid)` helper template. The Firebase Console rules compiler flagged unused functions, so `isOwner` was documented in comments instead, resulting in zero compiler warnings.
2. **Firestore 20-Function-Call Limit on Full 26-Field Game Documents (`bf8d0e2`)**:
   - During live testing with newly scanned games (*Batman: Arkham Asylum*), `deleteGame` (1 function call) and `saveChatMessage` (8 function calls) succeeded, whereas `saveGame` (24–26 populated fields) failed with `403 PERMISSION_DENIED` because calling a separate helper function per field exceeded Cloud Firestore's hard limit of **20 function calls per rule evaluation**.
   - Refactored `isValidGame(gameId)` and `isValidChatMessage(messageId)` in [`firestore.rules`](file:///Users/ivolopes/development/physical%20disk%20follow/firestore.rules) to evaluate all field checks inline using `let data = request.resource.data;` and `data.get(field, defaultVal)`. Every request now makes only **2 function calls** (`isAuthenticated()` + `isValidGame(gameId)`), and 26-field `GameItem` writes were verified returning `200 OK` in production.

---

## 🏛️ Key Architectural Decisions

- **Multiplatform REST Auth over Native SDK Lock-in**: Using the Firebase Auth REST API (`identitytoolkit` + `securetoken`) via Ktor in `commonMain` works identically across Desktop (JVM), Android, and Wasm without platform-specific SDK disparities.
- **Inline CEL Rule Validation**: Inlining field validations inside `isValidGame` and `isValidChatMessage` avoids Firestore's 20-function-call limit while preserving 100% of field whitelist, enum, and size constraints.
- **Resilient Merge on Cloud Sync**: `GameRepositoryImpl.syncFromFirestore()` now merges remote and local games by ID and re-pushes any unsynced local games instead of overwriting local memory with remote state.
