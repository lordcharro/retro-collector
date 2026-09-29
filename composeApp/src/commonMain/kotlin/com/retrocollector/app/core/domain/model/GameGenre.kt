package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class GameGenre(
    val displayName: String,
    val icon: String,
    val promptDescription: String
) {
    ALL(
        displayName = "Todos",
        icon = "🌟",
        promptDescription = "Recomendações equilibradas entre clássicos essenciais, jogos de culto e pérolas ocultas"
    ),
    POINT_AND_CLICK(
        displayName = "Point & Click",
        icon = "🕹️",
        promptDescription = "Aventuras gráficas Point & Click, quebra-cabeças com humor e histórias narrativas (estilo LucasArts, Revolution Software, Double Fine)"
    ),
    FPS_TACTICAL(
        displayName = "FPS & Tático",
        icon = "🎯",
        promptDescription = "Jogos de tiro na primeira pessoa (FPS), shooters táticos, simuladores militares e stealth FPS"
    ),
    STRATEGY_RTS(
        displayName = "Estratégia & RTS",
        icon = "⚔️",
        promptDescription = "Estratégia em tempo real (RTS), estratégia por turnos, táticas de combate e simulação"
    ),
    SURVIVAL_HORROR(
        displayName = "Survival Horror",
        icon = "👁️",
        promptDescription = "Survival horror clássico, terror psicológico, gestão de recursos escassos e câmaras fixas ou sobre o ombro"
    ),
    RPG_JRPG(
        displayName = "RPG & JRPG",
        icon = "🛡️",
        promptDescription = "Role-playing games (RPGs ocidentais e JRPGs japoneses com combate por turnos ou ação, grande foco em história e progressão)"
    ),
    ACTION_ADVENTURE(
        displayName = "Ação & Aventura",
        icon = "🗺️",
        promptDescription = "Jogos de ação e aventura cinemática, exploração em terceira pessoa e hack & slash"
    ),
    STEALTH(
        displayName = "Stealth & Espionagem",
        icon = "👥",
        promptDescription = "Jogos de infiltração, espionagem tática e furtividade (estilo Metal Gear Solid, Splinter Cell, Hitman)"
    ),
    PLATFORMER(
        displayName = "Plataformas",
        icon = "🍄",
        promptDescription = "Plataformas 2D e 3D de precisão, aventura e exploração (collect-a-thons)"
    ),
    HIDDEN_GEMS(
        displayName = "Pérolas Ocultas",
        icon = "💎",
        promptDescription = "Jogos de culto menos conhecidos, ignorados comercialmente no lançamento mas adorados por colecionadores"
    )
}
