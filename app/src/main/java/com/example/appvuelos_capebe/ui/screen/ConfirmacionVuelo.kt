package com.example.appvuelos_capebe.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appvuelos_capebe.ui.components.CodigoQrRealCard
import com.example.appvuelos_capebe.ui.components.Footer
import com.example.appvuelos_capebe.ui.components.GoogleCalendarHelper
import com.example.appvuelos_capebe.ui.components.Header
import com.example.appvuelos_capebe.ui.viewModel.CalendarioViewModel
import com.example.appvuelos_capebe.ui.viewModel.VueloViewModel

@Composable
fun ConfirmacionVuelo(
    nombreSocio: String,
    viewModel: CalendarioViewModel,
    vueloViewModel: VueloViewModel = viewModel(),
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current
    val vuelo = viewModel.uiState.vueloElegido
    var confirmado by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { Header("PASE QR Y CONFIRMACIÓN DE VUELO") },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070D19))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(Color(0xFF10B981).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✓", fontSize = 28.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
            }

            Text(
                text = if (confirmado) "¡Vuelo y Pase QR Emitidos!" else "Revisa tu Reserva y Emite el QR",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (vuelo != null) {
                val codigoMostrar = vueloViewModel.codigoQrEmitido ?: vuelo.codigoVerificacion

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PASE DE EMBARQUE EN LOSA",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (vueloViewModel.sincronizadoEnNube) "☁ PostgreSQL OK" else "Listo para emitir",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        CodigoQrRealCard(
                            codigoVerificacion = codigoMostrar,
                            tamano = 175.dp,
                            mostrarBotonCopiar = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text("Pasajero: ${vuelo.nombreInvitado}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("RUT: ${vuelo.rutInvitado} • Socio: ${if (vuelo.esSocio) "SÍ" else "NO"}", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                            Text("Oficina Banco: ${vuelo.oficinaBanco}", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("Teléfono: ${vuelo.telefonoMovil} • Pasajeros: ${vuelo.cantidadPasajeros}/${vuelo.plazasAeronave - 1}", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Fecha: ${vuelo.fecha} • Bloque: ${vuelo.horario}", color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Base: ${vuelo.base} • Aeronave: ${vuelo.aeronave}", color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Destino: ${vuelo.destino} (Vuelo ≤ 20 min)", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }
                }

                if (!confirmado) {
                    Button(
                        onClick = {
                            vueloViewModel.crearVueloYGenerarQr(vuelo) { codigoFinal, syncOk ->
                                viewModel.actualizarCodigoConfirmado(codigoFinal)
                                viewModel.guardarVueloConfirmado()
                                confirmado = true
                                val msg = if (syncOk) {
                                    "¡Reserva y QR $codigoFinal guardados en PostgreSQL!"
                                } else {
                                    "Reserva y QR $codigoFinal emitidos (modo local)"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = !vueloViewModel.cargando,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (vueloViewModel.cargando) "Emitiendo QR en Spring Boot..." else "Confirmar Reserva y Emitir QR",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            GoogleCalendarHelper.agregarVueloAGoogleCalendar(
                                context = context,
                                fechaIso = vuelo.fecha,
                                bloqueHorario = vuelo.horario,
                                aeronave = vuelo.aeronave,
                                baseAerea = vuelo.base,
                                pasajero = vuelo.nombreInvitado,
                                codigoVerificacion = codigoMostrar,
                                detalleExtra = "RUT: ${vuelo.rutInvitado} | Oficina: ${vuelo.oficinaBanco} | Socio: ${if (vuelo.esSocio) "Sí" else "No"} | Tel: ${vuelo.telefonoMovil}"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color(0xFF070D19)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("📅 Agregar Vuelo a Google Calendar", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onNavigateHome,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Finalizar y Volver al Menú", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
