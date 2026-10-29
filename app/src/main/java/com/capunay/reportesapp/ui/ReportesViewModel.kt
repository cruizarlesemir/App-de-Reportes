package com.capunay.reportesapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capunay.reportesapp.data.ApiConfig
import com.capunay.reportesapp.data.ReportesRepository
import com.capunay.reportesapp.data.mensajeUsuario
import com.capunay.reportesapp.data.model.NuevoReporte
import com.capunay.reportesapp.data.model.Reporte
import com.capunay.reportesapp.data.model.ReportesPage
import com.capunay.reportesapp.util.Fechas
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListaUiState(
    val reportes: List<Reporte> = emptyList(),
    val cargando: Boolean = false,
    val cargandoMas: Boolean = false,
    val error: String? = null,
    val hayMas: Boolean = true,
    val dniConsultado: String? = null
)

data class FormularioUiState(
    val enviando: Boolean = false,
    val error: String? = null,
    val registrado: Boolean = false
)

data class DetalleUiState(
    val reporte: Reporte? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val atendiendo: Boolean = false,
    val mensaje: String? = null
)

class ReportesViewModel : ViewModel() {

    private val repositorio = ReportesRepository

    private val _lista = MutableStateFlow(ListaUiState())
    val lista = _lista.asStateFlow()

    private val _formulario = MutableStateFlow(FormularioUiState())
    val formulario = _formulario.asStateFlow()

    private val _detalle = MutableStateFlow(DetalleUiState())
    val detalle = _detalle.asStateFlow()

    private var offsetActual = ApiConfig.OFFSET_INICIAL
    private var dniActivo: String? = null
    private var iniciado = false

    // ----------------------------- Lista -----------------------------

    /** Se llama una sola vez al abrir la app (sobrevive a rotaciones). */
    fun iniciar() {
        if (iniciado) return
        iniciado = true
        recargar()
    }

    /** Vuelve a la primera página (botón refrescar / después de registrar). */
    fun recargar() {
        if (_lista.value.cargando) return
        dniActivo = null
        _lista.update { it.copy(dniConsultado = null) }
        cargarPagina(ApiConfig.OFFSET_INICIAL, reemplazar = true)
    }

    /** Carga los 5 siguientes reportes. */
    fun cargarSiguientes() {
        val estado = _lista.value
        if (estado.cargando || estado.cargandoMas || !estado.hayMas || dniActivo != null) return
        cargarPagina(offsetActual + ApiConfig.TAMANO_PAGINA, reemplazar = false)
    }

    private fun cargarPagina(offset: Int, reemplazar: Boolean) {
        viewModelScope.launch {
            _lista.update {
                it.copy(cargando = reemplazar, cargandoMas = !reemplazar, error = null)
            }

            try {
                val respuesta = repositorio.listar(offset)
                offsetActual = offset

                _lista.update { actual ->
                    val acumulados = if (reemplazar) {
                        respuesta.items
                    } else {
                        (actual.reportes + respuesta.items).distinctBy { reporte -> reporte.id }
                    }

                    actual.copy(
                        reportes = acumulados,
                        cargando = false,
                        cargandoMas = false,
                        error = null,
                        hayMas = hayMasPaginas(respuesta, offset)
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _lista.update {
                    it.copy(cargando = false, cargandoMas = false, error = e.mensajeUsuario())
                }
            }
        }
    }

    private fun hayMasPaginas(respuesta: ReportesPage, offset: Int): Boolean {
        respuesta.hasNext?.let { return it }
        respuesta.last?.let { return !it }
        respuesta.totalElements?.let { return offset + respuesta.items.size < it }
        respuesta.totalPages?.let {
            return (offset / ApiConfig.TAMANO_PAGINA) + 1 < it
        }
        return respuesta.items.size >= ApiConfig.TAMANO_PAGINA
    }

    // --------------------------- Búsqueda ----------------------------

    fun buscarPorDni(dni: String) {
        val limpio = dni.trim()
        if (limpio.isEmpty()) {
            limpiarBusqueda()
            return
        }
        if (_lista.value.cargando) return

        dniActivo = limpio
        viewModelScope.launch {
            _lista.update {
                it.copy(cargando = true, error = null, dniConsultado = limpio)
            }

            try {
                val encontrados = repositorio.buscarPorDni(limpio)
                _lista.update {
                    it.copy(
                        reportes = encontrados,
                        cargando = false,
                        error = null,
                        hayMas = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _lista.update {
                    it.copy(cargando = false, error = e.mensajeUsuario())
                }
            }
        }
    }

    fun limpiarBusqueda() {
        if (dniActivo == null && _lista.value.dniConsultado == null) return
        dniActivo = null
        _lista.update { it.copy(dniConsultado = null) }
        recargar()
    }

    // -------------------------- Formulario ---------------------------

    fun registrarReporte(
        dni: String,
        tipo: String,
        direccion: String,
        referencia: String
    ) {
        if (_formulario.value.enviando) return

        viewModelScope.launch {
            _formulario.update { it.copy(enviando = true, error = null, registrado = false) }

            try {
                repositorio.crear(
                    NuevoReporte(
                        dni = dni.trim(),
                        fecha = Fechas.ahoraIso(),
                        tipo = tipo,
                        direccion = direccion.trim(),
                        referencia = referencia.trim().ifBlank { null }
                    )
                )
                _formulario.update { it.copy(enviando = false, registrado = true) }
                recargar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _formulario.update {
                    it.copy(enviando = false, error = e.mensajeUsuario())
                }
            }
        }
    }

    fun reiniciarFormulario() {
        _formulario.value = FormularioUiState()
    }

    // ---------------------------- Detalle ----------------------------

    fun cargarDetalle(id: Int, forzar: Boolean = false) {
        val actual = _detalle.value
        if (!forzar && actual.reporte?.id == id && !actual.cargando) return
        if (actual.cargando && actual.reporte?.id == id) return

        viewModelScope.launch {
            _detalle.value = DetalleUiState(cargando = true)

            try {
                val reporte = repositorio.obtener(id)
                _detalle.value = DetalleUiState(reporte = reporte)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _detalle.value = DetalleUiState(error = e.mensajeUsuario())
            }
        }
    }

    fun atenderReporte(reporte: Reporte) {
        val estado = _detalle.value
        if (estado.atendiendo || reporte.estaAtendido) return

        viewModelScope.launch {
            _detalle.update { it.copy(atendiendo = true, error = null, mensaje = null) }

            try {
                val actualizado = repositorio.atender(reporte)
                _detalle.update {
                    it.copy(
                        reporte = actualizado,
                        atendiendo = false,
                        mensaje = "Reporte atendido correctamente."
                    )
                }
                _lista.update { lista ->
                    lista.copy(
                        reportes = lista.reportes.map {
                            if (it.id == actualizado.id) actualizado else it
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _detalle.update {
                    it.copy(atendiendo = false, error = e.mensajeUsuario())
                }
            }
        }
    }

    fun limpiarMensajeDetalle() {
        _detalle.update { it.copy(mensaje = null) }
    }
}
