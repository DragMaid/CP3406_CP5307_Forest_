package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.media.RingtoneManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

// ponytail: Unified AndroidViewModel manages all state transitions, persistence, and sound/vibe.

class ForestViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = AppPrefs(application)

    // App Settings
    private val _settings = MutableStateFlow(prefs.loadSettings())
    val settings = _settings.asStateFlow()

    // Timer State
    private val _sessionType = MutableStateFlow(SessionType.FOCUS)
    val sessionType = _sessionType.asStateFlow()

    private val _secondsRemaining = MutableStateFlow(25L * 60)
    val secondsRemaining = _secondsRemaining.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning = _isTimerRunning.asStateFlow()

    private val _isTimerPaused = MutableStateFlow(false)
    val isTimerPaused = _isTimerPaused.asStateFlow()

    // Cycle & Tree State
    private val _completedSessionsInCycle = MutableStateFlow(prefs.getCycleCompletedSessions())
    val completedSessionsInCycle = _completedSessionsInCycle.asStateFlow()

    private val _activeTreeSpecies = MutableStateFlow(prefs.getActiveTreeSpecies() ?: TreeSpecies.OAK)
    val activeTreeSpecies = _activeTreeSpecies.asStateFlow()

    private val _totalFocusTimeInCurrentCycle = MutableStateFlow(prefs.getCycleTotalFocusMinutes())

    private val _pomodoroCyclesCount = MutableStateFlow(prefs.getPomodoroCyclesCount())

    // Garden & History
    private val _garden = MutableStateFlow(prefs.loadGarden())
    val garden = _garden.asStateFlow()

    private val _completedSessionDates = MutableStateFlow(prefs.loadCompletedSessions())
    val completedSessionDates = _completedSessionDates.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Automatically set up active tree species if it hasn't been set yet
        if (prefs.getActiveTreeSpecies() == null) {
            plantNewSeed()
        }
        
        // Restore active timer if it was running when app was closed
        restorePersistedTimer()
        
        // Sync default timer duration
        if (!_isTimerRunning.value && !_isTimerPaused.value) {
            resetTimerToCurrentSession()
        }
    }
