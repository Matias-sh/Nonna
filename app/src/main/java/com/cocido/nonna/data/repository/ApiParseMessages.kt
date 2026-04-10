package com.cocido.nonna.data.repository

/** Mensaje estable cuando Gson/Retrofit no pueden interpretar el JSON (típico si R8 o el API cambió). */
internal const val API_RESPONSE_PARSE_ERROR =
    "No se pudo leer la respuesta del servidor. Actualizá la app o probá más tarde."
