package com.example.appvuelos_capebe.network

data class VueloDto(
    val id: Long? = null,
    val pasajero: String,
    val rut: String = "",
    val oficinaBanco: String = "",
    val esSocio: Boolean = true,
    val telefonoMovil: String = "",
    val cantidadPasajeros: Int = 1,
    val fecha: String = "",
    val bloqueHorario: String = "",
    val aeronave: String,
    val plazasAeronave: Int = 4,
    val baseAerea: String,
    val destino: String = "",
    val pilotoAlMando: String = "",
    val codigoVerificacion: String? = null,
    val pesoKg: Double? = 0.0,
    val horometro: Double? = 0.0
)

data class PaseQrDto(
    val idQr: Long? = null,
    val codigoQr: String? = null,
    val socio: String,
    val rut: String = "",
    val oficinaBanco: String = "",
    val esSocio: Boolean = true,
    val telefonoMovil: String = "",
    val cantidadPasajeros: Int = 1,
    val plazasAeronave: Int = 4,
    val fecha: String,
    val horario: String,
    val base: String,
    val aeronave: String,
    val destino: String = "",
    val pilotoAlMando: String = "",
    val estadoQr: String? = null
)

data class AeronaveDto(
    val idAeronave: Long? = null,
    val matricula: String,
    val modelo: String,
    val plazasTotales: Int,
    val asientosDisponibles: Int,
    val baseAerea: String,
    val horometroActual: Double,
    val horometroMaxMantenimiento: Double,
    val enMantenimiento: Boolean
)
