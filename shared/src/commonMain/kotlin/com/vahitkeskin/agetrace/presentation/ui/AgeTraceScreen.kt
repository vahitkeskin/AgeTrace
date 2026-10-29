package com.vahitkeskin.agetrace.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vahitkeskin.agetrace.domain.model.DetectionMode
import com.vahitkeskin.agetrace.presentation.mvi.AgeTraceContract
import com.vahitkeskin.agetrace.presentation.mvi.AgeTraceViewModel
import com.vahitkeskin.agetrace.presentation.theme.AccentBlue
import com.vahitkeskin.agetrace.presentation.theme.AmbientGlassBackdrop
import com.vahitkeskin.agetrace.presentation.theme.BackgroundDark
import com.vahitkeskin.agetrace.presentation.theme.GlassBorder
import com.vahitkeskin.agetrace.presentation.theme.GlassSpecularBorderBrush
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicCard
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicIconButton
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicPill
import com.vahitkeskin.agetrace.presentation.theme.MinorColor
import com.vahitkeskin.agetrace.presentation.theme.MinorColorBg
import com.vahitkeskin.agetrace.presentation.theme.NeonRed
import com.vahitkeskin.agetrace.presentation.theme.SurfaceCard
import com.vahitkeskin.agetrace.presentation.theme.TextMuted
import com.vahitkeskin.agetrace.presentation.theme.TextPrimary
import com.vahitkeskin.agetrace.presentation.theme.TextSecondary
import com.vahitkeskin.agetrace.presentation.ui.components.DetectedFaceAvatarsStrip
import com.vahitkeskin.agetrace.presentation.ui.components.FaceOverlayCanvas
import com.vahitkeskin.agetrace.presentation.ui.components.FileUploadDropZone
import com.vahitkeskin.agetrace.presentation.ui.components.MetricsSidePanel
import com.vahitkeskin.agetrace.presentation.ui.components.rememberGalleryPickerLauncher

/**
 * AgeTrace Main Screen.
 * Completely redesigned with a polite, minimal, executive Glassmorphism design system.
 */
@Composable
fun AgeTraceScreen(
    modifier: Modifier = Modifier,
    viewModel: AgeTraceViewModel = viewModel { AgeTraceViewModel() }
) {
    val state by viewModel.state.collectAsState()

    // Multiplatform Native Gallery Launcher (Android PhotoPicker, iOS Photos, Desktop FileDialog)
    val galleryLauncher = rememberGalleryPickerLauncher { name, bytes ->
        viewModel.handleIntent(AgeTraceContract.Intent.OnFileUploaded(name, bytes))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AgeTraceContract.Effect.OpenNativeFilePicker -> {
                    galleryLauncher()
                }
                is AgeTraceContract.Effect.Notify -> {
                    // Feedback notification
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Living atmospheric ambient glow spots for authentic glass refraction
        AmbientGlassBackdrop()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // 1. Floating Frosted Glass Header
            MinimalHeader(
                isAnalyzing = state.isAnalyzing,
                selectedMode = state.selectedMode,
                isCameraStreaming = state.isCameraStreaming,
                onModeChange = { mode -> viewModel.handleIntent(AgeTraceContract.Intent.ChangeMode(mode)) },
                onClearClick = { viewModel.handleIntent(AgeTraceContract.Intent.ClearCurrentAnalysis) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Responsive Workspace
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val isWide = maxWidth >= 880.dp

                if (isWide) {
                    // Desktop / Tablet Wide Layout (Two Columns)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Left Column: Viewport + Controls
                        Column(
                            modifier = Modifier
                                .weight(1.4f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            MainViewport(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                state = state,
                                onFaceClick = { face -> viewModel.handleIntent(AgeTraceContract.Intent.SelectFace(face)) },
                                onRotateClick = { viewModel.handleIntent(AgeTraceContract.Intent.RotateCurrentImage) },
                                onToggleBlurClick = { viewModel.handleIntent(AgeTraceContract.Intent.ToggleMinorBlur) },
                                onToggleAgeBalloonClick = { viewModel.handleIntent(AgeTraceContract.Intent.ToggleAgeBalloon) },
                                onPickFileClick = { galleryLauncher() }
                            )

                            if (state.detectedFaces.isNotEmpty()) {
                                DetectedFaceAvatarsStrip(
                                    modifier = Modifier.fillMaxWidth(),
                                    faces = state.detectedFaces,
                                    pixelBuffer = state.pixelBuffer,
                                    selectedFace = state.selectedFace,
                                    onFaceClick = { face -> viewModel.handleIntent(AgeTraceContract.Intent.SelectFace(face)) }
                                )
                            }

                            if (state.selectedMode == DetectionMode.IMAGE_UPLOAD) {
                                FileUploadDropZone(
                                    modifier = Modifier.fillMaxWidth(),
                                    sampleFaces = state.sampleFaces,
                                    currentImageSource = state.currentImageSource,
                                    onPickFileClick = { galleryLauncher() },
                                    onSelectSample = { sample -> viewModel.handleIntent(AgeTraceContract.Intent.SelectSample(sample)) }
                                )
                            }
                        }

                        // Right Column: Live Metrics Dashboard (Scrollable)
                        MetricsSidePanel(
                            modifier = Modifier
                                .weight(1.0f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            metrics = state.metrics,
                            selectedFace = state.selectedFace
                        )
                    }
                } else {
                    // Mobile Portrait Layout (Smooth scroll, non-colliding cards)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        MainViewport(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp),
                            state = state,
                            onFaceClick = { face -> viewModel.handleIntent(AgeTraceContract.Intent.SelectFace(face)) },
                            onRotateClick = { viewModel.handleIntent(AgeTraceContract.Intent.RotateCurrentImage) },
                            onToggleBlurClick = { viewModel.handleIntent(AgeTraceContract.Intent.ToggleMinorBlur) },
                            onToggleAgeBalloonClick = { viewModel.handleIntent(AgeTraceContract.Intent.ToggleAgeBalloon) },
                            onPickFileClick = { galleryLauncher() }
                        )

                        if (state.detectedFaces.isNotEmpty()) {
                            DetectedFaceAvatarsStrip(
                                modifier = Modifier.fillMaxWidth(),
                                faces = state.detectedFaces,
                                pixelBuffer = state.pixelBuffer,
                                selectedFace = state.selectedFace,
                                onFaceClick = { face -> viewModel.handleIntent(AgeTraceContract.Intent.SelectFace(face)) }
                            )
                        }

                        if (state.selectedMode == DetectionMode.IMAGE_UPLOAD) {
                            FileUploadDropZone(
                                modifier = Modifier.fillMaxWidth(),
                                sampleFaces = state.sampleFaces,
                                currentImageSource = state.currentImageSource,
                                onPickFileClick = { galleryLauncher() },
                                onSelectSample = { sample -> viewModel.handleIntent(AgeTraceContract.Intent.SelectSample(sample)) }
                            )
                        }

                        MetricsSidePanel(
                            modifier = Modifier.fillMaxWidth(),
                            metrics = state.metrics,
                            selectedFace = state.selectedFace
                        )

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

/**
 * Floating Frosted Glass Header with brand mark, status indicator, mode selector, and reset action.
 */
@Composable
private fun MinimalHeader(
    isAnalyzing: Boolean,
    selectedMode: DetectionMode,
    isCameraStreaming: Boolean,
    onModeChange: (DetectionMode) -> Unit,
    onClearClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        borderBrush = GlassSpecularBorderBrush
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Mark: Glowing Sparkle + Clean Typography + Live Indicator Dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0x3838BDF8), Color(0x1438BDF8))
                            )
                        )
                        .border(1.dp, Color(0x3338BDF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✦",
                        color = AccentBlue,
                        fontSize = 15.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "AgeTrace",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )

                    // Subtle Live/Active Status Dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isCameraStreaming) NeonRed else Color(0xFF10B981))
                    )
                }
            }

            // Action Controls: Spinner + Glass Mode Pill + Reset Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = AccentBlue,
                        strokeWidth = 2.dp
                    )
                }

                // Frosted Segmented Mode Pill [ Fotoğraf | Kamera ]
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0x14FFFFFF))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(999.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetectionMode.entries.forEach { mode ->
                        val isSelected = (mode == selectedMode)
                        val pillBg by animateColorAsState(
                            targetValue = if (isSelected) Color(0x3838BDF8) else Color.Transparent,
                            label = "pill_bg"
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(pillBg)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(1.dp, AccentBlue.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                                    } else Modifier
                                )
                                .clickable { onModeChange(mode) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (mode == DetectionMode.LIVE_CAMERA && isCameraStreaming) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(NeonRed)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = if (mode == DetectionMode.IMAGE_UPLOAD) "Fotoğraf" else "Kamera",
                                    color = if (isSelected) TextPrimary else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Reset Action
                GlassmorphicIconButton(
                    icon = "↺",
                    onClick = onClearClick,
                    size = 34.dp
                )
            }
        }
    }
}

/**
 * Main Viewport for Camera / Image rendering with floating glassmorphic controls.
 */
@Composable
private fun MainViewport(
    modifier: Modifier = Modifier,
    state: AgeTraceContract.State,
    onFaceClick: (com.vahitkeskin.agetrace.domain.model.DetectedFace) -> Unit,
    onRotateClick: () -> Unit,
    onToggleBlurClick: () -> Unit,
    onToggleAgeBalloonClick: () -> Unit,
    onPickFileClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        borderBrush = if (state.isCameraStreaming) {
            Brush.verticalGradient(
                listOf(NeonRed.copy(alpha = 0.6f), Color(0x1AFFFFFF))
            )
        } else {
            GlassSpecularBorderBrush
        }
    ) {
        val displayBitmap = state.displayBitmap
        val pixelBuffer = state.pixelBuffer

        if (displayBitmap != null && pixelBuffer != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = displayBitmap,
                    contentDescription = "Target",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                FaceOverlayCanvas(
                    modifier = Modifier.fillMaxSize(),
                    faces = state.detectedFaces,
                    imageWidth = pixelBuffer.width,
                    imageHeight = pixelBuffer.height,
                    selectedFace = state.selectedFace,
                    onFaceClick = onFaceClick,
                    isScanning = state.isCameraStreaming || state.isAnalyzing,
                    isAgeBalloonVisible = state.isAgeBalloonVisible
                )

                // Sleek Floating Controls (Top-Right): Reşit Değil Blur Switch + Yaş Balonu Switch + Rotate Button
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val minorThumbOffset by animateDpAsState(
                        targetValue = if (state.isMinorBlurEnabled) 15.dp else 2.dp,
                        label = "minor_blur_switch"
                    )

                    val ageBalloonThumbOffset by animateDpAsState(
                        targetValue = if (state.isAgeBalloonVisible) 15.dp else 2.dp,
                        label = "age_balloon_switch"
                    )

                    // 1. Glassmorphic Switch Button for Minor Face Blur
                    Row(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                brush = if (state.isMinorBlurEnabled) {
                                    Brush.verticalGradient(
                                        listOf(MinorColorBg, Color(0xB3064E3B))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(Color(0xD90F172A), Color(0xB30F172A))
                                    )
                                }
                            )
                            .border(
                                width = 1.dp,
                                brush = if (state.isMinorBlurEnabled) {
                                    Brush.verticalGradient(
                                        listOf(MinorColor.copy(alpha = 0.8f), MinorColor.copy(alpha = 0.3f))
                                    )
                                } else {
                                    GlassSpecularBorderBrush
                                },
                                shape = RoundedCornerShape(999.dp)
                            )
                            .clickable { onToggleBlurClick() }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (state.isMinorBlurEnabled) "🛡️ Reşit Değil" else "👁️ Reşit Değil",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (state.isMinorBlurEnabled) MinorColor else TextSecondary
                        )

                        // Smooth Capsule Switch Track & Moving Thumb
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(16.dp)
                                .clip(CircleShape)
                                .background(if (state.isMinorBlurEnabled) MinorColor else Color(0xFF334155))
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(start = minorThumbOffset, top = 2.dp)
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    // 2. Glassmorphic Switch Button for Age Balloon & Face Frame Visibility
                    Row(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                brush = if (state.isAgeBalloonVisible) {
                                    Brush.verticalGradient(
                                        listOf(Color(0xD90369A1), Color(0xB30284C7))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(Color(0xD90F172A), Color(0xB30F172A))
                                    )
                                }
                            )
                            .border(
                                width = 1.dp,
                                brush = if (state.isAgeBalloonVisible) {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF38BDF8).copy(alpha = 0.8f), Color(0xFF38BDF8).copy(alpha = 0.3f))
                                    )
                                } else {
                                    GlassSpecularBorderBrush
                                },
                                shape = RoundedCornerShape(999.dp)
                            )
                            .clickable { onToggleAgeBalloonClick() }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🎈 Yaş & Çerçeve",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (state.isAgeBalloonVisible) Color(0xFF38BDF8) else TextSecondary
                        )

                        // Smooth Capsule Switch Track & Moving Thumb
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(16.dp)
                                .clip(CircleShape)
                                .background(if (state.isAgeBalloonVisible) Color(0xFF38BDF8) else Color(0xFF334155))
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(start = ageBalloonThumbOffset, top = 2.dp)
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    // Frosted Glass Rotate Button
                    GlassmorphicIconButton(
                        icon = "🔄",
                        onClick = onRotateClick,
                        size = 34.dp
                    )
                }
            }
        } else {
            // Polite, Minimalist Frosted Empty State
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0x38FFFFFF), Color(0x0FFFFFFF))
                                )
                            )
                            .border(1.dp, GlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Fotoğraf Bekleniyor",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bir fotoğraf seçin veya aşağıdaki referans portrelerden birini deneyin",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Polite Action Button inside empty state
                    GlassmorphicPill(
                        isSelected = true,
                        selectedColor = AccentBlue,
                        onClick = onPickFileClick
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "✦", color = AccentBlue, fontSize = 12.sp)
                            Text(
                                text = "Fotoğraf Seç",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
