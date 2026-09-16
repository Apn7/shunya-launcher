// Top-level build file. Plugins are declared here once (not applied) so every module
// resolves the same versions from gradle/libs.versions.toml.
// AGP 9 compiles Kotlin itself (built-in Kotlin): org.jetbrains.kotlin.android is intentionally absent.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
