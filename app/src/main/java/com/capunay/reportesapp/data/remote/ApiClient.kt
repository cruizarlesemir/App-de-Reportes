package com.capunay.reportesapp.data.remote

import com.capunay.reportesapp.data.ApiConfig
import com.capunay.reportesapp.data.model.ReportesPage
import com.capunay.reportesapp.data.model.ReportesPageDeserializer
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val gson = GsonBuilder()
        .registerTypeAdapter(ReportesPage::class.java, ReportesPageDeserializer())
        .create()

    private val clienteHttp = OkHttpClient.Builder()
        // Render "duerme" el plan gratuito: la primera petición puede tardar ~1 minuto.
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .callTimeout(180, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()

    val api: ReportesApi by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ReportesApi::class.java)
    }
}
