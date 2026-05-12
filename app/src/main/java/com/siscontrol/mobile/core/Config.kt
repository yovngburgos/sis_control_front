package com.siscontrol.mobile.core

import com.siscontrol.mobile.BuildConfig

object Config {
    /**
     * URL del backend real. Configurable con:
     * ./gradlew assembleDebug -PAPI_BASE_URL=http://<ip-o-dominio>:8080/
     *
     * En móvil no se usa localhost; el valor por defecto apunta al host del emulador Android.
     */
    val BASE_URL: String = BuildConfig.API_BASE_URL.ensureTrailingSlash()

    private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"
}
