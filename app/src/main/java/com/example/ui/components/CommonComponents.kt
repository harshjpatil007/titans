package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocalizationProvider
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun RakshAITopBar(
    currentLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onCoordinatorToggle: () -> Unit,
    isCoordinatorMode: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rakshai_top_bar"),
        color = GeoBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Title with Geometric Olive Square Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GeoGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = "RakshAI Shield",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "RakshAI",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = GeoTextPrimary
                    )
                    Text(
                        text = "Disaster Resilience Core",
                        style = MaterialTheme.typography.labelSmall,
                        color = GeoTextSecondary
                    )
                }
            }

            // Right Actions: Geometric Segmented Language Pill & Mode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mode Switcher Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isCoordinatorMode) GeoDarkCard else GeoBorder.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onCoordinatorToggle() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isCoordinatorMode) Icons.Filled.AdminPanelSettings else Icons.Outlined.Person,
                            contentDescription = "Toggle Mode",
                            tint = if (isCoordinatorMode) Color.White else GeoTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isCoordinatorMode) "CMD" else "Citizen",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (isCoordinatorMode) Color.White else GeoTextSecondary
                        )
                    }
                }

                // Segmented Language Switcher (EN | HI | MR)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GeoBorder,
                    modifier = Modifier.padding(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Language.values().forEach { lang ->
                            val isSelected = lang == currentLanguage
                            val shortLabel = when (lang) {
                                Language.ENGLISH -> "EN"
                                Language.HINDI -> "HI"
                                Language.MARATHI -> "MR"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable { onLanguageSelected(lang) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = shortLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) GeoTextPrimary else GeoTextSecondary.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RakshAIBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    language: Language
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bottom_nav_bar"),
        color = GeoBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Tab 0: Home
            BottomNavItem(
                icon = Icons.Filled.Home,
                label = LocalizationProvider.get("nav_home", language),
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                testTag = "nav_home_button"
            )

            // Tab 1: Map
            BottomNavItem(
                icon = Icons.Filled.Map,
                label = LocalizationProvider.get("nav_map", language),
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                testTag = "nav_map_button"
            )

            // Tab 2: SOS
            SosNavButton(
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )

            // Tab 3: AI
            BottomNavItem(
                icon = Icons.Filled.SmartToy,
                label = LocalizationProvider.get("nav_ai", language),
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                testTag = "nav_ai_button"
            )

            // Tab 4: Coordinator
            BottomNavItem(
                icon = Icons.Filled.AdminPanelSettings,
                label = LocalizationProvider.get("nav_profile", language),
                isSelected = selectedTab == 4,
                onClick = { onTabSelected(4) },
                testTag = "nav_coordinator_button"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) GeoGreenPrimary else GeoTextSecondary.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 10.sp
            ),
            color = if (isSelected) GeoGreenPrimary else GeoTextSecondary.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SosNavButton(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .testTag("nav_sos_main_button"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = GeoRedCritical,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Emergency,
                    contentDescription = "SOS",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "SOS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun SimulationBadge(text: String = "SIMULATION DATA") {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.85f),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(GeoRedCritical)
            )
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White
            )
        }
    }
}
