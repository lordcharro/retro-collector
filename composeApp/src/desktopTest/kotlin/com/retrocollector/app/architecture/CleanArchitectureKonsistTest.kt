package com.retrocollector.app.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertNotEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

/**
 * Clean Architecture integrity tests inspired by the Swiss Confederation
 * official reference architecture (swiyu-admin-ch/eidch-android-wallet).
 */
class CleanArchitectureKonsistTest {

    @Test
    fun `clean architecture has correct layer dependencies`() {
        Konsist.scopeFromProduction().assertArchitecture {
            val presentation = Layer("Presentation", "..presentation..")
            val domain = Layer("Domain", "..domain..")
            val data = Layer("Data", "..data..")

            domain.dependsOnNothing()
            presentation.dependsOn(domain)
            data.dependsOn(domain)
        }
    }

    @Test
    fun `domain_repository should only contain interfaces and no classes`() {
        val domainRepositories = Konsist.scopeFromPackage("..domain.repository..")
        domainRepositories
            .interfaces()
            .assertNotEmpty()

        domainRepositories
            .classes()
            .assertEmpty()
    }

    @Test
    fun `'Repository' files should reside in 'repository' package`() {
        Konsist
            .scopeFromProduction()
            .files
            .withNameEndingWith("Repository.kt")
            .assertTrue {
                it.packagee?.hasNameContaining(".repository") == true
            }
    }

    @Test
    fun `ViewModels classes should have 'ViewModel' suffix and reside in 'presentation' package`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue {
                it.resideInPackage("..presentation..")
            }
    }

    @Test
    fun `Use cases should reside in domain usecase package and have invoke operator`() {
        Konsist
            .scopeFromProduction()
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue {
                it.resideInPackage("..domain.usecase..") &&
                it.hasFunction { function -> function.name == "invoke" }
            }
    }
}
