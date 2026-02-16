package com.cegb03.archeryscore.ui.theme.screens.access

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cegb03.archeryscore.R
import com.cegb03.archeryscore.viewmodel.AuthViewModel

@Composable
fun AccessScreen(
    onLoginSuccess: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var isLoginMode by rememberSaveable { mutableStateOf(true) }

    // Navegar cuando esté autenticado
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.wa_80_cm_archery_target),
            contentDescription = "Logo Archery"
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Título
        Text(
            text = if (isLoginMode) "Iniciar Sesión" else "Registrarse",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo Usuario/Email
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(if (isLoginMode) "Usuario o Email" else "Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = !isLoading,
            keyboardOptions = if (isLoginMode) {
                KeyboardOptions(keyboardType = KeyboardType.Text)
            } else {
                KeyboardOptions(keyboardType = KeyboardType.Email)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            enabled = !isLoading,
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                Button(
                    onClick = { showPassword = !showPassword },
                    modifier = Modifier.size(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(if (showPassword) "Ocultar" else "Ver")
                }
            }
        )

        // Mensaje de error
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Login/Register
        Button(
            onClick = {
                if (isLoginMode) {
                    authViewModel.loginWithCredentials(username, password)
                } else {
                    authViewModel.registerWithCredentials(username, username, password, "")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            enabled = !isLoading && username.isNotEmpty() && password.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (isLoginMode) "Iniciar Sesión" else "Registrarse")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle entre Login y Register
        TextButton(
            onClick = { isLoginMode = !isLoginMode },
            enabled = !isLoading
        ) {
            Text(
                text = if (isLoginMode) {
                    "¿No tienes cuenta? Registrate aquí"
                } else {
                    "¿Ya tienes cuenta? Inicia sesión"
                }
            )
        }
    }
}