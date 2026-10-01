# 🗄️ Esquema da Base de Dados (Cloud Firestore) - RetroCollector

Este documento descreve a estrutura de dados, coleções, documentos, campos, índices recomendados e regras de segurança para a sincronização da aplicação **RetroCollector** no **Google Cloud Firestore**.

---

## 📌 Arquitetura de Dados

O RetroCollector utiliza uma estrutura híbrida no Cloud Firestore para garantir:
1. **Indexação Rápida**: Campos-chave expostos no nível raiz do documento para pesquisas, filtros por consola e triagem por idioma.
2. **Portabilidade Multiplataforma**: Campo `data` (JSON serializado) contendo o modelo completo `GameItem` para desserialização direta em Kotlin Multiplatform (Android, macOS Desktop e WebAssembly).

```
Firestore (default database)
├── 📁 games/                          # Catálogo de jogos e Wishlist
│   └── 📄 {gameId}                    # Ex: "game_ssbm_gc", "game_deadspace_ps3"
│       ├── (Campos raiz indexados)
│       └── (Subcoleção opcional)
│           └── 📁 messages/           # Thread de chat Gemini específica do jogo
│               └── 📄 {messageId}
│
├── 📁 chat_threads/                   # Threads de inteligência por jogo/franquia
│   └── 📄 {messageId}                 # Mensagens do assistente Gemini
│
└── 📁 app_settings/                   # Configurações globais ou por utilizador
    └── 📄 preferences
```

---

## 📂 Coleção: `games`

Cada documento nesta coleção representa um jogo físico (quer esteja na coleção pessoal, em wishlist, ou em avaliação ativa num anúncio do Ricardo.ch / feira da ladra).

### 🏷️ Estrutura do Documento (`/games/{gameId}`)

| Campo | Tipo Firestore | Obrigatório | Descrição | Exemplo |
| :--- | :--- | :---: | :--- | :--- |
| `id` | `string` | Sim | Identificador único do jogo | `"game_gc_smash_melee"` |
| `title` | `string` | Sim | Título oficial do jogo | `"Super Smash Bros. Melee"` |
| `franchiseName` | `string` | Não | Franquia/Série associada | `"Super Smash Bros."` |
| `platform` | `string` | Sim | Código da plataforma (`nes`, `snes`, `n64`, `gamecube`, `wii`, `wii_u`, `switch`, `switch_2`, `game_boy`, `gba`, `nds`, `n3ds`, `ps1`, `ps2`, `ps3`, `ps4`, `ps5`, `psp`, `ps_vita`, `xbox_og`, `xbox_360`, `xbox_one`, `xbox_series`, `master_system`, `megadrive`, `sega_saturn`, `dreamcast`, `game_gear`, `retro_vintage`) | `"gamecube"` |
| `releaseYear` | `string` | Não | Ano de lançamento europeu/regional | `"2002"` |
| `coverImageUrl` | `string` | Não | URL da capa frontal | `"https://.../cover.jpg"` |
| `spineImageUrl` | `string` | Não | URL da foto da lombada com o código serial | `"https://.../spine.jpg"` |
| `productCode` | `string` | Não | Código de produto físico (lombada/disco) | `"DOL-P-GALE"` |
| `barcode` | `string` | Não | Código de barras EAN-13 | `"045496520786"` |
| `spottedLocation` | `string` | Não | Local onde foi avistado/comprado | `"Ricardo.ch"`, `"Brocki Bern"` |
| `askingPriceChf` | `number` (double) | Não | Preço pedido pelo vendedor em CHF | `65.00` |
| `targetPriceChf` | `number` (double) | Não | Preço alvo que o colecionador deseja pagar | `55.00` |
| `paidPriceChf` | `number` (double) | Não | Preço efetivamente pago na compra | `60.00` |
| `collectionStatus` | `string` | Sim | Estado da coleção (`OWNED`, `WISHLIST`, `HUNTING`, `EVALUATING`, `AVOID`) | `"HUNTING"` |
| `languageStatus` | `string` | Sim | Status de idioma (`FULL_ENGLISH`, `SUBTITLES_ONLY`, `GERMAN_ONLY`, `DEPENDS_ON_EDITION`, `UNVERIFIED`) | `"FULL_ENGLISH"` |
| `languageAudio` | `array<string>` | Não | Idiomas de voz presentes no disco/cartucho | `["English", "Japanese"]` |
| `languageSubtitles` | `array<string>`| Não | Idiomas de texto/menu | `["English", "French", "German"]` |
| `safeSkus` | `array<map>` | Não | Lista de SKUs seguros com inglês | *Ver sub-mapa SkuInfo* |
| `riskySkus` | `array<map>` | Não | Lista de SKUs de risco (ex: edições USK só alemão) | *Ver sub-mapa SkuInfo* |
| `marketRadar` | `map` | Não | Métricas de mercado suíço (preço mediano 90d, etc.) | *Ver sub-mapa MarketRadar* |
| `censorshipWarning`| `string` | Não | Alerta de versão censurada (comum na Alemanha/USK) | `"Cuts blood effects in USK release"` |
| `collectorVerdict` | `string` | Não | Veredito sumário do Gemini para colecionadores | `"Cópia segura para compra. UKV e NOE têm inglês."` |
| `listingUrl` | `string` | Não | Link direto para o anúncio do Ricardo.ch / Tutti.ch | `"https://www.ricardo.ch/de/a/..."` |
| `updatedAt` | `integer` (int64) | Sim | Timestamp da última atualização (epoch milliseconds) | `1727464000000` |
| `data` | `string` | Sim | JSON completo serializado para KMP | `"{...}"` |

---

### 🧩 Sub-estruturas (Maps)

#### 1. `SkuInfo` (Itens de `safeSkus` e `riskySkus`)
```json
{
  "code": "DOL-P-GALE",
  "region": "NOE / FRG",
  "editionNote": "Includes English, German, French in game options",
  "isSafe": true
}
```

#### 2. `marketRadar` (Métricas do Mercado Suíço)
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

## 📂 Coleção: `chat_threads`

Armazena as mensagens contextuais de assistência entre o colecionador e o Gemini Flash sobre os jogos ou inspeções de lombada.

### 🏷️ Estrutura do Documento (`/chat_threads/{messageId}`)

| Campo | Tipo Firestore | Descrição | Exemplo |
| :--- | :--- | :--- | :--- |
| `id` | `string` | ID único da mensagem | `"msg_1727464100_user"` |
| `contextId` | `string` | ID do jogo associado (`gameId`) | `"game_gc_smash_melee"` |
| `sender` | `string` | Quem enviou a mensagem (`USER` ou `GEMINI`) | `"GEMINI"` |
| `text` | `string` | Conteúdo da resposta ou pergunta | `"A edição NOE tem seletor de 60Hz e inglês."` |
| `imageBase64` | `string` (opcional)| Foto enviada pelo utilizador para análise (thumbnail) | `null` |
| `timestamp` | `integer` (int64) | Data/hora de envio em epoch ms | `1727464100000` |
| `suggestedGameUpdate` | `map` (opcional) | Sugestão de alteração de campos para aceitar com 1 clique | `null` |

---

## 📄 Exemplo Real de Documento JSON (`games/game_deadspace_ps3`)

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
      "editionNote": "German audio and text only! USK logo on front.",
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
  "censorshipWarning": "Cópia alemã USK (BLES-00351) não tem áudio nem legendas em inglês. Requer versão UK/PEGI (BLES-00350).",
  "collectorVerdict": "ATENÇÃO: Vendedor no Ricardo está a vender a versão alemã BLES-00351. Rejeitar ou pedir fotos da contracapa.",
  "listingUrl": "https://www.ricardo.ch/de/a/dead-space-ps3-1240000000/",
  "updatedAt": 1727464000000
}
```

---

## ⚡ Índices Compostos Recomendados

Para otimizar as consultas e filtros da app sem overhead:

1. **Catálogo por Consola e Atualização**:
   - Coleção: `games`
   - Campos: `platform` (ASC), `updatedAt` (DESC)
2. **Filtro por Estado da Coleção**:
   - Coleção: `games`
   - Campos: `collectionStatus` (ASC), `updatedAt` (DESC)
3. **Triagem por Risco de Idioma**:
   - Coleção: `games`
   - Campos: `languageStatus` (ASC), `platform` (ASC)
4. **Mensagens de Chat por Jogo**:
   - Coleção: `chat_threads`
   - Campos: `contextId` (ASC), `timestamp` (ASC)

---

## 🔒 Regras de Segurança e Validação de Tipos (`firestore.rules`)

Para proteger a tua base de dados na consola do Firebase e garantir a integridade dos tipos de dados:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Função auxiliar: validação rigorosa dos tipos de dados para jogos
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
          && doc.status in ['HUNTING', 'OWNED', 'PASS', 'WISHLIST']
          && doc.data is string 
          && doc.data.size() <= 100000 // Limite de 100KB para o JSON serializado
          && (doc.updatedAt is int || doc.updatedAt is timestamp);
    }
    
    // 1. Coleção Games: leitura pública e escrita com validação de tipos
    match /games/{gameId} {
      allow read: if true;
      allow create, update: if isValidGame(request.resource.data);
      allow delete: if true;
    }
    
    // 2. Coleção Chat Threads: mensagens do assistente Gemini
    match /chat_threads/{messageId} {
      allow read: if true;
      allow create, update: if request.resource.data.text is string
                            && request.resource.data.text.size() <= 20000
                            && request.resource.data.sender is string
                            && request.resource.data.sender in ['USER', 'GEMINI'];
      allow delete: if true;
    }
    
    // 3. Configurações da App
    match /app_settings/{document} {
      allow read, write: if true;
    }

    // 4. Bloqueia qualquer outra coleção ou tentativa fora de esquema
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```
