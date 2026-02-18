package com.cegb03.archeryscore.ui.theme.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.cegb03.archeryscore.R
import com.cegb03.archeryscore.ui.theme.Screen
import com.cegb03.archeryscore.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import android.util.Log
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.cegb03.archeryscore.util.BiometricAuth
import com.cegb03.archeryscore.viewmodel.SettingsViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isInitialized by authViewModel.isInitialized.collectAsState()
    val biometricEnabled by settingsViewModel.biometricEnabled.collectAsState(initial = false)
    var hasCheckedAuth by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }
    var biometricPassed by remember { mutableStateOf(false) }
    var biometricAttempted by remember { mutableStateOf(false) }
    var lastBiometricError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // ✅ INICIAR verificación de autenticación solo una vez
    LaunchedEffect(Unit) {
        if (!hasCheckedAuth) {
            Log.d("ArcheryScore_Debug", "🔄 SplashScreen: Iniciando checkAuthStatus...")
            authViewModel.checkAuthStatus()
            hasCheckedAuth = true
        }
    }

    // ✅ Mostrar biometría cuando sea necesario (separado de navegación)
    LaunchedEffect(isInitialized, isLoggedIn, biometricEnabled) {
        Log.d("ArcheryScore_Debug", "🎯 SplashScreen: Estado - isInitialized=$isInitialized, isLoggedIn=$isLoggedIn, biometricEnabled=$biometricEnabled, biometricAttempted=$biometricAttempted, biometricPassed=$biometricPassed")
        
        if (isInitialized && isLoggedIn && biometricEnabled && !biometricAttempted) {
            Log.d("ArcheryScore_Debug", "🔐 SplashScreen: NECESITA BIOMETRÍA")
            val activity = context as? FragmentActivity
            if (activity != null && BiometricAuth.canAuthenticate(context)) {
                biometricAttempted = true
                Log.d("ArcheryScore_Debug", "🔐 SplashScreen: Mostrando prompt biométrico...")
                BiometricAuth.authenticate(
                    activity = activity,
                    title = "Desbloquear Manos Locales",
                    subtitle = "Usa huella o Face para continuar",
                    onSuccess = {
                        Log.d("ArcheryScore_Debug", "✅ SplashScreen: Biometría exitosa")
                        biometricPassed = true
                        lastBiometricError = null
                    },
                    onError = { err ->
                        Log.e("ArcheryScore_Debug", "❌ SplashScreen: Error biométrico - $err")
                        lastBiometricError = err
                    },
                    onFail = {
                        Log.w("ArcheryScore_Debug", "⚠️ SplashScreen: Biometría cancelada, reintento disponible")
                    }
                )
            } else {
                Log.w("ArcheryScore_Debug", "⚠️ SplashScreen: BiometricAuth no disponible, saltando")
                biometricPassed = true
            }
        } else if (isInitialized && isLoggedIn && !biometricEnabled) {
            Log.d("ArcheryScore_Debug", "✅ SplashScreen: Biometría DESHABILITADA, permitir entrada")
        }
    }

    // ✅ NAVEGAR cuando esté todo listo
    LaunchedEffect(isInitialized, isLoggedIn, biometricEnabled, biometricPassed, biometricAttempted) {
        if (isInitialized && !hasNavigated) {
            Log.d("ArcheryScore_Debug", "🎯 SplashScreen (NAVEGACIÓN): isInitialized=$isInitialized, isLoggedIn=$isLoggedIn, biometricEnabled=$biometricEnabled, biometricPassed=$biometricPassed, biometricAttempted=$biometricAttempted")
            
            // Si necesita biometría pero aún no la intentó, esperar
            if (isLoggedIn && biometricEnabled && !biometricAttempted) {
                Log.d("ArcheryScore_Debug", "⏳ SplashScreen: Esperando intento de biometría...")
                return@LaunchedEffect
            }

            // Si necesita biometría y no pasó, no navegar
            if (isLoggedIn && biometricEnabled && !biometricPassed) {
                Log.d("ArcheryScore_Debug", "🔒 SplashScreen: Biometría requerida pero no pasada, esperando reintento...")
                return@LaunchedEffect
            }

            // ⏰ Pequeño delay para suavizar transición
            delay(800)

            hasNavigated = true
            val destination = if (isLoggedIn) Screen.Feed.route else Screen.Access.route
            Log.d("ArcheryScore_Debug", "➡️ SplashScreen: Navegando a $destination")
            navController.navigate(destination) {
                popUpTo(Screen.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // ✅ FALLBACK: Si después de 5 segundos no se inicializa, navegar a Access
    LaunchedEffect(Unit) {
        delay(5000) // Aumentado timeout a 5 segundos
        if (!hasNavigated) {
            Log.w("ArcheryScore_Debug", "⏰ SplashScreen: Timeout, navegando a Access")
            hasNavigated = true
            navController.navigate(Screen.Access.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.wa_80_cm_archery_target),
                contentDescription = "Logo",
                modifier = Modifier.size(160.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Bienvenidos a ", style = MaterialTheme.typography.titleLarge)
            Text("Manos Locales", style = MaterialTheme.typography.titleLarge)

            // ⏰ Indicador de carga adicional
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )

            lastBiometricError?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Error biométrico: $it")
            }
        }
    }
}