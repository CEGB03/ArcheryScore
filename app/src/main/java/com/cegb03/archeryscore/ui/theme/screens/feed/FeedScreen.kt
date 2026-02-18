@file:OptIn(ExperimentalMaterial3Api::class)

package com.cegb03.archeryscore.ui.theme.screens.feed

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cegb03.archeryscore.ui.theme.Screen
import com.cegb03.archeryscore.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    var hasInitialized by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.isLoggedIn.collect { loggedIn ->
            if (loggedIn) {
                Log.d("ArcheryScore_Debug", "✅ Usuario logueado, cargando productos y favoritos")
                //hacer cargar lo que haga falta
            }
        }
    }

    // Estados de UI
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var expanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (!isLoggedIn) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Log.e("ArcheryScore_Debug", "el isLoggedIn dio false")
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Productos Manos Locales") },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Cart.route) }) {
                            BadgedBox(
                                badge = {

                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                            }
                        }
                        IconButton(onClick = {
                            navController.navigate(Screen.FavoritesOnly.route)
                        }) {
                            Icon(Icons.Default.Favorite, contentDescription = "Ver favoritos")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = {
                            navController.navigate(Screen.Settings.route)
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuración")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // 🔍 Búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar por nombre") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                )

                // 🎯 Filtro por categoría
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Categoría: $selectedCategory")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {

                    }
                }

            }
        }
    }
}