# Pinned toolchain & dependency versions (researched Sep 2026)

Every version below was checked against official release pages. Do not bump or add versions without
updating this file.

| Item | Version | Notes / source |
|---|---|---|
| Gradle wrapper | 9.6.1 | gradle.org/releases. AGP 9.4 needs ≥ 9.6.0 |
| Android Gradle Plugin | 9.4.0 | developer.android.com/build/releases/agp-9-4-0-release-notes. JDK 17+, build-tools 36.0.0, max API 37 |
| Kotlin (KGP, compose + serialization compiler plugins) | 2.4.20 | blog.jetbrains.com (Sep 2026). AGP 9 has **built-in Kotlin**: do NOT apply `org.jetbrains.kotlin.android`. Apply `org.jetbrains.kotlin.plugin.compose` and `org.jetbrains.kotlin.plugin.serialization` at 2.4.20 |
| compileSdk / targetSdk / minSdk | 37 / 36 / 26 | Compose 1.12 + lifecycle 2.11 require compileSdk 37 (AGP ≥ 9.2) |
| Compose BOM | 2026.09.00 | developer.android.com/develop/ui/compose/bom/bom-mapping (Compose 1.12.x, Material3 from BOM) |
| androidx.activity:activity-compose | 1.13.0 | |
| androidx.lifecycle (runtime-ktx, runtime-compose, viewmodel-compose) | 2.11.0 | |
| androidx.core:core-ktx | 1.19.0 | (core-ktx is now an empty shim over core; fine) |
| androidx.datastore:datastore | 1.2.1 | typed DataStore with a kotlinx.serialization JSON `Serializer` |
| org.jetbrains.kotlinx:kotlinx-serialization-json | 1.11.0 | |
| org.jetbrains.kotlinx:kotlinx-coroutines-android | 1.11.0 | |
| androidx.profileinstaller:profileinstaller | 1.4.1 | installs Compose's bundled baseline profiles for faster startup |
| junit:junit (unit tests) | 4.13.2 | |

Deliberately **not** used (to keep the dependency surface small and the build predictable):
Hilt/Koin (manual DI instead), Room/KSP (DataStore + JSON instead), Navigation (tiny in-house back stack),
material-icons (text-first UI; a few hand-written vector drawables if ever needed), Accompanist.

Dev machine: Windows, no Android Studio. Android SDK at `C:\Users\ekram\AppData\Local\Android\Sdk`
(has build-tools 36.0.0, platforms 33 and 36.1, cmdline-tools, platform-tools). Platform 37 must be installed:
`sdkmanager "platforms;android-37"` (README explains). JDK 17+ required on PATH or JAVA_HOME.
