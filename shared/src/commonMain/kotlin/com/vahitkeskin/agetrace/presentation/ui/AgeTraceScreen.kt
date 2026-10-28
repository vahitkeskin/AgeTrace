package com.vahitkeskin.agetrace.presentation.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vahitkeskin.agetrace.domain.model.DetectionMode
import com.vahitkeskin.agetrace.presentation.mvi.AgeTraceContract
import com.vahitkeskin.agetrace.presentation.mvi.AgeTraceViewModel
import com.vahitkeskin.agetrace.presentation.theme.BackgroundDark
import com.vahitkeskin.agetrace.presentation.theme.CyanNeon
import com.vahitkeskin.agetrace.presentation.theme.GlassBorder
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicCard
import com.vahitkeskin.agetrace.presentation.theme.GreenSuccess
import com.vahitkeskin.agetrace.presentation.theme.NeonRed
import com.vahitkeskin.agetrace.presentation.theme.SurfaceBorder
import com.vahitkeskin.agetrace.presentation.theme.SurfaceCard
import com.vahitkeskin.agetrace.presentation.theme.TextMuted
import com.vahitkeskin.agetrace.presentation.theme.TextPrimary
import com.vahitkeskin.agetrace.presentation.theme.TextSecondary
import com.vahitkeskin.agetrace.presentation.ui.components.DetectedFaceAvatarsStrip
import com.vahitkeskin.agetrace.presentation.ui.components.FaceOverlayCanvas
import com.vahitkeskin.agetrace.presentation.ui.components.FileUploadDropZone
import com.vahitkeskin.agetrace.presentation.ui.components.MetricsSidePanel
import com.vahitkeskin.agetrace.presentation.ui.components.rememberGalleryPickerLauncher

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            // 1. Sleek Minimal Header (Unified title, status, mode toggle, and reset)
            MinimalHeader(
                isAnalyzing = state.isAnalyzing,
                selectedMode = state.selectedMode,
                isCameraStreaming = state.isCameraStreaming,
                onModeChange = { mode -> viewModel.handleIntent(AgeTraceContract.Intent.ChangeMode(mode)) },
                onClearClick = { viewModel.handleIntent(AgeTraceContract.Intent.ClearCurrentAnalysis) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Responsive Workspace: Viewport & Overlay + Live Metrics Panel
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val isWide = maxWidth >= 880.dp

                if (isWide) {
                    // Desktop / Wide Layout (Two Columns, Zero Overlap)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        // Left Column: Viewport + File Picker Controls
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
                                onRotateClick = { viewModel.handleIntent(AgeTraceContract.Intent.RotateCurrentImage) }
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MainViewport(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp),
                            state = state,
                            onFaceClick = { face -> viewModel.handleIntent(AgeTraceContract.Intent.SelectFace(face)) },
                            onRotateClick = { viewModel.handleIntent(AgeTraceContract.Intent.RotateCurrentImage) }
                        )

                        // Head Avatars Strip positioned directly above the File Upload Card
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

                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalHeader(
    isAnalyzing: Boolean,
    selectedMode: DetectionMode,
    isCameraStreaming: Boolean,
    onModeChange: (DetectionMode) -> Unit,
    onClearClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title with small glowing status dot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "AgeTrace",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            // Tiny green active status dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
            )
        }

        // Action controls: Clean Mode Pill [ Fotoğraf | Kamera ] + Subtle Reset
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color(0xFF38BDF8),
                    strokeWidth = 2.dp
                )
            }

            // Compact Mode Selector Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard.copy(alpha = 0.85f))
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetectionMode.entries.forEach { mode ->
                    val isSelected = (mode == selectedMode)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF26334A) else Color.Transparent)
                            .clickable { onModeChange(mode) }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
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

            // Reset Button (Icon only, subtle and polite)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SurfaceCard.copy(alpha = 0.85f))
                    .border(1.dp, GlassBorder, CircleShape)
                    .clickable { onClearClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "↺",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MainViewport(
    modifier: Modifier = Modifier,
    state: AgeTraceContract.State,
    onFaceClick: (com.vahitkeskin.agetrace.domain.model.DetectedFace) -> Unit,
    onRotateClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = modifier,
        cornerRadius = 16.dp,
        backgroundColor = SurfaceCard.copy(alpha = 0.85f),
        borderColor = if (state.isCameraStreaming) NeonRed.copy(alpha = 0.5f) else GlassBorder
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
                    isScanning = state.isCameraStreaming || state.isAnalyzing
                )

                // Sleek Floating Rotate Button (Top-Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xD90F172A))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable { onRotateClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔄",
                        fontSize = 15.sp
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                            .border(1.dp, GlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Fotoğraf Bekleniyor",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bir fotoğraf seçin veya referans portrelerden birini deneyin",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

