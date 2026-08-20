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

    fun getSafePlaces(): List<SafePlace> = listOf(
        SafePlace(
            id = "sp_1",
            name = "KTHM College Elevated Relief Campus",
            nameHi = "केटीएचएम कॉलेज उच्च राहत शिविर",
            nameMr = "केटीएचएम कॉलेज उंच मदत छावणी",
            type = FacilityType.SHELTER,
            lat = 20.0110,
            lng = 73.7740,
            capacityBeds = 2200,
            availableBeds = 840,
            distanceKm = 1.1,
            address = "Gangapur Road, Near Old CBS, Nashik",
            safeZoneElevationMeters = 615,
            contactNumber = "+91 253 257 1376"
        ),
        SafePlace(
            id = "sp_2",
            name = "Shivaji Nagar Disaster Evacuation Center",
            nameHi = "शिवाजी नगर आपदा निकासी केंद्र",
            nameMr = "शिवाजी नगर आपत्ती निवारण केंद्र",
            type = FacilityType.SHELTER,
            lat = 20.0045,
            lng = 73.7710,
            capacityBeds = 1500,
            availableBeds = 520,
            distanceKm = 0.9,
            address = "Municipal School 14, Shivaji Nagar",
            safeZoneElevationMeters = 608,
            contactNumber = "1077 (Disaster Cell)"
        ),
        SafePlace(
            id = "sp_3",
            name = "Mahatma Nagar Indoor Sports Haven",
            nameHi = "महात्मा नगर इनडोर स्पोर्ट्स शेल्टर",
            nameMr = "महात्मा नगर इनडोअर स्पोर्ट्स शेल्टर",
            type = FacilityType.SHELTER,
            lat = 19.9890,
            lng = 73.7980,
            capacityBeds = 3000,
            availableBeds = 1450,
            distanceKm = 2.1,
            address = "Mahatma Nagar Ground, Sector 12",
            safeZoneElevationMeters = 622,
            contactNumber = "0253-222211"
        ),
        SafePlace(
            id = "sp_4",
            name = "Nashik District Civil Hospital Safe Wing",
            nameHi = "नासिक जिला सिविल अस्पताल सुरक्षित विंग",
            nameMr = "नाशिक जिल्हा शासकीय रुग्णालय सुरक्षित विभाग",
            type = FacilityType.HOSPITAL,
            lat = 19.9982,
            lng = 73.7845,
            capacityBeds = 450,
            availableBeds = 68,
            distanceKm = 1.4,
            address = "Trimbak Road, Old Agra Highway",
            safeZoneElevationMeters = 602,
            contactNumber = "+91 253 257 2341"
        ),
        SafePlace(
            id = "sp_5",
            name = "CIDCO Elevated High Ground Haven",
            nameHi = "सिडको उच्च सुरक्षित आश्रय स्थल",
            nameMr = "सिडको उंच सुरक्षित निवारा केंद्र",
            type = FacilityType.SHELTER,
            lat = 19.9700,
            lng = 73.7600,
            capacityBeds = 4000,
            availableBeds = 2100,
            distanceKm = 3.2,
            address = "Pawan Nagar Stadium, CIDCO",
            safeZoneElevationMeters = 635,
            contactNumber = "0253-239100"
        )
    )

    fun getEmergencyVehicles(userLat: Double, userLng: Double): List<EmergencyVehicle> {
        val baseVehicles = listOf(
            EmergencyVehicle(
                id = "veh_bus_108",
                vehicleNumber = "MH-15-EV-4012",
                type = VehicleType.CITYLINK_BUS,
                routeName = "CityLink Line 108: CBS ↔ Gangapur Evacuation",
                routeNameHi = "सिटीलिंक रूट 108: सीबीएस ↔ गंगापुर राहत सेवा",
                routeNameMr = "सिटीलिंक मार्ग 108: सीबीएस ↔ गंगापूर मदत फेरी",
                lat = userLat + 0.0035,
                lng = userLng - 0.0042,
                speedKmH = 34,
                capacity = 52,
                availableSeats = 28,
                destination = "KTHM College Shelter",
                destinationHi = "केटीएचएम कॉलेज आश्रय",
                destinationMr = "केटीएचएम कॉलेज निवारा",
                driverContact = "+91 94222 55108",
                isEvacuating = true
            ),
            EmergencyVehicle(
                id = "veh_bus_204",
                vehicleNumber = "MH-15-EV-8821",
                type = VehicleType.CITYLINK_BUS,
                routeName = "CityLink Line 204: Shalimar ↔ Shivaji Nagar High Ground",
                routeNameHi = "सिटीलिंक रूट 204: शालीमार ↔ शिवाजी नगर ऊँचा मार्ग",
                routeNameMr = "सिटीलिंक मार्ग 204: शालीमार ↔ शिवाजी नगर उंच रस्ता",
                lat = userLat - 0.0051,
                lng = userLng + 0.0038,
                speedKmH = 28,
                capacity = 52,
                availableSeats = 19,
                destination = "Shivaji Nagar Evac Camp",
                destinationHi = "शिवाजी नगर निकासी शिविर",
                destinationMr = "शिवाजी नगर मदत छावणी",
                driverContact = "+91 98220 11204",
                isEvacuating = true
            ),
            EmergencyVehicle(
                id = "veh_amb_1",
                vehicleNumber = "MH-15-AMB-1081",
                type = VehicleType.AMBULANCE,
                routeName = "108 Advanced Life Support (ALS Unit 7)",
                routeNameHi = "108 एडवांस्ड लाइफ सपोर्ट एम्बुलेंस #7",
                routeNameMr = "१०८ अद्ययावत अतिदक्षता रुग्णवाहिका #७",
                lat = userLat + 0.0022,
                lng = userLng + 0.0031,
                speedKmH = 45,
                capacity = 4,
                availableSeats = 2,
                destination = "Civil Hospital Trauma Wing",
                destinationHi = "सिविल अस्पताल ट्रॉमा विंग",
                destinationMr = "जिल्हा रुग्णालय अतिदक्षता विभाग",
                driverContact = "108 (Dispatched by Dr. Deshmukh)",
                isEvacuating = true
            ),
            EmergencyVehicle(
                id = "veh_amb_2",
                vehicleNumber = "MH-15-AMB-1084",
                type = VehicleType.AMBULANCE,
                routeName = "108 Paramedic First Responder #4",
                routeNameHi = "108 पैरामेडिक आपातकालीन एम्बुलेंस #4",
                routeNameMr = "१०८ पॅरामेडिक आपत्कालीन रुग्णवाहिका #४",
                lat = userLat - 0.0038,
                lng = userLng - 0.0049,
                speedKmH = 40,
                capacity = 4,
                availableSeats = 3,
                destination = "KEM Multi-Specialty Disaster Ward",
                destinationHi = "केईएम अस्पताल आपदा वार्ड",
                destinationMr = "केईएम आपत्ती कक्ष",
                driverContact = "108 (Paramedic Unit)",
                isEvacuating = true
            ),
            EmergencyVehicle(
                id = "veh_bus_101",
                vehicleNumber = "MH-15-EV-1019",
                type = VehicleType.CITYLINK_BUS,
                routeName = "CityLink Line 101: Nimani ↔ CIDCO High Plateau",
                routeNameHi = "सिटीलिंक रूट 101: निमानी ↔ सिडको सुरक्षित पठार",
                routeNameMr = "सिटीलिंक मार्ग 101: निमाणी ↔ सिडको सुरक्षित पठार",
                lat = userLat + 0.0075,
                lng = userLng - 0.0080,
                speedKmH = 32,
                capacity = 52,
                availableSeats = 35,
                destination = "CIDCO Stadium Haven",
                destinationHi = "सिडको स्टेडियम आश्रय",
                destinationMr = "सिडको स्टेडियम निवारा",
                driverContact = "+91 97655 44101",
                isEvacuating = true
            )
        )

        return baseVehicles.map { v ->
            val dist = calculateDistanceKm(userLat, userLng, v.lat, v.lng)
            v.copy(distanceKm = dist)
        }.sortedBy { it.distanceKm }
    }

    fun generateSafeNavigationRoute(
        startLat: Double,
        startLng: Double,
        destination: SafePlace
    ): List<SafeNavigationStep> {
        val totalDistanceKm = calculateDistanceKm(startLat, startLng, destination.lat, destination.lng)
        val dMeters = (totalDistanceKm * 1000).toInt()

        return listOf(
            SafeNavigationStep(
                stepNumber = 1,
                instructionEn = "Head away from riverbank towards High Street (Elevated Corridor).",
                instructionHi = "नदी के किनारे से दूर मुख्य सड़क (ऊँचे मार्ग) की ओर आगे बढ़ें।",
                instructionMr = "नदीच्या काठापासून दूर मुख्य उंच रस्त्याच्या दिशेने पुढे चला.",
                distanceMeters = minOf(150, (dMeters * 0.15).toInt()),
                turnType = "STRAIGHT",
                lat = startLat + 0.001,
                lng = startLng - 0.001
            ),
            SafeNavigationStep(
                stepNumber = 2,
                instructionEn = "Turn RIGHT onto the Elevated Flyover Bypass. Avoid low-lying underpass.",
                instructionHi = "ऊँचे फ्लाईओवर बाईपास पर दाएँ मुड़ें। निचले अंडरपास से बचें।",
                instructionMr = "उंच पुलाच्या रस्त्याकडे उजवीकडे वळा. सखल रस्ता टाळा.",
                distanceMeters = minOf(350, (dMeters * 0.35).toInt()),
                turnType = "RIGHT",
                lat = startLat + (destination.lat - startLat) * 0.4,
                lng = startLng + (destination.lng - startLng) * 0.4
            ),
            SafeNavigationStep(
                stepNumber = 3,
                instructionEn = "Continue straight along Gangapur Ridge for ${(dMeters * 0.4).toInt()} meters. High ground safety index: 98%.",
                instructionHi = "गंगापुर रिज पर सीधे ${(dMeters * 0.4).toInt()} मीटर चलें। ऊँचे क्षेत्र का सुरक्षा सूचकांक: 98%।",
                instructionMr = "गंगापूर उंच रस्त्याने ${(dMeters * 0.4).toInt()} मीटर सरळ जा. सुरक्षितता निर्देशांक: ९८%.",
                distanceMeters = (dMeters * 0.4).toInt(),
                turnType = "ELEVATION",
                lat = startLat + (destination.lat - startLat) * 0.7,
                lng = startLng + (destination.lng - startLng) * 0.7
            ),
            SafeNavigationStep(
                stepNumber = 4,
                instructionEn = "Turn LEFT into ${destination.name}. Safe haven gate with triage & supplies ahead.",
                instructionHi = "बाएँ मुड़कर ${destination.nameHi} में प्रवेश करें। सुरक्षित राहत केंद्र द्वार सामने है।",
                instructionMr = "डावीकडे वळून ${destination.nameMr} मध्ये प्रवेश करा. सुरक्षित केंद्र समोर आहे.",
                distanceMeters = minOf(100, (dMeters * 0.1).toInt()),
                turnType = "DESTINATION",
                lat = destination.lat,
                lng = destination.lng
            )
        )
    }

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
