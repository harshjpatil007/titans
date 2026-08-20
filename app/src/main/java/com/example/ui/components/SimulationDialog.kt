package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DisasterRepository
import com.example.model.DisasterScenario
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun SimulationDialog(
    currentScenario: DisasterScenario,
    onDismiss: () -> Unit,
    onRunPresetScenario: (DisasterScenario) -> Unit,
    onRunCustomSimulation: (Int, Double, Int, Int) -> Unit
) {
    var rainfall by remember { mutableFloatStateOf(currentScenario.rainfallIntensityMmPerHour.toFloat()) }
    var riverLevel by remember { mutableFloatStateOf(currentScenario.riverLevelChangeMeters.toFloat()) }
    var population by remember { mutableFloatStateOf(currentScenario.populationExposed.toFloat()) }
    var hazardSeverity by remember { mutableFloatStateOf(currentScenario.hazardSeverity.toFloat()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Science,
                            contentDescription = "Simulate",
                            tint = GeoGreenPrimary
                        )
                        Text(
                            text = "Disaster Simulator",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = GeoTextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Calibrate rainfall, river surge, and exposed population to observe dynamic AI impact re-ranking.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GeoTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PRESET SCENARIOS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GeoGreenPrimary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets Buttons
                DisasterRepository.scenarios.forEach { scenario ->
                    val isSelected = scenario.id == currentScenario.id
                    OutlinedButton(
                        onClick = {
                            onRunPresetScenario(scenario)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) GeoGreenContainer else Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) GeoGreenPrimary else GeoBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = scenario.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = GeoTextPrimary
                            )
                            Text(
                                text = "${scenario.rainfallIntensityMmPerHour} mm/h | +${scenario.riverLevelChangeMeters}m surge | %,d exposed".format(scenario.populationExposed),
                                style = MaterialTheme.typography.bodySmall,
                                color = GeoTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = GeoBorder)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CUSTOM CALIBRATION",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GeoGreenPrimary)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Slider 1: Rainfall
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rainfall Intensity", style = MaterialTheme.typography.bodyMedium)
                    Text("${rainfall.roundToInt()} mm/h", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = GeoRedCritical))
                }
                Slider(
                    value = rainfall,
                    onValueChange = { rainfall = it },
                    valueRange = 0f..250f,
                    colors = SliderDefaults.colors(thumbColor = GeoRedCritical, activeTrackColor = GeoRedCritical)
                )

                // Slider 2: River Level Surge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("River Level Surge", style = MaterialTheme.typography.bodyMedium)
                    Text("+%.1f m".format(riverLevel), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = GeoOrangeWarning))
                }
                Slider(
                    value = riverLevel,
                    onValueChange = { riverLevel = it },
                    valueRange = 0f..8f,
                    colors = SliderDefaults.colors(thumbColor = GeoOrangeWarning, activeTrackColor = GeoOrangeWarning)
                )

                // Slider 3: Population Exposed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Population Exposed", style = MaterialTheme.typography.bodyMedium)
                    Text("%,d".format(population.roundToInt()), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = GeoGreenPrimary))
                }
                Slider(
                    value = population,
                    onValueChange = { population = it },
                    valueRange = 1000f..200000f,
                    colors = SliderDefaults.colors(thumbColor = GeoGreenPrimary, activeTrackColor = GeoGreenPrimary)
                )

                // Slider 4: Hazard Severity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hazard Severity Index", style = MaterialTheme.typography.bodyMedium)
                    Text("${hazardSeverity.roundToInt()}/100", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = GeoRedCritical))
                }
                Slider(
                    value = hazardSeverity,
                    onValueChange = { hazardSeverity = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(thumbColor = GeoRedCritical, activeTrackColor = GeoRedCritical)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onRunCustomSimulation(
                            rainfall.roundToInt(),
                            (riverLevel * 10.0).roundToInt() / 10.0,
                            population.roundToInt(),
                            hazardSeverity.roundToInt()
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeoGreenPrimary)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RECALCULATE IMPACT",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
