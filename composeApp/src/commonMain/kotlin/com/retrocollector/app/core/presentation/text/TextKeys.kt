package com.retrocollector.app.core.presentation.text

/**
 * Catálogo centralizado de TextKeys para a aplicação RetroCollector.
 * Evita strings hardcoded na interface gráfica, facilitando manutenção,
 * testes e internacionalização futura (DE / FR / EN / PT).
 */
object TextKeys {

    object App {
        const val TITLE = "RetroCollector"
        const val SUBTITLE = "Swiss Retrogaming Terminal"
        const val VERSION = "v1.0.0 • Clean Architecture"
        const val TAGLINE = "Language Detection & Swiss Market Radar"
    }

    object Dashboard {
        const val SEARCH_PLACEHOLDER = "Search title, franchise, SKU (e.g. DOL-P-GALE), or barcode..."
        const val STATS_TRACKED = "Tracked Items"
        const val STATS_SAFE_RATIO = "Safe Copies"
        const val STATS_AVG_CHF = "Avg Value (CHF)"
        const val FILTER_ALL = "All"
        const val FILTER_ENGLISH_ONLY = "English Friendly ✅"
        const val FILTER_USK_ALERTS = "USK Alerts ⚠️"
        const val EMPTY_CATALOG = "No games found matching your search criteria."
        const val CLEAR_FILTERS = "Clear filters"
        const val NO_GAME_SELECTED = "Select a game from the catalog to view its dossier and intelligence chat."
        const val ACTION_QUICK_SCAN = "⚡ Quick Scan"
        const val ACTION_SETTINGS = "⚙️ Settings"
    }

    object Navigation {
        const val TAB_CATALOG = "Catalog"
        const val TAB_THREADS = "Dossier"
        const val TAB_SETTINGS = "Settings"
        const val BACK = "Back"
    }

    object Radar {
        const val TITLE = "SWISS MARKET RADAR (RICARDO.CH)"
        const val ASKING_PRICE = "Asking / Spotted"
        const val MEDIAN_90D = "Ricardo.ch 90d Median"
        const val HISTORICAL_RANGE = "Historical Range"
        const val TREND = "Trend"
        const val DEAL_BARGAIN = "Bargain Deal"
        const val DEAL_GOOD = "Good Deal"
        const val DEAL_FAIR = "Fair Market Price"
        const val DEAL_OVERPRICED = "Overpriced"
        const val VS_RICARDO = "vs Ricardo.ch"
    }

    object Sku {
        const val MATRIX_TITLE = "REGIONAL SKU MATRIX"
        const val SAFE_TITLE = "SAFE RELEASES (ENGLISH CONFIRMED)"
        const val RISKY_TITLE = "RISKY / MONOLINGUAL RELEASES (USK/GER)"
        const val EMPTY_SAFE = "No confirmed safe SKUs recorded yet."
        const val EMPTY_RISKY = "No known risky/monolingual SKUs recorded."
        const val UNCUT_LABEL = "Uncut"
        const val CUT_LABEL = "Censored"
    }

    object Dossier {
        const val TITLE = "INTELLIGENCE DOSSIER"
        const val SPOTTED_LOCATION = "Spotted Location"
        const val BARCODE = "Barcode (EAN-13)"
        const val PRODUCT_CODE = "Product Code (SKU)"
        const val AUDIO_LANGUAGES = "Audio Languages"
        const val SUBTITLE_LANGUAGES = "Subtitle Languages"
        const val CENSORSHIP_WARNING = "Censorship Warning"
        const val COLLECTOR_VERDICT = "Collector Verdict"
        const val CHAT_TITLE = "GEMINI RETRO ASSISTANT"
        const val CHAT_PLACEHOLDER = "Ask Gemini about revisions, uncut versions, or market prices..."
        const val CHAT_SEND = "Send"
        const val STATUS_CHANGE = "Status"
    }

    object Scanner {
        const val TITLE = "⚡ Quick Scan & Verification"
        const val TAB_URL = "Auction Link"
        const val TAB_MANUAL = "Manual Search"
        const val TAB_PHOTO = "Spine / Box Photo"
        const val URL_LABEL = "Paste Ricardo.ch / Tutti.ch auction URL"
        const val URL_PLACEHOLDER = "https://www.ricardo.ch/de/a/..."
        const val URL_BUTTON = "Fetch & Analyze Listing"
        const val MANUAL_LABEL = "Title, Serial Code or Barcode"
        const val MANUAL_PLACEHOLDER = "e.g. Dead Space BLES-00350"
        const val MANUAL_BUTTON = "Verify with Gemini"
        const val PHOTO_LABEL = "Upload or Drop Spine / Cover Photo"
        const val PHOTO_BUTTON = "Select Image"
        const val PHOTO_ANALYZE_BUTTON = "Analyze Photo with Gemini Flash"
        const val ANALYZING = "Analyzing with Gemini Flash..."
        const val CLOSE = "Close"
    }

    object Settings {
        const val TITLE = "Settings & Integrations"
        const val GEMINI_SECTION = "Vision Intelligence Engine"
        const val GEMINI_MODEL = "FLASH 3.8"
        const val GEMINI_API_KEY_LABEL = "Google Gemini API Key"
        const val GEMINI_API_KEY_HINT = "Enter API key (AQ.A... or AIzaSy...)"
        const val GEMINI_EXPLAINER = "Used for real-time OCR spine recognition and European multi-language verification (detects USK forced German dubs & UK English codes)."
        const val GEMINI_TEST_BUTTON = "Test API Connection"
        const val CURRENCY_SECTION = "Regional Pricing & Market Feeds"
        const val CURRENCY_LABEL = "Evaluation Base Currency"
        const val SCRAPER_TITLE = "Ricardo & Tutti Parser"
        const val SCRAPER_SUBTITLE = "Auto-detect second-hand lots, SKU serials & condition tags from copied listing URLs."
        const val SCRAPER_BADGE = "CH ONLY"
        const val FIREBASE_SECTION = "Cloud Database Sync & Offline Engine"
        const val FIREBASE_PROJECT_ID_LABEL = "Firebase Project ID"
        const val FIREBASE_PROJECT_ID_HINT = "e.g. retro-collector-swiss"
        const val FIREBASE_TEST_BUTTON = "Test Firestore Connection"
        const val SAVE_BUTTON = "Save Settings"
        const val STATUS_CONNECTED = "Connection verified successfully!"
        const val STATUS_FAILED = "Connection test failed. Verify credentials."
    }

    object Status {
        const val OWNED = "Owned 📦"
        const val WISHLIST = "Wishlist 🎯"
        const val HUNTING = "Hunting 🔍"
        const val HUNTING_CLEAN = "Hunting"
        const val EVALUATING = "Evaluating ⏳"
        const val AVOID = "Avoid 🚫"
    }

    object Language {
        const val FULL_ENGLISH_TITLE = "Full English"
        const val FULL_ENGLISH_DESC = "Audio & text fully supported in English"
        const val SUBTITLES_ONLY_TITLE = "Subtitles Only"
        const val SUBTITLES_ONLY_DESC = "English text/menus with foreign audio"
        const val GERMAN_ONLY_TITLE = "German USK Only"
        const val GERMAN_ONLY_DESC = "German audio/text only (No English!)"
        const val DEPENDS_TITLE = "Check Edition"
        const val DEPENDS_DESC = "Language support varies by regional SKU code"
        const val UNVERIFIED_TITLE = "Unverified"
        const val UNVERIFIED_DESC = "Audio & text language not yet verified"
    }

    object Platform {
        const val N64_TITLE = "Nintendo 64"
        const val GAMECUBE_TITLE = "GameCube"
        const val PS3_TITLE = "PlayStation 3"
        const val SWITCH_TITLE = "Nintendo Switch"
    }
}
