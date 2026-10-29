package com.capunay.reportesapp.data

import com.google.gson.JsonSyntaxException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Convierte excepciones técnicas en mensajes entendibles para el usuario. */
fun Throwable.mensajeUsuario(): String = when (this) {
    is SocketTimeoutException ->
        "La API tardó demasiado en responder. En Render la primera petición puede tardar hasta 1 minuto: espera un momento y reintenta."

    is UnknownHostException ->
        "No se pudo encontrar el servidor. Revisa la URL en ApiConfig y tu conexión a internet."

    is ConnectException ->
        "No se pudo conectar con el servidor. Verifica que la API esté activa y que la IP/puerto sean correctos."

    is HttpException -> when (code()) {
        400 -> "La API rechazó los datos enviados (HTTP 400). Revisa el formulario."
        401, 403 -> "No tienes permiso para realizar esta acción (HTTP ${code()})."
        404 -> "No se encontró el recurso (HTTP 404). Revisa las rutas en ApiConfig."
        405 -> "La operación no está permitida por la API (HTTP 405)."
        in 500..599 ->
            "El servidor falló (HTTP ${code()}). Si usas Render, espera unos segundos y reintenta."
        else -> "Error del servidor (HTTP ${code()})."
    }

    is JsonSyntaxException ->
        "La respuesta de la API no tiene el formato esperado."

    is IOException ->
        "Error de red. Revisa tu conexión a internet e inténtalo de nuevo."

    else -> message ?: "Ocurrió un error inesperado."
}
