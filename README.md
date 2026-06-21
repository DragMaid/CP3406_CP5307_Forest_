# Forest Timer

Forest Timer is an Android focus/productivity app built for **CP3406 / CP5307**. It turns the
[Pomodoro technique](https://en.wikipedia.org/wiki/Pomodoro_Technique) into a gamified gardening
experience: every completed focus session grows a tree, and every four-session cycle harvests a
mature tree into your personal garden. The app is written entirely in **Kotlin** with **Jetpack
Compose** and **Material 3**, and all tree/weather artwork is drawn procedurally on a `Canvas`
(no image assets).

---

## Getting Started

1. Clone this repo and open it in **Android Studio**.
2. Let Gradle sync (the build uses the version catalog in `gradle/libs.versions.toml`).
3. Run on an emulator or device. `minSdk = 24`, `targetSdk = 36`.
4. (Optional) For real-time weather, grant location permission when prompted — see
   [Weather Integration](#5-weather-integration-real-time--manual).

---

## Architecture at a Glance

The app follows a lightweight **MVVM** structure with a single source of truth.

```
MainActivity ──hosts──► UtilityApp (Scaffold + bottom nav)
                          │
        ┌─────────────────┼─────────────────┐
   ForestTimerScreen   GardenScreen    SettingsFormScreen   (Composables — ForestScreens.kt)
        │                 │                 │
        └──────── observe StateFlow ────────┘
                          │
                   ForestViewModel  (all state, timer logic, persistence orchestration)
                     │          │           │
                 AppPrefs   WeatherRepository   LocationRepository
              (SharedPrefs)  (Retrofit/Open-Meteo) (FusedLocation)
```

- **`ForestViewModel`** (`AndroidViewModel`) owns every piece of state as a `MutableStateFlow` and
  exposes read-only `StateFlow`s. The UI is fully reactive via `collectAsState()`.
- **`AppPrefs`** wraps `SharedPreferences` and handles all persistence with hand-rolled string
  serialization (no Room/Gson for local state).
- **Repositories** isolate the two external concerns: weather (network) and location (Play
  Services). The ViewModel never touches Retrofit or the location client directly.

### Key Files

| File | Responsibility |
|------|----------------|
| `MainActivity.kt` | Entry point, theme wrapper, bottom-nav scaffold, save-on-stop |
| `ForestViewModel.kt` | Timer engine, Pomodoro cycle logic, stats, persistence, weather polling |
| `ForestScreens.kt` | The three screens: Focus, Garden, Settings |
| `TreePainter.kt` | Procedural `Canvas` rendering of trees + weather effects |
| `Models.kt` | Enums and data classes (`TreeSpecies`, `TreeStage`, `SessionType`, `AppSettings`, `GardenTree`, `WeatherCondition`) |
| `AppPrefs.kt` | `SharedPreferences` persistence + serialization |
| `ui/theme/Theme.kt` | Dynamic light/dark + accent-color theming |
| `network/`, `data/weather/`, `data/location/` | Open-Meteo API client and FusedLocation wrapper |
| `extensions/WeatherMapping.kt` | Maps WMO weather codes → `WeatherCondition` |

---

## Core Features & Implementation

### 1. Pomodoro Focus Timer

A countdown timer that cycles through **Focus → Short Break → Focus → … → Long Break** sessions.

- **State** lives in `ForestViewModel`: `secondsRemaining`, `isTimerRunning`, `isTimerPaused`,
  and `sessionType` (`FOCUS`, `SHORT_BREAK`, `LONG_BREAK`).
- **The tick loop** is a coroutine launched in `viewModelScope` that `delay(1000ms)` and decrements
  `secondsRemaining` until it hits zero, then calls `onSessionComplete()` (`startTimer()` in
  `ForestViewModel.kt`).
- **Controls** — Start, Pause/Resume, Skip, and Cancel — are wired in `ForestTimerScreen`.
  `cancelSession()` deliberately does **not** advance the tree, while `skipCurrentSession()` jumps
  straight to completion.
- **Durations** are user-configurable (focus, short break, long break) and validated in Settings
  (focus must be ≥ 25 min to be eligible for tree growth).
- **Automation**: optional `autoStartBreak` / `autoStartFocus` toggles chain sessions together
  without manual taps.
- **Completion effects**: on finish, `triggerCompletionEffects()` plays the default notification
  ringtone and fires a one-shot vibration (each gated by a settings toggle).

### 2. Tree Growth & The 4-Session Cycle

The gamification core: focus work literally grows a tree through five stages.

- A Pomodoro **cycle = 4 focus sessions**, tracked by `completedSessionsInCycle`.
- Each completed focus session maps to a `TreeStage` via `TreeStage.fromInt()`:
  `SEED → SPROUT → YOUNG_TREE → NEARLY_MATURE → MATURE`.
- When the 4th focus session completes (`onSessionComplete()` in `ForestViewModel.kt`), the active
  tree is harvested into the garden as a `GardenTree` (recording species, completion date, total
  focus minutes, and a running cycle index), the cycle counter resets, a new seed is planted, and a
  **Long Break** is triggered. Otherwise a **Short Break** follows.
- **Species selection** for each new seed is driven by `speciesMode` (`plantNewSeed()`):
  - `Random` — any of the five species.
  - `Manual` — the user's chosen species.
  - `Seasonal` — weighted by the current month using **Southern Hemisphere** seasons
    (`selectSeasonalSpecies()`), e.g. Cherry Blossom is most likely in spring, Pine in winter.

### 3. Procedural Tree & Weather Rendering

All visuals are drawn with Compose `Canvas` — there are **no PNG/vector tree assets**.

- **`TreeCanvas`** (`TreePainter.kt`) draws the trunk, branches, and canopy differently for each of
  the five `TreeSpecies` and each `TreeStage`, plus a sky layer, ground/soil, and the empty-plot
  daisy.
- **Living animation**: `rememberInfiniteTransition` drives a gentle trunk **sway**; the speed and
  amplitude react to the current weather (calm when sunny, fast/wide in Wind and Storm).
- **Weather overlays** are rendered on the same canvas — a rayed sun for Sunny, drifting clouds for
  Cloudy/Storm, animated falling rain, and keyframed lightning flashes for Storm.

### 4. Productivity Garden & Statistics

The "Garden" tab is a trophy case plus an analytics dashboard.

- **`GardenScreen`** renders harvested trees in a `LazyVerticalGrid` of plots (padded to a minimum
  grid size). Tapping a tree opens a detail `Dialog` (species, date, minutes invested, cycle #);
  tapping an empty plot explains how to earn one.
- **Stats** are computed on the fly in `ForestViewModel`:
  - `totalMatureTreesCount` — garden size.
  - `totalFocusHours` — sum of all focus minutes (garden + in-progress cycle).
  - `currentFocusStreak` — consecutive-day streak from completed-session dates
    (`calculateStreak()`).
  - `averageSessionsPerDay` — sessions ÷ days since first session (`calculateAverageSessionsPerDay()`).
- **Daily goal**: the Focus screen shows a `LinearProgressIndicator` tracking today's focus minutes
  against the configurable `dailyFocusGoalMinutes`.

### 5. Weather Integration (Real-time & Manual)

Weather drives the on-screen ambiance and can be set manually or pulled live.

- **Manual mode**: the user picks a `WeatherCondition` in Settings and it's applied immediately.
- **Real-time mode**: `ForestViewModel` starts a polling coroutine that refreshes weather every
  **5 minutes** (`startWeatherPolling()`).
  1. `LocationRepository` gets the current location via Play Services **FusedLocationProvider**.
  2. `WeatherRepository` calls the **Open-Meteo** `v1/forecast` endpoint through **Retrofit**
     (`OpenMeteoApi`), with a 5-minute in-memory cache guarded by a `Mutex`.
  3. The WMO `weathercode` is mapped to one of the app's five conditions by
     `mapWeatherCodeToCondition()`.
- **Permissions**: switching to real-time triggers a runtime request for fine/coarse location
  (`SettingsFormScreen`); the app degrades gracefully (keeps the last condition) if denied.
- Networking is configured in `NetworkModule` (OkHttp logging interceptor, 15s timeouts, Gson
  converter).

### 6. Settings & Customization

`SettingsFormScreen` is a single scrollable form whose changes apply immediately (no Save button).

- **Profile**: display name (shown as "Welcome back, …").
- **Appearance**: theme mode (System/Light/Dark) and one of six accent colors. `MainActivity`
  observes settings and re-wraps the UI in `CP3406_CP5603...Theme` with the chosen
  `darkTheme`/`accentColorName` (`Theme.kt`).
- **Timers**: focus / short break / long break durations with inline validation
  (focus ≥ 25, short break < long break).
- **Automation, sounds, vibration, daily goal, species mode, and weather mode** as described above.
- Valid edits are pushed through `applySettingsIfValid()` → `viewModel.updateSettings()` →
  `AppPrefs.saveSettings()`.

### 7. Persistence & Background Resume

State survives app restarts and timers keep running across backgrounding.

- **`AppPrefs`** persists settings, the garden (`species|date|minutes|cycle` rows joined by `;`),
  completed-session dates, cycle progress, the active species, and the cycle counter — all in
  `SharedPreferences` with manual string serialization.
- **Timer resume**: when the app is backgrounded, `MainActivity.onStop()` saves a `SavedTimerState`
  (remaining seconds, an absolute target timestamp, session type, paused flag). On next launch
  `restorePersistedTimer()` either:
  - restores a paused timer, or
  - recomputes remaining time from the target timestamp and resumes — and if the target already
    passed, it completes the session as though it finished in the background.

---

## Tech Stack

- **Language**: Kotlin (2.0.21)
- **UI**: Jetpack Compose + Material 3 (Compose BOM)
- **Async**: Kotlin Coroutines + `StateFlow`
- **Architecture**: MVVM (`AndroidViewModel`)
- **Networking**: Retrofit + OkHttp + Gson — [Open-Meteo](https://open-meteo.com/) API
- **Location**: Google Play Services FusedLocationProvider
- **Persistence**: Android `SharedPreferences`
- **Graphics**: Compose `Canvas` (fully procedural, no image assets)
- **Build**: Gradle (Kotlin DSL) with a version catalog

---

## Permissions

| Permission | Why |
|------------|-----|
| `INTERNET` | Fetch live weather from Open-Meteo |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Locate the device for real-time weather |
| `VIBRATE` | Haptic feedback on session completion |

---

## License

Provided for educational use in CP3406 / CP5307. Feel free to modify and extend.
