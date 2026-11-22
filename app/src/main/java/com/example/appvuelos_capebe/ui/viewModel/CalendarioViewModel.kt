package com.example.appvuelos_capebe.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appvuelos_capebe.model.AeronaveInfo
import com.example.appvuelos_capebe.model.CalendarioModel
import com.example.appvuelos_capebe.model.VueloModel
import com.example.appvuelos_capebe.network.RetrofitVuelo
import com.example.appvuelos_capebe.ui.components.esDiaHabilitadoVuelo
import com.example.appvuelos_capebe.ui.components.proximoDiaVuelo
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class CalendarioViewModel : ViewModel() {

    val bloquesHorarios = listOf(
        "09:00 - 10:00",
        "10:00 - 11:00",
        "11:00 - 12:00",
        "12:00 - 13:00",
        "15:00 - 16:00",
        "16:00 - 17:00"
    )

    private val flotaRespaldo = listOf(
        AeronaveInfo("CC-AAA", "Cessna 172", 4, 3, "Tobalaba", 1245.2, 1250.0, false),
        AeronaveInfo("CC-BBB", "Piper PA-28", 4, 3, "Tobalaba", 2198.4, 2200.0, false),
        AeronaveInfo("CC-DDD", "Cessna 206", 6, 5, "Talcahuano", 3410.0, 3450.0, false),
        AeronaveInfo("CC-EEE", "Piper PA-32 Saratoga", 6, 5, "Tobalaba", 4100.0, 4100.0, true)
    )

    var uiState by mutableStateOf(
        CalendarioModel(
            fechaSeleccionada = proximoDiaVuelo(),
            aeronaves = flotaRespaldo,
            aeronaveSeleccionada = flotaRespaldo.first()
        )
    )
        private set

    init {
        cargarFlotaDesdeServidor()
    }

    fun cargarFlotaDesdeServidor() {
        viewModelScope.launch {
            try {
                val remotos = RetrofitVuelo.pistaConsultaApi.listarAeronaves()
                if (remotos.isNotEmpty()) {
                    val lista = remotos.map {
                        AeronaveInfo(
                            matricula = it.matricula,
                            modelo = it.modelo,
                            plazasTotales = it.plazasTotales,
                            asientosDisponibles = it.asientosDisponibles,
                            baseAerea = it.baseAerea,
                            horometroActual = it.horometroActual,
                            horometroMaxMantenimiento = it.horometroMaxMantenimiento,
                            enMantenimiento = it.enMantenimiento
                        )
                    }
                    val actual = uiState.aeronaveSeleccionada?.let { sel ->
                        lista.find { it.matricula == sel.matricula && !it.enMantenimiento }
                    } ?: lista.firstOrNull { !it.enMantenimiento }

                    uiState = uiState.copy(
                        aeronaves = lista,
                        aeronaveSeleccionada = actual
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun prellenarNombreSiVacio(nombre: String) {
        if (uiState.nombreInvitado.isBlank() && nombre.isNotBlank() && nombre != "Socio CAPEBE") {
            uiState = uiState.copy(nombreInvitado = nombre)
        }
    }

    fun onFechaSeleccionada(fecha: LocalDate) {
        uiState = uiState.copy(fechaSeleccionada = fecha, errorFormulario = null)
    }

    fun onBloqueSeleccionado(bloque: String) {
        uiState = uiState.copy(bloqueSeleccionado = bloque, errorFormulario = null)
    }

    fun onAeronaveSeleccionada(avion: AeronaveInfo) {
        if (avion.enMantenimiento) {
            uiState = uiState.copy(
                errorFormulario = "La aeronave ${avion.matricula} está FUERA DE VUELO por mantenimiento obligatorio DGAC."
            )
            return
        }
        val destinoBase = if (avion.baseAerea.contains("Talcahuano", ignoreCase = true)) {
            "Circuito Bahía Concepción (SCIE)"
        } else {
            "Circuito Precordillera Santiago (SCTB)"
        }
        val maxPax = avion.asientosDisponibles
        uiState = uiState.copy(
            aeronaveSeleccionada = avion,
            baseSeleccionada = avion.baseAerea,
            destino = destinoBase,
            cantidadPasajeros = uiState.cantidadPasajeros.coerceIn(1, maxPax),
            errorFormulario = null
        )
    }

    fun onNombreInvitadoChanged(v: String) {
        uiState = uiState.copy(nombreInvitado = v, errorFormulario = null)
    }

    fun onRutInvitadoChanged(v: String) {
        uiState = uiState.copy(rutInvitado = v, errorFormulario = null)
    }

    fun onOficinaBancoChanged(v: String) {
        uiState = uiState.copy(oficinaBanco = v, errorFormulario = null)
    }

    fun onEsSocioChanged(v: Boolean) {
        uiState = uiState.copy(esSocio = v, errorFormulario = null)
    }

    fun onTelefonoMovilChanged(v: String) {
        val filtrado = v.filter { it.isDigit() || it == '+' || it == ' ' }.take(15)
        uiState = uiState.copy(telefonoMovil = filtrado, errorFormulario = null)
    }

    fun onCantidadPasajerosChanged(cant: Int) {
        val max = uiState.aeronaveSeleccionada?.asientosDisponibles ?: 3
        uiState = uiState.copy(
            cantidadPasajeros = cant.coerceIn(1, max),
            errorFormulario = null
        )
    }

    fun onDestinoChanged(v: String) {
        uiState = uiState.copy(destino = v, errorFormulario = null)
    }

    fun validarYPrepararReserva(): Boolean {
        val s = uiState
        if (!esDiaHabilitadoVuelo(s.fechaSeleccionada)) {
            uiState = s.copy(errorFormulario = "Los vuelos se realizan sábados, domingos y festivos.")
            return false
        }
        val avion = s.aeronaveSeleccionada
        if (avion == null || avion.enMantenimiento) {
            uiState = s.copy(errorFormulario = "Selecciona una aeronave operativa (no en mantenimiento).")
            return false
        }
        if (s.nombreInvitado.trim().length < 5) {
            uiState = s.copy(errorFormulario = "Ingresa nombres y apellidos completos del pasajero.")
            return false
        }
        if (!validarRutChileno(s.rutInvitado)) {
            uiState = s.copy(errorFormulario = "Ingresa un RUT chileno válido (ej: 18.452.310-K o 12345678-5).")
            return false
        }
        if (s.oficinaBanco.trim().length < 3) {
            uiState = s.copy(errorFormulario = "Ingresa la Oficina BancoEstado a la que pertenece.")
            return false
        }
        val digitosTel = s.telefonoMovil.filter { it.isDigit() }
        if (digitosTel.length < 8) {
            uiState = s.copy(errorFormulario = "Ingresa un número de teléfono móvil válido (mín. 8 dígitos).")
            return false
        }
        if (s.cantidadPasajeros < 1 || s.cantidadPasajeros > avion.asientosDisponibles) {
            uiState = s.copy(
                errorFormulario = "El avión de ${avion.plazasTotales} plazas admite máximo ${avion.asientosDisponibles} pasajeros (descontando piloto)."
            )
            return false
        }

        val prefijo = if (avion.baseAerea.contains("Talcahuano", ignoreCase = true)) "SCIE" else "SCTB"
        val sufijo = UUID.randomUUID().toString().substring(0, 4).uppercase()
        val codigoUnico = "CAPEBE-$prefijo-$sufijo"

        val vuelo = VueloModel(
            fecha = s.fechaSeleccionada.toString(),
            horario = s.bloqueSeleccionado,
            base = avion.baseAerea,
            aeronave = avion.etiqueta,
            plazasAeronave = avion.plazasTotales,
            destino = s.destino.trim().ifBlank { "Circuito Local ${avion.baseAerea}" },
            pilotoAlMando = if (avion.baseAerea.contains("Talcahuano", ignoreCase = true)) {
                "Cap. Javier Muñoz"
            } else {
                "Cap. Rodrigo Fuentes"
            },
            nombreInvitado = s.nombreInvitado.trim(),
            rutInvitado = s.rutInvitado.trim().uppercase(),
            oficinaBanco = s.oficinaBanco.trim(),
            esSocio = s.esSocio,
            telefonoMovil = s.telefonoMovil.trim(),
            cantidadPasajeros = s.cantidadPasajeros,
            codigoVerificacion = codigoUnico
        )

        uiState = s.copy(vueloElegido = vuelo, errorFormulario = null)
        return true
    }

    fun actualizarCodigoConfirmado(codigoServidor: String) {
        val actual = uiState.vueloElegido ?: return
        val actualizado = actual.copy(codigoVerificacion = codigoServidor)
        uiState = uiState.copy(
            vueloElegido = actualizado,
            vuelosGuardados = listOf(actualizado) + uiState.vuelosGuardados.filterNot {
                it.codigoVerificacion == actual.codigoVerificacion || it.codigoVerificacion == codigoServidor
            }
        )
    }

    fun guardarVueloConfirmado() {
        val elegido = uiState.vueloElegido ?: return
        if (uiState.vuelosGuardados.none { it.codigoVerificacion == elegido.codigoVerificacion }) {
            uiState = uiState.copy(vuelosGuardados = listOf(elegido) + uiState.vuelosGuardados)
        }
    }

    private fun validarRutChileno(rutTexto: String): Boolean {
        val limpio = rutTexto.replace(".", "").replace(" ", "").uppercase()
        val partes = limpio.split("-")
        if (partes.size != 2) return false
        val cuerpo = partes[0]
        val dv = partes[1]
        if (cuerpo.length !in 7..8 || !cuerpo.all { it.isDigit() } || dv.length != 1) return false

        var suma = 0
        var mult = 2
        for (i in cuerpo.length - 1 downTo 0) {
            suma += (cuerpo[i] - '0') * mult
            mult = if (mult == 7) 2 else mult + 1
        }
        val resto = 11 - (suma % 11)
        val dvEsperado = when (resto) {
            11 -> "0"
            10 -> "K"
            else -> resto.toString()
        }
        return dv == dvEsperado
    }
}
