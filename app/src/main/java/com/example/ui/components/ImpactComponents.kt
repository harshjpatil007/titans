package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CascadingStep
import com.example.model.ImpactFactors
import com.example.ui.theme.*

@Composable
fun CascadingImpactPanel(
    steps: List<CascadingStep>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cascading_impact_panel"),
        shape = RoundedCornerShape(24.dp),
        color = GeoDarkCard
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
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
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GeoDarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountTree,
                            contentDescription = "Cascade Model",
                            tint = GeoGreenLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Cascading Impact Chain",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GeoDarkText
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GeoRedCritical
                ) {
                    Text(
                        text = "STAGE 3 ACTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp,
                            color = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal Scrollable Flow Timeline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, step ->
                    CascadeStepItem(step = step)

                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .offset(y = (-12).dp)
                                .background(
                                    if (step.isCurrentActive) GeoRedCritical
                                    else GeoDarkSurface
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CascadeStepItem(step: CascadingStep) {
    val icon = when (step.iconKey) {
        "thunderstorm" -> Icons.Filled.Thunderstorm
        "flood", "waves" -> Icons.Filled.Flood
        "block" -> Icons.Filled.Block
        "local_hospital" -> Icons.Filled.LocalHospital
        "groups" -> Icons.Filled.Groups
        "water_damage", "water_drop" -> Icons.Filled.WaterDrop
        "flash_off" -> Icons.Filled.FlashOff
        "sunny" -> Icons.Filled.WbSunny
        "cleaning_services" -> Icons.Filled.CleaningServices
        else -> Icons.Filled.CheckCircle
    }

    val (bgColor, iconColor, ringColor) = when {
        step.isCurrentActive -> Triple(GeoRedCritical, Color.White, GeoRedCritical.copy(alpha = 0.3f))
        step.isTrigger -> Triple(GeoDarkSurface, GeoGreenLight, Color.Transparent)
        step.isPredicted -> Triple(GeoDarkSurface.copy(alpha = 0.5f), GeoTextMuted, Color.Transparent)
        else -> Triple(GeoDarkSurface, Color.White, Color.Transparent)
    }

    Column(
        modifier = Modifier
            .width(96.dp)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (step.isCurrentActive) 44.dp else 38.dp)
                .clip(CircleShape)
                .background(bgColor)
                .then(
                    if (step.isCurrentActive) Modifier.border(3.dp, ringColor, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = step.title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = step.title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (step.isCurrentActive) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp
            ),
            color = GeoDarkText,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Text(
            text = step.timeLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = if (step.isCurrentActive) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (step.isCurrentActive) GeoRedBorder else GeoTextMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ImpactScoreCard(
    factors: ImpactFactors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("impact_score_card"),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Disaster Impact Factors",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GeoTextPrimary
                    )
                    Text(
                        text = "Weighted real-time vulnerability matrix",
                        style = MaterialTheme.typography.labelSmall,
                        color = GeoTextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GeoRedContainer
                ) {
                    Text(
                        text = "${factors.overallScore}/100",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = GeoRedText
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            FactorProgressRow("Hazard Severity", factors.hazardSeverity, 25, GeoRedCritical)
            Spacer(modifier = Modifier.height(8.dp))
            FactorProgressRow("Population Exposure", factors.populationExposure, 25, GeoOrangeWarning)
            Spacer(modifier = Modifier.height(8.dp))
            FactorProgressRow("Infrastructure Risk", factors.infrastructureRisk, 20, GeoRedDark)
            Spacer(modifier = Modifier.height(8.dp))
            FactorProgressRow("Cascade Risk", factors.cascadeRisk, 15, GeoYellowModerate)
            Spacer(modifier = Modifier.height(8.dp))
            FactorProgressRow("Response Difficulty", factors.responseDifficulty, 15, GeoGreenPrimary)
        }
    }
}

@Composable
private fun FactorProgressRow(
    title: String,
    score: Int,
    weightPercent: Int,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$title ($weightPercent% wt)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = GeoTextPrimary
            )
            Text(
                text = "$score/100",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = GeoBackground
        )
    }
}
