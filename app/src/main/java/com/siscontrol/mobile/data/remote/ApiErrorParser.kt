package com.siscontrol.mobile.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

object ApiErrorParser {
    private val gson = Gson()

    fun messageFrom(response: Response<*>): String {
        val raw = response.errorBody()?.string().orEmpty()
        val backendMessage = runCatching {
            val json = gson.fromJson(raw, JsonObject::class.java)
            listOf("message", "mensaje", "error", "detail", "title")
                .firstNotNullOfOrNull { key -> json.get(key)?.asString }
        }.getOrNull()

        val message = backendMessage?.takeIf { it.isNotBlank() } ?: raw.takeIf { it.isNotBlank() }
        return normalize(message ?: "Error del servidor. Código HTTP: ${response.code()}")
    }

    fun normalize(message: String): String {
        val lower = message.lowercase()
        return when {
            "credenciales" in lower -> "Credenciales inválidas."
            "inactivo" in lower -> "El usuario está inactivo. Contacte al administrador."
            "solo guardias" in lower -> "Solo guardias pueden iniciar jornada."
            "punto" in lower && "escaneado" in lower -> "Punto ya escaneado."
            "ronda no activa" in lower -> "Ronda no activa."
            "ronda" in lower && ("progreso" in lower || "progress" in lower) -> "La ronda ya no está en progreso."
            "jornada activa" in lower -> "No tienes jornada activa aquí."
            "ronda antes" in lower || "cerrar jornada" in lower -> "Termina la ronda antes de cerrar jornada."
            "permiso" in lower || "forbidden" in lower || "403" in lower || "insuficiente" in lower ->
                "Permisos insuficientes para realizar esta acción."
            else -> message
        }
    }
}
