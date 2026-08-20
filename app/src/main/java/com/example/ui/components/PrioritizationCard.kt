package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmergencyRecommendation
import com.example.ui.theme.*

@Composable
fun PrioritizationCard(
    recommendations: List<EmergencyRecommendation>,
    onDispatch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prioritization_card"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GeoGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ElectricBolt,
                            contentDescription = "Priority Action",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "WHAT TO DO FIRST?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = GeoTextPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GeoGreenContainer
                ) {
                    Text(
                        text = "AI OPTIMIZED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = GeoGreenDark
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            recommendations.take(3).forEach { rec ->
                RecommendationItem(
                    recommendation = rec,
                    onDispatch = { onDispatch(rec.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun RecommendationItem(
    recommendation: EmergencyRecommendation,
    onDispatch: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GeoBackground,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (recommendation.isDispatched) GeoGreenPrimary else GeoBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Priority Tag & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (recommendation.priority) {
                            1 -> GeoRedCritical
                            2 -> GeoOrangeWarning
                            else -> GeoGreenPrimary
                        }
                    ) {
                        Text(
                            text = "PRIORITY ${recommendation.priority}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color.White
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = recommendation.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = GeoTextPrimary,
                        maxLines = 1
                    )
                }

                if (recommendation.isDispatched) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Dispatched",
                            tint = GeoGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Dispatched",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GeoGreenPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action & Reason
            Text(
                text = recommendation.action,
                style = MaterialTheme.typography.bodyMedium,
                color = GeoTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Rationale: ${recommendation.reason}",
                style = MaterialTheme.typography.bodySmall,
                color = GeoTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metric Badges & Dispatch Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricChip(label = "Risk Reduction", value = "${recommendation.riskReductionPercent}%", color = GeoGreenPrimary)
                    MetricChip(label = "Protected", value = "%,d".format(recommendation.populationProtected), color = GeoTextPrimary)
                    MetricChip(label = "Time Saved", value = "${recommendation.estimatedTimeSavedMinutes}m", color = GeoRedCritical)
                }

                if (!recommendation.isDispatched) {
                    Button(
                        onClick = onDispatch,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoGreenPrimary
                        )
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dispatch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = GeoTextSecondary
            )
        }
    }
}
