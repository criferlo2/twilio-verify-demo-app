package com.hashitoapps.twiliotestcriferlo.data.repository

import com.hashitoapps.twiliotestcriferlo.data.model.*
import retrofit2.http.Body
import retrofit2.http.POST

interface IRemoteRepository {
    @POST("/api/login")
    suspend fun login(@Body body: LoggedInUser): LoggedInUserResponse

    @POST("/api/devices/token")
    suspend fun tokenDevice(@Body body: TokenDevice): TokenDeviceResponse

    @POST("/api/devices/register")
    suspend fun registerDevice(@Body body: RegisterDevice): RegisterDeviceResponse
}