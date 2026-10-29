package com.capunay.reportesapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.capunay.reportesapp.data.model.Reporte
import com.capunay.reportesapp.ui.DetalleUiState
import com.capunay.reportesapp.ui.components.Cargando
import com.capunay.reportesapp.ui.components.EstadoChip
import com.capunay.reportesapp.ui.components.FilaInfo
import com.capunay.reportesapp.ui.components.TarjetaError
import com.capunay.reportesapp.util.Fechas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleReporteScreen(
    reporteId: Int,
    state: DetalleUiState,
    onCargar: (Int) -> Unit,
    onVolver: () -> Unit,
    onAtender: (Reporte) -> Unit
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    LaunchedEffect(reporteId) {
        onCargar(reporteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del reporte") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        val reporte = state.reporte

        when {
            reporte == null && state.cargando -> {
                Cargando(
                    texto = "Cargando reporte...",
                    modifier = Modifier.padding(padding)
                )
            }

            reporte == null && state.error != null -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    TarjetaError(
                        mensaje = state.error,
                        onReintentar = { onCargar(reporteId) }
                    )
                }
            }

            reporte != null -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = reporte.tipo.ifBlank { "Sin tipo" },
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        EstadoChip(reporte.estado)
                    }

                    Spacer(Modifier.height(16.dp))

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            FilaInfo("ID", reporte.id.toString())
                            HorizontalDivider()
                            FilaInfo("DNI", reporte.dni.ifBlank { "—" })
                            HorizontalDivider()
                            FilaInfo("Fecha", Fechas.formatear(reporte.fecha))
                            HorizontalDivider()
                            FilaInfo("Tipo", reporte.tipo.ifBlank { "—" })
                            HorizontalDivider()
                            FilaInfo("Dirección", reporte.direccion.ifBlank { "—" })
                            HorizontalDivider()
                            FilaInfo("Referencia", reporte.referencia?.takeIf { it.isNotBlank() } ?: "—")
                            HorizontalDivider()
                            FilaInfo(
                                "Estado",
                                if (reporte.estaAtendido) "Atendido (0)" else "Pendiente (${reporte.estado})"
                            )
                        }
                    }

                    if (state.mensaje != null) {
                        Spacer(Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFC8E6C9)
                            )
                        ) {
                            Text(
                                text = state.mensaje,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }

                    if (state.error != null) {
                        Spacer(Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = state.error,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    if (reporte.estaAtendido) {
                        Text(
                            text = "Este reporte ya fue atendido.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1B5E20)
                        )
                    } else {
                        Button(
                            onClick = { mostrarConfirmacion = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !state.atendiendo
                        ) {
                            if (state.atendiendo) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.size(10.dp))
                                Text("Atendiendo...")
                            } else {
                                Text("Atender reporte")
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (mostrarConfirmacion && state.reporte != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Atender reporte") },
            text = {
                Text(
                    "¿Confirmas que el reporte #${state.reporte.id} ya fue atendido? " +
                        "Su estado cambiará a Atendido."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        state.reporte?.let(onAtender)
                    }
                ) {
                    Text("Sí, atender")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
