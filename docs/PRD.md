# Shunya (শূন্য) — Product Requirements

> *Shunya* is Bangla/Sanskrit for **zero** or **emptiness**. Tagline: **"Zero noise."**
> A minimalist, text-first Android home screen that helps people use their phone on purpose.
> Inspired by minimalist phone, Niagara and Olauncher. It is not a copy: it has its own name, look and code.

Author: Ekramul Alam (Apn7).
Target: personal daily driver first, Play Store later (free core + paid "Focus" tier later; no billing in v1).

## 1. Product principles
1. **Calm by default.** No icons, no badges, no color except what the user chooses. Type is the interface.
2. **Instant.** Pressing Home must feel immediate. Nothing on the home path waits on I/O.
3. **Honest friction.** We slow down distracting apps; we never trick or shame the user.
4. **Private and offline.** No INTERNET permission. No analytics. Everything stays on the device.
5. **Respect the platform.** Back never exits home; Home returns home; edge-to-edge; font scaling; TalkBack labels.
6. **Bilingual.** English + Bangla (বাংলা) UI from day one.

Priorities: **P0** = must ship in v1, **P1** = should ship in v1, **P2** = only if time allows.

## 2. Identity & design language ("Paper & Ink")
- App name: `Shunya`. Launcher label: `Shunya`. applicationId / namespace: `dev.apn7.shunya`.
- Monochrome, typographic, generous whitespace, left-aligned by default.
- Themes: **Follow system** (default) · **Paper** (warm off-white `#F3EFE6`, ink `#1B1B1B`) · **Ink** (pure black `#000000`, text `#EDEDED`, AMOLED) · **Slate** (`#15171A`, text `#E6E6E6`).
  Secondary text = primary at ~60% alpha; dividers ~12%. No accent color; emphasis via weight/opacity.
- Fonts bundled in `res/font` (OFL): Inter, Space Grotesk, IBM Plex Mono, Lora, Hind Siliguri (Bangla-capable), plus "System". Each has light/regular/medium (Lora: regular/medium).
  When the UI language is Bangla, text should render with Hind Siliguri if the chosen font lacks Bangla glyphs (fallback family).
- Motion: short (150–250 ms) fades/slides; no bouncy springs. Haptic tick on long-press and fast-scroller letters.
- Launcher icon: adaptive icon — a thin ring (zero) on ink background, monochrome layer for themed icons.

## 3. Features

### 3.1 Home screen (P0)
- Big clock (light weight), date line below (e.g. "Tuesday, 22 September"). 12/24h follows system unless overridden.
  - Tap clock → system alarms/clock app (`AlarmClock.ACTION_SHOW_ALARMS`). Tap date → calendar app (calendar content URI).
- Optional lines (each toggleable): battery % (+ "charging"), next alarm, **status line**, **intention line**.
  - Status line shows the most relevant one of: "Focus · 23m left" / "4 notifications held" / "Screen time today 1h 12m".
  - Intention line: user-written one-liner ("Today: finish thesis slides"), tap to edit.
- **Favorites**: 0–8 apps (default max 6), text only, custom labels, vertical list, alignment L/C/R, sizes S/M/L/XL.
- **Gestures** (each configurable in Settings → Gestures):
  - Swipe up → App drawer (default, not changeable to none)
  - Swipe down → Notification shade (default) | Search | Nothing
  - Swipe left / right → Open a chosen app | Screen time | Focus toggle | Nothing (defaults: right = Nothing, left = Screen time)
  - Double-tap → Lock screen (needs accessibility service; if off, show a one-time hint with a button to enable)
  - Long-press empty space → Quick menu sheet: Settings, Focus now, Wallpaper mode on/off, Grayscale on/off, Edit home
- Wallpaper mode (P1): solid theme background (default) or show system wallpaper with a dim scrim (0–80%).
- Pressing Home while on home: closes any sheet/drawer/search and scrolls to top. Back never leaves home.

### 3.2 App drawer & search (P0)
- Full-screen, text-only, alphabetical list sorted with a locale-aware `Collator` (Bangla names sort correctly).
  Sort option: A–Z (default) or Most used (needs usage access; falls back to A–Z).
- Search field at the top; keyboard opens automatically (setting, default on). Clear button. IME action "Go" launches the top result.
- **Fuzzy matching** ranking: exact > prefix > word-initials ("yt" → YouTube, "gm" → Google Maps) > word-prefix > substring > subsequence. Case/diacritic-insensitive. Pure Kotlin, unit tested.
- Auto-launch when exactly one match (setting, default off).
- **Search extras** (P1): inline calculator (`12*(3+4)` → "= 84", supports + − × ÷ ^ %, parentheses, decimals; pure Kotlin, unit tested) — tap copies result; "Search the web for “…”" (`Intent.ACTION_WEB_SEARCH`); "Search Play Store for “…”" (`market://search?q=`).
- **Alphabet fast-scroller** on the right edge (Niagara-style "wave"): letters near the finger magnify and shift left, list jumps to section, haptic tick per letter. Only letters that exist are shown; "#" for digits/others.
- Section letter headers (setting, default off).
- Work-profile apps included, with a small "work" tag (setting to hide).
- Long-press an app → action sheet:
  Rename · Add to/Remove from home · Hide · Mark as distracting (mindful pause) · Daily limit… · App shortcuts (P1, via `LauncherApps.getShortcuts`, only when we are the default launcher) · App info · Uninstall (`ACTION_DELETE`, not for system apps) · "Used 23m today · opened 5×" (when usage access granted).
- Hidden apps are excluded from drawer and search; managed in Settings → Hidden apps (unhide, open).

### 3.3 Focus & wellbeing (P0 unless noted)
- **Mindful pause**: apps marked *distracting* show the Gate screen when launched from Shunya: slow breathing circle animation, countdown (3–30 s, default 10), "Open Instagram?" copy, today's usage ("47m today · opened 12×"), buttons **Not now** (go home) and **Open** (enabled after countdown). Per-app pause length optional (defaults to global).
- **Daily limits**: per-app minutes. If today's usage ≥ limit when launching: Gate screen in *limit* mode ("You've reached your 30 min for YouTube") with **Close** and **5 more minutes** (at most one extension per app per day, stored).
- **Focus sessions**: "Focus now" for 25 / 45 / 60 / 90 min or "until I stop". While active, distracting apps are **blocked** (Gate in *blocked* mode with time left and **End focus** requiring a 10 s hold). Home status line shows remaining time.
- **Schedules** (P1): named recurring windows (e.g. "Bedtime 23:00–07:00 every day", "Work 09:00–17:00 Sun–Thu" — Bangladesh work week as the default example). Overnight windows supported. While active they behave like a focus session. Optional "grayscale during this schedule". Evaluated on demand (no background alarms needed).
- **Screen time** screen: today's total, unlock count (API 28+), per-app list with proportional bars (time + opens), last 7 days bar chart (Canvas), tap app → detail with hourly bars (P2). Needs *Usage access*; shows a friendly permission card otherwise.
- **Grayscale** (P1): toggle via secure settings (`accessibility_display_daltonizer_enabled=1`, `accessibility_display_daltonizer=0`). Requires `WRITE_SECURE_SETTINGS` granted once over ADB; Settings shows the exact command with a copy button and detects whether it's granted.
- **Accessibility service** (P1, opt-in, prominent disclosure first):
  1. performs Lock screen for double-tap (`GLOBAL_ACTION_LOCK_SCREEN`, API 28+);
  2. **system-wide blocking**: when a blocked app comes to the foreground (focus/schedule/limit) show the Gate screen, even if opened from notifications/recents.
  Without the service, blocking applies only to launches from Shunya (clearly stated in UI).

### 3.4 Notifications (P1)
- **Notification filter** (NotificationListenerService, opt-in with disclosure): modes *Off* / *Hold*. In Hold mode, notifications from apps not on the **allowed list** are removed from the shade and saved to the **Inbox**. Default allowed: dialer, SMS/messages, clock, calendar, Shunya itself. Ongoing/foreground-service and group-summary notifications are never held.
- **Inbox** screen: grouped by app, newest first, tap opens (original PendingIntent while the service process lives, else the app), swipe or button to dismiss, "Clear all". Max 300 items, oldest dropped.
- Home status line shows the held count.

### 3.5 Settings (P0)
Sections: Appearance · Home · Gestures · App drawer · Focus & wellbeing · Notifications · Permissions · Backup & restore · Language · About.
- Appearance: theme, font, text size (S/M/L/XL), home alignment, clock style (large/medium, show seconds off), show date / battery / next alarm / status line, show status bar, wallpaper mode + dim.
- Home: edit favorites (add/remove/reorder/rename), max favorites, intention line on/off.
- Gestures: map each gesture (see 3.1).
- App drawer: sort, section letters, auto keyboard, auto-launch single match, show work apps, hidden apps list.
- Focus & wellbeing: distracting apps, default pause length, daily limits, focus durations, schedules, blocking mode (launcher-only vs system-wide), grayscale.
- Notifications: filter mode, allowed apps, open inbox.
- **Permissions dashboard**: Default launcher, Usage access, Notification access, Accessibility, Secure settings (grayscale) — each shows granted/not granted + action button + one-line why.
- Backup & restore (P1): export/import one JSON file via Storage Access Framework (settings, overrides, focus rules, schedules; not the inbox).
- Language: Follow system / English / বাংলা (per-app language, `LocaleManager` on 33+, `AppCompatDelegate` not used; API < 33 falls back to follow system with a note). `locales_config.xml` declared.
- About: version, the meaning of শূন্য, privacy statement ("No internet permission. Nothing leaves your phone."), open-source font licenses (OFL).

### 3.6 Onboarding (P0)
First launch only (and re-runnable from About): Welcome → Theme & font (live preview) → Pick favorites (multi-select, up to max) → Set as default launcher (`RoleManager.ROLE_HOME` on 29+, `Settings.ACTION_HOME_SETTINGS` fallback) → Optional permissions (usage access, notifications) with honest explanations → Done. Skippable steps. Never blocks the home screen.

## 4. Non-functional requirements (P0)
- Cold start to first frame fast: app list cached to disk and shown immediately; refreshed in background. No app icons are ever loaded.
- Live updates on install/uninstall/update/profile changes (`LauncherApps.Callback`).
- No crash when any permission is missing or revoked; every feature degrades gracefully.
- minSdk 26, targetSdk 36, compileSdk 37. Kotlin only. Jetpack Compose + Material 3 (restyled to our tokens).
- Edge-to-edge with proper insets (status, navigation, IME).
- Accessibility: all actions reachable without gestures (drawer via a subtle "apps" text button option; settings via long-press menu), content descriptions, font scale respected.
- Unit tests (JUnit 4, pure JVM) for: search ranking, calculator, schedule evaluation, usage aggregation, limit/pause decision logic, backup JSON round-trip model mapping.
- No INTERNET permission. Minimal permissions, each justified in the Permissions screen.

## 5. Out of scope (for now)
Widgets, icon packs, weather, cloud sync, billing/paywall, short-video blocking inside other apps, tablets/foldables optimisation, Android TV.

## 6. Definition of done (v1)
All P0 + as many P1 as possible implemented with clean architecture; unit tests pass; README explains how to build/install on Windows with VS Code + USB phone; code is in `D:\codes\shunya-launcher` with git history.
