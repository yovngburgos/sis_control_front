package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.UserRequestDto
import com.siscontrol.mobile.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Servicio Retrofit para la gestión de usuarios según /api/usuarios.
 */
interface PersonnelApiService {

    @GET("/api/usuarios")
    suspend fun getPersonnel(@Query("requesterId") requesterId: Long? = null): List<UserResponseDto>

    @POST("/api/usuarios")
    suspend fun createPersonnel(
        @Body request: UserRequestDto,
        @Query("creatorId") creatorId: Long? = null
    ): UserResponseDto
}
