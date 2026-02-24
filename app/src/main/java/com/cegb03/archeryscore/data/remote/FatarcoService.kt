package com.cegb03.archeryscore.data.remote

import com.cegb03.archeryscore.data.model.FatarcoSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Servicio Retrofit para consultar el endpoint AJAX de FATARCO
 */
interface FatarcoService {
    
    /**
     * Busca un arquero en la base de datos de FATARCO usando el endpoint AJAX
     * @param query Puede ser DNI, nombre o club
     */
    @GET("arqueros/action")
    suspend fun getArchersPage(
        @Query("query") query: String
    ): FatarcoSearchResponse
}
