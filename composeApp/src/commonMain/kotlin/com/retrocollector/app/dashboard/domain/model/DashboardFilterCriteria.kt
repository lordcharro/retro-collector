package com.retrocollector.app.dashboard.domain.model

import com.retrocollector.app.core.domain.model.CollectionStatus
import com.retrocollector.app.core.domain.model.ConsolePlatform

data class DashboardFilterCriteria(
    val query: String = "",
    val platform: ConsolePlatform? = null,
    val status: CollectionStatus? = null,
    val englishOnly: Boolean = false,
    val uskOnly: Boolean = false
)
