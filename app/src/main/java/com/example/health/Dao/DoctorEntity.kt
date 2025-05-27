package com.example.health.Dao

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.health.api.Address
import com.example.health.api.Doctor
import com.example.health.api.Fees
import com.example.health.api.Practice
import com.example.health.api.Ratings

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey val doctorId: Int,
    val doctorName: String,
    val profileUrl: String,
    val specialization: String,
    val experience: Int,
    val practiceName: String,
    val addressLine1: String,
    val addressCity: String,
    val feesAmount: Int,
    val feesCurrency: String,
    val recommendationPercent: Int,
    val patientsCount: Int,
    val imageUrl: String?
)

// Convert Doctor to DoctorEntity for Room storage
fun Doctor.toEntity() = DoctorEntity(
    doctorId = doctorId,
    doctorName = doctorName,
    profileUrl = profileUrl,
    specialization = specialization,
    experience = experience,
    practiceName = practice.name,
    addressLine1 = practice.address.line1,
    addressCity = practice.address.city,
    feesAmount = fees.amount,
    feesCurrency = fees.currency,
    recommendationPercent = ratings.recommendationPercent,
    patientsCount = ratings.patientsCount,
    imageUrl = imageUrl
)

// Convert DoctorEntity back to Doctor for use in the UI
fun DoctorEntity.toDoctor() = Doctor(
    doctorId = doctorId,
    doctorName = doctorName,
    profileUrl = profileUrl,
    specialization = specialization,
    experience = experience,
    practice = Practice(
        name = practiceName,
        address = Address(line1 = addressLine1, city = addressCity)
    ),
    fees = Fees(amount = feesAmount, currency = feesCurrency),
    ratings = Ratings(recommendationPercent = recommendationPercent, patientsCount = patientsCount),
    imageUrl = imageUrl
)