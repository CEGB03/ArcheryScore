package com.cegb03.archeryscore.data.repository

import android.util.Log
import com.cegb03.archeryscore.data.model.FatarcoArcherData
import com.cegb03.archeryscore.data.model.FatarcoVerificationResult
import com.cegb03.archeryscore.data.remote.FatarcoRetrofitInstance
import com.cegb03.archeryscore.util.FatarcoParser
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para verificar usuarios en la base de datos de FATARCO
 */
@Singleton
class FatarcoRepository @Inject constructor() {

    private val service = FatarcoRetrofitInstance.api

    /**
     * Verifica si un usuario existe en FATARCO por su DNI
     * @param dni Documento Nacional de Identidad
     * @return Resultado de la verificación
     */
    suspend fun verifyUserByDni(dni: String): FatarcoVerificationResult {
        return try {
            Log.d("ArcheryScore_Debug", "🔍 Buscando DNI: $dni en FATARCO")
            
            val response = service.getArchersPage(dni)
            
            if (response.table_data.isBlank()) {
                Log.e("ArcheryScore_Debug", "❌ HTML vacío")
                return FatarcoVerificationResult.Error("No se recibió respuesta del servidor")
            }
            
            // Parsear el HTML de la tabla
            val archers = FatarcoParser.parseArchersTable(response.table_data)
            
            Log.d("ArcheryScore_Debug", "📊 Arqueros encontrados: ${archers.size}")
            
            if (archers.isEmpty()) {
                Log.w("ArcheryScore_Debug", "⚠️ No se encontró el DNI en FATARCO")
                return FatarcoVerificationResult.NotFound
            }
            
            // Tomar el primer resultado (debería ser exacto por DNI)
            val archer = archers.first()
            Log.d("ArcheryScore_Debug", "✅ Encontrado: ${archer.nombre} - Club: ${archer.club}")
            
            FatarcoVerificationResult.Success(archer)
            
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "❌ Exception en verifyUserByDni", e)
            FatarcoVerificationResult.Error("Error: ${e.localizedMessage ?: e.message ?: "Desconocido"}")
        }
    }
    
    /**
     * Busca arqueros por nombre o club
     * @param query Nombre o club a buscar
     * @return Lista de arqueros encontrados
     */
    suspend fun searchArchers(query: String): List<FatarcoArcherData> {
        return try {
            Log.d("ArcheryScore_Debug", "🔍 Buscando: $query en FATARCO")
            
            val response = service.getArchersPage(query)
            
            if (response.table_data.isBlank()) {
                Log.e("ArcheryScore_Debug", "❌ HTML vacío")
                return emptyList()
            }
            
            val archers = FatarcoParser.parseArchersTable(response.table_data)
            Log.d("ArcheryScore_Debug", "✅ Encontrados ${archers.size} arqueros")
            
            archers
            
        } catch (e: Exception) {
            Log.e("ArcheryScore_Debug", "❌ Exception en searchArchers", e)
            emptyList()
        }
    }
}
