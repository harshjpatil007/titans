package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalizationProvider
import com.example.model.*
import com.example.ui.components.SimulationBadge
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SosScreen(
    activeSos: SosRecord?,
    personalRiskScore: Int,
    userLocationName: String,
    userLat: Double,
    userLng: Double,
    nearestHazardName: String,
    language: Language,
    onTriggerSos: (medicalNeeds: String, message: String) -> Unit,
    onCancelSos: () -> Unit
) {
    var selectedNeeds by remember { mutableStateOf(setOf<String>()) }
    var additionalNotes by remember { mutableStateOf("") }
    var isCountingDown by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableIntStateOf(3) }
    var showSmsPayloadDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isCountingDown) {
        if (isCountingDown) {
            countdownSeconds = 3
            while (countdownSeconds > 0) {
                delay(1000)
            }
            isCountingDown = false
            val needsStr = if (selectedNeeds.isEmpty()) "Standard Emergency Evacuation" else selectedNeeds.joinToString(", ")
            val notesStr = if (additionalNotes.isBlank()) "Immediate evacuation beacon dispatched." else additionalNotes
            onTriggerSos(needsStr, notesStr)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 90.dp)
            .testTag("sos_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SimulationBadge(LocalizationProvider.get("sos_title", language))

        if (activeSos != null) {
            // ACTIVE SOS BEACON MONITOR
            ActiveSosTrackingCard(
                sosRecord = activeSos,
                language = language,
                onCancel = onCancelSos,
                onShowSms = { showSmsPayloadDialog = true }
            )
        } else {
            // SOS TRIGGER DISPATCH INTERFACE
            SosTriggerInterface(
                isCountingDown = isCountingDown,
                countdownSeconds = countdownSeconds,
                userLocationName = userLocationName,
                userLat = userLat,
                userLng = userLng,
                personalRiskScore = personalRiskScore,
                selectedNeeds = selectedNeeds,
                language = language,
                onToggleNeed = { need ->
                    selectedNeeds = if (selectedNeeds.contains(need)) selectedNeeds - need else selectedNeeds + need
                },
                additionalNotes = additionalNotes,
                onNotesChange = { additionalNotes = it },
                onStartSos = { isCountingDown = true },
                onCancelCountdown = { isCountingDown = false }
            )
        }

        // Offline Emergency Fallback Card
        OfflineSmsFallbackCard(
            userLat = userLat,
            userLng = userLng,
            onShowSmsDialog = { showSmsPayloadDialog = true }
        )
    }

    if (showSmsPayloadDialog) {
        val payload = "RAKSHAI-SOS|LAT:%.4f|LNG:%.4f|RISK:%d|LOC:%s|NEEDS:%s".format(
            userLat, userLng, personalRiskScore, userLocationName,
            if (selectedNeeds.isEmpty()) "NONE" else selectedNeeds.joinToString(";")
        )

        AlertDialog(
            onDismissRequest = { showSmsPayloadDialog = false },
            title = { Text("Offline Mesh SMS Payload", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("If internet connectivity fails, RakshAI encodes this high-density triage payload to transmit via zero-data emergency SMS to the nearest disaster relay base station:", style = MaterialTheme.typography.bodySmall)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GeoDarkCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = payload,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = GeoGreenLight),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSmsPayloadDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoGreenPrimary)
                ) {
                    Text("Simulate SMS Transmission")
                }
            }
        )
    }
}

@Composable
private fun SosTriggerInterface(
    isCountingDown: Boolean,
    countdownSeconds: Int,
    userLocationName: String,
    userLat: Double,
    userLng: Double,
    personalRiskScore: Int,
    selectedNeeds: Set<String>,
    language: Language,
    onToggleNeed: (String) -> Unit,
    additionalNotes: String,
    onNotesChange: (String) -> Unit,
    onStartSos: () -> Unit,
    onCancelCountdown: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = LocalizationProvider.get("sos_title", language),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                color = GeoRedCritical
            )
            Text(
                text = LocalizationProvider.get("sos_subtitle", language),
                style = MaterialTheme.typography.bodySmall,
                color = GeoTextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Giant Circular SOS Dispatch Button
            val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(if (isCountingDown) GeoOrangeWarning else GeoRedCritical)
                    .clickable {
                        if (isCountingDown) onCancelCountdown() else onStartSos()
                    }
                    .shadow(12.dp, CircleShape, ambientColor = GeoRedCritical, spotColor = GeoRedCritical),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isCountingDown) {
                        Text(
                            text = "$countdownSeconds",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp, fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                        Text(LocalizationProvider.get("tap_to_cancel", language), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    } else {
                        Icon(Icons.Filled.Emergency, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                        Text(
                            text = LocalizationProvider.get("send_sos", language),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Medical & Vulnerability Needs Checklist
            Text(
                text = LocalizationProvider.get("special_needs", language),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GeoTextSecondary),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val needsList = listOf(
                LocalizationProvider.get("need_elderly", language) to Icons.Filled.Elderly,
                LocalizationProvider.get("need_wheelchair", language) to Icons.Filled.Accessible,
                LocalizationProvider.get("need_oxygen", language) to Icons.Filled.MedicalServices,
                LocalizationProvider.get("need_flooded", language) to Icons.Filled.Flood
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                needsList.forEach { (need, icon) ->
                    val isSelected = selectedNeeds.contains(need)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleNeed(need) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) GeoGreenContainer else GeoBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GeoGreenPrimary else GeoBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(icon, contentDescription = null, tint = if (isSelected) GeoGreenDark else GeoTextSecondary, modifier = Modifier.size(18.dp))
                                Text(need, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal), color = GeoTextPrimary)
                            }
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onToggleNeed(need) },
                                colors = CheckboxDefaults.colors(checkedColor = GeoGreenPrimary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = additionalNotes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Specific Rescue Location Details (e.g. 2nd floor)") },
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ActiveSosTrackingCard(
    sosRecord: SosRecord,
    language: Language,
    onCancel: () -> Unit,
    onShowSms: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = GeoRedContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoRedBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GeoRedCritical))
                    Text(LocalizationProvider.get("beacon_active", language), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = GeoRedCritical))
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (sosRecord.status == SosStatus.DISPATCHED) GeoGreenPrimary else GeoOrangeWarning
                ) {
                    val statusLabel = when (sosRecord.status) {
                        SosStatus.ACTIVE -> LocalizationProvider.get("critical", language)
                        SosStatus.DISPATCHED -> LocalizationProvider.get("dispatched", language)
                        SosStatus.RESOLVED -> LocalizationProvider.get("mark_resolved", language)
                    }
                    Text(
                        text = statusLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Beacon ID: ${sosRecord.id}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = GeoRedText)
            Text("Assigned Sector: ${sosRecord.locationName}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = GeoRedText)
            Text("Coordinates: %.4f° N, %.4f° E".format(sosRecord.lat, sosRecord.lng), style = MaterialTheme.typography.bodySmall, color = GeoRedText)
            Text("Assigned Hospital: ${sosRecord.nearestHospital}", style = MaterialTheme.typography.bodySmall, color = GeoRedText)
            Text("Special Needs: ${sosRecord.medicalNeeds}", style = MaterialTheme.typography.bodySmall, color = GeoRedText)

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShowSms,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeoRedCritical)
                ) {
                    Text("Mesh SMS Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoRedCritical)
                }

                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoRedCritical)
                ) {
                    Text(LocalizationProvider.get("cancel_safe", language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun OfflineSmsFallbackCard(
    userLat: Double,
    userLng: Double,
    onShowSmsDialog: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onShowSmsDialog() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GeoGreenContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Sms, contentDescription = null, tint = GeoGreenDark, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Zero-Data Mesh SMS Relay", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text("Pre-packaged binary dispatch payload for cell-tower blackout conditions.", style = MaterialTheme.typography.bodySmall, color = GeoTextSecondary)
            }

            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = GeoTextSecondary)
        }
    }
}
