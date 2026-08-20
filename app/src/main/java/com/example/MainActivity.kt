package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.model.Language
import com.example.ui.components.RakshAIBottomNavBar
import com.example.ui.components.RakshAITopBar
import com.example.ui.components.SimulationDialog
import com.example.ui.screens.*
import com.example.ui.theme.GeoBackground
import com.example.ui.theme.RakshAITheme
import com.example.ui.viewmodel.RakshAIViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RakshAIViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            RakshAITheme {
                val language by viewModel.language.collectAsState()
                val selectedTab by viewModel.selectedTab.collectAsState()
                val currentScenario by viewModel.currentScenario.collectAsState()
                val personalRiskScore by viewModel.personalRiskScore.collectAsState()
                val personalRiskLevel by viewModel.personalRiskLevel.collectAsState()
                val impactFactors by viewModel.impactFactors.collectAsState()
                val cascadingSteps by viewModel.cascadingSteps.collectAsState()
                val recommendations by viewModel.recommendations.collectAsState()
                val nearestHazardDistanceKm by viewModel.nearestHazardDistanceKm.collectAsState()
                val nearestHazardName by viewModel.nearestHazardName.collectAsState()
                val userLocationName by viewModel.userLocationName.collectAsState()
                val userLat by viewModel.userLat.collectAsState()
                val userLng by viewModel.userLng.collectAsState()
                val isSimulating by viewModel.isSimulating.collectAsState()
                val simulationProgress by viewModel.simulationProgress.collectAsState()
                val hazardZones by viewModel.hazardZones.collectAsState()
                val facilities by viewModel.facilities.collectAsState()
                val safePlaces by viewModel.safePlaces.collectAsState()
                val selectedSafePlace by viewModel.selectedSafePlace.collectAsState()
                val navigationSteps by viewModel.navigationSteps.collectAsState()
                val isNavigating by viewModel.isNavigating.collectAsState()
                val isVoiceGuidanceActive by viewModel.isVoiceGuidanceActive.collectAsState()
                val emergencyVehicles by viewModel.emergencyVehicles.collectAsState()
                val selectedVehicle by viewModel.selectedVehicle.collectAsState()
                val activeSos by viewModel.activeUserSos.collectAsState()
                val coordinatorSosList by viewModel.coordinatorSosList.collectAsState()
                val chatMessages by viewModel.chatMessages.collectAsState()
                val isChatLoading by viewModel.isChatLoading.collectAsState()
                val mapLayers by viewModel.mapLayers.collectAsState()

                var showSimulationDialog by remember { mutableStateOf(false) }
                val isCoordinatorMode = selectedTab == 4

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = GeoBackground,
                    topBar = {
                        RakshAITopBar(
                            currentLanguage = language,
                            onLanguageSelected = { viewModel.setLanguage(it) },
                            onCoordinatorToggle = {
                                if (selectedTab == 4) viewModel.setSelectedTab(0) else viewModel.setSelectedTab(4)
                            },
                            isCoordinatorMode = isCoordinatorMode
                        )
                    },
                    bottomBar = {
                        RakshAIBottomNavBar(
                            selectedTab = selectedTab,
                            onTabSelected = { viewModel.setSelectedTab(it) },
                            language = language
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(GeoBackground)
                    ) {
                        when (selectedTab) {
                            0 -> HomeScreen(
                                scenario = currentScenario,
                                personalRiskScore = personalRiskScore,
                                personalRiskLevel = personalRiskLevel,
                                impactFactors = impactFactors,
                                cascadingSteps = cascadingSteps,
                                recommendations = recommendations,
                                nearestHazardDistanceKm = nearestHazardDistanceKm,
                                nearestHazardName = nearestHazardName,
                                userLocationName = userLocationName,
                                userLat = userLat,
                                userLng = userLng,
                                isSimulating = isSimulating,
                                simulationProgress = simulationProgress,
                                language = language,
                                onRunSimulationClick = { showSimulationDialog = true },
                                onNavigateToTab = { viewModel.setSelectedTab(it) },
                                onDispatchRecommendation = { viewModel.dispatchRecommendation(it) }
                            )
                            1 -> MapScreen(
                                userLat = userLat,
                                userLng = userLng,
                                userLocationName = userLocationName,
                                hazardZones = hazardZones,
                                facilities = facilities,
                                safePlaces = safePlaces,
                                selectedSafePlace = selectedSafePlace,
                                navigationSteps = navigationSteps,
                                isNavigating = isNavigating,
                                isVoiceGuidanceActive = isVoiceGuidanceActive,
                                emergencyVehicles = emergencyVehicles,
                                selectedVehicle = selectedVehicle,
                                activeLayers = mapLayers,
                                onToggleLayer = { viewModel.toggleMapLayer(it) },
                                onSelectSafePlace = { viewModel.selectSafePlace(it) },
                                onStartNavigation = { viewModel.startNavigation(it) },
                                onStopNavigation = { viewModel.stopNavigation() },
                                onToggleVoiceGuidance = { viewModel.toggleVoiceGuidance() },
                                onSelectVehicle = { viewModel.selectVehicle(it) },
                                onOpenGoogleMaps = { ctx, lat, lng, name ->
                                    viewModel.launchExternalGoogleMaps(ctx, lat, lng, name)
                                },
                                onSimulateRelocate = { lat, lng, name ->
                                    viewModel.updateLocation(lat, lng, name)
                                },
                                language = language,
                                onTriggerSos = { viewModel.setSelectedTab(2) }
                            )
                            2 -> SosScreen(
                                activeSos = activeSos,
                                personalRiskScore = personalRiskScore,
                                userLocationName = userLocationName,
                                userLat = userLat,
                                userLng = userLng,
                                nearestHazardName = nearestHazardName,
                                language = language,
                                onTriggerSos = { needs, msg ->
                                    viewModel.triggerSos(needs, msg)
                                },
                                onCancelSos = { viewModel.cancelSos() }
                            )
                            3 -> AiScreen(
                                messages = chatMessages,
                                isLoading = isChatLoading,
                                onSendMessage = { viewModel.sendUserChatMessage(it) },
                                language = language
                            )
                            4 -> CoordinatorScreen(
                                sosList = coordinatorSosList,
                                onDispatchSos = { viewModel.dispatchSos(it) },
                                onResolveSos = { viewModel.resolveSos(it) },
                                onOpenSimulation = { showSimulationDialog = true },
                                language = language
                            )
                        }

                        if (showSimulationDialog) {
                            SimulationDialog(
                                currentScenario = currentScenario,
                                onDismiss = { showSimulationDialog = false },
                                onRunPresetScenario = { scenario ->
                                    viewModel.selectScenario(scenario)
                                },
                                onRunCustomSimulation = { rainfall, surge, pop, severity ->
                                    viewModel.runSimulation(rainfall, surge, pop, severity)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
