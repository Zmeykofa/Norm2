package com.example.normirovshikapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.normirovshikapp.data.OperationEntity
import com.example.normirovshikapp.ui.theme.GazGreenActive
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.isSystemInDarkTheme
import com.example.normirovshikapp.ui.theme.*

@Composable
fun OperationItem(
    op: OperationEntity,
    currentTime: Long,
    onStop: () -> Unit,
    onEdit: () -> Unit,
    onSplit: () -> Unit,
    onDelete: () -> Unit,
    onRepeat: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()) }

    val durationMillis = (op.stopEpoch ?: currentTime) - op.startEpoch
    val durationSeconds = durationMillis / 1000
    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60
    val durationFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val isActive = op.stopEpoch == null
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val isDark = isSystemInDarkTheme()
    val staffBg = if (isDark) ChipStaffBgDark else ChipStaffBg
    val staffText = if (isDark) ChipStaffTextDark else ChipStaffText
    val toolBg = if (isDark) ChipToolBgDark else ChipToolBg
    val toolText = if (isDark) ChipToolTextDark else ChipToolText
    val eqBg = if (isDark) ChipEqBgDark else ChipEqBg
    val eqText = if (isDark) ChipEqTextDark else ChipEqText
    val matBg = if (isDark) ChipMatBgDark else ChipMatBg
    val matText = if (isDark) ChipMatTextDark else ChipMatText

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(20.dp),
        border = if (isActive) {
            BorderStroke(2.dp, GazGreenActive.copy(alpha = alpha))
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                GazGreenActive.copy(alpha = 0.03f)
            else
                MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 8.dp else 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = op.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isActive) GazGreenActive else MaterialTheme.colorScheme.primary
                    )
                    if (isActive) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = GazGreenActive.copy(alpha = alpha),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "АКТИВНАЯ ОПЕРАЦИЯ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GazGreenActive
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Редактировать", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.width(6.dp))
                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Удалить", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            InfoRow("Начало", sdf.format(Date(op.startEpoch)), leadingIcon = Icons.Outlined.AccessTime)
            InfoRow("Окончание", op.stopEpoch?.let { sdf.format(Date(it)) } ?: "—", leadingIcon = Icons.Outlined.AccessTime)
            InfoRow("Продолжительность", durationFormatted, leadingIcon = Icons.Outlined.Refresh)

            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            // Чипы вместо простого текста
            ResourceChipGroup(
                label = "Исполнители (${op.people} чел.)",
                itemsRaw = op.workers,
                chipBgColor = staffBg,
                chipTextColor = staffText,
                icon = Icons.Outlined.People
            )

            ResourceChipGroup(
                label = "Инструменты",
                itemsRaw = op.tools,
                chipBgColor = toolBg,
                chipTextColor = toolText,
                icon = Icons.Outlined.Build
            )

            ResourceChipGroup(
                label = "Спецтехника",
                itemsRaw = op.equipment,
                chipBgColor = eqBg,
                chipTextColor = eqText,
                icon = Icons.Outlined.LocalShipping
            )

            ResourceChipGroup(
                label = "Материалы",
                itemsRaw = op.materials,
                chipBgColor = matBg,
                chipTextColor = matText,
                icon = Icons.Outlined.Layers
            )

            if (op.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                InfoRow("Заметки", op.notes)
            }

            Spacer(Modifier.height(8.dp))

            if (op.stopEpoch == null) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onStop,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Outlined.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Остановить")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onSplit,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Завершить и новая")
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onRepeat,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Повторить операцию")
                    }
                }
            }
        }
    }
}

