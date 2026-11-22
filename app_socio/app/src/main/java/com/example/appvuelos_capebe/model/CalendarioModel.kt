package com.example.appvuelos_capebe.model

import java.time.LocalDate

data class CalendarioModel(
    val fechaSeleccionada: LocalDate = LocalDate.now(),
    val bloqueSeleccionado: String = "10:00 - 11:00",
    val baseSeleccionada: String = "Tobalaba",
    val aeronaveSeleccionada: AeronaveInfo? = null,
    val aeronaves: List<AeronaveInfo> = emptyList(),
    val nombreInvitado: String = "",
    val rutInvitado: String = "",
    val oficinaBanco: String = "",
    val esSocio: Boolean = true,
    val telefonoMovil: String = "",
    val cantidadPasajeros: Int = 1,
    val destino: String = "Circuito Precordillera Santiago (SCTB)",
    val errorFormulario: String? = null,
    val vueloElegido: VueloModel? = null,
    val vuelosGuardados: List<VueloModel> = emptyList()
)
