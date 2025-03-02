package com.example.health.api

data class Doctor(
    val doctorId: Int,
    val doctorName: String,
    val profileUrl: String,
    val specialization: String,
    val experience: Int,
    val practice: Practice,
    val fees: Fees,
    val ratings: Ratings,
    val imageUrl: String
)

data class Practice(
    val name: String,
    val address: Address
)

data class Address(
    val line1: String,
    val city: String
)

data class Fees(
    val amount: Int,
    val currency: String
)

data class Ratings(
    val recommendationPercent: Int,
    val patientsCount: Int
)
