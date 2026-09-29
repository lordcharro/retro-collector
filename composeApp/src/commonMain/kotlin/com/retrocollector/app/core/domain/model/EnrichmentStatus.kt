package com.retrocollector.app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class EnrichmentStatus(val label: String) {
    PENDING("Pending"),
    ENRICHING("Enriching"),
    COMPLETE("Complete"),
    FAILED("Failed");

    companion object {
        fun fromString(value: String?): EnrichmentStatus =
            entries.find { it.name.equals(value, ignoreCase = true) || it.label.equals(value, ignoreCase = true) }
                ?: COMPLETE
    }
}
