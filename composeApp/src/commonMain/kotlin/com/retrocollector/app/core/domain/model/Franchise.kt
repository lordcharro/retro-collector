package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Franchise(
    val id: String,
    val name: String,
    val description: String = "",
    val gameCount: Int = 0,
    val platforms: List<ConsolePlatform> = emptyList()
)
