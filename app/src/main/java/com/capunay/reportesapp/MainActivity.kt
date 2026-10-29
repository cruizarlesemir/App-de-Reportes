package com.capunay.reportesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capunay.reportesapp.ui.ReportesViewModel
import com.capunay.reportesapp.ui.screens.DetalleReporteScreen
import com.capunay.reportesapp.ui.screens.FormularioReporteScreen
import com.capunay.reportesapp.ui.screens.ListaReportesScreen
import com.capunay.reportesapp.ui.theme.ReportesAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ReportesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ReportesAppTheme {
                ReportesApp(viewModel)
            }
        }
    }
}

@Composable
private fun ReportesApp(viewModel: ReportesViewModel) {
    val lista by viewModel.lista.collectAsStateWithLifecycle()
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()
    val detalle by viewModel.detalle.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var enFormulario by rememberSaveable { mutableStateOf(false) }
    var detalleId by rememberSaveable { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.iniciar()
    }

    BackHandler(enabled = detalleId != null || enFormulario) {
        if (detalleId != null) {
            detalleId = null
        } else {
            enFormulario = false
            viewModel.reiniciarFormulario()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val idSeleccionado = detalleId

        when {
            idSeleccionado != null -> {
                DetalleReporteScreen(
                    reporteId = idSeleccionado,
                    state = detalle,
                    onCargar = viewModel::cargarDetalle,
                    onVolver = { detalleId = null },
                    onAtender = viewModel::atenderReporte
                )
            }

            enFormulario -> {
                FormularioReporteScreen(
                    state = formulario,
                    onVolver = {
                        enFormulario = false
                        viewModel.reiniciarFormulario()
                    },
                    onRegistrar = { dni, tipo, direccion, referencia ->
                        viewModel.registrarReporte(dni, tipo, direccion, referencia)
                    },
                    onRegistrado = {
                        enFormulario = false
                        viewModel.reiniciarFormulario()
                        scope.launch {
                            snackbarHostState.showSnackbar("Reporte registrado correctamente.")
                        }
                    }
                )
            }

            else -> {
                ListaReportesScreen(
                    state = lista,
                    onRecargar = viewModel::recargar,
                    onCargarMas = viewModel::cargarSiguientes,
                    onBuscar = viewModel::buscarPorDni,
                    onLimpiarBusqueda = viewModel::limpiarBusqueda,
                    onNuevo = {
                        viewModel.reiniciarFormulario()
                        enFormulario = true
                    },
                    onAbrir = { id -> detalleId = id }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
