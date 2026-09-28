# 📋 RetroCollector - Registo de Progresso e Próximos Passos (`progress.md`)

Este ficheiro resume o estado atual do desenvolvimento, os artefactos gerados, e o roteiro detalhado dos próximos passos para a evolução da aplicação **RetroCollector**.

---

## ✅ O Que Já Está Implementado e Validado

### 1. Arquitetura e Engenharia de Software
* [x] **Organização por Conjuntos Funcionais (Package-by-Feature)**:
  * Estrutura direta sem camadas intermédias: `dashboard`, `dossier`, `scanner`, `settings`, e `core`.
  * Cada conjunto contém internamente as suas camadas Clean Architecture (`domain/model`, `domain/usecase`, `presentation/ui`, `presentation/viewmodel`).
* [x] **Clean Architecture Rigorosa e Desacoplada**:
  * **`domain/`**: Modelos puros (`GameItem`, `ConsolePlatform`, `LanguageStatus`, `SwissMarketRadar`, `SkuInfo`, `ChatMessage`), interface abstrata `IGameRepository`, e UseCases desacoplados (`GetDashboardGamesUseCase`, `SaveGameUseCase`, `DeleteGameUseCase`, `AnalyzeGameWithGeminiUseCase`, `SendFollowUpChatUseCase`, `UpdateSettingsUseCase`). O domínio não possui dependências de infraestrutura nem frameworks.
  * **`data/`**: `GeminiRemoteDataSource` (cliente Ktor multimodal e extração JSON com schema estruturado), `FirestoreService` (sincronização Cloud Firestore via REST compatível com todas as plataformas KMP), `ListingScraper` (extrator de metadados do Ricardo.ch e Tutti.ch), e repositório `GameRepositoryImpl`.
  * **`presentation/`**: `DashboardViewModel` reativo com `StateFlow` e pipeline de filtragem tipada, e componentes modulares em Compose Multiplatform.
* [x] **Catálogo Centralizado de Textos (`TextKeys`)**:
  * Substituição integral de strings hardcoded por constantes semânticas categorizadas (`TextKeys.App`, `TextKeys.Dashboard`, `TextKeys.Radar`, `TextKeys.Sku`, `TextKeys.Dossier`, `TextKeys.Scanner`, `TextKeys.Settings`, etc.), garantindo consistência e preparação para i18n.
* [x] **Testes de Arquitetura com Konsist**:
  * Inspirado no repositório oficial da Confederação Suíça (`swiyu-admin-ch/eidch-android-wallet`).
  * 5 testes automatizados a validar: isolamento de camadas Clean Architecture (`domain.dependsOnNothing()`, `presentation.dependsOn(domain)`), apenas interfaces no pacote de repositório de domínio, convenção de sufixo e pacote de ViewModels, e estrutura `invoke()` nos UseCases.
* [x] **Análise Estática de Código com Detekt**:
  * Regras configuradas em `config/detekt/detekt.yml` com suporte a Compose Multiplatform (`ignoreAnnotated: ['Composable']`) e JUnit 5 (`ignoreAnnotated: ['Test']`).
  * 0 erros e 0 avisos em toda a base de código.
* [x] **Ambiente JDK 21 LTS (Eclipse Adoptium Temurin)**:
  * Instalado em `~/.jdks/jdk-21.0.12.1+1` e configurado no `gradlew` para compatibilidade total com Gradle 8.7 e Android Studio 2026.1+.

### 2. Design System: "Tactile Field Dark" (Google Stitch)
* [x] **Split-View Workstation macOS (Desktop)**: Layout profissional de 2 colunas com navegação rápida de catálogo à esquerda (~42%) e dossier do jogo com chat de IA à direita (~58%).
* [x] **Layout Adaptativo Mobile**: Ecrã otimizado para smartphone com bottom bar, cards táteis e vista detalhada.
* [x] **Swiss Market Radar**: Comparação de preço pedido com a mediana dos últimos 90 dias do Ricardo.ch, exibindo a variação percentual (`-23.5% vs Ricardo.ch`, `Good Deal`).
* [x] **Matriz de SKUs Seguros vs Arriscados**: Destaque para códigos europeus seguros (ex: `DOL-P-GALE (NOE/FRG)`) versus edições USK alemãs monolíngues sem inglês.
* [x] **Tipografia e Cores**: Estilo Zinc escuro (`#09090B`, `#18181B`, `#27272A`), `FontFamily.Monospace` (JetBrains Mono) para códigos e preços em CHF, e cores cromáticas para as consolas (N64, GameCube, PS3, Switch).

### 3. Compilação e Build Multiplataforma
* [x] **macOS Desktop (JVM)**: Compilado com sucesso via `./gradlew compileKotlinDesktop` e empacotado em `RetroCollector-macos-x64-1.0.0.jar`.
* [x] **Android Nativo**: Compilado com sucesso via `./gradlew :composeApp:assembleDebug`.
* [x] **WebAssembly (wasmJs)**: Compilado e otimizado com sucesso via `./gradlew :composeApp:wasmJsBrowserDistribution`.
* [x] **Docker Synology**: Ficheiros `Dockerfile`, `nginx.conf` (MIME `application/wasm`) e `docker-compose.yml` prontos em `docker/`.

---

## 🎯 Roteiro de Próximos Passos

### 🔹 Fase 1: Ambiente Local e Primeiro Sync no Android Studio ✅
1. [x] **Configurar o Gradle JDK no Android Studio**: Concluído (JDK 21 Temurin configurado e sync validado).
2. [x] **Executar a app Android no Emulador ou Telemóvel**: Validado via `./gradlew :composeApp:assembleDebug`.

### 🔹 Fase 2: Experimentar a App no macOS Desktop ✅
1. [x] **Lançar a versão nativa de Mac**:
   ```bash
   ./gradlew :composeApp:run
   ```
2. [x] **Otimização de Janela e Layout Defensivo (Anti-Wrapping & Crop)**:
   * **Janela Inicial Maior**: Janela inicial configurada para `1440x900` com centragem automática no ecrã e `minimumSize` de `1024x680`.
   * **Crop Elegante (`TextOverflow.Ellipsis`)**: Títulos longos de jogos (ex: *The Legend of Zelda: The Wind Waker*) usam peso flexível e truncam com reticências em vez de empurrarem as tags.
   * **Zero Quebra Vertical (`softWrap = false`, `maxLines = 1`)**: Badges de produto (`DOL-P-GZLP`), preços em CHF e tags de idioma nunca quebram letras na vertical.
   * **Layout Responsivo (`FlowRow` & `BoxWithConstraints`)**: Matriz de SKUs e Radar de Preço Suíço adaptam-se dinamicamente (passam para linha seguinte ou empilham-se) caso a coluna do dossier seja estreita.

### 🔹 Fase 3: Ligar a Inteligência Artificial (Google Gemini Flash 3.8)
1. **Obter chave de API gratuita**:
   * Criar ou aceder a uma chave em [Google AI Studio](https://aistudio.google.com/app/apikey).
   * *Nota sobre formatos de chave*: As chaves da Google podem começar tanto pelo formato clássico (`AIzaSy...`) como pelo formato recente (`AQ.A...` ou `AQ.AB...`). Ambos os formatos são 100% suportados e válidos.
2. **Configurar e Validar na Aplicação**:
   * Abrir a app (Mac ou Android), aceder ao menu **⚙️ Definições**.
   * Inserir a chave e premir **"Testar Conexão Gemini"** (o botão executa uma validação em tempo real contra a Google API e indica se a ligação foi bem-sucedida).
   * Premir **"Save Settings"**.
3. **Testar o "⚡ Quick Scan"**:
   * Colar um link de um leilão de retrogaming do Ricardo.ch ou Tutti.ch.
   * Carregar uma fotografia de uma lombada/capa física para o Gemini ler o código e confirmar os idiomas de áudio e legendas.

### 🔹 Fase 4: Sincronização Cloud (Firebase Firestore)
1. **Criar o Projeto no Firebase**:
   * Aceder à consola [Firebase Console](https://console.firebase.google.com/) e criar o projeto.
   * Ativar o **Cloud Firestore Database**.
2. **Adicionar as Regras Básicas de Validação e Segurança (Firestore Rules)**:
   * No separador **Firestore Database ➔ Rules** da consola, aplicar as regras de validação de tipos de dados (`title is string`, `platform in [...]`, `status in [...]`, etc.).
   * Consultar o ficheiro [schema.md](file:///Users/ivolopes/development/physical%20disk%20follow/schema.md#L182-L225) para o código pronto a copiar, evitando a expiração dos 30 dias do modo de teste e protegendo a integridade da BD.
3. **Registar o Project ID na App**:
   * Nas **⚙️ Definições** da app, inserir o teu `Project ID` do Firebase.
   * Testar a sincronização gravando ou editando um jogo para validar a comunicação REST.

### 🔹 Fase 5: Alojamento Web no Synology NAS (Opcional)
1. Gerar o pacote WebAssembly de produção:
   ```bash
   ./gradlew :composeApp:wasmJsBrowserDistribution
   ```
2. Transferir a pasta `docker/` e os ficheiros gerados em `composeApp/build/dist/wasmJs/productionExecutable/` para o teu Synology NAS.
3. Iniciar o contentor no Synology Container Manager com:
   ```bash
   docker-compose up -d
   ```

---

## 🚀 Backlog de Funcionalidades Futuras

* [ ] **Scanner de Código de Barras com CameraX**: Adicionar leitura nativa de código de barras na versão Android usando a câmara do telemóvel.
* [ ] **Notificações de Oportunidades**: Monitorizar anúncios novos no Ricardo.ch e enviar notificações quando surgir um jogo da Wishlist abaixo do preço alvo em CHF.
* [ ] **Exportação da Coleção**: Exportar catálogo e valores de inventário em formato CSV ou PDF para efeitos de seguro ou partilha entre colecionadores.
