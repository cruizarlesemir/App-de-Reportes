package com.capunay.reportesapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.capunay.reportesapp.data.ApiConfig
import com.capunay.reportesapp.data.model.Reporte
import com.capunay.reportesapp.ui.ListaUiState
import com.capunay.reportesapp.ui.components.Cargando
import com.capunay.reportesapp.ui.components.EstadoChip
import com.capunay.reportesapp.ui.components.MensajeVacio
import com.capunay.reportesapp.ui.components.TarjetaError
import com.capunay.reportesapp.util.Fechas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaReportesScreen(
    state: ListaUiState,
    onRecargar: () -> Unit,
    onCargarMas: () -> Unit,
    onBuscar: (String) -> Unit,
    onLimpiarBusqueda: () -> Unit,
    onNuevo: () -> Unit,
    onAbrir: (Int) -> Unit
) {
    var dni by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes") },
                actions = {
                    IconButton(
                        onClick = {
                            dni = ""
                            onRecargar()
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevo,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (ApiConfig.sinConfigurar) {
                AvisoConfiguracion()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = dni,
                    onValueChange = { texto ->
                        dni = texto.filter { it.isDigit() }.take(8)
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("DNI del ciudadano") },
                    placeholder = { Text("Ej. 12345678") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            onBuscar(dni)
                        }
                    ),
                    trailingIcon = {
                        if (dni.isNotEmpty()) {
                            IconButton(onClick = { dni = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Borrar DNI")
                            }
                        }
                    }
                )

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onBuscar(dni)
                    },
                    enabled = !state.cargando
                ) {
                    Text("Buscar")
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.cargando && state.reportes.isEmpty() ->
                        Cargando("Cargando reportes...")

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (state.error != null) {
                            item {
                                TarjetaError(
                                    mensaje = state.error,
                                    onReintentar = {
                                        if (state.dniConsultado != null) onBuscar(state.dniConsultado)
                                        else onRecargar()
                                    }
                                )
                            }
                        }

                        if (state.dniConsultado != null) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Resultados para DNI ${state.dniConsultado}",
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    TextButton(onClick = {
                                        dni = ""
                                        onLimpiarBusqueda()
                                    }) {
                                        Text("Ver todos")
                                    }
                                }
                            }
                        }

                        items(items = state.reportes, key = { it.id }) { reporte ->
                            ReporteCard(reporte = reporte, onClick = { onAbrir(reporte.id) })
                        }

                        if (state.reportes.isEmpty() &&
                            !state.cargando &&
                            state.error == null
                        ) {
                            item {
                                if (state.dniConsultado != null) {
                                    MensajeVacio(
                                        titulo = "Sin resultados",
                                        detalle = "No hay reportes registrados con el DNI ${state.dniConsultado}."
                                    )
                                } else {
                                    MensajeVacio(
                                        titulo = "Sin reportes",
                                        detalle = "Registra el primer reporte con el botón \"Nuevo\"."
                                    )
                                }
                            }
                        }

                        if (state.reportes.isNotEmpty()) {
                            item {
                                PieDeLista(
                                    state = state,
                                    onCargarMas = onCargarMas
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvisoConfiguracion() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = "Falta configurar la URL de la API",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Abre ApiConfig.kt y reemplaza BASE_URL por la URL de tu API en Render (o por http://10.0.2.2:8080/ si usas el emulador).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun ReporteCard(reporte: Reporte, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = reporte.tipo.ifBlank { "Sin tipo" },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                EstadoChip(reporte.estado)
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = reporte.direccion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DNI: ${reporte.dni}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Fechas.formatear(reporte.fecha),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PieDeLista(state: ListaUiState, onCargarMas: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.cargandoMas -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Cargando más reportes...")
            }

            state.dniConsultado != null -> Text(
                text = "${state.reportes.size} resultado(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            state.hayMas -> OutlinedButton(onClick = onCargarMas) {
                Text("Cargar 5 más")
            }

            else -> Text(
                text = "No hay más reportes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
