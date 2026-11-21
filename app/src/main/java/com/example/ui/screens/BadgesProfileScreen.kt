package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleanHands
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Recycling
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BadgeCategory
import com.example.ui.BadgeFilter
import com.example.ui.DigitalBadgeUi
import com.example.ui.TertemizUiState
import com.example.ui.components.ResponsiveContentColumn
import com.example.ui.components.formatLiters
import com.example.ui.theme.StatusCleanBg
import com.example.ui.theme.StatusCleanFg
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.StatusWarningFg
import com.example.ui.theme.StatusWaterBg
import com.example.ui.theme.StatusWaterFg

@Composable
fun BadgesProfileScreen(
    uiState: TertemizUiState,
    onBadgeFilterChange: (BadgeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedBadge by remember { mutableStateOf<DigitalBadgeUi?>(null) }

    selectedBadge?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            onDismiss = { selectedBadge = null }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ResponsiveContentColumn {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Profil & Dijital Rozetler",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sürdürülebilir alışkanlıklarınla yeni rozetlerin kilidini aç.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Kullanıcı eko-seviye ve ilerleme profil kartı
        item {
            ResponsiveContentColumn {
                ProfileRankSummaryCard(uiState = uiState)
            }
        }

        // Rozet filtreleme çipleri
        item {
            ResponsiveContentColumn {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Rozet Koleksiyonu (${uiState.unlockedBadgesCount}/${uiState.totalBadgesCount})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BadgeFilter.entries.forEach { filter ->
                            val count = when (filter) {
                                BadgeFilter.ALL -> uiState.badges.size
                                BadgeFilter.UNLOCKED -> uiState.badges.count { it.isUnlocked }
                                BadgeFilter.IN_PROGRESS -> uiState.badges.count { !it.isUnlocked }
                            }
                            FilterChip(
                                selected = uiState.badgeFilter == filter,
                                onClick = { onBadgeFilterChange(filter) },
                                label = {
                                    Text(
                                        text = "${filter.label} ($count)",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("badge_filter_${filter.name}")
                            )
                        }
                    }
                }
            }
        }

        if (uiState.filteredBadges.isEmpty()) {
            item {
                ResponsiveContentColumn {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Bu kategoride henüz rozet yok",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Bildirim oluşturarak veya su/sıfır atık aksiyonu kaydederek rozet kazanabilirsiniz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(
                items = uiState.filteredBadges,
                key = { it.definition.id }
            ) { badge ->
                ResponsiveContentColumn {
                    DigitalBadgeCard(
                        badge = badge,
                        onClick = { selectedBadge = badge }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileRankSummaryCard(uiState: TertemizUiState) {
    val overallProgress = if (uiState.totalBadgesCount > 0) {
        uiState.unlockedBadgesCount.toFloat() / uiState.totalBadgesCount.toFloat()
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_rank_card"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF097275),
                                Color(0xFF1A6B5C)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp),
                        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.45f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.WorkspacePremium,
                                contentDescription = "Eko Seviye Arması",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Eko Profilin",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = uiState.ecoRank.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.ecoRank.nextHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kazanılan Dijital Rozetler",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.unlockedBadgesCount} / ${uiState.totalBadgesCount}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { overallProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                // Profil katkı özetleri
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileMiniStat(
                        label = "Bildirim",
                        value = "${uiState.totalReportsCount}",
                        modifier = Modifier.weight(1f)
                    )
                    ProfileMiniStat(
                        label = "Su Tasarrufu",
                        value = formatLiters(uiState.totalLitersSaved),
                        modifier = Modifier.weight(1f)
                    )
                    ProfileMiniStat(
                        label = "Sıfır Atık",
                        value = "${uiState.totalZeroWasteActions}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMiniStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DigitalBadgeCard(
    badge: DigitalBadgeUi,
    onClick: () -> Unit
) {
    val (bg, fg, icon) = badgeVisualStyle(badge)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("badge_card_${badge.definition.id}"),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }
        ),
        border = BorderStroke(
            width = if (badge.isUnlocked) 1.5.dp else 1.dp,
            color = if (badge.isUnlocked) fg.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Madalyon rozet ikonu
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    color = if (badge.isUnlocked) bg else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                    border = BorderStroke(
                        2.dp,
                        if (badge.isUnlocked) fg.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = badge.definition.title,
                            tint = if (badge.isUnlocked) fg else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Surface(
                    color = if (badge.isUnlocked) StatusCleanFg else MaterialTheme.colorScheme.surface,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (badge.isUnlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                            contentDescription = if (badge.isUnlocked) "Kazanıldı" else "Kilitli",
                            tint = if (badge.isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = badge.definition.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (badge.isUnlocked) bg else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = badge.definition.category.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (badge.isUnlocked) fg else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = badge.definition.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LinearProgressIndicator(
                    progress = { badge.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = if (badge.isUnlocked) fg else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (badge.isUnlocked) {
                            "Kazanıldı • ${badge.unlockedAtFormatted ?: ""}"
                        } else {
                            "İlerleme: ${badge.currentValue} / ${badge.definition.targetValue} ${badge.definition.unitLabel}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (badge.isUnlocked) fg else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (badge.isUnlocked) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeDetailDialog(
    badge: DigitalBadgeUi,
    onDismiss: () -> Unit
) {
    val (bg, fg, icon) = badgeVisualStyle(badge)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                color = if (badge.isUnlocked) bg else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (badge.isUnlocked) fg else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = badge.definition.title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = badge.definition.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(
                    color = if (badge.isUnlocked) StatusCleanBg else StatusWarningBg,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (badge.isUnlocked) {
                            "Durum: Kazanıldı (${badge.unlockedAtFormatted ?: "Tamamlandı"})"
                        } else {
                            val remaining = (badge.definition.targetValue - badge.currentValue).coerceAtLeast(1)
                            "Hedefe $remaining ${badge.definition.unitLabel} kaldı (${badge.currentValue}/${badge.definition.targetValue})"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (badge.isUnlocked) StatusCleanFg else StatusWarningFg,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat", fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun badgeVisualStyle(badge: DigitalBadgeUi): Triple<Color, Color, ImageVector> {
    val icon = when (badge.definition.id) {
        "badge_first_report" -> Icons.Rounded.NotificationsActive
        "badge_five_reports" -> Icons.Rounded.Verified
        "badge_problem_solver" -> Icons.Rounded.AutoAwesome
        "badge_handwash_master" -> Icons.Rounded.CleanHands
        "badge_week_water_saver" -> Icons.Rounded.WaterDrop
        "badge_water_guardian" -> Icons.Rounded.WaterDrop
        "badge_zero_waste_ambassador" -> Icons.Rounded.Recycling
        else -> Icons.Rounded.FactCheck
    }

    return when (badge.definition.category) {
        BadgeCategory.HYGIENE -> Triple(StatusCleanBg, StatusCleanFg, icon)
        BadgeCategory.WATER -> Triple(StatusWaterBg, StatusWaterFg, icon)
        BadgeCategory.ZERO_WASTE -> Triple(StatusWarningBg, StatusWarningFg, icon)
    }
}
