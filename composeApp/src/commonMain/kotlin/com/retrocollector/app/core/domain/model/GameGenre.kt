package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class GameGenre(
    val displayName: String,
    val icon: String,
    val promptDescription: String
) {
    ALL(
        displayName = "All",
        icon = "🌟",
        promptDescription = "Balanced recommendations between essential classics, cult games, and hidden gems"
    ),
    POINT_AND_CLICK(
        displayName = "Point & Click",
        icon = "🕹️",
        promptDescription = "Point & Click graphic adventures, humorous puzzles, and narrative stories (LucasArts, Revolution Software, Double Fine style)"
    ),
    FPS_TACTICAL(
        displayName = "FPS & Tactical",
        icon = "🎯",
        promptDescription = "First-person shooters (FPS), tactical shooters, military simulations, and stealth FPS"
    ),
    STRATEGY_RTS(
        displayName = "Strategy & RTS",
        icon = "⚔️",
        promptDescription = "Real-time strategy (RTS), turn-based strategy, tactical combat, and simulation"
    ),
    SURVIVAL_HORROR(
        displayName = "Survival Horror",
        icon = "👁️",
        promptDescription = "Classic survival horror, psychological terror, scarce resource management, and fixed or over-the-shoulder camera angles"
    ),
    RPG_JRPG(
        displayName = "RPG & JRPG",
        icon = "🛡️",
        promptDescription = "Role-playing games (Western RPGs and Japanese JRPGs with turn-based or action combat, heavy focus on story and progression)"
    ),
    ACTION_ADVENTURE(
        displayName = "Action & Adventure",
        icon = "🗺️",
        promptDescription = "Cinematic action and adventure games, third-person exploration, and hack & slash"
    ),
    STEALTH(
        displayName = "Stealth & Espionage",
        icon = "👥",
        promptDescription = "Infiltration, tactical espionage, and stealth games (Metal Gear Solid, Splinter Cell, Hitman style)"
    ),
    PLATFORMER(
        displayName = "Platformers",
        icon = "🍄",
        promptDescription = "Precision 2D and 3D platformers, adventure, and exploration (collect-a-thons)"
    ),
    HIDDEN_GEMS(
        displayName = "Hidden Gems",
        icon = "💎",
        promptDescription = "Lesser-known cult games, overlooked at commercial release but beloved by collectors"
    )
}
