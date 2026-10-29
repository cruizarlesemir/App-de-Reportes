package com.capunay.reportesapp.data

import com.capunay.reportesapp.data.model.NuevoReporte
import com.capunay.reportesapp.data.model.Reporte
import com.capunay.reportesapp.data.model.ReportesPage
import com.capunay.reportesapp.data.remote.ApiClient
import com.capunay.reportesapp.util.Fechas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Punto único de acceso a los datos.
 */
object ReportesRepository {

    private val api get() = ApiClient.api

    /** GET reportes/?offset=..&limit=.. */
    suspend fun listar(offset: Int, limite: Int = ApiConfig.TAMANO_PAGINA): ReportesPage =
        withContext(Dispatchers.IO) {
            api.listar(offset = offset, limit = limite)
        }

    /** GET reportes/{id} */
    suspend fun obtener(id: Int): Reporte = withContext(Dispatchers.IO) {
        api.obtener(id)
    }

    /** POST reportes/ */
    suspend fun crear(reporte: NuevoReporte): Reporte? = withContext(Dispatchers.IO) {
        var respuesta = api.crear(reporte)

        // Si el backend exige otro formato de fecha, reintenta una vez.
        if (!respuesta.isSuccessful && respuesta.code() == 400) {
            respuesta = api.crear(reporte.copy(fecha = Fechas.aFormatoApi(reporte.fecha)))
        }

        if (!respuesta.isSuccessful) throw HttpException(respuesta)
        respuesta.body()
    }

    /** GET reportes/ciudadano/{dni} */
    suspend fun buscarPorDni(dni: String): List<Reporte> = withContext(Dispatchers.IO) {
        api.buscarPorDni(dni).items
    }

    /** PUT reportes/{id}: la API cambia el estado de 1 a 0. */
    suspend fun atender(reporte: Reporte): Reporte = withContext(Dispatchers.IO) {
        val actualizado = reporte.copy(estado = 0)
        val respuesta = api.atender(reporte.id, actualizado)
        if (!respuesta.isSuccessful) throw HttpException(respuesta)
        respuesta.body() ?: actualizado
    }
}
