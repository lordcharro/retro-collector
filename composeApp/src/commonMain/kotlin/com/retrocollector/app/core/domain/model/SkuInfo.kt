package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SkuInfo(
    val code: String,
    val region: String,
    val editionNote: String,
    val isSafe: Boolean
)
