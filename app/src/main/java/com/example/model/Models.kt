package com.example.model

enum class Language(val code: String, val label: String, val nativeName: String) {
    ENGLISH("en", "EN", "English"),
    HINDI("hi", "HI", "हिंदी"),
    MARATHI("mr", "MR", "मराठी")
}

enum class RiskLevel(val label: String, val scoreMin: Int) {
    CRITICAL("Critical", 75),
    HIGH("High", 50),
    MODERATE("Moderate", 25),
    SAFE("Safe Zone", 0)
}

enum class FacilityType {
    HOSPITAL,
    SHELTER
}

enum class SosStatus {
    ACTIVE,
    DISPATCHED,
    RESOLVED
}

enum class AgentType(val displayName: String, val role: String) {
    COORDINATOR("Coordinator Agent", "Central Emergency Orchestrator"),
    HAZARD("Hazard Agent", "Hydrology & Sensor Analytics"),
    GUARDIAN("Guardian Agent", "GPS Proximity & Vulnerability"),
    IMPACT("Impact Agent", "Cascading Grid & Route Modeling"),
    HOSPITAL("Hospital Agent", "Medical Logistics & Triage")
}

data class DisasterScenario(
    val id: String,
    val title: String,
    val titleHi: String,
    val titleMr: String,
    val subtitle: String,
    val rainfallIntensityMmPerHour: Int,
    val riverLevelChangeMeters: Double,
    val populationExposed: Int,
    val hazardSeverity: Int,
    val isSimulation: Boolean = true,
    val epicenterName: String,
    val epicenterLat: Double,
    val epicenterLng: Double,
    val description: String,
    val defaultCascadeLevel: Int
)

data class CascadingStep(
    val id: String,
    val title: String,
    val timeLabel: String,
    val status: String,
    val iconKey: String,
    val description: String,
    val isTrigger: Boolean = false,
    val isCurrentActive: Boolean = false,
    val isPredicted: Boolean = false
)

data class ImpactFactors(
    val hazardSeverity: Int,       // 0-100 (25% weight)
    val populationExposure: Int,   // 0-100 (25% weight)
    val infrastructureRisk: Int,   // 0-100 (20% weight)
    val cascadeRisk: Int,          // 0-100 (15% weight)
    val responseDifficulty: Int,   // 0-100 (15% weight)
    val overallScore: Int
)

data class EmergencyRecommendation(
    val id: String,
    val priority: Int,
    val title: String,
    val action: String,
    val reason: String,
    val riskReductionPercent: Int,
    val populationProtected: Int,
    val estimatedTimeSavedMinutes: Int,
    val isDispatched: Boolean = false
)

data class HazardZone(
    val id: String,
    val name: String,
    val hazardType: String,
    val riskLevel: RiskLevel,
    val lat: Double,
    val lng: Double,
    val radiusKm: Double,
    val intensityScore: Int,
    val description: String
)

data class EmergencyFacility(
    val id: String,
    val name: String,
    val type: FacilityType,
    val lat: Double,
    val lng: Double,
    val totalBeds: Int,
    val availableBeds: Int,
    val distanceKm: Double,
    val contactNumber: String,
    val isAccessible: Boolean = true,
    val address: String
)

data class SosRecord(
    val id: String,
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
    val status: SosStatus,
    val medicalNeeds: String,
    val message: String,
    val nearestHospital: String = "City Civil Hospital",
    val recommendedAction: String = "Immediate boat extraction to Sector 4 Relief Camp"
)

data class AgentInsight(
    val agentType: AgentType,
    val status: String,
    val analysisText: String,
    val confidenceScore: Int,
    val keyMetric: String
)

data class ChatMessage(
    val id: String,
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUser: Boolean = false,
    val agentInsights: List<AgentInsight> = emptyList(),
    val recommendations: List<EmergencyRecommendation> = emptyList()
)
