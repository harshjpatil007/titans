package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalizationProvider
import com.example.model.*
import com.example.ui.components.SimulationBadge
import com.example.ui.theme.*

@Composable
fun MapScreen(
    userLat: Double,
    userLng: Double,
    userLocationName: String,
    hazardZones: List<HazardZone>,
    facilities: List<EmergencyFacility>,
    safePlaces: List<SafePlace>,
    selectedSafePlace: SafePlace?,
    navigationSteps: List<SafeNavigationStep>,
    isNavigating: Boolean,
    isVoiceGuidanceActive: Boolean,
    emergencyVehicles: List<EmergencyVehicle>,
    selectedVehicle: EmergencyVehicle?,
    activeLayers: Set<String>,
    onToggleLayer: (String) -> Unit,
    onSelectSafePlace: (SafePlace) -> Unit,
    onStartNavigation: (SafePlace) -> Unit,
    onStopNavigation: () -> Unit,
    onToggleVoiceGuidance: () -> Unit,
    onSelectVehicle: (EmergencyVehicle?) -> Unit,
    onOpenGoogleMaps: (Context, Double, Double, String) -> Unit,
    onSimulateRelocate: (Double, Double, String) -> Unit,
    language: Language,
    onTriggerSos: () -> Unit
) {
    val context = LocalContext.current
    var selectedFacility by remember { mutableStateOf<EmergencyFacility?>(null) }
    var selectedHazard by remember { mutableStateOf<HazardZone?>(null) }
    var showRelocateSheet by remember { mutableStateOf(false) }
    var showSafePlacesSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBackground)
            .testTag("map_screen")
    ) {
        // 1. Fullscreen GIS Interactive Canvas
        GisInteractiveCanvas(
            userLat = userLat,
            userLng = userLng,
            hazardZones = hazardZones,
            facilities = facilities,
            safePlaces = safePlaces,
            selectedSafePlace = selectedSafePlace,
            navigationSteps = navigationSteps,
            isNavigating = isNavigating,
            emergencyVehicles = emergencyVehicles,
            activeLayers = activeLayers,
            onFacilityClick = {
                selectedFacility = it
                selectedHazard = null
                onSelectVehicle(null)
            },
            onHazardClick = {
                selectedHazard = it
                selectedFacility = null
                onSelectVehicle(null)
            },
            onVehicleClick = {
                onSelectVehicle(it)
                selectedFacility = null
                selectedHazard = null
            },
            onSafePlaceClick = {
                onSelectSafePlace(it)
                selectedFacility = null
                selectedHazard = null
                onSelectVehicle(null)
            }
        )

        // 2. Top Header Overlay: Layer Filters & GPS Relocate
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimulationBadge(LocalizationProvider.get("map_header_title", language))

                // Simulate GPS Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder),
                    modifier = Modifier.clickable { showRelocateSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.MyLocation, contentDescription = null, tint = GeoGreenPrimary, modifier = Modifier.size(14.dp))
                        Text(
                            text = LocalizationProvider.get("simulate_gps", language),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GeoTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Layer Filter Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    LayerFilterChip("SAFE_ROUTE", LocalizationProvider.get("layer_safe_route", language), activeLayers.contains("SAFE_ROUTE"), GeoGreenDark) { onToggleLayer("SAFE_ROUTE") }
                }
                item {
                    LayerFilterChip("BUSES", LocalizationProvider.get("layer_buses", language), activeLayers.contains("BUSES"), GeoGreenPrimary) { onToggleLayer("BUSES") }
                }
                item {
                    LayerFilterChip("AMBULANCES", LocalizationProvider.get("layer_ambulances", language), activeLayers.contains("AMBULANCES"), GeoRedCritical) { onToggleLayer("AMBULANCES") }
                }
                item {
                    LayerFilterChip("SHELTERS", LocalizationProvider.get("layer_shelters", language), activeLayers.contains("SHELTERS"), GeoGreenLight) { onToggleLayer("SHELTERS") }
                }
                item {
                    LayerFilterChip("HOSPITALS", LocalizationProvider.get("layer_hospitals", language), activeLayers.contains("HOSPITALS"), GeoOrangeWarning) { onToggleLayer("HOSPITALS") }
                }
                item {
                    LayerFilterChip("HAZARDS", LocalizationProvider.get("layer_hazards", language), activeLayers.contains("HAZARDS"), GeoRedCritical) { onToggleLayer("HAZARDS") }
                }
            }
        }

        // 3. Floating Quick Safe Places Selector Pill (Top-Right under chips)
        FloatingSafePlacesPill(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 92.dp, end = 16.dp),
            selectedSafePlace = selectedSafePlace,
            language = language,
            onClick = { showSafePlacesSheet = true }
        )

        // 4. Bottom Dynamic Context Cards (Navigation Mode / Vehicle Details / Facility / Nearest Radar)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 85.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isNavigating && selectedSafePlace != null) {
                // Active Turn-by-Turn Voice Navigation Card
                ActiveNavigationCard(
                    destination = selectedSafePlace,
                    steps = navigationSteps,
                    isVoiceActive = isVoiceGuidanceActive,
                    language = language,
                    onToggleVoice = onToggleVoiceGuidance,
                    onStop = onStopNavigation,
                    onOpenGoogleMaps = {
                        onOpenGoogleMaps(context, selectedSafePlace.lat, selectedSafePlace.lng, selectedSafePlace.name)
                    }
                )
            } else if (selectedVehicle != null) {
                // Vehicle Details Card (CityLink Bus or 108 Ambulance)
                VehicleDetailsCard(
                    vehicle = selectedVehicle,
                    language = language,
                    onClose = { onSelectVehicle(null) },
                    onCall = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${selectedVehicle.driverContact.filter { it.isDigit() || it == '+' }}"))
                        context.startActivity(dialIntent)
                    }
                )
            } else if (selectedFacility != null) {
                FacilityDetailsCard(
                    facility = selectedFacility!!,
                    language = language,
                    onClose = { selectedFacility = null },
                    onNavigate = {
                        val matchedSafePlace = safePlaces.firstOrNull { it.name == selectedFacility!!.name }
                            ?: SafePlace(
                                id = selectedFacility!!.id,
                                name = selectedFacility!!.name,
                                nameHi = selectedFacility!!.name,
                                nameMr = selectedFacility!!.name,
                                type = selectedFacility!!.type,
                                lat = selectedFacility!!.lat,
                                lng = selectedFacility!!.lng,
                                capacityBeds = selectedFacility!!.totalBeds,
                                availableBeds = selectedFacility!!.availableBeds,
                                distanceKm = selectedFacility!!.distanceKm,
                                address = selectedFacility!!.address,
                                safeZoneElevationMeters = 610,
                                contactNumber = selectedFacility!!.contactNumber
                            )
                        onStartNavigation(matchedSafePlace)
                    },
                    onOpenGoogleMaps = {
                        onOpenGoogleMaps(context, selectedFacility!!.lat, selectedFacility!!.lng, selectedFacility!!.name)
                    }
                )
            } else if (selectedHazard != null) {
                HazardDetailsCard(
                    hazard = selectedHazard!!,
                    onClose = { selectedHazard = null }
                )
            } else {
                // Default Hub: Nearest Transit & Safe Evacuation Launchpad
                DefaultMapOverviewCard(
                    safePlaces = safePlaces,
                    selectedSafePlace = selectedSafePlace ?: safePlaces.first(),
                    nearestVehicle = emergencyVehicles.firstOrNull(),
                    userLocationName = userLocationName,
                    language = language,
                    onSelectSafePlace = { showSafePlacesSheet = true },
                    onStartNavigation = { onStartNavigation(selectedSafePlace ?: safePlaces.first()) },
                    onOpenGoogleMaps = {
                        val dest = selectedSafePlace ?: safePlaces.first()
                        onOpenGoogleMaps(context, dest.lat, dest.lng, dest.name)
                    },
                    onTriggerSos = onTriggerSos
                )
            }
        }

        // 5. Safe Places Bottom Sheet Dialog
        if (showSafePlacesSheet) {
            SafePlacesSelectionSheet(
                safePlaces = safePlaces,
                selected = selectedSafePlace,
                language = language,
                onDismiss = { showSafePlacesSheet = false },
                onSelect = {
                    onSelectSafePlace(it)
                    showSafePlacesSheet = false
                },
                onStartNav = {
                    onStartNavigation(it)
                    showSafePlacesSheet = false
                }
            )
        }

        // 6. GPS Relocation Dialog
        if (showRelocateSheet) {
            AlertDialog(
                onDismissRequest = { showRelocateSheet = false },
                title = { Text(LocalizationProvider.get("simulate_gps", language), fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select an area in Nashik to recalculate proximity to flood waters and safe havens:", style = MaterialTheme.typography.bodySmall)

                        LocationPresetButton("Godavari Bank, Ramkund (High Risk)", 20.0050, 73.7880) { lat, lng, name ->
                            onSimulateRelocate(lat, lng, name)
                            showRelocateSheet = false
                        }
                        LocationPresetButton("Trimbakeshwar East Valley (Critical Inundation)", 19.9390, 73.5420) { lat, lng, name ->
                            onSimulateRelocate(lat, lng, name)
                            showRelocateSheet = false
                        }
                        LocationPresetButton("Gangapur Road Elevated Ridge (Safe Zone)", 20.0110, 73.7740) { lat, lng, name ->
                            onSimulateRelocate(lat, lng, name)
                            showRelocateSheet = false
                        }
                        LocationPresetButton("CIDCO High Plateau Haven (Elevated Ground)", 19.9700, 73.7600) { lat, lng, name ->
                            onSimulateRelocate(lat, lng, name)
                            showRelocateSheet = false
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRelocateSheet = false }) { Text("Close") }
                }
            )
        }
    }
}

@Composable
private fun FloatingSafePlacesPill(
    modifier: Modifier = Modifier,
    selectedSafePlace: SafePlace?,
    language: Language,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoGreenPrimary)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Filled.Shield, contentDescription = null, tint = GeoGreenPrimary, modifier = Modifier.size(16.dp))
            val displayName = when (language) {
                Language.HINDI -> selectedSafePlace?.nameHi ?: LocalizationProvider.get("select_safe_place", language)
                Language.MARATHI -> selectedSafePlace?.nameMr ?: LocalizationProvider.get("select_safe_place", language)
                Language.ENGLISH -> selectedSafePlace?.name ?: LocalizationProvider.get("select_safe_place", language)
            }
            Text(
                text = displayName.take(22) + if (displayName.length > 22) "..." else "",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = GeoTextPrimary
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = GeoGreenPrimary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun LayerFilterChip(
    key: String,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) accentColor else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) accentColor else GeoBorder),
        modifier = Modifier.clickable { onToggle() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp
            ),
            color = if (isSelected) Color.White else GeoTextPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun GisInteractiveCanvas(
    userLat: Double,
    userLng: Double,
    hazardZones: List<HazardZone>,
    facilities: List<EmergencyFacility>,
    safePlaces: List<SafePlace>,
    selectedSafePlace: SafePlace?,
    navigationSteps: List<SafeNavigationStep>,
    isNavigating: Boolean,
    emergencyVehicles: List<EmergencyVehicle>,
    activeLayers: Set<String>,
    onFacilityClick: (EmergencyFacility) -> Unit,
    onHazardClick: (HazardZone) -> Unit,
    onVehicleClick: (EmergencyVehicle) -> Unit,
    onSafePlaceClick: (SafePlace) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, _, _ -> }
            }
    ) {
        val w = size.width
        val h = size.height

        // Clean neutral terrain GIS background
        drawRect(color = Color(0xFFE6EBE0))

        // GIS coordinate grid
        for (i in 0..12) {
            val x = i * (w / 12)
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1.dp.toPx()
            )
        }
        for (j in 0..20) {
            val y = j * (h / 20)
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // River Channel (Godavari River Inundation Ribbon)
        val riverPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, h * 0.35f)
            cubicTo(w * 0.3f, h * 0.38f, w * 0.6f, h * 0.48f, w, h * 0.45f)
        }
        drawPath(
            path = riverPath,
            color = Color(0xFF93C5FD),
            style = Stroke(width = 26.dp.toPx())
        )

        // Draw Inundation Hazard Zones
        if (activeLayers.contains("HAZARDS")) {
            hazardZones.forEach { zone ->
                val centerOffset = Offset(
                    w * (0.35f + (zone.lng.toFloat() - 73.7f) * 1.5f),
                    h * (0.42f + (zone.lat.toFloat() - 19.9f) * 1.8f)
                )
                val color = when (zone.riskLevel) {
                    RiskLevel.CRITICAL -> GeoRedCritical
                    RiskLevel.HIGH -> GeoOrangeWarning
                    RiskLevel.MODERATE -> GeoYellowModerate
                    RiskLevel.SAFE -> GeoGreenPrimary
                }

                // Heatmap Danger Radius Aura
                drawCircle(
                    color = color.copy(alpha = 0.25f),
                    radius = (zone.radiusKm * 28.0).toFloat().dp.toPx(),
                    center = centerOffset
                )
                // Center pin
                drawCircle(
                    color = color.copy(alpha = 0.85f),
                    radius = 8.dp.toPx(),
                    center = centerOffset
                )
            }
        }

        // Draw Safe Route (Turn-by-turn Navigation Corridor)
        val userPos = Offset(w * 0.5f, h * 0.55f)
        val targetPos = if (selectedSafePlace != null) {
            Offset(
                w * (0.28f + (selectedSafePlace.lng.toFloat() - 73.7f) * 1.6f),
                h * (0.32f + (selectedSafePlace.lat.toFloat() - 19.9f) * 1.9f)
            )
        } else {
            Offset(w * 0.28f, h * 0.32f)
        }

        if (activeLayers.contains("SAFE_ROUTE")) {
            // Draw waypoints along route
            val waypoint1 = Offset(userPos.x + (targetPos.x - userPos.x) * 0.3f, userPos.y - 40.dp.toPx())
            val waypoint2 = Offset(userPos.x + (targetPos.x - userPos.x) * 0.7f, targetPos.y + 30.dp.toPx())

            // Elevated bypass escape path
            val routePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(userPos.x, userPos.y)
                lineTo(waypoint1.x, waypoint1.y)
                lineTo(waypoint2.x, waypoint2.y)
                lineTo(targetPos.x, targetPos.y)
            }

            drawPath(
                path = routePath,
                color = GeoGreenPrimary,
                style = Stroke(
                    width = 5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                )
            )

            // Draw route waypoints
            drawCircle(color = GeoGreenPrimary, radius = 5.dp.toPx(), center = waypoint1)
            drawCircle(color = GeoGreenPrimary, radius = 5.dp.toPx(), center = waypoint2)
        }

        // Draw Safe Places & Shelters
        if (activeLayers.contains("SHELTERS")) {
            safePlaces.forEach { sp ->
                val spPos = Offset(
                    w * (0.28f + (sp.lng.toFloat() - 73.7f) * 1.6f),
                    h * (0.32f + (sp.lat.toFloat() - 19.9f) * 1.9f)
                )
                val isSelected = sp.id == selectedSafePlace?.id
                if (isSelected) {
                    drawCircle(color = GeoGreenPrimary.copy(alpha = 0.3f), radius = 18.dp.toPx(), center = spPos)
                }
                drawCircle(color = Color.White, radius = 11.dp.toPx(), center = spPos)
                drawCircle(color = GeoGreenPrimary, radius = 8.dp.toPx(), center = spPos)
            }
        }

        // Draw Hospitals
        if (activeLayers.contains("HOSPITALS")) {
            facilities.filter { it.type == FacilityType.HOSPITAL }.forEach { fac ->
                val fCenter = Offset(
                    w * (0.3f + (fac.lng.toFloat() - 73.7f) * 1.6f),
                    h * (0.38f + (fac.lat.toFloat() - 19.9f) * 1.9f)
                )
                drawCircle(color = Color.White, radius = 10.dp.toPx(), center = fCenter)
                drawCircle(color = GeoOrangeWarning, radius = 7.dp.toPx(), center = fCenter)
            }
        }

        // Draw Nashik CityLink Buses (Live GPS Icons)
        if (activeLayers.contains("BUSES")) {
            emergencyVehicles.filter { it.type == VehicleType.CITYLINK_BUS }.forEach { bus ->
                val busPos = Offset(
                    w * (0.45f + (bus.lng.toFloat() - 73.7f) * 1.7f),
                    h * (0.48f + (bus.lat.toFloat() - 19.9f) * 1.8f)
                )
                // CityLink emerald green vehicle marker
                drawCircle(color = GeoGreenDark.copy(alpha = 0.25f), radius = 16.dp.toPx(), center = busPos)
                drawCircle(color = Color.White, radius = 10.dp.toPx(), center = busPos)
                drawCircle(color = GeoGreenDark, radius = 7.dp.toPx(), center = busPos)
            }
        }

        // Draw 108 Emergency Ambulances (Live Beacon Cross Icons)
        if (activeLayers.contains("AMBULANCES")) {
            emergencyVehicles.filter { it.type == VehicleType.AMBULANCE }.forEach { amb ->
                val ambPos = Offset(
                    w * (0.48f + (amb.lng.toFloat() - 73.7f) * 1.7f),
                    h * (0.52f + (amb.lat.toFloat() - 19.9f) * 1.8f)
                )
                // Flashing red/white beacon marker
                drawCircle(color = GeoRedCritical.copy(alpha = 0.35f), radius = 18.dp.toPx(), center = ambPos)
                drawCircle(color = Color.White, radius = 10.dp.toPx(), center = ambPos)
                drawCircle(color = GeoRedCritical, radius = 7.dp.toPx(), center = ambPos)
            }
        }

        // Current User GPS Location Pin (Pulsing Center Dot)
        drawCircle(color = GeoGreenPrimary.copy(alpha = 0.25f), radius = 22.dp.toPx(), center = userPos)
        drawCircle(color = Color.White, radius = 9.dp.toPx(), center = userPos)
        drawCircle(color = GeoGreenPrimary, radius = 5.dp.toPx(), center = userPos)
    }
}

@Composable
private fun ActiveNavigationCard(
    destination: SafePlace,
    steps: List<SafeNavigationStep>,
    isVoiceActive: Boolean,
    language: Language,
    onToggleVoice: () -> Unit,
    onStop: () -> Unit,
    onOpenGoogleMaps: () -> Unit
) {
    val destName = when (language) {
        Language.HINDI -> destination.nameHi
        Language.MARATHI -> destination.nameMr
        Language.ENGLISH -> destination.name
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GeoGreenPrimary),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Safe Navigation Active
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GeoGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.DirectionsWalk, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = LocalizationProvider.get("active_corridor", language),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = GeoGreenDark
                        )
                        Text(
                            text = "→ $destName (${destination.distanceKm} km)",
                            style = MaterialTheme.typography.labelSmall,
                            color = GeoTextSecondary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Voice Guidance Toggle
                    IconButton(
                        onClick = onToggleVoice,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isVoiceActive) GeoGreenContainer else GeoBackground)
                    ) {
                        Icon(
                            imageVector = if (isVoiceActive) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                            contentDescription = "Voice Guidance",
                            tint = if (isVoiceActive) GeoGreenDark else GeoTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Stop Nav
                    IconButton(
                        onClick = onStop,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(GeoRedContainer)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Stop", tint = GeoRedCritical, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Turn-by-Turn Active Steps
            val activeStep = steps.firstOrNull()
            if (activeStep != null) {
                val stepText = when (language) {
                    Language.HINDI -> activeStep.instructionHi
                    Language.MARATHI -> activeStep.instructionMr
                    Language.ENGLISH -> activeStep.instructionEn
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GeoBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (activeStep.turnType) {
                                "RIGHT" -> Icons.Filled.TurnRight
                                "LEFT" -> Icons.Filled.TurnLeft
                                "DESTINATION" -> Icons.Filled.Flag
                                else -> Icons.Filled.Straight
                            },
                            contentDescription = null,
                            tint = GeoGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Next Step (${activeStep.distanceMeters}m)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = GeoGreenDark
                            )
                            Text(
                                text = stepText,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = GeoTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Google Maps Launcher Button
            Button(
                onClick = onOpenGoogleMaps,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(21.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GeoGreenPrimary)
            ) {
                Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = LocalizationProvider.get("open_google_maps", language),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun VehicleDetailsCard(
    vehicle: EmergencyVehicle,
    language: Language,
    onClose: () -> Unit,
    onCall: () -> Unit
) {
    val isBus = vehicle.type == VehicleType.CITYLINK_BUS
    val routeTitle = when (language) {
        Language.HINDI -> vehicle.routeNameHi
        Language.MARATHI -> vehicle.routeNameMr
        Language.ENGLISH -> vehicle.routeName
    }
    val destName = when (language) {
        Language.HINDI -> vehicle.destinationHi
        Language.MARATHI -> vehicle.destinationMr
        Language.ENGLISH -> vehicle.destination
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isBus) GeoGreenPrimary else GeoRedCritical),
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isBus) GeoGreenPrimary else GeoRedCritical),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBus) Icons.Filled.DirectionsBus else Icons.Filled.MedicalServices,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isBus) LocalizationProvider.get("citylink_bus_label", language) else LocalizationProvider.get("ambulance_label", language),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isBus) GeoGreenDark else GeoRedCritical
                        )
                        Text(
                            text = "${vehicle.vehicleNumber} • ${"%.1f".format(vehicle.distanceKm)} km away",
                            style = MaterialTheme.typography.labelSmall,
                            color = GeoTextSecondary
                        )
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = routeTitle,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = GeoTextPrimary
            )
            Text(
                text = "Destination: $destName | Seats: ${vehicle.availableSeats}/${vehicle.capacity} | Speed: ${vehicle.speedKmH} km/h",
                style = MaterialTheme.typography.labelSmall,
                color = GeoTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onCall,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isBus) GeoGreenPrimary else GeoRedCritical)
            ) {
                Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${LocalizationProvider.get("call_driver", language)} (${vehicle.driverContact})",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun DefaultMapOverviewCard(
    safePlaces: List<SafePlace>,
    selectedSafePlace: SafePlace,
    nearestVehicle: EmergencyVehicle?,
    userLocationName: String,
    language: Language,
    onSelectSafePlace: () -> Unit,
    onStartNavigation: () -> Unit,
    onOpenGoogleMaps: () -> Unit,
    onTriggerSos: () -> Unit
) {
    val destName = when (language) {
        Language.HINDI -> selectedSafePlace.nameHi
        Language.MARATHI -> selectedSafePlace.nameMr
        Language.ENGLISH -> selectedSafePlace.name
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Nearest Transit Radar (CityLink / Ambulance)
            if (nearestVehicle != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GeoBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder),
                    modifier = Modifier.clickable { onSelectSafePlace() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (nearestVehicle.type == VehicleType.CITYLINK_BUS) Icons.Filled.DirectionsBus else Icons.Filled.MedicalServices,
                                contentDescription = null,
                                tint = if (nearestVehicle.type == VehicleType.CITYLINK_BUS) GeoGreenPrimary else GeoRedCritical,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = LocalizationProvider.get("nearest_evac_vehicle", language),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = GeoTextSecondary
                                )
                                Text(
                                    text = "${nearestVehicle.vehicleNumber} (${"%.1f".format(nearestVehicle.distanceKm)} km) • ${nearestVehicle.availableSeats} seats",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GeoTextPrimary
                                )
                            }
                        }

                        Text(
                            text = "ETA ~${(nearestVehicle.distanceKm * 3.5).toInt() + 2}m",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GeoGreenDark)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Safe Corridor Target Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GeoGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text(
                            text = LocalizationProvider.get("active_corridor", language),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Target: $destName",
                            style = MaterialTheme.typography.labelSmall,
                            color = GeoTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GeoGreenContainer
                ) {
                    Text(
                        text = LocalizationProvider.get("safe_evac_path", language),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GeoGreenDark, fontSize = 9.sp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = LocalizationProvider.get("avoiding_floods", language),
                style = MaterialTheme.typography.bodySmall,
                color = GeoTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStartNavigation,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(19.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeoGreenPrimary)
                ) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp), tint = GeoGreenPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(LocalizationProvider.get("start_voice_nav", language), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoGreenPrimary)
                }

                Button(
                    onClick = onOpenGoogleMaps,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(19.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoGreenDark)
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SafePlacesSelectionSheet(
    safePlaces: List<SafePlace>,
    selected: SafePlace?,
    language: Language,
    onDismiss: () -> Unit,
    onSelect: (SafePlace) -> Unit,
    onStartNav: (SafePlace) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = LocalizationProvider.get("safe_places_title", language),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(safePlaces) { place ->
                    val isSelected = place.id == selected?.id
                    val name = when (language) {
                        Language.HINDI -> place.nameHi
                        Language.MARATHI -> place.nameMr
                        Language.ENGLISH -> place.name
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) GeoGreenContainer else Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GeoGreenPrimary else GeoBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(place) }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) GeoGreenDark else GeoTextPrimary
                                )
                                Text(
                                    text = "${place.distanceKm} km",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GeoGreenPrimary)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${place.address} • Beds: ${place.availableBeds}/${place.capacityBeds} • Elevation: ${place.safeZoneElevationMeters}m",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = GeoTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { onStartNav(place) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GeoGreenPrimary)
                            ) {
                                Text(LocalizationProvider.get("start_voice_nav", language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun FacilityDetailsCard(
    facility: EmergencyFacility,
    language: Language,
    onClose: () -> Unit,
    onNavigate: () -> Unit,
    onOpenGoogleMaps: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(facility.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
            Text("${facility.type.name} • Beds: ${facility.availableBeds}/${facility.totalBeds} • Phone: ${facility.contactNumber}", style = MaterialTheme.typography.bodySmall, color = GeoTextSecondary)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onNavigate,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoGreenPrimary)
                ) {
                    Text(LocalizationProvider.get("navigate_to_facility", language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onOpenGoogleMaps,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeoGreenPrimary)
                ) {
                    Text("Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoGreenPrimary)
                }
            }
        }
    }
}

@Composable
private fun HazardDetailsCard(
    hazard: HazardZone,
    onClose: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = GeoRedContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoRedBorder),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(hazard.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GeoRedText))
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
            Text("Risk Level: ${hazard.riskLevel.label} • Radius: ${hazard.radiusKm} km • ${hazard.description}", style = MaterialTheme.typography.bodySmall, color = GeoRedText)
        }
    }
}

@Composable
private fun LocationPresetButton(
    label: String,
    lat: Double,
    lng: Double,
    onClick: (Double, Double, String) -> Unit
) {
    OutlinedButton(
        onClick = { onClick(lat, lng, label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
    }
}
