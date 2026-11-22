package com.example.appvuelos_capebe.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface VueloApi {

    @GET("api/vuelos")
    suspend fun listar(): List<VueloDto>

    @GET("api/vuelos/{id}")
    suspend fun obtener(@Path("id") id: Long): VueloDto

    @POST("api/vuelos")
    suspend fun crear(@Body vuelo: VueloDto): VueloDto

}