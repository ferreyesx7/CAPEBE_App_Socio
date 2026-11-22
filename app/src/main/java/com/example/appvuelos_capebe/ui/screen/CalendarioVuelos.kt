package com.example.appvuelos_capebe.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appvuelos_capebe.ui.components.CalendarioMensualCapebe
import com.example.appvuelos_capebe.ui.components.Footer
import com.example.appvuelos_capebe.ui.components.Header
import com.example.appvuelos_capebe.ui.viewModel.CalendarioViewModel

@Composable
fun CalendarioVuelos(
    nombreSocio: String = "",
    onNavigateBack: () -> Unit,
    onNavigateToConfirmacion: () -> Unit,
    viewModel: CalendarioViewModel = viewModel()
) {
    val state = viewModel.uiState

    LaunchedEffect(nombreSocio) {
        viewModel.prellenarNombreSiVacio(nombreSocio)
        viewModel.cargarFlotaDesdeServidor()
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFF38BDF8),
        unfocusedBorderColor = Color(0xFF1E3A5F),
        focusedLabelColor = Color(0xFF38BDF8),
        unfocusedLabelColor = Color(0xFF94A3B8),
        cursorColor = Color(0xFF38BDF8)
    )

    Scaffold(
        topBar = { Header("CALENDARIO DE VUELOS CAPEBE") },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF070D19))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "1. Selecciona fecha (Sábados, Domingos y Festivos)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )

            CalendarioMensualCapebe(
                fechaSeleccionada = state.fechaSeleccionada,
                fechasConVuelos = state.vuelosGuardados.map { it.fecha }.toSet(),
                soloFinesDeSemanaYFestivos = true,
                onFechaSeleccionada = { viewModel.onFechaSeleccionada(it) }
            )

            Text(
                text = "2. Bloque horario de 1 hora (tiempo efectivo de vuelo ≤ 20 min)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )

            viewModel.bloquesHorarios.chunked(2).forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fila.forEach { bloque ->
                        val activo = state.bloqueSeleccionado == bloque
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.onBloqueSeleccionado(bloque) },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (activo) Color(0xFF0284C7) else Color(0xFF13223E)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (activo) Color(0xFF38BDF8) else Color(0xFF1E3A5F)
                            )
                        ) {
                            Text(
                                text = bloque,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "3. Elige Aeronave (4 plazas = máx. 3 pax • 6 plazas = máx. 5 pax)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )

            state.aeronaves.forEach { avion ->
                val seleccionada = state.aeronaveSeleccionada?.matricula == avion.matricula
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onAeronaveSeleccionada(avion) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            avion.enMantenimiento -> Color(0xFF2B1317)
                            seleccionada -> Color(0xFF1E3A5F)
                            else -> Color(0xFF13223E)
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        when {
                            avion.enMantenimiento -> Color(0xFFEF4444)
                            seleccionada -> Color(0xFF38BDF8)
                            else -> Color(0xFF1E3A5F)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = avion.etiqueta,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (avion.enMantenimiento) "FUERA DE VUELO (MANT.)" else "Máx. ${avion.asientosDisponibles} pasajeros",
                                color = if (avion.enMantenimiento) Color(0xFFF87171) else Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Base: ${avion.baseAerea} • ${avion.plazasTotales} plazas (1 piloto + ${avion.asientosDisponibles} asientos)",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Horómetro actual: ${String.format("%.1f", avion.horometroActual)} / ${String.format("%.1f", avion.horometroMaxMantenimiento)} hrs DGAC",
                            color = Color(0xFFF59E0B),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Text(
                text = "4. Registro Obligatorio de Pasajero / Invitado",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
                border = BorderStroke(1.dp, Color(0xFF1E3A5F))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = state.nombreInvitado,
                        onValueChange = { viewModel.onNombreInvitadoChanged(it) },
                        label = { Text("Nombres y Apellidos") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = state.rutInvitado,
                        onValueChange = { viewModel.onRutInvitadoChanged(it) },
                        label = { Text("RUT (ej: 18.452.310-K)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = state.oficinaBanco,
                        onValueChange = { viewModel.onOficinaBancoChanged(it) },
                        label = { Text("Oficina BancoEstado") },
                        placeholder = { Text("Ej: Casa Matriz Alameda") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = state.telefonoMovil,
                        onValueChange = { viewModel.onTelefonoMovilChanged(it) },
                        label = { Text("Teléfono Móvil Principal") },
                        placeholder = { Text("+56987654321") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("¿Calidad de Socio CAPEBE?", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (state.esSocio) "Sí — Socio titular del club" else "No — Invitado externo",
                                color = if (state.esSocio) Color(0xFF10B981) else Color(0xFFF59E0B),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = state.esSocio,
                            onCheckedChange = { viewModel.onEsSocioChanged(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF0284C7)
                            )
                        )
                    }

                    val maxPax = state.aeronaveSeleccionada?.asientosDisponibles ?: 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Cantidad de Pasajeros", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("Disponible en cabina: 1 a $maxPax", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.onCantidadPasajerosChanged(state.cantidadPasajeros - 1) },
                                enabled = state.cantidadPasajeros > 1
                            ) {
                                Text("-", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "${state.cantidadPasajeros}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            OutlinedButton(
                                onClick = { viewModel.onCantidadPasajerosChanged(state.cantidadPasajeros + 1) },
                                enabled = state.cantidadPasajeros < maxPax
                            ) {
                                Text("+", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = state.destino,
                        onValueChange = { viewModel.onDestinoChanged(it) },
                        label = { Text("Destino / Circuito") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                }
            }

            state.errorFormulario?.let { err ->
                Text(
                    text = err,
                    color = Color(0xFFF87171),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = {
                    if (viewModel.validarYPrepararReserva()) {
                        onNavigateToConfirmacion()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0284C7),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Continuar a Términos y Generar QR", fontWeight = FontWeight.Bold)
            }

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
