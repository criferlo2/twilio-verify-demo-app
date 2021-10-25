package com.hashitoapps.twiliotestcriferlo.data.model

data class TokenDeviceResponse(
    val token: String,
    val serviceSid: String,
    val identity: String,
    val factorType: String
)