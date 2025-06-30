package com.example.spring_boot_learning.controller.utils

import java.time.Instant

data class GenericResponse(
    val status: Int,
    val message: String,
    val data: Any? = null,
    val timestamp: Instant = Instant.now()
)

