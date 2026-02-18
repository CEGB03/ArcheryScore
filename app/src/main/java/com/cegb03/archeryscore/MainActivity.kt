package com.cegb03.archeryscore

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.BackHandler
import com.cegb03.archeryscore.ui.theme.ArcheryScoreTheme
import com.cegb03.archeryscore.ui.theme.screens.access.AccessScreen
import com.cegb03.archeryscore.ui.theme.screens.login.LoginScreen
import com.cegb03.archeryscore.ui.theme.screens.register.RegisterScreen
import com.cegb03.archeryscore.ui.theme.screens.trainings.TrainingsScreen
import com.cegb03.archeryscore.ui.theme.screens.tournaments.TournamentsScreen
import com.cegb03.archeryscore.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

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
    var backPressedCount by rememberSaveable { mutableStateOf(0) }
    var hasDetailOpen by rememberSaveable { mutableStateOf(false) } // Detecta si hay sub-pantalla abierta
    val navController = rememberNavController()
    val context = LocalContext.current
    Log.d("ArcheryScore_Debug", "✅ MainAppContent - Estados inicializados")

    // Manejar el back button del sistema - solo activo cuando NO hay detalles abiertos
    BackHandler(enabled = !hasDetailOpen) {
        when {
            // Si estamos en INICIO, contar clicks para cerrar la app
            currentDestination == AppDestinations.INICIO -> {
                backPressedCount++
                Log.d("ArcheryScore_Debug", "👈 Back button presionado en INICIO - Click $backPressedCount/2")
                
                if (backPressedCount == 1) {
                    Toast.makeText(
                        context,
                        "Presiona nuevamente para salir de la aplicación",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (backPressedCount >= 2) {
                    Log.d("ArcheryScore_Debug", "❌ Cerrando aplicación - Usuario presionó back 2 veces en INICIO")
                    exitProcess(0) // Cerrar la aplicación
                }
            }
            // Si estamos en una sub-pantalla de PERFIL, volver a LOGIN
            currentDestination == AppDestinations.PERFIL && profileSubScreen == ProfileScreen.REGISTER -> {
                profileSubScreen = ProfileScreen.LOGIN
                backPressedCount = 0
                Log.d("ArcheryScore_Debug", "👈 Volviendo a LOGIN desde REGISTER")
            }
            // Si estamos en cualquier otra pantalla, volver a INICIO
            else -> {
                currentDestination = AppDestinations.INICIO
                backPressedCount = 0
                Log.d("ArcheryScore_Debug", "👈 Volviendo a INICIO desde ${currentDestination.label}")
            }
        }
    }

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
                            backPressedCount = 0 // Resetear contador de back button
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
                    TrainingsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        onDetailOpenChanged = { isOpen ->
                            hasDetailOpen = isOpen
                            Log.d("ArcheryScore_Debug", "📋 TrainingsScreen detalle abierto: $isOpen")
                        }
                    )
                }
                AppDestinations.TORNEOS -> {
                    Log.d("ArcheryScore_Debug", "🎯 Navegación - Entrando en ruta TORNEOS")
                    TournamentsScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        onDetailOpenChanged = { isOpen ->
                            hasDetailOpen = isOpen
                            Log.d("ArcheryScore_Debug", "🎯 TournamentsScreen detalle abierto: $isOpen")
                        }
                    )
                    Log.d("ArcheryScore_Debug", "✅ Navegación - Scaffold TORNEOS renderizado")
                }
                AppDestinations.PERFIL -> {
                    when (profileSubScreen) {
                        ProfileScreen.LOGIN -> {
                            LoginScreen(
                                navController = navController,
                                onBackPressed = { 
                                    currentDestination = AppDestinations.INICIO
                                    backPressedCount = 0
                                },
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
    REGISTROS("Entrenamientos", Icons.Default.Event),
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