package com.example.appvuelos_capebe.model

data class AeronaveInfo(
    val matricula: String,
    val modelo: String,
    val plazasTotales: Int,
    val asientosDisponibles: Int,
    val baseAerea: String,
    val horometroActual: Double,
    val horometroMaxMantenimiento: Double,
    val enMantenimiento: Boolean
) {
    val etiqueta: String get() = "$matricula $modelo"
}

data class VueloModel(
    val fecha: String = "",
    val horario: String = "",
    val base: String = "",
    val aeronave: String = "",
    val plazasAeronave: Int = 4,
    val destino: String = "",
    val pilotoAlMando: String = "Cap. Rodrigo Fuentes",
    val nombreInvitado: String = "",
    val rutInvitado: String = "",
    val oficinaBanco: String = "",
    val esSocio: Boolean = true,
    val telefonoMovil: String = "",
    val cantidadPasajeros: Int = 1,
    val codigoVerificacion: String = ""
)
