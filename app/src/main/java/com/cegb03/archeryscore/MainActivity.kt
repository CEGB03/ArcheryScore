package com.cegb03.archeryscore

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RecentActors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tour
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.cegb03.archeryscore.ui.theme.ArcheryScoreTheme
import com.cegb03.archeryscore.ui.theme.screens.access.AccessScreen
import com.cegb03.archeryscore.ui.theme.screens.login.LoginScreen
import com.cegb03.archeryscore.ui.theme.screens.register.RegisterScreen
import com.cegb03.archeryscore.ui.theme.screens.tournaments.TournamentsScreen
import com.cegb03.archeryscore.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("ArcheryScore_Debug", "📱 MainActivity.onCreate() - INICIANDO")
        try {
            super.onCreate(savedInstanceState)
            Log.d("ArcheryScore_Debug", "✅ MainActivity - super.onCreate() completado")
            
            enableEdgeToEdge()
            Log.d("ArcheryScore_Debug", "✅ MainActivity - enableEdgeToEdge() completado")
            
            setContent {
                Log.d("ArcheryScore_Debug", "🎨 MainActivity - setContent iniciando composición")
                ArcheryScoreTheme {
                    ArcheryScoreApp()
                }
            }
            Log.d("ArcheryScore_Debug", "✅ MainActivity.onCreate() - COMPLETADO")
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "❌ ERROR en MainActivity.onCreate(): ${e.message}", e)
            throw e
        }
    }
}

@PreviewScreenSizes
@Composable
fun ArcheryScoreApp(authViewModel: AuthViewModel = hiltViewModel()) {
    Log.d("ArcheryScore_Debug", "🎯 ArcheryScoreApp - Composable iniciado")
    // Mostrar navegación principal directamente
    MainAppContent()
    Log.d("ArcheryScore_Debug", "✅ ArcheryScoreApp - MainAppContent renderizado")
}

@Composable
fun MainAppContent() {
    Log.d("ArcheryScore_Debug", "📋 MainAppContent - Composable iniciado")
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.INICIO) }
    var profileSubScreen by rememberSaveable { mutableStateOf<ProfileScreen>(ProfileScreen.LOGIN) }
    val navController = rememberNavController()
    Log.d("ArcheryScore_Debug", "✅ MainAppContent - Estados inicializados")

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppDestinations.entries.forEach { destination ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) },
                        selected = destination == currentDestination,
                        onClick = {
                            Log.d("ArcheryScore_Debug", "🔀 Navegación - Destino seleccionado: ${destination.label}")
                            currentDestination = destination
                            if (destination == AppDestinations.PERFIL) {
                                profileSubScreen = ProfileScreen.LOGIN
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentDestination) {
                AppDestinations.INICIO -> {
                    Greeting(
                        name = "Android",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
                AppDestinations.REGISTROS -> {
                    Text(
                        text = "Registro de entrenamientos",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
                AppDestinations.TORNEOS -> {
                    Log.d("ArcheryScore_Debug", "🎯 Navegación - Entrando en ruta TORNEOS")
                    TournamentsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                    Log.d("ArcheryScore_Debug", "✅ Navegación - Scaffold TORNEOS renderizado")
                }
                AppDestinations.PERFIL -> {
                    when (profileSubScreen) {
                        ProfileScreen.LOGIN -> {
                            LoginScreen(
                                navController = navController,
                                onBackPressed = { currentDestination = AppDestinations.INICIO },
                                onRegisterPressed = { profileSubScreen = ProfileScreen.REGISTER }
                            )
                        }
                        ProfileScreen.REGISTER -> {
                            RegisterScreen(
                                navController = navController,
                                onBackPressed = { profileSubScreen = ProfileScreen.LOGIN }
                            )
                        }
                    }
                }
            }
        }
    Log.d("ArcheryScore_Debug", "✅ MainAppContent - Scaffold con NavigationBar renderizado")
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    INICIO("Inicio", Icons.Default.Home),
    REGISTROS("Registros", Icons.Default.Event),
    TORNEOS("Torneos", Icons.Default.EmojiEvents),
    PERFIL("Perfil", Icons.Default.AccountBox),
}

enum class ProfileScreen {
    LOGIN,
    REGISTER,
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ArcheryScoreTheme {
        Greeting("Android")
    }
}