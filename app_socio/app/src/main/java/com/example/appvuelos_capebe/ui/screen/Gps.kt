package com.example.appvuelos_capebe.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.tan

@SuppressLint("MissingPermission")
@Composable
fun TarjetaGps() {
    val context = LocalContext.current

    var latitud by remember { mutableStateOf(-33.4552) }
    var longitud by remember { mutableStateOf(-70.5476) }
    var baseSeleccionada by remember { mutableStateOf("Aeródromo Tobalaba (SCTB)") }

    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcherPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermiso = concedido
    }

    LaunchedEffect(Unit) {
        if (!tienePermiso) {
            launcherPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    DisposableEffect(tienePermiso) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                latitud = location.latitude
                longitud = location.longitude
                baseSeleccionada = "Ubicación GPS en Vivo"
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (tienePermiso) {
            try {
                val ultimaUbicacion = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (ultimaUbicacion != null) {
                    latitud = ultimaUbicacion.latitude
                    longitud = ultimaUbicacion.longitude
                    baseSeleccionada = "Ubicación GPS del Dispositivo"
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 1f, listener)
            } catch (_: Exception) {}
        }

        onDispose {
            try {
                locationManager.removeUpdates(listener)
            } catch (_: Exception) {}
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13223E)),
        border = BorderStroke(1.dp, Color(0xFF1E3A5F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GPS y Mapa de Vuelo",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = baseSeleccionada,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF38BDF8)
            )

            Spacer(modifier = Modifier.height(10.dp))

            MapaCuadriculaSimple(lat = latitud, lon = longitud)

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "LATITUD: $latitud", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = "LONGITUD: $longitud", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        latitud = -33.4552
                        longitud = -70.5476
                        baseSeleccionada = "Base Tobalaba (SCTB)"
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tobalaba", fontSize = 12.sp, color = Color(0xFF38BDF8))
                }

                OutlinedButton(
                    onClick = {
                        latitud = -36.7726
                        longitud = -73.0631
                        baseSeleccionada = "Base Talcahuano (SCIE)"
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Talcahuano", fontSize = 12.sp, color = Color(0xFFF59E0B))
                }
            }
        }
    }
}

@Composable
fun MapaCuadriculaSimple(lat: Double, lon: Double) {
    val zoom = 15
    val n = 2.0.pow(zoom.toDouble())
    val tileX = floor((lon + 180.0) / 360.0 * n).toInt()
    val latRad = lat * PI / 180.0
    val tileY = floor((1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n).toInt()

    val imagenes = remember { mutableStateMapOf<String, ImageBitmap>() }

    LaunchedEffect(tileX, tileY) {
        for (dy in -1..1) {
            for (dx in -1..1) {
                val x = tileX + dx
                val y = tileY + dy
                val clave = "$zoom/$x/$y"
                if (!imagenes.containsKey(clave)) {
                    withContext(Dispatchers.IO) {
                        try {
                            val url = URL("https://tile.openstreetmap.org/$clave.png")
                            val conn = url.openConnection() as HttpURLConnection
                            conn.connectTimeout = 3000
                            conn.readTimeout = 3000
                            conn.setRequestProperty("User-Agent", "AppSocioCAPEBE")
                            conn.inputStream.use { stream ->
                                val bmp = BitmapFactory.decodeStream(stream)
                                if (bmp != null) {
                                    imagenes[clave] = bmp.asImageBitmap()
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0A192F)),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (dy in -1..1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (dx in -1..1) {
                        val clave = "$zoom/${tileX + dx}/${tileY + dy}"
                        val img = imagenes[clave]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        ) {
                            if (img != null) {
                                Image(
                                    bitmap = img,
                                    contentDescription = "Mapa",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(3.dp)
                .clip(CircleShape)
                .background(Color(0xFF0284C7))
        )
    }
}