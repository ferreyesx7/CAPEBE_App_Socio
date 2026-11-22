package com.example.appvuelos_capebe.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appvuelos_capebe.model.VueloModel
import com.example.appvuelos_capebe.network.OperacionPistaCrearDto
import com.example.appvuelos_capebe.network.PaseQrDto
import com.example.appvuelos_capebe.network.RetrofitVuelo
import com.example.appvuelos_capebe.network.VueloDto
import kotlinx.coroutines.launch

class VueloViewModel : ViewModel() {

    var listaVuelos by mutableStateOf<List<VueloDto>>(emptyList())
        private set

    var cargando by mutableStateOf(false)
        private set

    var mensajeError by mutableStateOf<String?>(null)
        private set

    var ultimoVueloCreado by mutableStateOf<VueloDto?>(null)
        private set

    var codigoQrEmitido by mutableStateOf<String?>(null)
        private set

    var sincronizadoEnNube by mutableStateOf(false)
        private set

    // GET /api/vuelos - Obtener historial desde Spring Boot (:8082)
    fun cargarVuelos() {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            try {
                listaVuelos = RetrofitVuelo.api.listar()
            } catch (e: Exception) {
                mensajeError = "No se pudo conectar al servidor: ${e.localizedMessage}"
            } finally {
                cargando = false
            }
        }
    }

    // Sincroniza en los 3 microservicios: service-vuelos (:8082), service-pista (:8083) y service-qr (:8084)
    fun crearVueloYGenerarQr(
        vuelo: VueloModel,
        onExito: (codigoFinal: String, sincronizado: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            cargando = true
            mensajeError = null
            var codigoFinal = vuelo.codigoVerificacion
            var syncOk = false

            // 1. Generar Pase QR oficial en service-qr (:8084)
            try {
                val paseCreado = RetrofitVuelo.qrApi.generarQr(
                    PaseQrDto(
                        codigoQr = codigoFinal,
                        socio = vuelo.nombreInvitado,
                        rut = vuelo.rutInvitado,
                        oficinaBanco = vuelo.oficinaBanco,
                        esSocio = vuelo.esSocio,
                        telefonoMovil = vuelo.telefonoMovil,
                        cantidadPasajeros = vuelo.cantidadPasajeros,
                        plazasAeronave = vuelo.plazasAeronave,
                        fecha = vuelo.fecha,
                        horario = vuelo.horario,
                        base = vuelo.base,
                        aeronave = vuelo.aeronave,
                        destino = vuelo.destino,
                        pilotoAlMando = vuelo.pilotoAlMando
                    )
                )
                if (!paseCreado.codigoQr.isNullOrBlank()) {
                    codigoFinal = paseCreado.codigoQr
                }
                syncOk = true
            } catch (_: Exception) {}

            // 2. Registrar operación en service-pista (:8083) para que aparezca de inmediato en App Pista
            try {
                RetrofitVuelo.pistaConsultaApi.crearOperacion(
                    OperacionPistaCrearDto(
                        fecha = vuelo.fecha,
                        horario = vuelo.horario,
                        base = vuelo.base,
                        destino = vuelo.destino,
                        aeronave = vuelo.aeronave,
                        pilotoAlMando = vuelo.pilotoAlMando,
                        pasajero = vuelo.nombreInvitado,
                        cantidadPasajeros = vuelo.cantidadPasajeros,
                        codigoQr = codigoFinal,
                        estadoPista = "PENDIENTE EN LOSA"
                    )
                )
                syncOk = true
            } catch (_: Exception) {}

            // 3. Registrar vuelo en service-vuelos (:8082)
            try {
                val nuevoVuelo = VueloDto(
                    pasajero = vuelo.nombreInvitado,
                    rut = vuelo.rutInvitado,
                    oficinaBanco = vuelo.oficinaBanco,
                    esSocio = vuelo.esSocio,
                    telefonoMovil = vuelo.telefonoMovil,
                    cantidadPasajeros = vuelo.cantidadPasajeros,
                    fecha = vuelo.fecha,
                    bloqueHorario = vuelo.horario,
                    aeronave = vuelo.aeronave,
                    plazasAeronave = vuelo.plazasAeronave,
                    baseAerea = vuelo.base,
                    destino = vuelo.destino,
                    pilotoAlMando = vuelo.pilotoAlMando,
                    codigoVerificacion = codigoFinal,
                    pesoKg = 0.0,
                    horometro = 0.0
                )
                val respuesta = RetrofitVuelo.api.crear(nuevoVuelo)
                ultimoVueloCreado = respuesta
                listaVuelos = listOf(respuesta) + listaVuelos
                if (!respuesta.codigoVerificacion.isNullOrBlank()) {
                    codigoFinal = respuesta.codigoVerificacion
                }
                syncOk = true
            } catch (e: Exception) {
                if (!syncOk) {
                    mensajeError = "Guardado en modo local (${e.localizedMessage})"
                }
            } finally {
                codigoQrEmitido = codigoFinal
                sincronizadoEnNube = syncOk
                cargando = false
                onExito(codigoFinal, syncOk)
            }
        }
    }
}
