package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalizationProvider
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    scenario: DisasterScenario,
    personalRiskScore: Int,
    personalRiskLevel: RiskLevel,
    impactFactors: ImpactFactors,
    cascadingSteps: List<CascadingStep>,
    recommendations: List<EmergencyRecommendation>,
    nearestHazardDistanceKm: Double,
    nearestHazardName: String,
    userLocationName: String,
    userLat: Double,
    userLng: Double,
    isSimulating: Boolean,
    simulationProgress: Float,
    language: Language,
    onRunSimulationClick: () -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onDispatchRecommendation: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBackground)
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Personal Risk Score Banner (Geometric Pink/Red Container)
        item {
            GeometricRiskBanner(
                personalRiskScore = personalRiskScore,
                personalRiskLevel = personalRiskLevel,
                scenarioTitle = scenario.title,
                nearestHazard = nearestHazardName,
                language = language
            )
        }

        // 2. 2-Column Grid: GPS Location & Hazard Distance Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GeometricInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.LocationOn,
                    iconColor = GeoGreenPrimary,
                    label = "GPS LOCATION",
                    value = userLocationName.split("(").first().trim()
                )
                GeometricInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Warning,
                    iconColor = GeoRedCritical,
                    label = "HAZARD DIST.",
                    value = "%.1f km Away".format(nearestHazardDistanceKm)
                )
            }
        }

        // 3. Mini Map Overview Card with Safe Route Pill Overlay
        item {
            GeometricMapPreviewCard(
                nearestHazard = nearestHazardName,
                onOpenMap = { onNavigateToTab(1) }
            )
        }

        // 4. Dark AI Guardian Recommendation Banner (#2F312C)
        item {
            GeometricAiGuardianBanner(
                onOpenAi = { onNavigateToTab(3) }
            )
        }

        // 5. Full Width Primary Emergency SOS Action Button
        item {
            Button(
                onClick = { onNavigateToTab(2) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(29.dp), ambientColor = GeoRedCritical.copy(alpha = 0.4f), spotColor = GeoRedCritical)
                    .testTag("home_send_sos_main_btn"),
                shape = RoundedCornerShape(29.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GeoRedCritical
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Emergency,
                        contentDescription = "SOS",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "SEND SOS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        // 6. Active Scenario Controls & Simulation
        item {
            GeometricScenarioCard(
                scenario = scenario,
                isSimulating = isSimulating,
                simulationProgress = simulationProgress,
                language = language,
                onRunSimulation = onRunSimulationClick
            )
        }

        // 7. Cascading Impact Timeline
        item {
            CascadingImpactPanel(steps = cascadingSteps)
        }

        // 8. Disaster Impact Score Breakdown
        item {
            ImpactScoreCard(factors = impactFactors)
        }

        // 9. AI Priority Recommendations
        item {
            PrioritizationCard(
                recommendations = recommendations,
                onDispatch = onDispatchRecommendation
            )
        }
    }
}

@Composable
private fun GeometricRiskBanner(
    personalRiskScore: Int,
    personalRiskLevel: RiskLevel,
    scenarioTitle: String,
    nearestHazard: String,
    language: Language
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("geometric_risk_banner"),
        shape = RoundedCornerShape(24.dp),
        color = GeoRedContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoRedBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PERSONAL RISK SCORE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    ),
                    color = GeoRedText.copy(alpha = 0.7f)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GeoRedCritical
                ) {
                    Text(
                        text = personalRiskLevel.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$personalRiskScore",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Light,
                        fontSize = 48.sp,
                        lineHeight = 48.sp
                    ),
                    color = GeoRedText
                )
                Text(
                    text = "/ 100",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal
                    ),
                    color = GeoRedText.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$scenarioTitle impact active. Elevated hazard exposure detected at your current coordinates near $nearestHazard.",
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 16.sp
                ),
                color = GeoRedText.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun GeometricInfoCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = GeoTextSecondary.copy(alpha = 0.6f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = GeoTextPrimary,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun GeometricMapPreviewCard(
    nearestHazard: String,
    onOpenMap: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .clickable { onOpenMap() },
        shape = RoundedCornerShape(24.dp),
        color = GeoSurfaceMuted,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle Radial Gradient for danger hazard overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GeoRedCritical.copy(alpha = 0.22f), Color.Transparent),
                            radius = 450f
                        )
                    )
            )

            // Top Simulation Tag
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                SimulationBadge("SIMULATION MAP")
            }

            // Bottom Floating Safe Route Pill
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(10.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GeoGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Navigation,
                            contentDescription = "Safe Route",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "RECOMMENDED SAFE ROUTE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = GeoTextSecondary.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Evacuate South-West towards KTHM High Ground",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = GeoTextPrimary,
                            maxLines = 1
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Open Map",
                        tint = GeoTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GeometricAiGuardianBanner(
    onOpenAi: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onOpenAi() },
        shape = RoundedCornerShape(24.dp),
        color = GeoDarkCard
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GeoDarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = "AI Guardian",
                    tint = GeoGreenLight,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI GUARDIAN INSIGHT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = GeoTextMuted
                )
                Text(
                    text = "\"Godavari river surge rate is +0.3m/hr. Move to elevated shelters within 20 mins.\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp
                    ),
                    color = GeoDarkText
                )
            }

            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Ask AI",
                tint = GeoTextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun GeometricScenarioCard(
    scenario: DisasterScenario,
    isSimulating: Boolean,
    simulationProgress: Float,
    language: Language,
    onRunSimulation: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GeoGreenPrimary)
                    )
                    Text(
                        text = "ACTIVE PROTOCOL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = GeoGreenPrimary
                    )
                }

                Text(
                    text = "Epicenter: ${scenario.epicenterName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = GeoTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scenario.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = GeoTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = scenario.description,
                style = MaterialTheme.typography.bodySmall,
                color = GeoTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Mini metric pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniMetricPill("Rainfall", "${scenario.rainfallIntensityMmPerHour} mm/h")
                MiniMetricPill("Surge", "+%.1f m".format(scenario.riverLevelChangeMeters))
                MiniMetricPill("Exposed", "%,d".format(scenario.populationExposed))
                MiniMetricPill("Severity", "${scenario.hazardSeverity}/100")
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isSimulating) {
                LinearProgressIndicator(
                    progress = { simulationProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = GeoGreenPrimary,
                    trackColor = GeoBorder
                )
            } else {
                OutlinedButton(
                    onClick = onRunSimulation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(21.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeoGreenPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GeoGreenPrimary
                    )
                ) {
                    Icon(Icons.Filled.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Calibrate Simulation Parameters",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniMetricPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = GeoBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = GeoTextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = GeoTextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}
