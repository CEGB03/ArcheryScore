package com.cegb03.archeryscore.ui.theme.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cegb03.archeryscore.data.repository.UserRepository
import com.cegb03.archeryscore.ui.theme.Screen
import com.cegb03.archeryscore.viewmodel.AuthViewModel
import com.cegb03.archeryscore.viewmodel.UserViewModel
import com.cegb03.archeryscore.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onBackPressed: () -> Unit = {},
    onRegisterPressed: () -> Unit = {},
    userViewModel: UserViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val loginResult by userViewModel.loginSuccess.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val errorMsg by userViewModel.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var hasNavigated by remember { mutableStateOf(false) }

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val user by settingsViewModel.user.collectAsState()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Si ya está logueado, saltar a FeedScreen automáticamente
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate(Screen.Feed.route) {
                launchSingleTop = true
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    // 🔹 Limpiar el estado al entrar al login
    LaunchedEffect(Unit) {
        authViewModel.clearAuthState()
    }

    LaunchedEffect(loginResult) {
        if (loginResult == true && !hasNavigated) {
            hasNavigated = true
            authViewModel.refresh()
            delay(100)
            //DebugDev.refreshUserIfLoggedIn()

            navController.navigate(Screen.Feed.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }

            userViewModel.clearLoginResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Iniciar Sesión") },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Iniciar Sesión", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    userViewModel.loginUser(email, password) {
                        authViewModel.refresh()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text("Ingresar")
            }

            LaunchedEffect(errorMsg) {
                errorMsg?.let {
                    scope.launch {
                        snackbarHostState.showSnackbar(it)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = {
                onRegisterPressed()
            }) {
                Text("¿No tenés cuenta? Registrate")
            }
        }
    }
}