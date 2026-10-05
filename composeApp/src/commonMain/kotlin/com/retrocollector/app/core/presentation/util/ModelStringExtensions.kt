package com.retrocollector.app.core.presentation.util

import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.core.domain.model.AppSection
import com.retrocollector.app.core.domain.model.CollectionStatus
import com.retrocollector.app.core.domain.model.LanguageStatus
import org.jetbrains.compose.resources.StringResource

val AppSection.labelRes: StringResource
    get() = when (this) {
        AppSection.ACTIVITY -> Res.string.nav_activity
        AppSection.DISCOVER -> Res.string.nav_discover
        AppSection.WISHLIST -> Res.string.nav_wishlist
        AppSection.COLLECTION -> Res.string.nav_collection
    }

val CollectionStatus.labelRes: StringResource
    get() = when (this) {
        CollectionStatus.OWNED -> Res.string.status_owned
        CollectionStatus.WISHLIST -> Res.string.status_wishlist
        CollectionStatus.PASS -> Res.string.status_pass
    }

val LanguageStatus.titleRes: StringResource
    get() = when (this) {
        LanguageStatus.FULL_ENGLISH -> Res.string.lang_full_english_title
        LanguageStatus.SUBS_ONLY -> Res.string.lang_subtitles_only_title
        LanguageStatus.GERMAN_ONLY -> Res.string.lang_german_only_title
        LanguageStatus.EDITION_NOTICE -> Res.string.lang_depends_title
        LanguageStatus.UNVERIFIED -> Res.string.lang_unverified_title
    }

val LanguageStatus.descRes: StringResource
    get() = when (this) {
        LanguageStatus.FULL_ENGLISH -> Res.string.lang_full_english_desc
        LanguageStatus.SUBS_ONLY -> Res.string.lang_subtitles_only_desc
        LanguageStatus.GERMAN_ONLY -> Res.string.lang_german_only_desc
        LanguageStatus.EDITION_NOTICE -> Res.string.lang_depends_desc
        LanguageStatus.UNVERIFIED -> Res.string.lang_unverified_desc
    }


