package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
    activeLayers: Set<String>,
    onToggleLayer: (String) -> Unit,
    onSimulateRelocate: (Double, Double, String) -> Unit,
    language: Language,
    onTriggerSos: () -> Unit
) {
    var selectedFacility by remember { mutableStateOf<EmergencyFacility?>(null) }
    var selectedHazard by remember { mutableStateOf<HazardZone?>(null) }
    var showRelocateSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBackground)
            .testTag("map_screen")
    ) {
        // Fullscreen GIS Canvas
        GisInteractiveCanvas(
            userLat = userLat,
            userLng = userLng,
            hazardZones = hazardZones,
            facilities = facilities,
            activeLayers = activeLayers,
            onFacilityClick = { selectedFacility = it },
            onHazardClick = { selectedHazard = it }
        )

        // Top Layer Controls & Simulation Badge
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
                SimulationBadge("SIMULATION GIS MAP")

                // Relocate GPS Pin button
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
                        Text("Simulate GPS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = GeoTextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LayerFilterChip("HAZARDS", "Hazard Zones", activeLayers.contains("HAZARDS"), GeoRedCritical) { onToggleLayer("HAZARDS") }
                LayerFilterChip("SHELTERS", "Shelters", activeLayers.contains("SHELTERS"), GeoGreenPrimary) { onToggleLayer("SHELTERS") }
                LayerFilterChip("HOSPITALS", "Hospitals", activeLayers.contains("HOSPITALS"), GeoOrangeWarning) { onToggleLayer("HOSPITALS") }
                LayerFilterChip("SAFE_ROUTE", "Safe Route", activeLayers.contains("SAFE_ROUTE"), GeoGreenDark) { onToggleLayer("SAFE_ROUTE") }
            }
        }

        // Bottom Details Floating Card (when item selected or showing nearest safe route)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 85.dp)
        ) {
            if (selectedFacility != null) {
                FacilityDetailsCard(
                    facility = selectedFacility!!,
                    onClose = { selectedFacility = null },
                    onNavigate = {}
                )
            } else if (selectedHazard != null) {
                HazardDetailsCard(
                    hazard = selectedHazard!!,
                    onClose = { selectedHazard = null }
                )
            } else {
                DefaultSafeRouteSummaryCard(
                    userLocationName = userLocationName,
                    onTriggerSos = onTriggerSos
                )
            }
        }

        // GPS Relocation Sheet Dialog
        if (showRelocateSheet) {
            AlertDialog(
                onDismissRequest = { showRelocateSheet = false },
                title = { Text("Simulate GPS Location", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select a test sector to observe dynamic vulnerability changes:", style = MaterialTheme.typography.bodySmall)

                        LocationPresetButton("Ramkund Godavari Bank (High Danger)", 20.0050, 73.7880) { lat, lng, name ->
                            onSimulateRelocate(lat, lng, name)
                            showRelocateSheet = false
                        }
                        LocationPresetButton("Trimbakeshwar Lowland (Critical Flood)", 19.9390, 73.5420) { lat, lng, name ->
                            onSimulateRelocate(lat, lng, name)
                            showRelocateSheet = false
                        }
                        LocationPresetButton("KTHM College Relief Shelter (Safe Zone)", 20.0110, 73.7740) { lat, lng, name ->
                            onSimulateRelocate(lat, lng, name)
                            showRelocateSheet = false
                        }
                        LocationPresetButton("Nashik Elevated Ridge (Safe Zone)", 19.9850, 73.8050) { lat, lng, name ->
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
    activeLayers: Set<String>,
    onFacilityClick: (EmergencyFacility) -> Unit,
    onHazardClick: (HazardZone) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, _, _ ->
                    // Interactive pan/zoom gestures supported
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // Clean neutral map background
        drawRect(color = Color(0xFFE5EADF))

        // Grid lines to represent GIS coordinates
        for (i in 0..12) {
            val x = i * (w / 12)
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1.dp.toPx()
            )
        }
        for (j in 0..20) {
            val y = j * (h / 20)
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // River Channel (Godavari Simulated River Bend)
        val riverPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, h * 0.35f)
            cubicTo(w * 0.3f, h * 0.38f, w * 0.6f, h * 0.48f, w, h * 0.45f)
        }
        drawPath(
            path = riverPath,
            color = Color(0xFF93C5FD),
            style = Stroke(width = 24.dp.toPx())
        )

        // Draw Hazard Zones (Inundation Danger Heatmap)
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

                // Outer radius aura
                drawCircle(
                    color = color.copy(alpha = 0.25f),
                    radius = (zone.radiusKm * 30.0).toFloat().dp.toPx(),
                    center = centerOffset
                )
                // Center core
                drawCircle(
                    color = color.copy(alpha = 0.85f),
                    radius = 8.dp.toPx(),
                    center = centerOffset
                )
            }
        }

        // Safe Route Guidance (Dashed Line towards High Ground)
        if (activeLayers.contains("SAFE_ROUTE")) {
            val userCenter = Offset(w * 0.5f, h * 0.55f)
            val shelterCenter = Offset(w * 0.28f, h * 0.32f)

            drawLine(
                color = GeoGreenPrimary,
                start = userCenter,
                end = shelterCenter,
                strokeWidth = 4.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )
        }

        // Draw Facilities (Shelters & Hospitals)
        facilities.forEach { facility ->
            if ((facility.type == FacilityType.HOSPITAL && activeLayers.contains("HOSPITALS")) ||
                (facility.type == FacilityType.SHELTER && activeLayers.contains("SHELTERS"))
            ) {
                val fCenter = Offset(
                    w * (0.3f + (facility.lng.toFloat() - 73.7f) * 1.6f),
                    h * (0.38f + (facility.lat.toFloat() - 19.9f) * 1.9f)
                )
                val iconBg = if (facility.type == FacilityType.HOSPITAL) GeoOrangeWarning else GeoGreenPrimary

                drawCircle(color = Color.White, radius = 10.dp.toPx(), center = fCenter)
                drawCircle(color = iconBg, radius = 8.dp.toPx(), center = fCenter)
            }
        }

        // Current User GPS Location Pin (Pulsing Dot)
        val userPos = Offset(w * 0.5f, h * 0.55f)
        drawCircle(color = GeoGreenPrimary.copy(alpha = 0.3f), radius = 22.dp.toPx(), center = userPos)
        drawCircle(color = Color.White, radius = 10.dp.toPx(), center = userPos)
        drawCircle(color = GeoGreenPrimary, radius = 6.dp.toPx(), center = userPos)
    }
}

@Composable
private fun DefaultSafeRouteSummaryCard(
    userLocationName: String,
    onTriggerSos: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
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
                        Text("Active Safe Corridor", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(userLocationName, style = MaterialTheme.typography.labelSmall, color = GeoTextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GeoGreenContainer
                ) {
                    Text(
                        text = "SAFE EVAC PATH",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GeoGreenDark, fontSize = 9.sp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Proceed South-West toward KTHM Relief Center (Elevated Zone). Avoid Godavari Ghat Lowlands.",
                style = MaterialTheme.typography.bodySmall,
                color = GeoTextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(19.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeoGreenPrimary)
                ) {
                    Text("Turn-by-Turn GPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoGreenPrimary)
                }

                Button(
                    onClick = onTriggerSos,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(19.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoRedCritical)
                ) {
                    Icon(Icons.Filled.Emergency, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Request Rescue", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FacilityDetailsCard(
    facility: EmergencyFacility,
    onClose: () -> Unit,
    onNavigate: () -> Unit
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
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onNavigate,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GeoGreenPrimary)
            ) {
                Text("Navigate To Safe Facility", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
