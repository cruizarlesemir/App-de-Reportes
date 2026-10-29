package com.capunay.reportesapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.capunay.reportesapp.ui.FormularioUiState
import com.capunay.reportesapp.util.Fechas

private val TIPOS_REPORTE = listOf(
    "DOMICILIARIA",
    "DESMONTE",
    "MALEZA",
    "OTRO"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioReporteScreen(
    state: FormularioUiState,
    onVolver: () -> Unit,
    onRegistrar: (dni: String, tipo: String, direccion: String, referencia: String) -> Unit,
    onRegistrado: () -> Unit
) {
    var dni by rememberSaveable { mutableStateOf("") }
    var tipo by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var referencia by rememberSaveable { mutableStateOf("") }
    var errorLocal by rememberSaveable { mutableStateOf<String?>(null) }
    var tipoExpandido by rememberSaveable { mutableStateOf(false) }

    val fecha = remember { Fechas.ahoraIso() }

    LaunchedEffect(state.registrado) {
        if (state.registrado) onRegistrado()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo reporte") },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registrar un reporte",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Completa los datos del ciudadano y el tipo de incidencia.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = dni,
                onValueChange = { texto ->
                    dni = texto.filter { it.isDigit() }.take(8)
                    errorLocal = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("DNI *") },
                placeholder = { Text("8 dígitos") },
                singleLine = true,
                isError = errorLocal?.contains("DNI") == true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            ExposedDropdownMenuBox(
                expanded = tipoExpandido,
                onExpandedChange = { tipoExpandido = !tipoExpandido }
            ) {
                OutlinedTextField(
                    value = tipo,
                    onValueChange = {},
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    readOnly = true,
                    label = { Text("Tipo de reporte *") },
                    placeholder = { Text("Selecciona un tipo") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoExpandido)
                    },
                    isError = errorLocal?.contains("tipo") == true
                )

                ExposedDropdownMenu(
                    expanded = tipoExpandido,
                    onDismissRequest = { tipoExpandido = false }
                ) {
                    TIPOS_REPORTE.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                tipo = opcion
                                tipoExpandido = false
                                errorLocal = null
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = direccion,
                onValueChange = {
                    direccion = it
                    errorLocal = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Dirección *") },
                placeholder = { Text("Av. Ejemplo 123") },
                singleLine = true,
                isError = errorLocal?.contains("dirección") == true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = referencia,
                onValueChange = { referencia = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Referencia (opcional)") },
                placeholder = { Text("Cerca al parque, portón verde...") },
                minLines = 2,
                maxLines = 4
            )

            OutlinedTextField(
                value = Fechas.formatear(fecha),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false,
                label = { Text("Fecha de registro") },
                supportingText = { Text("Se registra automáticamente con la fecha y hora actual.") }
            )

            if (errorLocal != null) {
                Text(
                    text = errorLocal.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (state.error != null) {
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

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    val error = when {
                        dni.length != 8 -> "El DNI debe tener exactamente 8 dígitos."
                        tipo.isBlank() -> "Selecciona el tipo de reporte."
                        direccion.isBlank() -> "Ingresa la dirección del reporte."
                        else -> null
                    }
                    errorLocal = error
                    if (error == null) {
                        onRegistrar(dni, tipo, direccion, referencia)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !state.enviando
            ) {
                if (state.enviando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.size(10.dp))
                    Text("Registrando...")
                } else {
                    Text("Registrar reporte")
                }
            }
        }
    }
}
