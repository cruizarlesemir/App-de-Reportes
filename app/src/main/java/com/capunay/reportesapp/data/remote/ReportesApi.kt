package com.capunay.reportesapp.data.remote

import com.capunay.reportesapp.data.ApiConfig
import com.capunay.reportesapp.data.model.NuevoReporte
import com.capunay.reportesapp.data.model.Reporte
import com.capunay.reportesapp.data.model.ReportesPage
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ReportesApi {

    /** GET reportes/?offset=0&limit=5 */
    @GET(ApiConfig.RUTA_LISTA)
    suspend fun listar(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): ReportesPage

    /** GET reportes/{id} */
    @GET(ApiConfig.RUTA_DETALLE)
    suspend fun obtener(@Path("id") id: Int): Reporte

    /** POST reportes/ (el estado lo inicializa la API en 1) */
    @POST(ApiConfig.RUTA_LISTA)
    suspend fun crear(@Body reporte: NuevoReporte): Response<Reporte>

    /** GET reportes/ciudadano/{dni} */
    @GET(ApiConfig.RUTA_CIUDADANO)
    suspend fun buscarPorDni(@Path("dni") dni: String): ReportesPage

    /** PUT reportes/{id} (marca el reporte como atendido: estado 1 -> 0) */
    @PUT(ApiConfig.RUTA_DETALLE)
    suspend fun atender(@Path("id") id: Int, @Body reporte: Reporte): Response<Reporte>
}
