package com.example.appvuelos_capebe.data.local

import android.content.Context

// Sesion de usuarios manejada con Room SQLite (UsuarioDao) y aislamiento de vuelos por cuenta
object CuentaVuelosStore {
    private const val PREFS_NAME = "capebe_vuelos_por_cuenta"

    private fun claveCuenta(nombreSocio: String): String =
        "codigos_${nombreSocio.trim().lowercase()}"

    fun registrarCodigoParaCuenta(context: Context, nombreSocio: String, codigoQr: String) {
        if (nombreSocio.isBlank() || codigoQr.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = claveCuenta(nombreSocio)
        val actuales = prefs.getStringSet(key, emptySet())?.toMutableSet() ?: mutableSetOf()
        actuales.add(codigoQr.trim().uppercase())
        prefs.edit().putStringSet(key, actuales).apply()
    }

    fun obtenerCodigosDeCuenta(context: Context, nombreSocio: String): Set<String> {
        if (nombreSocio.isBlank()) return emptySet()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(claveCuenta(nombreSocio), emptySet())
            ?.map { it.trim().uppercase() }
            ?.toSet()
            ?: emptySet()
    }
}
