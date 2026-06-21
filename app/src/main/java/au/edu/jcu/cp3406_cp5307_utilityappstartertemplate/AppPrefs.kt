package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import android.content.Context
import android.content.SharedPreferences

// ponytail: Using native SharedPreferences with custom string serialization. No Room/Gson overhead.

class AppPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ForestPrefs", Context.MODE_PRIVATE)

    // Settings Keys
    private val KEY_DISPLAY_NAME = "pref_display_name"
    private val KEY_THEME_MODE = "pref_theme_mode"
    private val KEY_ACCENT_COLOR = "pref_accent_color"
    private val KEY_FOCUS_DURATION = "pref_focus_duration"
    private val KEY_SHORT_BREAK = "pref_short_break"
    private val KEY_LONG_BREAK = "pref_long_break"
    private val KEY_AUTO_START_BREAK = "pref_auto_break"
    private val KEY_AUTO_START_FOCUS = "pref_auto_focus"
    private val KEY_ENABLE_SOUNDS = "pref_sounds"
    private val KEY_ENABLE_VIBRATION = "pref_vibrate"
    private val KEY_DAILY_FOCUS_GOAL = "pref_daily_goal"
    private val KEY_SPECIES_MODE = "pref_species_mode"
    private val KEY_SELECTED_SPECIES = "pref_selected_species"

    // State Keys
    private val KEY_GARDEN = "state_garden"
    private val KEY_COMPLETED_SESSIONS = "state_completed_sessions"
    private val KEY_CYCLE_COMPLETED_SESSIONS = "state_cycle_completed_sessions"
    private val KEY_CYCLE_TOTAL_FOCUS_MINUTES = "state_cycle_focus_minutes"
    private val KEY_ACTIVE_TREE_SPECIES = "state_active_tree_species"
    private val KEY_POMODORO_CYCLES_COUNT = "state_pomodoro_cycles_count"

    // Timer Keys
    private val KEY_TIMER_SECONDS_REMAINING = "timer_seconds_remaining"
    private val KEY_TIMER_TARGET_MILLIS = "timer_target_millis"
    private val KEY_TIMER_SESSION_TYPE = "timer_session_type"
    private val KEY_TIMER_IS_PAUSED = "timer_is_paused"


