# RetroCollector - Game Language & Edition Tracker (CH / PAL) 🎮

Aplicação multi-plataforma construída em **Kotlin Multiplatform (Compose Multiplatform)** para colecionadores de consolas retro (**Nintendo 64, GameCube, PlayStation 3 e Nintendo Switch**) que vivem na **Suíça** ou compram no mercado europeu (PAL).

Resolve o clássico problema dos lançamentos regionais em segunda mão no mercado suíço (Ricardo.ch, Tutti.ch, lojas e feiras da ladra), onde muitas cópias físicas da GameCube e PS3 são edições alemãs (USK) bloqueadas a alemão, identificando edições seguras com **áudio e texto em inglês**.

---

## 🚀 Plataformas Suportadas (3-em-1 Partilhado)

1. **📱 Android Nativo (`:androidApp`)**:
   - APK de depuração já gerado em: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`
   - Integração com o menu de partilha do Android (`ACTION_SEND`): partilhe anúncios diretamente da app do Ricardo.ch para a app!
2. **💻 Desktop macOS (`:desktopApp`)**:
   - Executável UberJar gerado em: `composeApp/build/compose/jars/RetroCollector-macos-x64-1.0.0.jar`
   - Execute com: `./gradlew :composeApp:run`
3. **🌐 WebAssembly Wasm & Docker no Synology NAS (`:wasmJs`)**:
   - Compilação Wasm de alto desempenho via Skiko / Canvas.
   - Configuração pronta para Docker / Container Manager no Synology em `docker/docker-compose.yml` e `docker/nginx.conf`.

---

## 🛠️ Tecnologias Utilizadas

* **Kotlin 2.0 & Jetpack / Compose Multiplatform 1.6.11**
* **Material 3 Adaptativo** (Dark Theme retro gaming, alto contraste)
* **Google Gemini 2.0 / 1.5 Flash API** (com visão multimodal, OCR de códigos seriais na lombada e prompt especializado em lançamentos PAL)
* **Firebase Cloud Firestore** (sincronização de jogos e chats em tempo real entre telemóvel e computador)
* **Ktor Client 3.0** (chamadas REST e scraping multiplataforma de anúncios Ricardo.ch e Tutti.ch)
* **Kotlinx Serialization & Coroutines**

---

## 📦 Como Executar e Utilizar

### 1. No Mac (Desktop)
Para abrir a aplicação nativa de janela no teu Mac:
```bash
./gradlew :composeApp:run
```
Ou executar diretamente o JAR:
```bash
java -jar "composeApp/build/compose/jars/RetroCollector-macos-x64-1.0.0.jar"
```

### 2. No Telemóvel Android
Instala o APK diretamente no teu telemóvel com adb:
```bash
adb install -r "composeApp/build/outputs/apk/debug/composeApp-debug.apk"
```
Ou abre este projeto no **Android Studio** e seleciona o target `composeApp` para correr no teu dispositivo físico ou emulador.

### 3. No Synology NAS (Docker Container)
A pasta `docker/` contém tudo o que é necessário para servir a versão WebAssembly:
1. No teu Mac, gera a distribuição:
   ```bash
   ./gradlew :composeApp:wasmJsBrowserDistribution
   ```
2. Copia a pasta `dist` resultante para o teu Synology.
3. No **Container Manager** do Synology, inicia o `docker-compose.yml`:
   ```bash
   docker-compose up -d
   ```
4. Acede ao teu tracker em `http://<IP_DO_SYNOLOGY>:8085`.

---

## 🔑 Configuração das Chaves (Gemini & Firebase)

Na app, clica no ícone de engrenagem **⚙️ Definições**:
1. **Gemini API Key**:
   - Gera uma chave gratuita em [Google AI Studio](https://aistudio.google.com/).
   - Cola no campo correspondente e clica em **Testar Chave Gemini**.
2. **Firebase Firestore**:
   - Cria um projeto gratuito no [Firebase Console](https://console.firebase.google.com/).
   - Ativa o **Cloud Firestore** em modo de teste ou com regras de leitura/escrita.
   - Introduz o teu **Firebase Project ID** nas definições.

---

## 🔍 Como Funciona a Deteção de Idioma

A app tem um prompt de sistema rigoroso treinado nas peculiaridades do mercado PAL europeu:
* **PS3**: Identifica códigos `BLES-xxxxx` na lombada. Emite alertas para códigos USK da região DACH (ex: *Fallout 3 BLES-00561* sem inglês) e sugere a versão britânica (*BLES-00344*).
* **GameCube**: Distingue entre `DOL-P-xxxx-(NOE)` (muitas vezes apenas alemão) e `DOL-P-xxxx-(UKV/EUR)` (multi-5 ou inglês garantido).
* **N64**: Mapeia cartuchos europeus `NUS-xxxx-EUR`.
* **Switch**: Valida se o cartucho europeu inclui áudio em inglês.
* **Conversa Contextual**: Cada jogo ou franquia tem o seu próprio histórico de chat guardado para que possas colocar perguntas de seguimento a qualquer momento.
