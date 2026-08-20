package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.LocalizationProvider
import com.example.model.*
import com.example.ui.components.SimulationBadge
import com.example.ui.theme.*
import kotlin.math.roundToInt

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

    // Map Pan and Zoom State
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val buses = emergencyVehicles.filter { it.type == VehicleType.CITYLINK_BUS }
    val ambulances = emergencyVehicles.filter { it.type == VehicleType.AMBULANCE }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8ECE9))
            .testTag("map_screen")
    ) {
        // 1. Fullscreen Map View with Base Map Image & GIS Overlays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.85f, 2.5f)
                        offsetX = (offsetX + pan.x).coerceIn(-400f, 400f)
                        offsetY = (offsetY + pan.y).coerceIn(-400f, 400f)
                    }
                }
        ) {
            // Real Nashik Street Map Background
            Image(
                painter = painterResource(id = R.drawable.gis_map_bg),
                contentDescription = "Nashik GIS Map",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Polyline Navigation & Flood Zone Vectors Canvas
            GisVectorOverlayCanvas(
                hazardZones = hazardZones,
                selectedSafePlace = selectedSafePlace,
                isNavigating = isNavigating,
                activeLayers = activeLayers
            )

            // Interactive Pin Overlays
            Box(modifier = Modifier.fillMaxSize()) {
                // User Location Pin (CIDCO Sector)
                InteractiveUserLocationPin(
                    userLocationName = userLocationName,
                    language = language
                )

                // CityLink Nashik Buses Pins
                if (activeLayers.contains("BUSES") || activeLayers.contains("ALL")) {
                    buses.forEachIndexed { index, bus ->
                        InteractiveCityLinkBusPin(
                            bus = bus,
                            index = index,
                            isSelected = selectedVehicle?.id == bus.id,
                            language = language,
                            onClick = {
                                onSelectVehicle(bus)
                                selectedFacility = null
                                selectedHazard = null
                            }
                        )
                    }
                }

                // 108 Emergency Ambulances Pins
                if (activeLayers.contains("AMBULANCES") || activeLayers.contains("ALL")) {
                    ambulances.forEachIndexed { index, amb ->
                        InteractiveAmbulancePin(
                            ambulance = amb,
                            index = index,
                            isSelected = selectedVehicle?.id == amb.id,
                            language = language,
                            onClick = {
                                onSelectVehicle(amb)
                                selectedFacility = null
                                selectedHazard = null
                            }
                        )
                    }
                }

                // Safe Havens / Shelters Pins
                if (activeLayers.contains("SHELTERS") || activeLayers.contains("ALL")) {
                    safePlaces.forEachIndexed { index, place ->
                        InteractiveSafePlacePin(
                            place = place,
                            index = index,
                            isSelected = selectedSafePlace?.id == place.id,
                            language = language,
                            onClick = {
                                onSelectSafePlace(place)
                                selectedFacility = null
                                selectedHazard = null
                                onSelectVehicle(null)
                            }
                        )
                    }
                }

                // Hospitals Pins
                if (activeLayers.contains("HOSPITALS") || activeLayers.contains("ALL")) {
                    facilities.filter { it.type == FacilityType.HOSPITAL }.forEachIndexed { index, fac ->
                        InteractiveHospitalPin(
                            facility = fac,
                            index = index,
                            isSelected = selectedFacility?.id == fac.id,
                            onClick = {
                                selectedFacility = fac
                                selectedHazard = null
                                onSelectVehicle(null)
                            }
                        )
                    }
                }
            }
        }

        // 2. Top Bar: Title Badge, GPS Relocate & Quick Filter Chips
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GeoDarkCard.copy(alpha = 0.92f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeoDarkSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(GeoGreenLight)
                        )
                        Text(
                            text = "NASHIK LIVE GIS RADAR",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
                            color = GeoDarkText
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Safe Haven Shortcut Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GeoGreenPrimary,
                        modifier = Modifier.clickable { showSafePlacesSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Text(
                                text = LocalizationProvider.get("nav_shelters", language),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp)
                            )
                        }
                    }

                    // Simulate GPS Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder),
                        modifier = Modifier.clickable { showRelocateSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.MyLocation, contentDescription = null, tint = GeoGreenPrimary, modifier = Modifier.size(13.dp))
                            Text(
                                text = "GPS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GeoTextPrimary, fontSize = 10.sp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Multi-Layer Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    val isAll = activeLayers.contains("ALL")
                    LayerChip("ALL", "All Layers", isAll, GeoDarkCard) { onToggleLayer("ALL") }
                }
                item {
                    val isBus = activeLayers.contains("BUSES")
                    LayerChip("BUSES", "🚌 CityLink (${buses.size})", isBus, GeoGreenPrimary) { onToggleLayer("BUSES") }
                }
                item {
                    val isAmb = activeLayers.contains("AMBULANCES")
                    LayerChip("AMBULANCES", "🚑 108 (${ambulances.size})", isAmb, GeoRedCritical) { onToggleLayer("AMBULANCES") }
                }
                item {
                    val isShelter = activeLayers.contains("SHELTERS")
                    LayerChip("SHELTERS", "🛡️ Shelters (${safePlaces.size})", isShelter, GeoGreenDark) { onToggleLayer("SHELTERS") }
                }
                item {
                    val isHosp = activeLayers.contains("HOSPITALS")
                    LayerChip("HOSPITALS", "🏥 Hospitals (${facilities.size})", isHosp, GeoOrangeWarning) { onToggleLayer("HOSPITALS") }
                }
                item {
                    val isRoute = activeLayers.contains("SAFE_ROUTE")
                    LayerChip("SAFE_ROUTE", "📍 Safe Route", isRoute, Color(0xFF1A73E8)) { onToggleLayer("SAFE_ROUTE") }
                }
                item {
                    val isHazard = activeLayers.contains("HAZARDS")
                    LayerChip("HAZARDS", "🌊 Flood Zones", isHazard, GeoRedCritical) { onToggleLayer("HAZARDS") }
                }
            }
        }

        // 3. Floating Right Control Buttons (Center GPS, Safe Places, Emergency SOS)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 95.dp, end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingMapButton(
                icon = Icons.Filled.Navigation,
                label = "Center",
                color = GeoGreenPrimary,
                onClick = {
                    offsetX = 0f
                    offsetY = 0f
                    scale = 1f
                }
            )

            FloatingMapButton(
                icon = Icons.Filled.DirectionsBus,
                label = "Buses",
                color = GeoGreenDark,
                onClick = {
                    val firstBus = buses.firstOrNull()
                    if (firstBus != null) onSelectVehicle(firstBus)
                }
            )

            FloatingMapButton(
                icon = Icons.Filled.Emergency,
                label = "SOS",
                color = GeoRedCritical,
                onClick = onTriggerSos
            )
        }

        // 4. Bottom Active Context Floating HUD (Navigation, Vehicle Details, Safe Place, or Overview)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 85.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isNavigating && selectedSafePlace != null) {
                // Turn-by-Turn Google Maps Style Navigation HUD
                GoogleMapsNavigationHudCard(
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
                // CityLink Bus or 108 Ambulance Details Card
                LiveVehicleDetailsCard(
                    vehicle = selectedVehicle,
                    language = language,
                    onClose = { onSelectVehicle(null) },
                    onCallDriver = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${selectedVehicle.driverContact.filter { it.isDigit() || it == '+' }}"))
                        context.startActivity(dialIntent)
                    }
                )
            } else if (selectedFacility != null) {
                // Hospital Details Card
                HospitalDetailsCard(
                    facility = selectedFacility!!,
                    language = language,
                    onClose = { selectedFacility = null },
                    onNavigate = {
                        val matched = safePlaces.firstOrNull { it.name == selectedFacility!!.name }
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
                                safeZoneElevationMeters = 615,
                                contactNumber = selectedFacility!!.contactNumber
                            )
                        onStartNavigation(matched)
                    },
                    onOpenGoogleMaps = {
                        onOpenGoogleMaps(context, selectedFacility!!.lat, selectedFacility!!.lng, selectedFacility!!.name)
                    }
                )
            } else {
                // Default Hub: Nearest Safe Haven & CityLink Evacuation Radar
                DefaultMapRadarCard(
                    safePlaces = safePlaces,
                    selectedSafePlace = selectedSafePlace ?: safePlaces.first(),
                    nearestBus = buses.firstOrNull(),
                    userLocationName = userLocationName,
                    language = language,
                    onSelectSafePlace = { showSafePlacesSheet = true },
                    onStartNavigation = { onStartNavigation(selectedSafePlace ?: safePlaces.first()) },
                    onOpenGoogleMaps = {
                        val dest = selectedSafePlace ?: safePlaces.first()
                        onOpenGoogleMaps(context, dest.lat, dest.lng, dest.name)
                    }
                )
            }
        }

        // Safe Places Selection Sheet
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

        // GPS Relocation Dialog
        if (showRelocateSheet) {
            GpsRelocateDialog(
                language = language,
                onDismiss = { showRelocateSheet = false },
                onSelectLocation = { lat, lng, name ->
                    onSimulateRelocate(lat, lng, name)
                    showRelocateSheet = false
                }
            )
        }
    }
}

// -------------------------------------------------------------
// Interactive Pins & GIS Canvas Overlays
// -------------------------------------------------------------

@Composable
private fun BoxScope.InteractiveUserLocationPin(
    userLocationName: String,
    language: Language
) {
    val infiniteTransition = rememberInfiniteTransition(label = "user_pulse")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 24f,
        targetValue = 44f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "user_pulse_anim"
    )

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = (-40).dp, y = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Radar pulse
        Box(
            modifier = Modifier
                .size(pulseSize.dp)
                .clip(CircleShape)
                .background(GeoGreenPrimary.copy(alpha = 0.28f))
        )

        // Center Pin
        Surface(
            shape = CircleShape,
            color = GeoGreenPrimary,
            border = androidx.compose.foundation.BorderStroke(2.5.dp, Color.White),
            shadowElevation = 6.dp,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
            }
        }

        // Location Label Pill
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = GeoDarkCard.copy(alpha = 0.9f),
            modifier = Modifier
                .offset(y = (-24).dp)
                .shadow(4.dp, RoundedCornerShape(10.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(GeoGreenLight))
                Text(
                    text = "You (${userLocationName.take(16)})",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun BoxScope.InteractiveCityLinkBusPin(
    bus: EmergencyVehicle,
    index: Int,
    isSelected: Boolean,
    language: Language,
    onClick: () -> Unit
) {
    // Dynamic positions distributed across Nashik grid
    val (xOff, yOff) = when (index % 4) {
        0 -> Pair((-80).dp, (-60).dp)
        1 -> Pair(60.dp, (-20).dp)
        2 -> Pair((-20).dp, 160.dp)
        else -> Pair(90.dp, 110.dp)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bus_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "bus_alpha"
    )

    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = xOff, y = yOff)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tag Pill: CityLink Route & Seats
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) GeoGreenDark else GeoGreenPrimary,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(Icons.Filled.DirectionsBus, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                Text(
                    text = "CityLink ${bus.vehicleNumber.takeLast(4)} • ${bus.availableSeats} seats",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Emerald Circular Bus Pin
        Surface(
            shape = CircleShape,
            color = if (isSelected) GeoGreenDark else GeoGreenPrimary,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
            shadowElevation = 8.dp,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.DirectionsBus,
                    contentDescription = "CityLink Bus",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun BoxScope.InteractiveAmbulancePin(
    ambulance: EmergencyVehicle,
    index: Int,
    isSelected: Boolean,
    language: Language,
    onClick: () -> Unit
) {
    val (xOff, yOff) = when (index % 3) {
        0 -> Pair(40.dp, (-90).dp)
        1 -> Pair((-110).dp, 40.dp)
        else -> Pair(120.dp, 30.dp)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "siren_pulse")
    val sirenRing by infiniteTransition.animateFloat(
        initialValue = 28f,
        targetValue = 46f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "siren"
    )

    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = xOff, y = yOff)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Flashing Siren Ring
        Box(
            modifier = Modifier
                .size(sirenRing.dp)
                .clip(CircleShape)
                .background(GeoRedCritical.copy(alpha = 0.3f))
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GeoRedCritical,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(Icons.Filled.LocalHospital, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                    Text(
                        text = "108 ALS • ${ambulance.speedKmH}km/h",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Surface(
                shape = CircleShape,
                color = GeoRedCritical,
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                shadowElevation = 8.dp,
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.MedicalServices,
                        contentDescription = "108 Ambulance",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.InteractiveSafePlacePin(
    place: SafePlace,
    index: Int,
    isSelected: Boolean,
    language: Language,
    onClick: () -> Unit
) {
    val (xOff, yOff) = when (index % 5) {
        0 -> Pair((-50).dp, (-140).dp) // KTHM College
        1 -> Pair(10.dp, (-70).dp)   // Shivaji Nagar
        2 -> Pair((-120).dp, (-30).dp) // Mahatma Nagar
        3 -> Pair(90.dp, (-110).dp)  // Civil Hospital
        else -> Pair((-40).dp, 130.dp) // CIDCO Haven
    }

    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = xOff, y = yOff)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) GeoGreenDark else Color.White,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, GeoGreenPrimary),
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else GeoGreenDark,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "${place.name.take(12)} (${place.safeZoneElevationMeters}m)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = if (isSelected) Color.White else GeoTextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Surface(
            shape = CircleShape,
            color = if (isSelected) GeoGreenDark else GeoGreenPrimary,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
            shadowElevation = 8.dp,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.HomeWork,
                    contentDescription = place.name,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun BoxScope.InteractiveHospitalPin(
    facility: EmergencyFacility,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (xOff, yOff) = when (index % 4) {
        0 -> Pair(70.dp, (-60).dp)
        1 -> Pair((-90).dp, 90.dp)
        2 -> Pair(30.dp, 70.dp)
        else -> Pair(110.dp, 150.dp)
    }

    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = xOff, y = yOff)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) GeoOrangeWarning else Color.White,
            border = androidx.compose.foundation.BorderStroke(2.dp, GeoOrangeWarning),
            shadowElevation = 6.dp,
            modifier = Modifier.size(26.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "H",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (isSelected) Color.White else GeoOrangeWarning
                )
            }
        }
    }
}

// -------------------------------------------------------------
// GIS Vector Overlay: Turn-by-Turn Route & Flood Hazard Waves
// -------------------------------------------------------------

@Composable
private fun GisVectorOverlayCanvas(
    hazardZones: List<HazardZone>,
    selectedSafePlace: SafePlace?,
    isNavigating: Boolean,
    activeLayers: Set<String>
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Google Maps Styled Navigation Route Polyline
        if (activeLayers.contains("SAFE_ROUTE") || activeLayers.contains("ALL") || isNavigating) {
            val userCenter = Offset(w * 0.42f, h * 0.58f)
            val destCenter = Offset(w * 0.38f, h * 0.35f)

            val turn1 = Offset(w * 0.44f, h * 0.51f)
            val turn2 = Offset(w * 0.35f, h * 0.45f)
            val turn3 = Offset(w * 0.39f, h * 0.39f)

            val navPath = Path().apply {
                moveTo(userCenter.x, userCenter.y)
                lineTo(turn1.x, turn1.y)
                lineTo(turn2.x, turn2.y)
                lineTo(turn3.x, turn3.y)
                lineTo(destCenter.x, destCenter.y)
            }

            // Outer High-Contrast White Glow
            drawPath(
                path = navPath,
                color = Color.White.copy(alpha = 0.95f),
                style = Stroke(width = 10.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Primary Google Maps Blue Route Line
            drawPath(
                path = navPath,
                color = Color(0xFF1A73E8),
                style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Maneuver Turn Waypoints
            listOf(turn1, turn2, turn3).forEach { p ->
                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = p)
                drawCircle(color = Color(0xFF1A73E8), radius = 3.dp.toPx(), center = p)
            }
        }

        // 2. Godavari Inundation Hazard Zones
        if (activeLayers.contains("HAZARDS") || activeLayers.contains("ALL")) {
            val hazardCenters = listOf(
                Offset(w * 0.55f, h * 0.38f),
                Offset(w * 0.62f, h * 0.48f),
                Offset(w * 0.72f, h * 0.42f)
            )

            hazardCenters.forEach { center ->
                drawCircle(
                    color = GeoRedCritical.copy(alpha = 0.22f),
                    radius = 42.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = GeoRedCritical.copy(alpha = 0.65f),
                    radius = 6.dp.toPx(),
                    center = center
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Top Layer Filter & Floating Action Buttons
// -------------------------------------------------------------

@Composable
private fun LayerChip(
    key: String,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) accentColor else Color.White.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) accentColor else GeoBorder),
        shadowElevation = 3.dp,
        modifier = Modifier.clickable { onToggle() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 10.sp
            ),
            color = if (isSelected) Color.White else GeoTextPrimary,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun FloatingMapButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder),
        shadowElevation = 6.dp,
        modifier = Modifier
            .size(44.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        }
    }
}

// -------------------------------------------------------------
// Google Maps Turn-by-Turn HUD & Context Cards
// -------------------------------------------------------------

@Composable
private fun GoogleMapsNavigationHudCard(
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

    val activeStep = steps.firstOrNull()
    val stepText = when (language) {
        Language.HINDI -> activeStep?.instructionHi ?: "सुरक्षित मार्ग पर आगे बढ़ें।"
        Language.MARATHI -> activeStep?.instructionMr ?: "सुरक्षित मार्गाने पुढे चला."
        Language.ENGLISH -> activeStep?.instructionEn ?: "Proceed along elevated corridor."
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1A73E8)),
        shadowElevation = 10.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Google Maps Header
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
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1A73E8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "TURN-BY-TURN SAFE ROUTE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
                            color = Color(0xFF1A73E8)
                        )
                        Text(
                            text = "→ $destName (${destination.distanceKm} km)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = GeoTextPrimary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onToggleVoice,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isVoiceActive) GeoGreenContainer else GeoBackground)
                    ) {
                        Icon(
                            imageVector = if (isVoiceActive) Icons.Filled.VolumeUp else Icons.Filled.VolumeOff,
                            contentDescription = "Voice",
                            tint = if (isVoiceActive) GeoGreenDark else GeoTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onStop,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GeoRedContainer)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = GeoRedCritical, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step Instruction Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
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
                        imageVector = when (activeStep?.turnType) {
                            "RIGHT" -> Icons.Filled.TurnRight
                            "LEFT" -> Icons.Filled.TurnLeft
                            "DESTINATION" -> Icons.Filled.Flag
                            else -> Icons.Filled.Straight
                        },
                        contentDescription = null,
                        tint = Color(0xFF1A73E8),
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Next Maneuver (${activeStep?.distanceMeters ?: 200}m)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFF1A73E8)
                        )
                        Text(
                            text = stepText,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = GeoTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct Google Maps Directions Launcher Button
            Button(
                onClick = onOpenGoogleMaps,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(21.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
            ) {
                Icon(Icons.Filled.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Launch Live Route in Google Maps",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
            }
        }
    }
}

@Composable
private fun LiveVehicleDetailsCard(
    vehicle: EmergencyVehicle,
    language: Language,
    onClose: () -> Unit,
    onCallDriver: () -> Unit
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
        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isBus) GeoGreenPrimary else GeoRedCritical),
        shadowElevation = 8.dp
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
                            text = if (isBus) "NASHIK CITYLINK EVACUATION BUS" else "108 EMERGENCY AMBULANCE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
                            color = if (isBus) GeoGreenDark else GeoRedCritical
                        )
                        Text(
                            text = "${vehicle.vehicleNumber} • ${"%.1f".format(vehicle.distanceKm)} km away",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = GeoTextPrimary
                        )
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = routeTitle, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = GeoTextPrimary)
            Text(
                text = "Destination: $destName | Seats: ${vehicle.availableSeats}/${vehicle.capacity} | Speed: ${vehicle.speedKmH} km/h",
                style = MaterialTheme.typography.labelSmall,
                color = GeoTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onCallDriver,
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
private fun HospitalDetailsCard(
    facility: EmergencyFacility,
    language: Language,
    onClose: () -> Unit,
    onNavigate: () -> Unit,
    onOpenGoogleMaps: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoOrangeWarning),
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GeoOrangeWarning),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("H", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(
                            text = facility.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = GeoTextPrimary
                        )
                        Text(
                            text = "${facility.availableBeds}/${facility.totalBeds} Beds • ${facility.distanceKm} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = GeoTextSecondary
                        )
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenGoogleMaps,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(19.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1A73E8))
                ) {
                    Text("Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A73E8))
                }

                Button(
                    onClick = onNavigate,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(19.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoGreenPrimary)
                ) {
                    Text("Safe Route", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DefaultMapRadarCard(
    safePlaces: List<SafePlace>,
    selectedSafePlace: SafePlace,
    nearestBus: EmergencyVehicle?,
    userLocationName: String,
    language: Language,
    onSelectSafePlace: () -> Unit,
    onStartNavigation: () -> Unit,
    onOpenGoogleMaps: () -> Unit
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
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Designated Safe Haven
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).clickable { onSelectSafePlace() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(GeoGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = GeoGreenDark, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            text = "DESIGNATED HIGH GROUND HAVEN",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp),
                            color = GeoGreenDark
                        )
                        Text(
                            text = destName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = GeoTextPrimary
                        )
                        Text(
                            text = "${selectedSafePlace.safeZoneElevationMeters}m Elevation • ${selectedSafePlace.availableBeds} beds available",
                            style = MaterialTheme.typography.labelSmall,
                            color = GeoTextSecondary
                        )
                    }
                }

                IconButton(onClick = onSelectSafePlace) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Change Destination")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Safe Navigation vs Google Maps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenGoogleMaps,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1A73E8))
                ) {
                    Icon(Icons.Filled.Map, contentDescription = null, tint = Color(0xFF1A73E8), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A73E8))
                }

                Button(
                    onClick = onStartNavigation,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoGreenPrimary)
                ) {
                    Icon(Icons.Filled.DirectionsWalk, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Safe Route", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Dialogs: Safe Places Bottom Sheet & GPS Relocation
// -------------------------------------------------------------

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = GeoGreenPrimary)
                Text(LocalizationProvider.get("nav_shelters", language), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(safePlaces) { place ->
                    val isCurrent = place.id == selected?.id
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurrent) GeoGreenContainer else GeoBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrent) GeoGreenPrimary else GeoBorder),
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(place) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(place.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    "${place.safeZoneElevationMeters}m Elevation • ${place.availableBeds} beds",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GeoTextSecondary
                                )
                            }
                            Button(
                                onClick = { onStartNav(place) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GeoGreenPrimary),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Navigate", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(LocalizationProvider.get("close", language))
            }
        }
    )
}

@Composable
private fun GpsRelocateDialog(
    language: Language,
    onDismiss: () -> Unit,
    onSelectLocation: (Double, Double, String) -> Unit
) {
    val locations = listOf(
        Triple(19.9820, 73.7450, "Kamatwade (Near Godavari)"),
        Triple(20.0050, 73.7850, "Ramkund - Goda Ghat"),
        Triple(19.9970, 73.7900, "Panchavati Lowland"),
        Triple(19.9700, 73.7600, "CIDCO High Plateau (Safe Zone)"),
        Triple(20.0150, 73.7550, "Gangapur High Ground Haven")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(LocalizationProvider.get("simulate_gps", language), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select an area in Nashik to test live flood risk proximity and dynamic bus/shelter routing:", style = MaterialTheme.typography.bodySmall)
                locations.forEach { (lat, lng, name) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GeoBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder),
                        modifier = Modifier.fillMaxWidth().clickable { onSelectLocation(lat, lng, name) }
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(LocalizationProvider.get("close", language))
            }
        }
    )
}
