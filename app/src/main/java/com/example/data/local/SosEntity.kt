package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.SosRecord
import com.example.model.SosStatus

@Entity(tableName = "sos_records")
data class SosEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val phone: String,
    val lat: Double,
    val lng: Double,
    val locationName: String,
    val riskScore: Int,
    val hazardType: String,
    val timestamp: Long,
    val priority: String,
    val status: String,
    val medicalNeeds: String,
    val message: String,
    val nearestHospital: String,
    val recommendedAction: String
) {
    fun toSosRecord(): SosRecord = SosRecord(
        id = id,
        userId = userId,
        userName = userName,
        phone = phone,
        lat = lat,
        lng = lng,
        locationName = locationName,
        riskScore = riskScore,
        hazardType = hazardType,
        timestamp = timestamp,
        priority = priority,
        status = try { SosStatus.valueOf(status) } catch (e: Exception) { SosStatus.ACTIVE },
        medicalNeeds = medicalNeeds,
        message = message,
        nearestHospital = nearestHospital,
        recommendedAction = recommendedAction
    )

    companion object {
        fun fromSosRecord(record: SosRecord): SosEntity = SosEntity(
            id = record.id,
            userId = record.userId,
            userName = record.userName,
            phone = record.phone,
            lat = record.lat,
            lng = record.lng,
            locationName = record.locationName,
            riskScore = record.riskScore,
            hazardType = record.hazardType,
            timestamp = record.timestamp,
            priority = record.priority,
            status = record.status.name,
            medicalNeeds = record.medicalNeeds,
            message = record.message,
            nearestHospital = record.nearestHospital,
            recommendedAction = record.recommendedAction
        )
    }
}
