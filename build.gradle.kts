plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.detekt) apply false
}

tasks.register("detekt") {
    group = "verification"
    description = "Runs detekt analysis across all subprojects"
    dependsOn(":composeApp:detekt")
}

tasks.register("detektAll") {
    group = "verification"
    description = "Runs detekt analysis across all subprojects (alias for detekt)"
    dependsOn(":composeApp:detekt")
}

