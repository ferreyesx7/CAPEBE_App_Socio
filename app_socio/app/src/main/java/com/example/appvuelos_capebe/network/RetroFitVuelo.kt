package com.example.appvuelos_capebe.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class OperacionPistaCrearDto(
    val fecha: String,
    val horario: String,
    val base: String,
    val destino: String,
    val aeronave: String,
    val pilotoAlMando: String,
    val pasajero: String,
    val cantidadPasajeros: Int,
    val codigoQr: String,
    val estadoPista: String = "PENDIENTE EN LOSA"
)

interface QrSocioApi {
    @POST("api/v1/qr/generar")
    suspend fun generarQr(@Body pase: PaseQrDto): PaseQrDto
}

interface PistaConsultaApi {
    @GET("api/v1/pista/aeronaves")
    suspend fun listarAeronaves(): List<AeronaveDto>

    @POST("api/v1/pista")
    suspend fun crearOperacion(@Body operacion: OperacionPistaCrearDto): Map<String, Any>
}

object RetrofitVuelo {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val cliente = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private var cachedHost: String = ""
    private var cachedApi: VueloApi? = null
    private var cachedPistaApi: PistaConsultaApi? = null
    private var cachedQrApi: QrSocioApi? = null

    private fun asegurarClientes() {
        val host = ServerConfig.HOST
        if (host != cachedHost || cachedApi == null) {
            cachedHost = host
            cachedApi = Retrofit.Builder()
                .baseUrl(ServerConfig.url(8082))
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VueloApi::class.java)

            cachedPistaApi = Retrofit.Builder()
                .baseUrl(ServerConfig.url(8083))
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PistaConsultaApi::class.java)

            cachedQrApi = Retrofit.Builder()
                .baseUrl(ServerConfig.url(8084))
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(QrSocioApi::class.java)
        }
    }

    val api: VueloApi
        get() {
            asegurarClientes()
            return cachedApi!!
        }

    val pistaConsultaApi: PistaConsultaApi
        get() {
            asegurarClientes()
            return cachedPistaApi!!
        }

    val qrApi: QrSocioApi
        get() {
            asegurarClientes()
            return cachedQrApi!!
        }
}
