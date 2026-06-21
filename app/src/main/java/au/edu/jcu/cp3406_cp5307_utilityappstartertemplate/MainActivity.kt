package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.ui.theme.CP3406_CP5603UtilityAppStarterTemplateTheme

class MainActivity : ComponentActivity() {
    // ponytail: Activity viewModels delegate avoids adding compose-viewmodel artifact to dependencies.
    private val viewModel: ForestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            val isDark = when (settings.themeMode) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }
            
            // Dynamic theme wrapper based on user configurations
            CP3406_CP5603UtilityAppStarterTemplateTheme(
                darkTheme = isDark,
                accentColorName = settings.accentColor
            ) {
                UtilityApp(viewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Save current active session or timer state when user backgrounds the app
        viewModel.savePersistedState()
    }
}

@Composable
fun UtilityApp(viewModel: ForestViewModel) {
    var selectedTab by remember { mutableStateOf("Focus") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Focus") },
                    label = { Text("Focus") },
                    selected = selectedTab == "Focus",
                    onClick = { selectedTab = "Focus" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Garden") },
                    label = { Text("Garden") },
                    selected = selectedTab == "Garden",
                    onClick = { selectedTab = "Garden" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedTab == "Settings",
                    onClick = { selectedTab = "Settings" }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                "Focus" -> ForestTimerScreen(viewModel = viewModel)
                "Garden" -> GardenScreen(viewModel = viewModel)
                "Settings" -> SettingsFormScreen(viewModel = viewModel)
            }
        }
    }
}