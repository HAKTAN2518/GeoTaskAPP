package com.example.geotask_app.models

data class AuthRequest(
    val eposta: String,
    val sifre: String
)

data class AuthResponse(
    val token: String
)