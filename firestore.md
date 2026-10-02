# RetroCollector — Firestore Data Model

This document describes the Firestore collections, document structures, field types, recommended composite indexes, and security rules for the **RetroCollector** cloud sync.

> **Implementation status:** only the `games` collection is currently synced by [`FirestoreService.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/data/firestore/FirestoreService.kt). The `chat_threads` and `app_settings` collections are designed and ready but not yet integrated — see [TODO.md](TODO.md).

---

## 📐 Data Architecture

RetroCollector uses a **hybrid document structure**:

1. **Indexed root fields** — `title`, `platform`, `status`, `updatedAt` — exposed at the document root so Firestore can filter and sort without needing to parse the full payload.
2. **Serialised `data` field** — the complete `GameItem` model serialised as a JSON string for direct deserialisation in Kotlin Multiplatform (Android, macOS Desktop, WebAssembly).

```
Firestore (default database)
├── 📁 games/                          # Game catalogue, wishlist, and hunting list
│   └── 📄 {gameId}                    # e.g. "game_ssbm_gc", "game_deadspace_ps3"
│       ├── title, platform, status, updatedAt  (root indexed fields)
│       └── data                                (full GameItem JSON string)
│
├── 📁 chat_threads/                   # [PLANNED] Gemini chat history per game/franchise
│   └── 📄 {messageId}
│
└── 📁 app_settings/                   # [PLANNED] Global or per-user preferences
    └── 📄 preferences
```

---

## 📂 Collection: `games`

Each document represents a physical game — whether owned, on the wishlist, actively being hunted, or currently being evaluated from a listing on Ricardo.ch or at a flea market.

### Document structure: `/games/{gameId}`

| Field | Firestore type | Required | Description | Example |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `string` | ✅ | Unique game identifier | `"game_gc_smash_melee"` |
| `title` | `string` | ✅ | Official game title | `"Super Smash Bros. Melee"` |
| `franchiseName` | `string` | — | Franchise or series name | `"Super Smash Bros."` |
| `platform` | `string` | ✅ | Platform code (see allowed values below) | `"gamecube"` |
| `releaseYear` | `string` | — | European / regional release year | `"2002"` |
| `coverImageUrl` | `string` | — | URL of the front cover image | `"https://.../cover.jpg"` |
| `spineImageUrl` | `string` | — | URL of the spine photo showing the serial code | `"https://.../spine.jpg"` |
| `productCode` | `string` | — | Physical product code (spine/disc) | `"DOL-P-GALE"` |
| `barcode` | `string` | — | EAN-13 barcode | `"045496520786"` |
| `spottedLocation` | `string` | — | Where the copy was found or purchased | `"Ricardo.ch"`, `"Brocki Bern"` |
| `askingPriceChf` | `number` (double) | — | Seller's asking price in CHF | `65.00` |
| `targetPriceChf` | `number` (double) | — | Collector's target / max purchase price | `55.00` |
| `paidPriceChf` | `number` (double) | — | Price actually paid | `60.00` |
| `collectionStatus` | `string` | ✅ | Collection state — see allowed values | `"WISHLIST"` |
| `languageStatus` | `string` | ✅ | Language safety status — see allowed values | `"FULL_ENGLISH"` |
| `languageAudio` | `array<string>` | — | Audio languages on the disc/cartridge | `["English", "Japanese"]` |
| `languageSubtitles` | `array<string>` | — | Subtitle / menu languages | `["English", "French", "German"]` |
| `safeSkus` | `array<map>` | — | SKUs confirmed safe for English | *See SkuInfo sub-map below* |
| `riskySkus` | `array<map>` | — | SKUs known to be German-only or censored | *See SkuInfo sub-map below* |
| `marketRadar` | `map` | — | Swiss market pricing metrics | *See MarketRadar sub-map below* |
| `censorshipWarning` | `string` | — | Warning about censored editions (common for German USK releases) | `"Cuts blood effects in USK release"` |
| `collectorVerdict` | `string` | — | Gemini's concise buy/pass verdict | `"Safe copy. UKV and NOE both have English."` |
| `personalNotes` | `string` | — | Free-text notes added by the collector | `"Bought at Basel flea market"` |
| `enrichmentStatus` | `string` | — | AI enrichment state (`PENDING`, `ENRICHING`, `COMPLETE`, `FAILED`) | `"COMPLETE"` |
| `listingUrl` | `string` | — | Direct link to the Ricardo.ch / Tutti.ch listing | `"https://www.ricardo.ch/de/a/..."` |
| `acquiredCondition` | `string` | — | Condition when acquired (`CIB`, `BOXED`, `LOOSE`) | `"CIB"` |
| `offersCount` | `integer` | — | Number of registered marketplace offers | `3` |
| `bestOfferStore` | `string` | — | Store name of the lowest landed price offer | `"Anibis.ch"` |
| `bestOfferPriceChf` | `number` (double) | — | Best landed price across all active offers | `28.00` |
| `offers` | `array<map>` | — | Tracked marketplace offers / store listings | *See GameOffer sub-map below* |
| `updatedAt` | `integer` (int64) | ✅ | Last updated timestamp in epoch milliseconds | `1727464000000` |
| `data` | `string` | ✅ | Full `GameItem` serialised as JSON (used by the KMP app for deserialisation) | `"{...}"` |

> **Note on `status` vs `collectionStatus`:** The app saves the collection status under the root key `"status"` in Firestore (for indexing), while the full `GameItem` JSON inside the `data` field uses `"collectionStatus"`. The Firestore Security Rules validate the root `status` field.

---

### Allowed enum values

**`collectionStatus` / root `status` field** — from [`CollectionStatus.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/domain/model/CollectionStatus.kt):
```
WISHLIST | OWNED | PASS
```

**`acquiredCondition` / offer `condition`** — from [`GameCondition.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/domain/model/GameCondition.kt):
```
CIB | BOXED | LOOSE
```

**`languageStatus`** — from [`LanguageStatus.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/domain/model/LanguageStatus.kt):
```
FULL_ENGLISH | SUBS_ONLY | GERMAN_ONLY | EDITION_NOTICE | UNVERIFIED
```

**`platform`** — from [`ConsolePlatform.kt`](composeApp/src/commonMain/kotlin/com/retrocollector/app/core/domain/model/ConsolePlatform.kt):
```
nes | snes | n64 | gamecube | wii | wii_u | switch | switch_2
game_boy | gba | nds | n3ds
ps1 | ps2 | ps3 | ps4 | ps5 | psp | ps_vita
xbox_og | xbox_360 | xbox_one | xbox_series
master_system | megadrive | sega_saturn | dreamcast | game_gear
retro_vintage
```

---

### Sub-maps

#### `GameOffer` (items in `offers`)
```json
{
  "id": "offer_1790870000000",
  "source": "Anibis.ch",
  "priceChf": 28.0,
  "shippingChf": null,
  "totalLandedPriceChf": 28.0,
  "listingUrl": "https://www.anibis.ch",
  "condition": "CIB",
  "sellerOrLocation": "Basel Gundeli",
  "notes": "Includes uncut English manual",
  "isPurchased": false,
  "isArchived": false,
  "createdAt": 1790870000000
}
```

#### `SkuInfo` (items in `safeSkus` and `riskySkus`)
```json
{
  "code": "DOL-P-GALE",
  "region": "NOE / FRG",
  "editionNote": "Includes English, German, French in game options",
  "isSafe": true
}
```

#### `marketRadar` (Swiss market pricing)
```json
{
  "spottedPriceChf": 65.0,
  "medianPriceChf": 85.0,
  "historicalMinChf": 45.0,
  "historicalMaxChf": 120.0,
  "trend": "Upward"
}
```

---

## 📄 Example document: `games/game_deadspace_ps3`

```json
{
  "id": "game_deadspace_ps3",
  "title": "Dead Space",
  "franchiseName": "Dead Space",
  "platform": "ps3",
  "releaseYear": "2008",
  "coverImageUrl": null,
  "spineImageUrl": null,
  "productCode": "BLES-00350",
  "barcode": "5030930064716",
  "spottedLocation": "Ricardo.ch",
  "askingPriceChf": 35.0,
  "targetPriceChf": 25.0,
  "paidPriceChf": null,
  "collectionStatus": "EVALUATING",
  "languageStatus": "GERMAN_ONLY",
  "languageAudio": ["German"],
  "languageSubtitles": ["German"],
  "safeSkus": [
    {
      "code": "BLES-00350",
      "region": "UK / PEGI",
      "editionNote": "Full English voice and subtitles confirmed",
      "isSafe": true
    }
  ],
  "riskySkus": [
    {
      "code": "BLES-00351",
      "region": "GER / USK 18",
      "editionNote": "German audio and text only. USK logo on front.",
      "isSafe": false
    }
  ],
  "marketRadar": {
    "spottedPriceChf": 35.0,
    "medianPriceChf": 28.0,
    "historicalMinChf": 15.0,
    "historicalMaxChf": 45.0,
    "trend": "Stable"
  },
  "censorshipWarning": "German USK copy (BLES-00351) has no English audio or subtitles. Requires UK/PEGI version (BLES-00350).",
  "collectorVerdict": "CAUTION: Seller on Ricardo is listing the German BLES-00351. Reject or ask for photos of the back cover.",
  "personalNotes": "",
  "enrichmentStatus": "COMPLETE",
  "listingUrl": "https://www.ricardo.ch/de/a/dead-space-ps3-1240000000/",
  "updatedAt": 1727464000000
}
```

---

## 📂 Collection: `chat_threads` *(planned — not yet synced)*

Stores contextual Gemini chat messages for each game or inspection session.

### Document structure: `/chat_threads/{messageId}`

| Field | Firestore type | Description | Example |
| :--- | :--- | :--- | :--- |
| `id` | `string` | Unique message ID | `"msg_1727464100_user"` |
| `contextId` | `string` | Associated game ID | `"game_gc_smash_melee"` |
| `sender` | `string` | Message sender (`USER` or `GEMINI`) | `"GEMINI"` |
| `text` | `string` | Message body | `"The NOE edition has a 60Hz selector and English audio."` |
| `imageBase64` | `string` | Optional photo sent for analysis (thumbnail) | `null` |
| `timestamp` | `integer` (int64) | Send time in epoch milliseconds | `1727464100000` |
| `suggestedGameUpdate` | `map` | Optional one-click field update suggestion from Gemini | `null` |

---

## ⚡ Recommended Composite Indexes

Add these in the **Firestore Console → Indexes → Composite** tab:

| # | Collection | Fields | Use case |
|---|---|---|---|
| 1 | `games` | `platform` ASC, `updatedAt` DESC | Filter by console, sorted by recency |
| 2 | `games` | `status` ASC, `updatedAt` DESC | Filter by collection state |
| 3 | `games` | `languageStatus` ASC, `platform` ASC | Language risk triage per console |
| 4 | `chat_threads` | `contextId` ASC, `timestamp` ASC | Chat history per game, in order |

---

## 🔒 Security Rules (`firestore.rules`)

Paste these into **Firestore Console → Rules** to protect your database and enforce data integrity.

> These rules allow any client to read and write — suitable for a personal single-user setup. Once Firebase Auth is integrated, replace `if true` with `if request.auth != null` to restrict access to authenticated users only.

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper: strict type validation for game documents
    function isValidGame(doc) {
      return doc.title is string
          && doc.title.size() > 0
          && doc.title.size() <= 200
          && doc.platform is string
          && doc.platform in [
            'nes', 'snes', 'n64', 'gamecube', 'wii', 'wii_u', 'switch', 'switch_2',
            'game_boy', 'gba', 'nds', 'n3ds',
            'ps1', 'ps2', 'ps3', 'ps4', 'ps5', 'psp', 'ps_vita',
            'xbox_og', 'xbox_360', 'xbox_one', 'xbox_series',
            'master_system', 'megadrive', 'sega_saturn', 'dreamcast', 'game_gear',
            'retro_vintage'
          ]
          && doc.status is string
          && doc.status in ['WISHLIST', 'OWNED', 'PASS']
          && doc.data is string
          && doc.data.size() <= 100000  // 100 KB limit for the serialised JSON
          && (doc.updatedAt is int || doc.updatedAt is timestamp);
    }

    // 1. games collection — open read/write with type validation
    match /games/{gameId} {
      allow read: if true;
      allow create, update: if isValidGame(request.resource.data);
      allow delete: if true;
    }

    // 2. chat_threads collection — Gemini assistant messages
    match /chat_threads/{messageId} {
      allow read: if true;
      allow create, update: if request.resource.data.text is string
                            && request.resource.data.text.size() <= 20000
                            && request.resource.data.sender is string
                            && request.resource.data.sender in ['USER', 'GEMINI'];
      allow delete: if true;
    }

    // 3. App settings
    match /app_settings/{document} {
      allow read, write: if true;
    }

    // 4. Block any other collection or path not defined above
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```
