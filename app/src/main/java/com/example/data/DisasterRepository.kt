package com.example.data

import com.example.model.*
import kotlin.math.*

object DisasterRepository {

    val scenarios = listOf(
        DisasterScenario(
            id = "scenario_trimbak_cloudburst",
            title = "Trimbakeshwar Cloudburst",
            titleHi = "त्र्यंबकेश्वर बादल फटना (क्लाउडबर्स्ट)",
            titleMr = "त्र्यंबकेश्वर ढगफुटी (क्लाऊडबर्स्ट)",
            subtitle = "Extreme flash rainfall in Brahmagiri hills triggering immediate Godavari surge",
            rainfallIntensityMmPerHour = 185,
            riverLevelChangeMeters = 4.2,
            populationExposed = 78000,
            hazardSeverity = 92,
            epicenterName = "Trimbakeshwar Valley / Brahmagiri",
            epicenterLat = 19.9328,
            epicenterLng = 73.5307,
            description = "Intense localized cloudburst with 185mm/h precipitation causing flash flooding, road cutoffs to temple corridors, and rapid downstream flood pulse towards Nashik.",
            defaultCascadeLevel = 3
        ),
        DisasterScenario(
            id = "scenario_gangapur_dam",
            title = "Gangapur Dam Release",
            titleHi = "गंगापुर बांध जल विसर्जन",
            titleMr = "गंगापूर धरण विसर्ग",
            subtitle = "Controlled emergency spillway discharge of 45,000 cusecs into Godavari River",
            rainfallIntensityMmPerHour = 95,
            riverLevelChangeMeters = 3.8,
            populationExposed = 120000,
            hazardSeverity = 85,
            epicenterName = "Gangapur Dam Reservoir",
            epicenterLat = 20.0333,
            epicenterLng = 73.6833,
            description = "High inflow forced gates opening at 45,000 cusecs. Flood warning for Ramkund, Goda Ghat, Panchavati, and submerged low-lying vehicular bridges.",
            defaultCascadeLevel = 2
        ),
        DisasterScenario(
            id = "scenario_normal_recovery",
            title = "Normal Recovery",
            titleHi = "सामान्य स्थिति बहाली (रिकवरी)",
            titleMr = "सामान्य पूर्ववत स्थिती (रिकव्हरी)",
            subtitle = "Precipitation subsided, drainage normalized, de-escalated emergency posture",
            rainfallIntensityMmPerHour = 12,
            riverLevelChangeMeters = 0.4,
            populationExposed = 2500,
            hazardSeverity = 18,
            epicenterName = "Nashik Metropolitan Area",
            epicenterLat = 19.9975,
            epicenterLng = 73.7898,
            description = "River levels returning to safe mark (+0.4m). Waterlogged sectors cleared, traffic re-routed safely, essential supply chains operational.",
            defaultCascadeLevel = 0
        )
    )

    fun getInitialFacilities(): List<EmergencyFacility> = listOf(
        EmergencyFacility(
            id = "hosp_1",
            name = "Nashik District Civil Hospital",
            type = FacilityType.HOSPITAL,
            lat = 19.9982,
            lng = 73.7845,
            totalBeds = 450,
            availableBeds = 68,
            distanceKm = 1.4,
            contactNumber = "+91 253 257 2341",
            isAccessible = true,
            address = "Trimbak Road, Old Agra Highway, Nashik"
        ),
        EmergencyFacility(
            id = "hosp_2",
            name = "KEM Multi-Specialty Disaster Ward",
            type = FacilityType.HOSPITAL,
            lat = 19.9854,
            lng = 73.7621,
            totalBeds = 320,
            availableBeds = 24,
            distanceKm = 2.8,
            contactNumber = "+91 253 239 8800",
            isAccessible = true,
            address = "Sector 8, Gangapur Link Road"
        ),
        EmergencyFacility(
            id = "hosp_3",
            name = "Lifeline Emergency Trauma Center",
            type = FacilityType.HOSPITAL,
            lat = 20.0125,
            lng = 73.7950,
            totalBeds = 200,
            availableBeds = 11,
            distanceKm = 3.6,
            contactNumber = "+91 253 250 1199",
            isAccessible = false,
            address = "Panchavati Ghat Access Road"
        ),
        EmergencyFacility(
            id = "shelter_1",
            name = "Relief Camp Alpha (Municipal School 14)",
            type = FacilityType.SHELTER,
            lat = 20.0045,
            lng = 73.7710,
            totalBeds = 1200,
            availableBeds = 450,
            distanceKm = 0.9,
            contactNumber = "1077 (Disaster Helpline)",
            isAccessible = true,
            address = "Shivaji Nagar Elevated Complex"
        ),
        EmergencyFacility(
            id = "shelter_2",
            name = "Community Shelter Beta (Indoor Stadium)",
            type = FacilityType.SHELTER,
            lat = 19.9890,
            lng = 73.7980,
            totalBeds = 2500,
            availableBeds = 1120,
            distanceKm = 2.1,
            contactNumber = "0253-222211",
            isAccessible = true,
            address = "Mahatma Nagar Sports Complex"
        )
    )

    fun getInitialHazardZones(scenario: DisasterScenario): List<HazardZone> {
        return when (scenario.id) {
            "scenario_trimbak_cloudburst" -> listOf(
                HazardZone(
                    id = "hz_1",
                    name = "Brahmagiri Flash Flood Corridor",
                    hazardType = "Cloudburst Torrent",
                    riskLevel = RiskLevel.CRITICAL,
                    lat = 19.9350,
                    lng = 73.5320,
                    radiusKm = 3.5,
                    intensityScore = 94,
                    description = "Torrential runoff over 185mm/h velocity with mudflow on slope roads."
                ),
                HazardZone(
                    id = "hz_2",
                    name = "Trimbak Highway Inundation Zone",
                    hazardType = "Submerged Arterial Road",
                    riskLevel = RiskLevel.HIGH,
                    lat = 19.9650,
                    lng = 73.6200,
                    radiusKm = 2.8,
                    intensityScore = 78,
                    description = "Waterlogged highway cutting off emergency ambulance connectivity to city center."
                ),
                HazardZone(
                    id = "hz_3",
                    name = "Downstream Buffer Zone",
                    hazardType = "River Surcharge",
                    riskLevel = RiskLevel.MODERATE,
                    lat = 19.9850,
                    lng = 73.7100,
                    radiusKm = 4.2,
                    intensityScore = 45,
                    description = "Surging discharge reaching secondary canals."
                )
            )
            "scenario_gangapur_dam" -> listOf(
                HazardZone(
                    id = "hz_4",
                    name = "Ramkund - Goda Ghat Basin",
                    hazardType = "Riverine Overflow",
                    riskLevel = RiskLevel.CRITICAL,
                    lat = 20.0070,
                    lng = 73.7910,
                    radiusKm = 2.4,
                    intensityScore = 91,
                    description = "Water levels touching danger threshold (+3.8m). Historic temples and bridges fully submerged."
                ),
                HazardZone(
                    id = "hz_5",
                    name = "Panchavati Lowland Residential",
                    hazardType = "Urban Flooding",
                    riskLevel = RiskLevel.HIGH,
                    lat = 20.0120,
                    lng = 73.7990,
                    radiusKm = 3.1,
                    intensityScore = 82,
                    description = "Inundation of 1st floor houses, power substations powered down for safety."
                ),
                HazardZone(
                    id = "hz_6",
                    name = "Elevated CIDCO Safe Plateau",
                    hazardType = "Evacuation Haven",
                    riskLevel = RiskLevel.SAFE,
                    lat = 19.9700,
                    lng = 73.7600,
                    radiusKm = 5.0,
                    intensityScore = 12,
                    description = "High ground designated for temporary field hospitals and supply staging."
                )
            )
            else -> listOf(
                HazardZone(
                    id = "hz_7",
                    name = "Municipal Drainage Sector",
                    hazardType = "Minor Waterlogging",
                    riskLevel = RiskLevel.MODERATE,
                    lat = 19.9975,
                    lng = 73.7898,
                    radiusKm = 1.5,
                    intensityScore = 24,
                    description = "Normal drainage operations in progress. All arterial routes open."
                )
            )
        }
    }

    fun calculateImpactFactors(scenario: DisasterScenario): ImpactFactors {
        val hazard = scenario.hazardSeverity
        val popExp = ((scenario.populationExposed.toDouble() / 150000.0) * 100.0).toInt().coerceIn(10, 100)
        val infra = when {
            scenario.rainfallIntensityMmPerHour > 120 -> 88
            scenario.rainfallIntensityMmPerHour > 60 -> 72
            else -> 25
        }
        val cascade = when (scenario.defaultCascadeLevel) {
            3 -> 92
            2 -> 76
            1 -> 45
            else -> 15
        }
        val difficulty = when {
            hazard > 80 -> 85
            hazard > 50 -> 65
            else -> 20
        }

        val overall = (
            (hazard * 0.25) +
            (popExp * 0.25) +
            (infra * 0.20) +
            (cascade * 0.15) +
            (difficulty * 0.15)
        ).roundToInt().coerceIn(0, 100)

        return ImpactFactors(
            hazardSeverity = hazard,
            populationExposure = popExp,
            infrastructureRisk = infra,
            cascadeRisk = cascade,
            responseDifficulty = difficulty,
            overallScore = overall
        )
    }

    fun calculatePersonalRisk(
        userLat: Double,
        userLng: Double,
        scenario: DisasterScenario,
        nearestHazardDistanceKm: Double
    ): Pair<Int, RiskLevel> {
        val hazardWeight = scenario.hazardSeverity * 0.35
        
        // Proximity score: if < 1.0 km -> 100, if 5 km -> 20, if > 10 km -> 5
        val proximityScore = when {
            nearestHazardDistanceKm <= 0.8 -> 98.0
            nearestHazardDistanceKm <= 2.0 -> 82.0
            nearestHazardDistanceKm <= 4.0 -> 60.0
            nearestHazardDistanceKm <= 8.0 -> 35.0
            else -> 10.0
        }
        val proximityWeight = proximityScore * 0.35
        
        val popWeight = (scenario.populationExposed / 1500.0).coerceIn(10.0, 100.0) * 0.15
        val cascadeWeight = (scenario.defaultCascadeLevel * 30.0).coerceIn(10.0, 100.0) * 0.15

        val totalRiskScore = (hazardWeight + proximityWeight + popWeight + cascadeWeight).roundToInt().coerceIn(5, 99)

        val riskLevel = when {
            totalRiskScore >= 75 -> RiskLevel.CRITICAL
            totalRiskScore >= 50 -> RiskLevel.HIGH
            totalRiskScore >= 25 -> RiskLevel.MODERATE
            else -> RiskLevel.SAFE
        }

        return Pair(totalRiskScore, riskLevel)
    }

    fun getCascadingSteps(scenario: DisasterScenario): List<CascadingStep> {
        return when (scenario.id) {
            "scenario_trimbak_cloudburst" -> listOf(
                CascadingStep(
                    id = "cs_1",
                    title = "Cloudburst Trigger",
                    timeLabel = "T-12h",
                    status = "Initiating Event",
                    iconKey = "thunderstorm",
                    description = "185mm/h precipitation burst over Brahmagiri watershed",
                    isTrigger = true
                ),
                CascadingStep(
                    id = "cs_2",
                    title = "Urban Flash Flood",
                    timeLabel = "T-4h",
                    status = "Active Surge",
                    iconKey = "flood",
                    description = "Flash runoff entering low-lying municipal sectors and ghats"
                ),
                CascadingStep(
                    id = "cs_3",
                    title = "Road Closure",
                    timeLabel = "Current",
                    status = "Critical Bottleneck",
                    iconKey = "block",
                    description = "Trimbak-Nashik state highway impassable under 1.4m standing water",
                    isCurrentActive = true
                ),
                CascadingStep(
                    id = "cs_4",
                    title = "Hospital Accessibility Reduced",
                    timeLabel = "Est. T+2h",
                    status = "Predicted Impact",
                    iconKey = "local_hospital",
                    description = "Lifeline Emergency Trauma Center isolated; only boat access possible",
                    isPredicted = true
                ),
                CascadingStep(
                    id = "cs_5",
                    title = "Population Exposure Spreading",
                    timeLabel = "Est. T+6h",
                    status = "High Vulnerability",
                    iconKey = "groups",
                    description = "Estimated 78,000 citizens in evacuation perimeter requiring shelter",
                    isPredicted = true
                )
            )
            "scenario_gangapur_dam" -> listOf(
                CascadingStep(
                    id = "cs_1",
                    title = "Emergency Dam Discharge",
                    timeLabel = "T-8h",
                    status = "Discharge Trigger",
                    iconKey = "water_damage",
                    description = "45,000 cusecs spillway release to protect reservoir safety",
                    isTrigger = true
                ),
                CascadingStep(
                    id = "cs_2",
                    title = "Godavari Riverbed Inundation",
                    timeLabel = "T-3h",
                    status = "Active Surge",
                    iconKey = "waves",
                    description = "Water levels crossing +3.8m above flood datum at Ramkund"
                ),
                CascadingStep(
                    id = "cs_3",
                    title = "Power Grid De-energization",
                    timeLabel = "Current",
                    status = "Critical Risk",
                    iconKey = "flash_off",
                    description = "Panchavati 33kV substation shutdown to prevent electrocution hazards",
                    isCurrentActive = true
                ),
                CascadingStep(
                    id = "cs_4",
                    title = "Water Purification Interruption",
                    timeLabel = "Est. T+3h",
                    status = "Predicted Impact",
                    iconKey = "water_drop",
                    description = "Sediment saturation impacting Municipal Water Treatment Facility",
                    isPredicted = true
                )
            )
            else -> listOf(
                CascadingStep(
                    id = "cs_norm_1",
                    title = "Precipitation Subsiding",
                    timeLabel = "T-6h",
                    status = "Normalized",
                    iconKey = "sunny",
                    description = "Rainfall dropped to 12mm/h, storm front cleared"
                ),
                CascadingStep(
                    id = "cs_norm_2",
                    title = "Drainage De-clogging",
                    timeLabel = "Current",
                    status = "Active Cleanup",
                    iconKey = "cleaning_services",
                    description = "Stormwater pumps cleared municipal choke points",
                    isCurrentActive = true
                ),
                CascadingStep(
                    id = "cs_norm_3",
                    title = "Route Restoration",
                    timeLabel = "Complete",
                    status = "Safe Clearance",
                    iconKey = "check_circle",
                    description = "All arterial roads opened for transit and commercial supply"
                )
            )
        }
    }

    fun getPrioritizedRecommendations(scenario: DisasterScenario): List<EmergencyRecommendation> {
        return when (scenario.id) {
            "scenario_trimbak_cloudburst" -> listOf(
                EmergencyRecommendation(
                    id = "rec_1",
                    priority = 1,
                    title = "Protect Hospital Access & Establish Waterborne Corridor",
                    action = "Deploy NDRF inflatable motorized boats to create emergency medical corridor to KEM Hospital via elevated bypass.",
                    reason = "Critical trauma ward is isolated; 68 intensive care patients and urgent triage arriving from flood zone.",
                    riskReductionPercent = 73,
                    populationProtected = 46000,
                    estimatedTimeSavedMinutes = 45
                ),
                EmergencyRecommendation(
                    id = "rec_2",
                    priority = 2,
                    title = "Evacuate Low-lying Ramkund to Relief Camp Alpha",
                    action = "Sound community sirens and dispatch 20 state transport buses to Shivaji Nagar Elevated Camp.",
                    reason = "River surge expanding at 0.3m/hour; water will enter 1st floor residential units in under 90 minutes.",
                    riskReductionPercent = 58,
                    populationProtected = 32000,
                    estimatedTimeSavedMinutes = 60
                ),
                EmergencyRecommendation(
                    id = "rec_3",
                    priority = 3,
                    title = "Deploy High-Capacity Dewatering Sump Pumps",
                    action = "Install mobile pump units at Panchavati sub-station perimeter to prevent regional power collapse.",
                    reason = "Maintains communication relays and water pumping stations across Nashik East.",
                    riskReductionPercent = 41,
                    populationProtected = 18500,
                    estimatedTimeSavedMinutes = 30
                )
            )
            "scenario_gangapur_dam" -> listOf(
                EmergencyRecommendation(
                    id = "rec_4",
                    priority = 1,
                    title = "Barricade Goda Ghat Vehicular Bridges",
                    action = "Enforce hard police barricades across Holkar Bridge and Victoria Bridge against unauthorized crossing.",
                    reason = "Turbulent currents exceeding 45,000 cusecs risk vehicle sweeping and bridge structural stress.",
                    riskReductionPercent = 81,
                    populationProtected = 64000,
                    estimatedTimeSavedMinutes = 50
                ),
                EmergencyRecommendation(
                    id = "rec_5",
                    priority = 2,
                    title = "Activate Community Shelter Beta Logistics",
                    action = "Pre-position 5,000 emergency ration kits, dry blankets, and mobile solar generators at Indoor Stadium.",
                    reason = "Evacuees from submerged riverbank slums arriving rapidly without personal supplies.",
                    riskReductionPercent = 65,
                    populationProtected = 48000,
                    estimatedTimeSavedMinutes = 40
                ),
                EmergencyRecommendation(
                    id = "rec_6",
                    priority = 3,
                    title = "Broadcast Multilingual Evacuation SMS Warnings",
                    action = "Push geofenced emergency broadcast in Marathi, Hindi, and English advising movement to high ground.",
                    reason = "Ensures zero casualties in vulnerable riverside slums before nightfall.",
                    riskReductionPercent = 52,
                    populationProtected = 39000,
                    estimatedTimeSavedMinutes = 25
                )
            )
            else -> listOf(
                EmergencyRecommendation(
                    id = "rec_7",
                    priority = 1,
                    title = "Conduct Structural Safety Inspection of Arterial Culverts",
                    action = "Deploy civil engineering teams to inspect flood-affected bridges before full heavy transit resumes.",
                    reason = "Ensures safe public movement following receding floodwaters.",
                    riskReductionPercent = 90,
                    populationProtected = 120000,
                    estimatedTimeSavedMinutes = 20
                ),
                EmergencyRecommendation(
                    id = "rec_8",
                    priority = 2,
                    title = "Distribute Potable Water & Water Purification Tablets",
                    action = "Dispatch 8 mobile water tankers to flood-receded wards to avoid waterborne illness outbreaks.",
                    reason = "Main water lines undergoing sanitization and pressure testing.",
                    riskReductionPercent = 75,
                    populationProtected = 55000,
                    estimatedTimeSavedMinutes = 35
                ),
                EmergencyRecommendation(
                    id = "rec_9",
                    priority = 3,
                    title = "Gradual Return of Citizens from Temporary Relief Camps",
                    action = "Organize structured ward-by-ward return logistics with municipal sanitation support.",
                    reason = "Prevents congestion and ensures safe domestic sanitation restoration.",
                    riskReductionPercent = 60,
                    populationProtected = 25000,
                    estimatedTimeSavedMinutes = 15
                )
            )
        }
    }

    fun getSampleSosRecords(): List<SosRecord> = listOf(
        SosRecord(
            id = "SOS-104",
            userId = "USR-9921",
            userName = "Rajesh Kulkarni",
            phone = "+91 98230 44521",
            lat = 19.9360,
            lng = 73.5350,
            locationName = "Trimbakeshwar East, Brahmagiri Base",
            riskScore = 94,
            hazardType = "Flash Flood / Rapid Water Surge",
            timestamp = System.currentTimeMillis() - (12 * 60 * 1000),
            priority = "Critical",
            status = SosStatus.ACTIVE,
            medicalNeeds = "Elderly person with insulin requirement; trapped on 1st floor balcony",
            message = "Water level rising rapidly over 4 feet outside house. Road blocked by mud debris.",
            nearestHospital = "Nashik District Civil Hospital (14.2 km)",
            recommendedAction = "Deploy NDRF Zodiac boat unit #4 with emergency medical kit."
        ),
        SosRecord(
            id = "SOS-105",
            userId = "USR-4188",
            userName = "Pooja Patil & 4 Others",
            phone = "+91 94222 18903",
            lat = 20.0085,
            lng = 73.7925,
            locationName = "Goda Ghat Near Ramkund, Nashik",
            riskScore = 88,
            hazardType = "Submerged Commercial Building",
            timestamp = System.currentTimeMillis() - (28 * 60 * 1000),
            priority = "Critical",
            status = SosStatus.ACTIVE,
            medicalNeeds = "Child with high fever and chills",
            message = "Shop entrance flooded. Electricity cut off. Need evacuation to Relief Camp Alpha.",
            nearestHospital = "KEM Multi-Specialty Ward (2.1 km)",
            recommendedAction = "Dispatch local municipal rescue crew with life jackets and paramedic."
        ),
        SosRecord(
            id = "SOS-106",
            userId = "USR-3091",
            userName = "Amol Deshmukh",
            phone = "+91 97655 00921",
            lat = 19.9860,
            lng = 73.7650,
            locationName = "Gangapur Link Road Sector 5",
            riskScore = 72,
            hazardType = "Vehicle Stalled in Deep Water",
            timestamp = System.currentTimeMillis() - (55 * 60 * 1000),
            priority = "High",
            status = SosStatus.DISPATCHED,
            medicalNeeds = "No immediate injuries",
            message = "Car engine stalled in 2.5 ft water. Family moved to nearby temple roof.",
            nearestHospital = "Lifeline Trauma Center (3.4 km)",
            recommendedAction = "Tow unit and evacuation carrier assigned (Unit Beta-2)."
        ),
        SosRecord(
            id = "SOS-107",
            userId = "USR-1022",
            userName = "Sita Shinde",
            phone = "+91 98900 33412",
            lat = 20.0150,
            lng = 73.8010,
            locationName = "Panchavati Old Market",
            riskScore = 54,
            hazardType = "Structural Cracking in Wall",
            timestamp = System.currentTimeMillis() - (110 * 60 * 1000),
            priority = "Moderate",
            status = SosStatus.RESOLVED,
            medicalNeeds = "Safe; relocated to Community Shelter Beta",
            message = "Evacuation completed successfully by civil defense volunteers.",
            nearestHospital = "Nashik District Civil Hospital",
            recommendedAction = "Civil engineers inspecting structure integrity."
        )
    )

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c * 10.0).roundToInt() / 10.0
    }
}
