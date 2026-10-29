package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.agetrace.domain.model.DetectionMode
import com.vahitkeskin.agetrace.presentation.theme.AccentBlue
import com.vahitkeskin.agetrace.presentation.theme.GlassBorder
import com.vahitkeskin.agetrace.presentation.theme.GlassSpecularBorderBrush
import com.vahitkeskin.agetrace.presentation.theme.NeonRed
import com.vahitkeskin.agetrace.presentation.theme.SurfaceCard
import com.vahitkeskin.agetrace.presentation.theme.TextMuted
import com.vahitkeskin.agetrace.presentation.theme.TextPrimary

@Composable
fun ModeSelectorBar(
    modifier: Modifier = Modifier,
    selectedMode: DetectionMode,
    isCameraStreaming: Boolean,
    onModeChange: (DetectionMode) -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0x33FFFFFF), Color(0x14FFFFFF), Color(0x0AFFFFFF))
                )
            )
            .border(
                1.dp,
                GlassSpecularBorderBrush,
                RoundedCornerShape(999.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DetectionMode.entries.forEach { mode ->
            val isSelected = (mode == selectedMode)
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Color(0x3838BDF8) else Color.Transparent,
                label = "mode_bg"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(bgColor)
                    .then(
                        if (isSelected) {
                            Modifier.border(1.dp, AccentBlue.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                        } else Modifier
                    )
                    .clickable { onModeChange(mode) }
                    .padding(vertical = 7.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (mode == DetectionMode.LIVE_CAMERA && isCameraStreaming) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NeonRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = mode.title,
                        color = if (isSelected) TextPrimary else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
