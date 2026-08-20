package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
fun AiScreen(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    language: Language
) {
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBackground)
            .testTag("ai_screen")
    ) {
        // Multi-Agent Header Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GeoDarkCard
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
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
                            Icon(
                                imageVector = Icons.Filled.SmartToy,
                                contentDescription = null,
                                tint = GeoGreenLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Multi-Agent Disaster Engine",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = GeoDarkText
                            )
                            Text(
                                text = "5 Specialized Resilience Agents Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = GeoTextMuted
                            )
                        }
                    }

                    SimulationBadge("AI CORE")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Agent Pills Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AgentStatusBadge("Perception")
                    AgentStatusBadge("Cascading")
                    AgentStatusBadge("Triage")
                    AgentStatusBadge("Logistics")
                    AgentStatusBadge("Liaison")
                }
            }
        }

        // Quick Suggestion Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val q1 = when (language) {
                Language.HINDI -> "सुरक्षित निकास मार्ग?"
                Language.MARATHI -> "सुरक्षित बाहेर पडण्याचा मार्ग?"
                Language.ENGLISH -> "Safe Evac Route?"
            }
            val q2 = when (language) {
                Language.HINDI -> "निकटतम अस्पताल?"
                Language.MARATHI -> "जवळचे रुग्णालय?"
                Language.ENGLISH -> "Nearest Hospital?"
            }
            val q3 = when (language) {
                Language.HINDI -> "गोदावरी जल स्तर?"
                Language.MARATHI -> "गोदावरी पूर पातळी?"
                Language.ENGLISH -> "River Surge Level?"
            }

            SuggestionChip(
                label = q1,
                onClick = { onSendMessage(if (language == Language.ENGLISH) "What is the safest evacuation route right now?" else q1) }
            )
            SuggestionChip(
                label = q2,
                onClick = { onSendMessage(if (language == Language.ENGLISH) "Where is the nearest open hospital?" else q2) }
            )
            SuggestionChip(
                label = q3,
                onClick = { onSendMessage(if (language == Language.ENGLISH) "What is the current flood water surge speed?" else q3) }
            )
        }

        // Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = GeoGreenPrimary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Synthesizing multi-agent triage intelligence...",
                            style = MaterialTheme.typography.labelSmall,
                            color = GeoTextSecondary
                        )
                    }
                }
            }
        }

        // Input Field Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 80.dp),
            color = GeoBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = {
                        Text(
                            when (language) {
                                Language.HINDI -> "आपदा मार्गदर्शन के लिए पूछें..."
                                Language.MARATHI -> "आपत्ती मार्गदर्शनासाठी विचारा..."
                                Language.ENGLISH -> "Ask RakshAI for disaster guidance..."
                            },
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = GeoGreenPrimary,
                        unfocusedBorderColor = GeoBorder
                    ),
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (inputQuery.isNotBlank()) {
                            onSendMessage(inputQuery.trim())
                            inputQuery = ""
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(GeoGreenPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AgentStatusBadge(agentName: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = GeoDarkSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(GeoGreenLight)
            )
            Text(
                text = agentName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = GeoDarkText
                )
            )
        }
    }
}

@Composable
private fun SuggestionChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = GeoTextPrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser || message.sender == "USER"
    val isSystem = message.sender == "SYSTEM" || message.sender == "SYSTEM_ALERT"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
            ) {
                Icon(
                    imageVector = if (isSystem) Icons.Filled.Warning else Icons.Filled.SmartToy,
                    contentDescription = null,
                    tint = if (isSystem) GeoRedCritical else GeoGreenPrimary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (isSystem) "INCIDENT DISPATCH" else "RAKSHAI RESILIENCE AGENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    ),
                    color = if (isSystem) GeoRedCritical else GeoGreenDark
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = when {
                isUser -> GeoGreenPrimary
                isSystem -> GeoRedContainer
                else -> Color.White
            },
            border = if (isUser) null else androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSystem) GeoRedBorder else GeoBorder
            ),
            shadowElevation = if (isUser) 0.dp else 1.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = when {
                    isUser -> Color.White
                    isSystem -> GeoRedText
                    else -> GeoTextPrimary
                },
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
