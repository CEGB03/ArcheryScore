package com.cegb03.archeryscore.ui.theme.screens.settings

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.fragment.app.FragmentActivity
import com.cegb03.archeryscore.ui.theme.Screen
import com.cegb03.archeryscore.util.BiometricAuth
import com.cegb03.archeryscore.viewmodel.AuthViewModel
import com.cegb03.archeryscore.viewmodel.SettingsViewModel

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState(initial = true)
    val biometricEnabled by settingsViewModel.biometricEnabled.collectAsState(initial = false)
    val isGoogleUser by settingsViewModel.isGoogleUser.collectAsState(initial = false)
    val isLoading by settingsViewModel.isLoading.collectAsState()
    val errorMessage by settingsViewModel.errorMessage.collectAsState()
    val logoutSuccess by settingsViewModel.logoutSuccess.collectAsState()
    val user by settingsViewModel.user.collectAsState()
    val changePasswordResult by settingsViewModel.changePasswordResult.collectAsState()
    val fatarcoVerificationResult by settingsViewModel.fatarcoVerificationResult.collectAsState()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showFatarcoVerifyDialog by remember { mutableStateOf(false) }
    var showFatarcoResultDialog by remember { mutableStateOf(false) }
    var fatarcoDni by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }

    // Gate biométrico al abrir Configuración (si está habilitado y disponible)
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricRequired = biometricEnabled && BiometricAuth.canAuthenticate(context) && activity != null
    var biometricPassed by remember { mutableStateOf(!biometricRequired) }

    LaunchedEffect(biometricRequired) {
        if (biometricRequired && !biometricPassed && activity != null) {
            BiometricAuth.authenticate(
                activity = activity,
                title = "Desbloquear configuración",
                subtitle = "Usa tu huella o Face ID",
                onSuccess = { biometricPassed = true },
                onError = { err -> scope.launch { snackbarHostState.showSnackbar("Error biométrico: $err") } },
                onFail = { }
            )
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Log.d("ArcheryScore_Debug", "📢 SettingsScreen: Mostrando error al usuario: $errorMessage")
            scope.launch {
                snackbarHostState.showSnackbar(errorMessage!!)
                settingsViewModel.clearError()
            }
        }
    }

    if (!biometricPassed) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Configuración") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors()
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Autenticación requerida", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        if (activity != null) {
                            BiometricAuth.authenticate(
                                activity = activity,
                                title = "Desbloquear configuración",
                                subtitle = "Usa tu huella o Face ID",
                                onSuccess = { biometricPassed = true },
                                onError = { err -> scope.launch { snackbarHostState.showSnackbar("Error biométrico: $err") } },
                                onFail = { }
                            )
                        }
                    }) {
                        Text("Reintentar")
                    }
                }
            }
        }
        return
    }

    // Cargar usuario si corresponde (solo una vez)
    LaunchedEffect(Unit) {
        settingsViewModel.ensureUserLoaded()
    }

    // Logout: solo observar, no llamar logout de nuevo
    LaunchedEffect(logoutSuccess) {
        if (logoutSuccess) {
            snackbarHostState.showSnackbar("Sesión cerrada correctamente")
            settingsViewModel.clearLogoutFlag()
            // SettingsViewModel.logout() ya hizo logout en Supabase y limpió DataStore
            // Solo refrescamos AuthViewModel para que actualice su estado
            authViewModel.refresh()
        }
    }

    // Cambio de contraseña
    LaunchedEffect(changePasswordResult) {
        changePasswordResult?.let { result ->
            if (result.isSuccess) {
                snackbarHostState.showSnackbar("Contraseña cambiada con éxito")
                showChangePasswordDialog = false
                newPassword = ""
                confirmPassword = ""
                passwordError = null
            } else {
                passwordError = result.exceptionOrNull()?.localizedMessage ?: "Error al cambiar contraseña"
            }
            settingsViewModel.clearChangePasswordResult()
        }
    }

    // Resultado de verificación FATARCO
    LaunchedEffect(fatarcoVerificationResult) {
        fatarcoVerificationResult?.let { result ->
            showFatarcoResultDialog = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Cuenta editable
                Text(text = "Cuenta", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))

                if (user != null) {
                    var isEditing by remember { mutableStateOf(false) }
                    var editedUsername by remember { mutableStateOf(user!!.username) }
                    var editedTel by remember { mutableStateOf(user!!.tel) }
                    var fieldError by remember { mutableStateOf<String?>(null) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (isEditing) {
                                OutlinedTextField(
                                    value = editedUsername,
                                    onValueChange = { editedUsername = it },
                                    label = { Text("Nombre de usuario") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(text = "Email", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = user!!.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editedTel,
                                    onValueChange = { editedTel = it },
                                    label = { Text("Teléfono") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                fieldError?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            fieldError = when {
                                                editedUsername.isBlank() -> "El nombre de usuario no puede estar vacío"
                                                else -> null
                                            }
                                            if (fieldError == null) {
                                                val updatedUser = user!!.copy(
                                                    username = editedUsername,
                                                    tel = editedTel
                                                )
                                                val proceedUpdate: () -> Unit = {
                                                    settingsViewModel.updateUser(updatedUser)
                                                    isEditing = false
                                                }
                                                if (biometricRequired && activity != null) {
                                                    BiometricAuth.authenticate(
                                                        activity = activity,
                                                        title = "Confirmar cambios",
                                                        subtitle = "Autoriza con huella o Face",
                                                        onSuccess = { proceedUpdate() },
                                                        onError = { err -> scope.launch { snackbarHostState.showSnackbar("Autenticación fallida: $err") } },
                                                        onFail = { }
                                                    )
                                                } else {
                                                    proceedUpdate()
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Guardar")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            editedUsername = user!!.username
                                            editedTel = user!!.tel
                                            fieldError = null
                                            isEditing = false
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancelar")
                                    }
                                }
                            } else {
                                Text(
                                    text = user!!.username.ifBlank { "Nombre no disponible" },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = user!!.email,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (user!!.tel.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = user!!.tel, style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { isEditing = true }) {
                                    Text("Editar datos")
                                }
                            }
                        }
                    }
                } else {
                    Text("No se pudo cargar la información del usuario", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Preferencias
                Text(text = "Preferencias", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notificaciones")
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            settingsViewModel.setNotificationsEnabled(enabled)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seguridad
                Text(text = "Seguridad", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                // Toggle biometría
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Desbloqueo biométrico")
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { enabled ->
                            // Si se intenta deshabilitar (enabled=false) y actualmente está habilitado (biometricEnabled=true)
                            if (!enabled && biometricEnabled) {
                                // Solicitar biometría antes de deshabilitar
                                if (activity != null && BiometricAuth.canAuthenticate(context)) {
                                    BiometricAuth.authenticate(
                                        activity = activity,
                                        title = "Desactivar desbloqueo biométrico",
                                        subtitle = "Confirma tu identidad",
                                        onSuccess = {
                                            Log.d("ArcheryScore_Debug", "✅ Biometría confirmada, deshabilitando")
                                            settingsViewModel.setBiometricEnabled(false)
                                        },
                                        onError = { err ->
                                            Log.e("ArcheryScore_Debug", "❌ Error biométrico al deshabilitar: $err")
                                            scope.launch {
                                                snackbarHostState.showSnackbar("No se pudo confirmar identidad")
                                            }
                                        },
                                        onFail = {
                                            Log.w("ArcheryScore_Debug", "⚠️ Biometría cancelada al deshabilitar")
                                        }
                                    )
                                } else {
                                    // Si no hay biometría disponible, permitir deshabilitar
                                    settingsViewModel.setBiometricEnabled(false)
                                }
                            } else {
                                // Si se habilita, permitir directamente
                                settingsViewModel.setBiometricEnabled(enabled)
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                // Cambiar contraseña (solo si NO es usuario de Google)
                if (!isGoogleUser) {
                    Button(onClick = { showChangePasswordDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cambiar contraseña")
                    }
                } else {
                    Text(
                        text = "La contraseña se gestiona mediante Google",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Verificación FATARCO
                Text(text = "Verificación FATARCO", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showFatarcoVerifyDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verificar mi perfil en FATARCO")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cerrar sesión
                Button(
                    onClick = {
                        val doLogout: () -> Unit = { settingsViewModel.logout() }
                        if (biometricRequired && activity != null) {
                            BiometricAuth.authenticate(
                                activity = activity,
                                title = "Confirmar cierre de sesión",
                                subtitle = "Autoriza con huella o Face",
                                onSuccess = { doLogout() },
                                onError = { err -> scope.launch { snackbarHostState.showSnackbar("Autenticación fallida: $err") } },
                                onFail = { }
                            )
                        } else {
                            doLogout()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.onError)
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = msg, color = MaterialTheme.colorScheme.error)
                }
            }

            if (showChangePasswordDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showChangePasswordDialog = false
                        newPassword = ""
                        confirmPassword = ""
                        passwordError = null
                    },
                    title = { Text("Cambiar contraseña") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("Nueva contraseña") },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        val icon = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                        Icon(icon, contentDescription = if (showPassword) "Ocultar" else "Mostrar")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirmar contraseña") },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                            passwordError?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            passwordError = null
                            if (newPassword.isBlank() || confirmPassword.isBlank()) {
                                passwordError = "Ambos campos son requeridos"
                                return@TextButton
                            }
                            if (newPassword != confirmPassword) {
                                passwordError = "Las contraseñas no coinciden"
                                return@TextButton
                            }
                            val doChange: () -> Unit = {
                                scope.launch {
                                    settingsViewModel.changePassword(newPassword)
                                }
                            }

                            if (biometricRequired && activity != null) {
                                BiometricAuth.authenticate(
                                    activity = activity,
                                    title = "Confirmar cambio",
                                    subtitle = "Autoriza con huella o Face",
                                    onSuccess = { doChange() },
                                    onError = { err -> scope.launch { snackbarHostState.showSnackbar("Autenticación fallida: $err") } },
                                    onFail = { }
                                )
                            } else {
                                doChange()
                            }
                        }) {
                            Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showChangePasswordDialog = false
                            passwordError = null
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Dialog para ingresar DNI
            if (showFatarcoVerifyDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showFatarcoVerifyDialog = false
                        fatarcoDni = ""
                    },
                    title = { Text("Verificación FATARCO") },
                    text = {
                        Column {
                            Text(
                                text = "Ingresa tu DNI para verificar tu perfil en la base de datos de FATARCO.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = fatarcoDni,
                                onValueChange = { fatarcoDni = it },
                                label = { Text("DNI") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (fatarcoDni.isNotBlank()) {
                                    showFatarcoVerifyDialog = false
                                    settingsViewModel.verifyFatarco(fatarcoDni.trim())
                                    fatarcoDni = ""
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Debes ingresar un DNI")
                                    }
                                }
                            }
                        ) {
                            Text("Verificar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showFatarcoVerifyDialog = false
                            fatarcoDni = ""
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Dialog de resultado FATARCO
            if (showFatarcoResultDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showFatarcoResultDialog = false
                        settingsViewModel.clearFatarcoVerification()
                    },
                    title = { 
                        Text(
                            when (fatarcoVerificationResult) {
                                is com.cegb03.archeryscore.data.model.FatarcoVerificationResult.Success -> "✅ Perfil verificado"
                                is com.cegb03.archeryscore.data.model.FatarcoVerificationResult.NotFound -> "⚠️ No encontrado"
                                is com.cegb03.archeryscore.data.model.FatarcoVerificationResult.Error -> "❌ Error"
                                else -> "Verificación FATARCO"
                            }
                        )
                    },
                    text = {
                        Column {
                            when (val result = fatarcoVerificationResult) {
                                is com.cegb03.archeryscore.data.model.FatarcoVerificationResult.Success -> {
                                    val data = result.data
                                    Text("DNI: ${data.dni}", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Nombre: ${data.nombre}", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Fecha Nac.: ${data.fechaNacimiento}", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Club: ${data.club}", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Estados:", style = MaterialTheme.typography.titleSmall)
                                    data.estados.forEach { estado ->
                                        Text("  • $estado", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                is com.cegb03.archeryscore.data.model.FatarcoVerificationResult.NotFound -> {
                                    Text("No se encontró tu DNI en la base de datos de FATARCO.")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Verifica que tu documento esté correctamente configurado y que estés registrado en FATARCO.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                is com.cegb03.archeryscore.data.model.FatarcoVerificationResult.Error -> {
                                    Text("Error: ${result.message}")
                                }
                                null -> {
                                    Text("Verificando...")
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showFatarcoResultDialog = false
                            settingsViewModel.clearFatarcoVerification()
                        }) {
                            Text("Cerrar")
                        }
                    }
                )
            }
        }
    }
}