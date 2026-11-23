package com.example.appvuelos_capebe.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appvuelos_capebe.data.local.CuentaVuelosStore
import com.example.appvuelos_capebe.ui.components.CodigoQrRealCard
import com.example.appvuelos_capebe.ui.components.Footer
import com.example.appvuelos_capebe.ui.components.GoogleCalendarHelper
import com.example.appvuelos_capebe.ui.components.Header
import com.example.appvuelos_capebe.ui.viewModel.CalendarioViewModel
import com.example.appvuelos_capebe.ui.viewModel.VueloViewModel

@Composable
fun MisVuelosScreen(
    nombreSocio: String = "",
    viewModel: CalendarioViewModel,
    vueloViewModel: VueloViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(nombreSocio) {
        vueloViewModel.cargarVuelos()
    }

    val codigosDeEstaCuenta = remember(nombreSocio, vueloViewModel.listaVuelos, viewModel.uiState.vuelosGuardados) {
        CuentaVuelosStore.obtenerCodigosDeCuenta(context, nombreSocio)
    }

    val vuelosRemotos = vueloViewModel.listaVuelos.filter { vuelo ->
        val codigo = (vuelo.codigoVerificacion ?: "").trim().uppercase()
        val porCodigo = codigo.isNotBlank() && codigosDeEstaCuenta.contains(codigo)
        val porNombre = nombreSocio.isNotBlank() &&
            nombreSocio != "Socio CAPEBE" &&
            vuelo.pasajero.trim().equals(nombreSocio.trim(), ignoreCase = true)
        porCodigo || porNombre
    }

    val vuelosLocales = viewModel.uiState.vuelosGuardados.filter { vuelo ->
        val codigo = vuelo.codigoVerificacion.trim().uppercase()
        val porCodigo = codigo.isNotBlank() && codigosDeEstaCuenta.contains(codigo)
        val porNombre = nombreSocio.isNotBlank() &&
            nombreSocio != "Socio CAPEBE" &&
            vuelo.nombreInvitado.trim().equals(nombreSocio.trim(), ignoreCase = true)
        porCodigo || porNombre
    }

    Scaffold(
        topBar = { Header("MIS VUELOS Y PASES QR") },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070D19))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (nombreSocio.isNotBlank()) {
                Text(
                    text = "Cuenta activa: $nombreSocio",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (vueloViewModel.cargando) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF38BDF8),
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Consultando Spring Boot...", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }

            vueloViewModel.mensajeError?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF451A1A)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Text(
                        text = "Servidor no alcanzado (Modo local activo)",
                        color = Color(0xFFFCA5A5),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (vuelosRemotos.isNotEmpty()) {
                vuelosRemotos.forEach { vuelo ->
                    val codigo = vuelo.codigoVerificacion ?: "CAPEBE-SCTB-${vuelo.id ?: 1001}"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                        border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("VUELO #${vuelo.id ?: 0}", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 15.sp)
                                Text("☁ PostgreSQL OK", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            CodigoQrRealCard(codigoVerificacion = codigo, tamano = 125.dp, mostrarBotonCopiar = true)
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("Pasajero: ${vuelo.pasajero}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                if (vuelo.rut.isNotBlank()) {
                                    Text("RUT: ${vuelo.rut} • Socio: ${if (vuelo.esSocio) "SÍ" else "NO"}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                                }
                                if (vuelo.oficinaBanco.isNotBlank()) {
                                    Text("Oficina: ${vuelo.oficinaBanco} • Tel: ${vuelo.telefonoMovil}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                }
                                Text("Fecha: ${vuelo.fecha.ifBlank { "Fin de semana" }} • ${vuelo.bloqueHorario.ifBlank { "10:00 - 11:00" }}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                                Text("Aeronave: ${vuelo.aeronave} (${vuelo.cantidadPasajeros} pax)", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                Text("Base Aérea: ${vuelo.baseAerea}", color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    GoogleCalendarHelper.agregarVueloAGoogleCalendar(
                                        context = context,
                                        fechaIso = vuelo.fecha.ifBlank { "2026-11-21" },
                                        bloqueHorario = vuelo.bloqueHorario.ifBlank { "10:00 - 11:00" },
                                        aeronave = vuelo.aeronave,
                                        baseAerea = vuelo.baseAerea,
                                        pasajero = vuelo.pasajero,
                                        codigoVerificacion = codigo
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📅 Agregar a Google Calendar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (vuelosLocales.isNotEmpty()) {
                vuelosLocales.forEach { vuelo ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                        border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CodigoQrRealCard(codigoVerificacion = vuelo.codigoVerificacion, tamano = 125.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("${vuelo.fecha} • ${vuelo.horario}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Pasajero: ${vuelo.nombreInvitado} (${vuelo.rutInvitado})", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                                Text("Oficina: ${vuelo.oficinaBanco} • Socio: ${if (vuelo.esSocio) "SÍ" else "NO"}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                Text("Base: ${vuelo.base} • ${vuelo.aeronave}", color = Color(0xFFF59E0B), fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    GoogleCalendarHelper.agregarVueloAGoogleCalendar(
                                        context = context,
                                        fechaIso = vuelo.fecha,
                                        bloqueHorario = vuelo.horario,
                                        aeronave = vuelo.aeronave,
                                        baseAerea = vuelo.base,
                                        pasajero = vuelo.nombreInvitado,
                                        codigoVerificacion = vuelo.codigoVerificacion
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📅 Agregar a Google Calendar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (!vueloViewModel.cargando) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("No tienes vuelos registrados aún.", style = MaterialTheme.typography.bodyLarge, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Agenda un vuelo desde el Calendario de Planificación.", color = Color(0xFF38BDF8), fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onNavigateBack() },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Volver al Menú", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
            }
        }
    }
}
