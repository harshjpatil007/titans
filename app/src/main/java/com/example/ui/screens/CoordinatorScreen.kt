package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalizationProvider
import com.example.model.*
import com.example.ui.components.SimulationBadge
import com.example.ui.theme.*

@Composable
fun CoordinatorScreen(
    sosList: List<SosRecord>,
    onDispatchSos: (String) -> Unit,
    onResolveSos: (String) -> Unit,
    onOpenSimulation: () -> Unit,
    language: Language = Language.ENGLISH
) {
    var selectedFilter by remember { mutableStateOf<SosStatus?>(null) }

    val filteredList = if (selectedFilter == null) {
        sosList
    } else {
        sosList.filter { it.status == selectedFilter }
    }

    val activeCount = sosList.count { it.status == SosStatus.ACTIVE }
    val dispatchedCount = sosList.count { it.status == SosStatus.DISPATCHED }
    val resolvedCount = sosList.count { it.status == SosStatus.RESOLVED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBackground)
            .testTag("coordinator_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Incident Command Banner
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = GeoDarkCard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GeoDarkSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = GeoGreenLight, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    text = LocalizationProvider.get("coordinator_dashboard", language),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GeoDarkText)
                                )
                                Text("Nashik Emergency Command & Dispatch", style = MaterialTheme.typography.labelSmall, color = GeoTextMuted)
                            }
                        }

                        SimulationBadge("COMMAND CENTER")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 KPI cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KpiTile(LocalizationProvider.get("critical", language).uppercase(), "$activeCount", GeoRedCritical, Modifier.weight(1f))
                        KpiTile(LocalizationProvider.get("dispatched", language).uppercase(), "$dispatchedCount", GeoOrangeWarning, Modifier.weight(1f))
                        KpiTile(LocalizationProvider.get("mark_resolved", language).uppercase(), "$resolvedCount", GeoGreenLight, Modifier.weight(1f))
                    }
                }
            }
        }

        // Triage Filter Pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterPill("${LocalizationProvider.get("filter_all", language)} (${sosList.size})", selectedFilter == null) { selectedFilter = null }
                FilterPill("${LocalizationProvider.get("critical", language)} ($activeCount)", selectedFilter == SosStatus.ACTIVE) { selectedFilter = SosStatus.ACTIVE }
                FilterPill("${LocalizationProvider.get("dispatched", language)} ($dispatchedCount)", selectedFilter == SosStatus.DISPATCHED) { selectedFilter = SosStatus.DISPATCHED }
                FilterPill("${LocalizationProvider.get("mark_resolved", language)} ($resolvedCount)", selectedFilter == SosStatus.RESOLVED) { selectedFilter = SosStatus.RESOLVED }
            }
        }

        // Triage Queue List Items
        items(filteredList) { sos ->
            SosTriageCard(
                sos = sos,
                language = language,
                onDispatch = { onDispatchSos(sos.id) },
                onResolve = { onResolveSos(sos.id) }
            )
        }

        if (filteredList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GeoGreenPrimary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Emergency SOS Signals In This Queue", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text("All citizens in this filter category have been attended to.", style = MaterialTheme.typography.bodySmall, color = GeoTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiTile(label: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = GeoDarkSurface
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = color)
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold), color = GeoTextMuted)
        }
    }
}

@Composable
private fun FilterPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) GeoGreenPrimary else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GeoGreenPrimary else GeoBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
            color = if (isSelected) Color.White else GeoTextPrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SosTriageCard(
    sos: SosRecord,
    language: Language,
    onDispatch: () -> Unit,
    onResolve: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when (sos.status) {
                SosStatus.ACTIVE -> GeoRedBorder
                SosStatus.DISPATCHED -> GeoBorder
                SosStatus.RESOLVED -> GeoBorder
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (sos.status) {
                            SosStatus.ACTIVE -> GeoRedCritical
                            SosStatus.DISPATCHED -> GeoOrangeWarning
                            SosStatus.RESOLVED -> GeoGreenPrimary
                        }
                    ) {
                        Text(
                            text = sos.priority.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(sos.id, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = GeoTextPrimary)
                }

                Text(sos.userName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = GeoTextPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Sector: ${sos.locationName}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            Text("Coordinates: %.4f° N, %.4f° E".format(sos.lat, sos.lng), style = MaterialTheme.typography.bodySmall, color = GeoTextSecondary)
            Text("Assigned Hospital: ${sos.nearestHospital}", style = MaterialTheme.typography.bodySmall, color = GeoTextSecondary)
            Text("Triage Condition: ${sos.medicalNeeds}", style = MaterialTheme.typography.bodySmall, color = GeoTextPrimary)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (sos.status == SosStatus.ACTIVE) {
                    Button(
                        onClick = onDispatch,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GeoRedCritical)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LocalizationProvider.get("dispatch_unit", language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (sos.status == SosStatus.DISPATCHED) {
                    Button(
                        onClick = onResolve,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GeoGreenPrimary)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(LocalizationProvider.get("mark_resolved", language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = GeoGreenContainer
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = GeoGreenDark, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Citizen Evacuated & Safe", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GeoGreenDark))
                        }
                    }
                }
            }
        }
    }
}
