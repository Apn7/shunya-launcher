# Shunya (শূন্য) — zero noise

**Shunya** is a minimalist, text-first Android home screen that helps you use your phone on purpose.
No icons, no badges, no colour except the theme you pick: type is the interface. *Shunya* is Bangla
and Sanskrit for **zero**.

- **Private and offline.** Shunya has no INTERNET permission. Nothing leaves your phone.
- **Bilingual.** English and বাংলা from day one.
- **Honest friction.** Distracting apps are slowed down, never hidden behind tricks.

## Features

**Home**
- Large clock with the date; tap the clock for alarms, tap the date for the calendar. 12/24 h follows the system unless you change it.
- Optional lines: battery, next alarm, a status line (focus time left, notifications held or today's screen time) and an intention line you write yourself.
- Up to 8 text-only favorites, with custom names and left/centre/right alignment.
- Gestures you can change: swipe up for the app drawer, swipe down for notifications or search, swipe left or right to open an app, screen time or a focus toggle, double-tap to lock, long-press for the quick menu.
- Wallpaper mode with an adjustable dim, and four themes: follow system, Paper, Ink (AMOLED black) and Slate.

**App drawer and search**
- Alphabetical list that sorts Bangla names correctly, or "most used" order.
- A Niagara-style letter scroller with haptic ticks.
- Search matches on initials ("yt" finds YouTube); press Go to open the top result.
- The search box also works as a calculator (`12*(3+4)` gives `= 84`) and can hand the query to a web search or the Play Store.
- Long-press any app to rename, add to home, hide, mark as distracting, set a daily limit, open its shortcuts or app info, or uninstall it. Work-profile apps are included and tagged.

**Focus and wellbeing**
- **Mindful pause:** a short breathing screen and countdown before a distracting app opens, with today's usage for that app.
- **Daily limits** per app, with one "5 more minutes" per day.
- **Focus sessions** (25, 45, 60 or 90 minutes, or until you stop) and **schedules** such as Bedtime or Work (Sunday–Thursday). Schedules can turn on grayscale while they run.
- **Screen time:** today's total, unlocks, time per app, the last 7 days, and hourly detail for each app.
- Optional **system-wide blocking** and **lock on double-tap** through an opt-in accessibility service.

**Notifications**
- An optional filter that holds notifications from apps you haven't allowed.
- Held notifications go to a calm **Inbox** grouped by app, where you can open or dismiss them.

**Settings**
- Appearance, home, gestures, drawer, focus, notifications and language settings.
- A permissions dashboard that explains every access Shunya asks for.
- JSON backup and restore.
- A first-run onboarding you can run again from About.

## Screenshots

> _Screenshots will be added after the first build on a real phone._
>
> | Home | Drawer | Mindful pause | Screen time | Settings |
> |---|---|---|---|---|
> | _todo_ | _todo_ | _todo_ | _todo_ | _todo_ |

## Architecture in one minute

- **Single activity, Jetpack Compose.** It uses Material 3 restyled into a monochrome "Paper & Ink" design system. Navigation is a tiny in-house back stack: Back never leaves home, and Home always returns there.
- **`core/` holds the models, persistence and system services.** Persistence is typed DataStore storing kotlinx.serialization JSON. System services are app launching, permissions and intents.
- **Features only talk through interfaces in `core/contract`.** These are `LaunchPolicy`, `FocusController`, `UsageRepository`, `GrayscaleController` and `NotificationInbox`. They are wired by hand in `AppContainer`, with no DI framework.
- **Every app launch goes through `AppLauncher`.** It asks the focus policy first, so a pause, a limit or a block shows the gate screen instead of the app.
- **Business rules are plain Kotlin under `logic/`, with JVM unit tests.** This covers search ranking, the calculator, schedules, usage aggregation, launch decisions and backup mapping.

More in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md). Pinned versions are in [docs/VERSIONS.md](docs/VERSIONS.md): AGP 9.4, Kotlin 2.4.20 and Compose BOM 2026.09.

---

## Build and install on Windows (VS Code + USB phone, no Android Studio)

The commands below are for the **PowerShell** terminal that VS Code opens by default
(Terminal → New Terminal), run from the project folder `D:\codes\shunya-launcher`.

### 1. Java 17 or newer

```powershell
java -version
```

If the command is missing or the version is below 17, install OpenJDK 21, then **open a new terminal**:

```powershell
winget install Microsoft.OpenJDK.21
```

### 2. Tell Gradle where the Android SDK is

Create a file named `local.properties` in the project folder (it is git-ignored) with exactly this line:

```properties
sdk.dir=C\:\\Users\\ekram\\AppData\\Local\\Android\\Sdk
```

### 3. Install the Android 17 (API 37) platform

Your SDK already has build-tools 36.0.0, platform-tools and platform 36.1. Shunya compiles against
API 37. That platform is published under a minor-versioned name, **`platforms;android-37.0`**; a
bare `platforms;android-37` does not exist.

```powershell
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
& "$sdk\cmdline-tools\latest\bin\sdkmanager.bat" --licenses        # answer y to each license
& "$sdk\cmdline-tools\latest\bin\sdkmanager.bat" "platforms;android-37.0"
```

- If `cmdline-tools\latest` doesn't exist, run `dir "$sdk\cmdline-tools"` and use the folder you see instead of `latest`.
- Once the licenses are accepted, the Android Gradle plugin downloads any other SDK package it needs (for example a newer build-tools) during the first build.

### 4. Build

```powershell
.\gradlew.bat assembleDebug
```

The first run downloads Gradle 9.6.1 and all libraries and needs internet for about 5–15 minutes.
Later builds take well under a minute. The APK ends up in `app\build\outputs\apk\debug\app-debug.apk`.

### 5. Connect the phone and install

1. On the phone, open **Settings → About phone** and tap **Build number** seven times to unlock Developer options.
2. Open **Settings → System → Developer options** and turn on **USB debugging**.
3. Connect the USB cable and accept the "Allow USB debugging?" prompt on the phone.

```powershell
$env:Path += ";$env:LOCALAPPDATA\Android\Sdk\platform-tools"   # adb for this terminal
adb devices                                                    # your phone must show as "device"
.\gradlew.bat installDebug
```

### 6. Make Shunya your home screen

Press the phone's Home button, choose **Shunya** and then **Always**. The first-run setup inside
Shunya also offers this step. You can change it later in **Settings → Apps → Default apps → Home app**.

### 7. Optional: grayscale

Grayscale needs one permission that only a computer can grant. Grant it once:

```powershell
adb shell pm grant dev.apn7.shunya android.permission.WRITE_SECURE_SETTINGS
```

Usage access, notification access and the accessibility service are all granted on the phone, from
Shunya's **Settings → Permissions** screen.

### 8. Run the unit tests

```powershell
.\gradlew.bat testDebugUnitTest
```

The report is written to `app\build\reports\tests\testDebugUnitTest\index.html`.

### 9. Release build (optional)

```powershell
.\gradlew.bat assembleRelease
adb install -r app\build\outputs\apk\release\app-release.apk
```

The release build is minified and signed with your local debug key, so it installs over the debug
build. Use a real signing key before publishing to the Play Store.

### Editing in VS Code

Install the **Kotlin** extension by **JetBrains** (Extensions view → search "Kotlin"). It gives you
highlighting, navigation and errors once it has imported the Gradle project. Building always
happens in the terminal with `gradlew.bat`.

### Troubleshooting the first build

| Symptom | Fix |
|---|---|
| `SDK location not found` | `local.properties` is missing or the path is wrong (step 2). |
| `Failed to find target … android-37` or a license error | Run step 3 again: accept the licenses and install `platforms;android-37.0`. |
| `Unsupported class file major version`, or Gradle asks for Java 17 | `java -version` must show 17+. Open a new terminal after installing the JDK, or set `JAVA_HOME` to the JDK folder. |
| `adb devices` lists nothing or `unauthorized` | Use another USB cable or port, set USB mode to "File transfer", accept the prompt on the phone, and install the phone maker's USB driver (Samsung, Xiaomi, etc.; Pixel: `sdkmanager "extras;google;usb_driver"`). |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | An older copy was signed with another key: `adb uninstall dev.apn7.shunya`, then install again. |
| Kotlin errors (lines starting with `e:`) | See below. |

**Capturing build errors.** Save the whole log, then paste the first error block (not only the last lines):

```powershell
.\gradlew.bat assembleDebug --console=plain *> build-log.txt
notepad build-log.txt
```

Copy from the first line starting with `e:` (Kotlin: it contains `file.kt:line:column`) or from `* What went wrong:` down to `BUILD FAILED`.
For Gradle or plugin errors, run again with `--stacktrace` added and include the `Caused by:` lines.
Also mention the command you ran and the output of `java -version`.

---

## Permissions

| Permission | Why | Needed for |
|---|---|---|
| Default home app | Shunya is a launcher | everything |
| Usage access | read screen time (on the phone only) | screen time, daily limits, "most used" sort |
| Notification access | hold notifications in the Inbox | notification filter |
| Accessibility service (opt-in) | see which app is in front; lock the screen | system-wide blocking, double-tap to lock |
| `WRITE_SECURE_SETTINGS` (ADB) | switch colour correction | grayscale |
| `EXPAND_STATUS_BAR`, `REQUEST_DELETE_PACKAGES`, `VIBRATE`, `SET_ALARM` | small system actions | swipe down, uninstall, haptics, clock tap |

Every feature degrades gracefully when a permission is missing.

## Project documents

- [docs/PRD.md](docs/PRD.md): what Shunya is supposed to do.
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): how it is built, and why.
- [docs/VERSIONS.md](docs/VERSIONS.md): toolchain and dependency versions.

## Licenses

Fonts are Inter, Space Grotesk, IBM Plex Mono, Lora and Hind Siliguri, all under the
SIL Open Font License 1.1 ([docs/licenses/](docs/licenses/)).
