package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DisasterRepository
import com.example.data.LocalizationProvider
import com.example.data.MultiAgentEngine
import com.example.data.local.AppDatabase
import com.example.data.local.SosEntity
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RakshAIViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val sosDao = db.sosDao()

    private val _language = MutableStateFlow(Language.ENGLISH)
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Home, 1: Map, 2: SOS, 3: AI, 4: Coordinator
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _currentScenario = MutableStateFlow(DisasterRepository.scenarios.first())
    val currentScenario: StateFlow<DisasterScenario> = _currentScenario.asStateFlow()

    private val _userLat = MutableStateFlow(19.9920)
    val userLat: StateFlow<Double> = _userLat.asStateFlow()

    private val _userLng = MutableStateFlow(73.7810)
    val userLng: StateFlow<Double> = _userLng.asStateFlow()

    private val _userLocationName = MutableStateFlow("Nashik Central District (Sector 4)")
    val userLocationName: StateFlow<String> = _userLocationName.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _simulationProgress = MutableStateFlow(0f)
    val simulationProgress: StateFlow<Float> = _simulationProgress.asStateFlow()

    private val _hazardZones = MutableStateFlow(DisasterRepository.getInitialHazardZones(_currentScenario.value))
    val hazardZones: StateFlow<List<HazardZone>> = _hazardZones.asStateFlow()

    private val _facilities = MutableStateFlow(DisasterRepository.getInitialFacilities())
    val facilities: StateFlow<List<EmergencyFacility>> = _facilities.asStateFlow()

    private val _cascadingSteps = MutableStateFlow(DisasterRepository.getCascadingSteps(_currentScenario.value))
    val cascadingSteps: StateFlow<List<CascadingStep>> = _cascadingSteps.asStateFlow()

    private val _impactFactors = MutableStateFlow(DisasterRepository.calculateImpactFactors(_currentScenario.value))
    val impactFactors: StateFlow<ImpactFactors> = _impactFactors.asStateFlow()

    private val _personalRiskScore = MutableStateFlow(84)
    val personalRiskScore: StateFlow<Int> = _personalRiskScore.asStateFlow()

    private val _personalRiskLevel = MutableStateFlow(RiskLevel.CRITICAL)
    val personalRiskLevel: StateFlow<RiskLevel> = _personalRiskLevel.asStateFlow()

    private val _nearestHazardDistanceKm = MutableStateFlow(1.2)
    val nearestHazardDistanceKm: StateFlow<Double> = _nearestHazardDistanceKm.asStateFlow()

    private val _nearestHazardName = MutableStateFlow("Trimbak Highway Inundation Zone")
    val nearestHazardName: StateFlow<String> = _nearestHazardName.asStateFlow()

    private val _recommendations = MutableStateFlow(DisasterRepository.getPrioritizedRecommendations(_currentScenario.value))
    val recommendations: StateFlow<List<EmergencyRecommendation>> = _recommendations.asStateFlow()

    private val _activeUserSos = MutableStateFlow<SosRecord?>(null)
    val activeUserSos: StateFlow<SosRecord?> = _activeUserSos.asStateFlow()

    private val _coordinatorSosList = MutableStateFlow(DisasterRepository.getSampleSosRecords())
    val coordinatorSosList: StateFlow<List<SosRecord>> = _coordinatorSosList.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _mapLayers = MutableStateFlow(setOf("HEATMAP", "HAZARDS", "HOSPITALS", "SHELTERS", "SAFE_ROUTE"))
    val mapLayers: StateFlow<Set<String>> = _mapLayers.asStateFlow()

    init {
        recalculateAllMetrics()
        seedInitialSosAndChat()
        observeSosDatabase()
    }

    private fun observeSosDatabase() {
        viewModelScope.launch {
            sosDao.getAllSos().collect { entities ->
                if (entities.isNotEmpty()) {
                    val dbRecords = entities.map { it.toSosRecord() }
                    val existingIds = dbRecords.map { it.id }.toSet()
                    val merged = dbRecords + _coordinatorSosList.value.filter { it.id !in existingIds }
                    _coordinatorSosList.value = merged
                }
            }
        }
    }

    private fun seedInitialSosAndChat() {
        val initialBotMsg = MultiAgentEngine.generateAssistantResponse(
            query = "What is the emergency situation?",
            scenario = _currentScenario.value,
            userLat = _userLat.value,
            userLng = _userLng.value,
            personalRiskScore = _personalRiskScore.value,
            riskLevel = _personalRiskLevel.value,
            nearestHospital = _facilities.value.firstOrNull { it.type == FacilityType.HOSPITAL },
            nearestShelter = _facilities.value.firstOrNull { it.type == FacilityType.SHELTER },
            language = _language.value
        )
        _chatMessages.value = listOf(initialBotMsg)
    }

    fun setLanguage(lang: Language) {
        _language.value = lang
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun toggleMapLayer(layer: String) {
        val current = _mapLayers.value.toMutableSet()
        if (current.contains(layer)) {
            current.remove(layer)
        } else {
            current.add(layer)
        }
        _mapLayers.value = current
    }

    fun selectScenario(scenario: DisasterScenario) {
        _currentScenario.value = scenario
        _hazardZones.value = DisasterRepository.getInitialHazardZones(scenario)
        _cascadingSteps.value = DisasterRepository.getCascadingSteps(scenario)
        _impactFactors.value = DisasterRepository.calculateImpactFactors(scenario)
        _recommendations.value = DisasterRepository.getPrioritizedRecommendations(scenario)
        
        // Slightly adapt user location near scenario
        when (scenario.id) {
            "scenario_trimbak_cloudburst" -> {
                _userLat.value = 19.9390
                _userLng.value = 73.5420
                _userLocationName.value = "Trimbakeshwar East Valley (Sector 2)"
            }
            "scenario_gangapur_dam" -> {
                _userLat.value = 20.0050
                _userLng.value = 73.7880
                _userLocationName.value = "Godavari Bank, Ramkund Lowland"
            }
            else -> {
                _userLat.value = 19.9975
                _userLng.value = 73.7898
                _userLocationName.value = "Nashik Elevated Center"
            }
        }

        recalculateAllMetrics()
        
        // Add updated AI intelligence summary message
        val updatedAiMsg = MultiAgentEngine.generateAssistantResponse(
            query = "Scenario changed to ${scenario.title}",
            scenario = scenario,
            userLat = _userLat.value,
            userLng = _userLng.value,
            personalRiskScore = _personalRiskScore.value,
            riskLevel = _personalRiskLevel.value,
            nearestHospital = _facilities.value.firstOrNull { it.type == FacilityType.HOSPITAL },
            nearestShelter = _facilities.value.firstOrNull { it.type == FacilityType.SHELTER },
            language = _language.value
        )
        _chatMessages.value = _chatMessages.value + updatedAiMsg
    }

    fun runSimulation(
        customRainfall: Int? = null,
        customRiverLevel: Double? = null,
        customPop: Int? = null,
        customHazard: Int? = null
    ) {
        viewModelScope.launch {
            _isSimulating.value = true
            _simulationProgress.value = 0.1f

            delay(300)
            _simulationProgress.value = 0.4f
            delay(300)
            _simulationProgress.value = 0.75f
            delay(300)
            _simulationProgress.value = 1.0f

            val base = _currentScenario.value
            val simulated = base.copy(
                rainfallIntensityMmPerHour = customRainfall ?: base.rainfallIntensityMmPerHour,
                riverLevelChangeMeters = customRiverLevel ?: base.riverLevelChangeMeters,
                populationExposed = customPop ?: base.populationExposed,
                hazardSeverity = customHazard ?: base.hazardSeverity
            )

            _currentScenario.value = simulated
            _impactFactors.value = DisasterRepository.calculateImpactFactors(simulated)
            _hazardZones.value = DisasterRepository.getInitialHazardZones(simulated)
            _cascadingSteps.value = DisasterRepository.getCascadingSteps(simulated)
            _recommendations.value = DisasterRepository.getPrioritizedRecommendations(simulated)
            
            recalculateAllMetrics()

            _isSimulating.value = false
            _simulationProgress.value = 0f
        }
    }

    fun updateLocation(lat: Double, lng: Double, locationName: String? = null) {
        _userLat.value = lat
        _userLng.value = lng
        locationName?.let { _userLocationName.value = it }
        recalculateAllMetrics()
    }

    private fun recalculateAllMetrics() {
        val scenario = _currentScenario.value
        val zones = _hazardZones.value

        var minDistance = 999.0
        var closestHazardName = "Regional Flood Zone"

        for (zone in zones) {
            val dist = DisasterRepository.calculateDistanceKm(_userLat.value, _userLng.value, zone.lat, zone.lng)
            if (dist < minDistance) {
                minDistance = dist
                closestHazardName = zone.name
            }
        }

        if (minDistance > 100) minDistance = 1.2

        _nearestHazardDistanceKm.value = minDistance
        _nearestHazardName.value = closestHazardName

        val (score, level) = DisasterRepository.calculatePersonalRisk(
            _userLat.value,
            _userLng.value,
            scenario,
            minDistance
        )

        _personalRiskScore.value = score
        _personalRiskLevel.value = level
    }

    fun triggerSos(medicalNeeds: String = "None specified", message: String = "Urgent evacuation assistance required!") {
        val record = SosRecord(
            id = "SOS-${(100..999).random()}",
            userId = "USER-CITIZEN-01",
            userName = "Citizen (You)",
            phone = "+91 98000 12345",
            lat = _userLat.value,
            lng = _userLng.value,
            locationName = _userLocationName.value,
            riskScore = _personalRiskScore.value,
            hazardType = _nearestHazardName.value,
            timestamp = System.currentTimeMillis(),
            priority = if (_personalRiskScore.value >= 75) "Critical" else "High",
            status = SosStatus.ACTIVE,
            medicalNeeds = medicalNeeds,
            message = message,
            nearestHospital = _facilities.value.firstOrNull { it.type == FacilityType.HOSPITAL }?.name ?: "District Civil Hospital",
            recommendedAction = "Immediate rescue beacon dispatched to Sector Coordinator."
        )

        _activeUserSos.value = record
        _coordinatorSosList.value = listOf(record) + _coordinatorSosList.value

        viewModelScope.launch {
            sosDao.insertSos(SosEntity.fromSosRecord(record))
        }
    }

    fun cancelSos() {
        val active = _activeUserSos.value
        if (active != null) {
            val updated = active.copy(status = SosStatus.RESOLVED)
            _activeUserSos.value = null
            _coordinatorSosList.value = _coordinatorSosList.value.map {
                if (it.id == active.id) updated else it
            }
            viewModelScope.launch {
                sosDao.updateSosStatus(active.id, SosStatus.RESOLVED.name)
            }
        }
    }

    fun dispatchSos(sosId: String) {
        _coordinatorSosList.value = _coordinatorSosList.value.map {
            if (it.id == sosId) it.copy(status = SosStatus.DISPATCHED) else it
        }
        if (_activeUserSos.value?.id == sosId) {
            _activeUserSos.value = _activeUserSos.value?.copy(status = SosStatus.DISPATCHED)
        }
        viewModelScope.launch {
            sosDao.updateSosStatus(sosId, SosStatus.DISPATCHED.name)
        }
    }

    fun resolveSos(sosId: String) {
        _coordinatorSosList.value = _coordinatorSosList.value.map {
            if (it.id == sosId) it.copy(status = SosStatus.RESOLVED) else it
        }
        if (_activeUserSos.value?.id == sosId) {
            _activeUserSos.value = null
        }
        viewModelScope.launch {
            sosDao.updateSosStatus(sosId, SosStatus.RESOLVED.name)
        }
    }

    fun dispatchRecommendation(recId: String) {
        _recommendations.value = _recommendations.value.map {
            if (it.id == recId) it.copy(isDispatched = true) else it
        }
    }

    fun sendUserChatMessage(userQuery: String) {
        if (userQuery.isBlank()) return

        val userMsg = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = "Citizen (You)",
            text = userQuery,
            timestamp = System.currentTimeMillis(),
            isUser = true
        )
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isChatLoading.value = true
            delay(500) // Brief natural thinking cadence for multi-agent synthesis

            val botResponse = MultiAgentEngine.generateAssistantResponse(
                query = userQuery,
                scenario = _currentScenario.value,
                userLat = _userLat.value,
                userLng = _userLng.value,
                personalRiskScore = _personalRiskScore.value,
                riskLevel = _personalRiskLevel.value,
                nearestHospital = _facilities.value.firstOrNull { it.type == FacilityType.HOSPITAL },
                nearestShelter = _facilities.value.firstOrNull { it.type == FacilityType.SHELTER },
                language = _language.value
            )

            _chatMessages.value = _chatMessages.value + botResponse
            _isChatLoading.value = false
        }
    }
}
