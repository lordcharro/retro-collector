# RetroCollector — Game Language & Edition Tracker (CH / PAL) 🎮

A multiplatform app built with **Kotlin Multiplatform (Compose Multiplatform)** for retro game collectors focused on **Nintendo 64, GameCube, PlayStation 3, and Nintendo Switch** who live in **Switzerland** or buy from the European (PAL) market.

It solves the classic second-hand regional release problem in the Swiss market (Ricardo.ch, Tutti.ch, flea markets) where many physical GameCube and PS3 copies are German USK editions locked to German-only audio, by identifying safe editions with **full English audio and text**.

---

## 🚀 Supported Platforms (3-in-1 Shared Codebase)

1. **📱 Android (`:androidApp`)**
   - Install the debug APK directly: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`
   - Integrated with the Android Share menu (`ACTION_SEND`): share a Ricardo.ch or Tutti.ch listing directly into the app.

2. **💻 Desktop macOS (`:desktopApp`)**
   - UberJar built at: `composeApp/build/compose/jars/RetroCollector-macos-x64-1.0.0.jar`
   - Run with: `./gradlew :composeApp:run`

3. **🌐 WebAssembly (Wasm) & Docker on Synology NAS (`:wasmJs`)**
   - High-performance Wasm build via Skiko / Canvas.
   - Ready-to-use Docker setup for Synology Container Manager in `docker/docker-compose.yml` and `docker/nginx.conf`.

---

## 🛠️ Tech Stack

* **Kotlin 2.0 & Compose Multiplatform 1.6.11**
* **Material 3 Adaptive** (dark retro gaming theme, high contrast)
* **Google Gemini Flash API** (multimodal vision, spine serial OCR, PAL-specialised prompts)
* **Firebase Cloud Firestore** (real-time game and chat sync across devices)
* **Ktor Client 3.0** (multiplatform REST calls and listing scraping from Ricardo.ch / Tutti.ch)
* **Kotlinx Serialization & Coroutines**

---

## 📦 Getting Started

### 1. macOS Desktop

Run the native window app:
```bash
./gradlew :composeApp:run
```
Or run the JAR directly:
```bash
java -jar "composeApp/build/compose/jars/RetroCollector-macos-x64-1.0.0.jar"
```

### 2. Android

Install the debug APK via adb:
```bash
adb install -r "composeApp/build/outputs/apk/debug/composeApp-debug.apk"
```
Or open this project in **Android Studio** and select the `composeApp` run target for your device or emulator.

### 3. Synology NAS (Docker)

The `docker/` folder contains everything needed to serve the WebAssembly build:

1. Build the Wasm distribution on your Mac:
   ```bash
   ./gradlew :composeApp:wasmJsBrowserDistribution
   ```
2. Copy the generated `dist` folder to your Synology.
3. Start the container in **Synology Container Manager**:
   ```bash
   docker-compose up -d
   ```
4. Access the tracker at `http://<YOUR_SYNOLOGY_IP>:8085`.

---

## 🔑 API Keys Setup (Gemini & Firebase)

In the app, tap the gear icon **⚙️ Settings**:

1. **Gemini API Key**
   - Generate a free key at [Google AI Studio](https://aistudio.google.com/).
   - Paste it in the corresponding field and tap **Test Gemini Key**.

2. **Firebase Firestore**
   - Create a free project at the [Firebase Console](https://console.firebase.google.com/).
   - Enable **Cloud Firestore** in test mode or with read/write rules.
   - Enter your **Firebase Project ID** in Settings.
   > ⚠️ **Security note:** Test mode leaves the database publicly readable and writable. This is fine for personal use in a controlled environment, but configure proper [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started) before sharing your Project ID or using in production.

---

## 🔍 How Language Detection Works

The app uses a system prompt specialised in European PAL market quirks:

* **PS3**: Identifies `BLES-xxxxx` codes on the spine. Flags USK/DACH region codes that lack English audio (e.g. *Fallout 3 BLES-00561*) and suggests the UK version (*BLES-00344*).
* **GameCube**: Distinguishes between `DOL-P-xxxx-(NOE)` (often German-only) and `DOL-P-xxxx-(UKV/EUR)` (multi-5 or guaranteed English).
* **N64**: Maps European `NUS-xxxx-EUR` cartridges.
* **Switch**: Validates whether the European cartridge includes English audio.
* **Contextual Chat**: Every game or franchise keeps its own conversation history so you can ask follow-up questions at any time.

---

## ⚖️ Legal Disclaimer

The listing reader feature fetches publicly accessible pages from Ricardo.ch, Tutti.ch, Anibis.ch, and eBay to extract metadata from links shared by the user. This is provided for **personal convenience and educational purposes only**. Users are **solely responsible** for complying with each platform's Terms of Service. The author does not encourage or endorse any use that violates those terms.

---

## 📜 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.
