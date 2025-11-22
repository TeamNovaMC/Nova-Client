package com.radiantbyte.novaclient.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.radiantbyte.novaclient.ui.theme.NovaColors

@Composable
fun NovaSidebar(
    selectedPage: Any,
    pages: List<NovaNavItem>,
    onPageSelected: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    NovaGlassCard(
        modifier = modifier
            .width(200.dp)
            .fillMaxHeight(0.85f)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        glowColor = NovaColors.Primary,
        glowIntensity = 0.2f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            // Compact Header
            CompactNovaHeader()

            // Navigation items - compact layout (no scrolling)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                pages.forEach { page ->
                    CompactNovaNavItemComponent(
                        item = page,
                        isSelected = selectedPage == page.page,
                        onClick = { onPageSelected(page.page) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactNovaHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "header_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Nova",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Light,
            color = NovaColors.Primary.copy(alpha = glowAlpha)
        )

        Box(
            modifier = Modifier
                .height(1.dp)
                .width(25.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NovaColors.Primary.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}

@Composable
private fun CompactNovaNavItemComponent(
    item: NovaNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.7f,
        animationSpec = tween(300),
        label = "nav_alpha"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.5.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                NovaColors.Primary.copy(alpha = 0.15f)
            } else {
                Color.Transparent
            }
        ),
        shape = RoundedCornerShape(6.dp),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    colors = listOf(
                        NovaColors.Primary.copy(alpha = 0.5f),
                        NovaColors.Secondary.copy(alpha = 0.3f)
                    )
                )
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (isSelected) NovaColors.Primary else NovaColors.OnSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )

            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) NovaColors.Primary else NovaColors.OnSurface.copy(alpha = animatedAlpha),
                maxLines = 1
            )
        }
    }
}


data class NovaNavItem(
    val page: Any,
    val label: String,
    val icon: ImageVector
)
