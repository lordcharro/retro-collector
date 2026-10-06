import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.detekt)
}

kotlin {
    android {
        namespace = "com.retrocollector.app"
        compileSdk = 37
        minSdk = 26
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val desktopMain = getByName("desktopMain")

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.ui.tooling)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.core.ktx)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.java)
        }
        getByName("desktopTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.konsist)
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.engine)
                implementation(libs.junit.jupiter.params)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}



compose.desktop {
    application {
        mainClass = "com.retrocollector.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "RetroCollector"
            packageVersion = "1.0.0"
            description = "RetroCollector - Swiss & European Retro Video Game Price & Condition Tracker"
            copyright = "© 2026 RetroCollector"
            vendor = "RetroCollector"

            modules(
                "java.net.http",
                "java.instrument",
                "java.management",
                "jdk.unsupported",
                "java.naming",
                "java.xml"
            )

            macOS {
                iconFile.set(project.file("src/desktopMain/resources/app_icon.icns"))
                bundleID = "com.retrocollector.app"
                dockName = "RetroCollector"
            }
        }
    }
}

compose.resources {
    packageOfResClass = "com.retrocollector.app.generated.resources"
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    source.setFrom(
        files(
            "src/commonMain/kotlin",
            "src/desktopMain/kotlin",
            "src/androidMain/kotlin",
            "src/wasmJsMain/kotlin"
        )
    )
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    setSource(
        files(
            "src/commonMain/kotlin",
            "src/desktopMain/kotlin",
            "src/androidMain/kotlin",
            "src/wasmJsMain/kotlin"
        )
    )
    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}



val syncComposeResourcesToAndroid = tasks.register<Copy>("syncComposeResourcesToAndroid") {
    val srcDir = layout.buildDirectory.dir("generated/compose/resourceGenerator/assembledResources/desktopMain/composeResources")
    from(srcDir)
    into(layout.projectDirectory.dir("src/androidMain/assets/composeResources"))
    doLast {
        copy {
            from(srcDir)
            into(layout.projectDirectory.dir("src/androidMain/resources/composeResources"))
        }
    }
}

tasks.matching { it.name.contains("DesktopMainResources", ignoreCase = true) || it.name == "copyDebugComposeResourcesToAndroidAssets" }.configureEach {
    finalizedBy(syncComposeResourcesToAndroid)
}

tasks.matching { it.name.startsWith("merge") && it.name.endsWith("Assets") }.configureEach {
    dependsOn(syncComposeResourcesToAndroid)
}



