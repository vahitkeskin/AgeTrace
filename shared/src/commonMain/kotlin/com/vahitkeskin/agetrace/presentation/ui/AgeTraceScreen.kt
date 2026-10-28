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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vahitkeskin.agetrace.domain.model.DetectionMode
import com.vahitkeskin.agetrace.domain.model.VisionEngineType
import com.vahitkeskin.agetrace.presentation.mvi.AgeTraceContract
import com.vahitkeskin.agetrace.presentation.mvi.AgeTraceViewModel
import com.vahitkeskin.agetrace.presentation.theme.BackgroundDark
import com.vahitkeskin.agetrace.presentation.theme.CyanNeon
import com.vahitkeskin.agetrace.presentation.theme.GlassBorder
import com.vahitkeskin.agetrace.presentation.theme.GlassmorphicCard
import com.vahitkeskin.agetrace.presentation.theme.NeonRed
import com.vahitkeskin.agetrace.presentation.theme.SurfaceBorder
import com.vahitkeskin.agetrace.presentation.theme.SurfaceCard
import com.vahitkeskin.agetrace.presentation.theme.TextMuted
import com.vahitkeskin.agetrace.presentation.theme.TextPrimary
import com.vahitkeskin.agetrace.presentation.theme.TextSecondary
import com.vahitkeskin.agetrace.presentation.ui.components.FaceOverlayCanvas
import com.vahitkeskin.agetrace.presentation.ui.components.FileUploadDropZone
import com.vahitkeskin.agetrace.presentation.ui.components.MetricsSidePanel
import com.vahitkeskin.agetrace.presentation.ui.components.ModeSelectorBar
import com.vahitkeskin.agetrace.presentation.ui.components.rememberGalleryPickerLauncher

@Composable
fun AgeTraceScreen(
    modifier: Modifier = Modifier,
    viewModel: AgeTraceViewModel = viewModel { AgeTraceViewModel() }
) {
    val state by viewModel.state.collectAsState()

    // Multiplatform Native Gallery & Photo Picker launcher (Android Gallery, iOS Photos, Desktop FileDialog)
    val galleryLauncher = rememberGalleryPickerLauncher { name, bytes ->
        viewModel.handleIntent(AgeTraceContract.Intent.OnFileUploaded(name, bytes))
    }

    // Handle single-shot UI effects
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AgeTraceContract.Effect.OpenNativeFilePicker -> {
                    galleryLauncher()
                }
                is AgeTraceContract.Effect.Notify -> {
                    // Handled
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // 1. Top Header Bar
            HeaderBar(
                isAnalyzing = state.isAnalyzing,
                selectedEngine = state.selectedEngine,
                onClearClick = { viewModel.handleIntent(AgeTraceContract.Intent.ClearCurrentAnalysis) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Control Row: Mode Selector + Vision Engine Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeSelectorBar(
                    modifier = Modifier.weight(1.3f),
                    selectedMode = state.selectedMode,
                    isCameraStreaming = state.isCameraStreaming,
                    onModeChange = { mode -> viewModel.handleIntent(AgeTraceContract.Intent.ChangeMode(mode)) }
                )

                EngineSelectorBar(
                    modifier = Modifier.weight(1.0f),
                    selectedEngine = state.selectedEngine,
                    onEngineChange = { engine -> viewModel.handleIntent(AgeTraceContract.Intent.ChangeEngine(engine)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Responsive Workspace: Viewport & Overlay + Live Metrics Panel
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val isWide = maxWidth >= 860.dp

                if (isWide) {
                    // Desktop / Wide Layout
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Left Column: Viewport & Overlays + Upload Controls
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
                                onFaceClick = { face -> viewModel.handleIntent(AgeTraceContract.Intent.SelectFace(face)) }
                            )

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

                        // Right Column: Live Metrics & Biometric Telemetry
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
                    // Mobile Portrait Layout (Scrollable column with non-overlapping flow)
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
                            onFaceClick = { face -> viewModel.handleIntent(AgeTraceContract.Intent.SelectFace(face)) }
                        )

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
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderBar(
    isAnalyzing: Boolean,
    selectedEngine: VisionEngineType,
    onClearClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(NeonRed, Color(0xFF990022))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AgeTrace",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "v1.0",
                        color = NeonRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "Python & Edge AI Biyometrik Yüz & Yaş Analizi",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = NeonRed,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            // Engine status indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x2210B981))
                    .border(1.dp, Color(0x6610B981), RoundedCornerShape(8.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedEngine == VisionEngineType.PYTHON_CORE) "PYTHON CORE AKTİF" else "EDGE AI AKTİF",
                        color = Color(0xFF10B981),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard)
                    .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                    .clickable { onClearClick() }
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Sıfırla",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EngineSelectorBar(
    modifier: Modifier = Modifier,
    selectedEngine: VisionEngineType,
    onEngineChange: (VisionEngineType) -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard.copy(alpha = 0.85f))
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        VisionEngineType.entries.forEach { engine ->
            val isSelected = (engine == selectedEngine)
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) CyanNeon.copy(alpha = 0.16f) else Color.Transparent,
                label = "engine_bg"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) CyanNeon else Color.Transparent,
                label = "engine_border"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable { onEngineChange(engine) }
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${engine.icon} ${engine.title.substringBefore(" ")}",
                    color = if (isSelected) TextPrimary else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun MainViewport(
    modifier: Modifier = Modifier,
    state: AgeTraceContract.State,
    onFaceClick: (com.vahitkeskin.agetrace.domain.model.DetectedFace) -> Unit
) {
    GlassmorphicCard(
        modifier = modifier,
        cornerRadius = 20.dp,
        backgroundColor = SurfaceCard.copy(alpha = 0.85f),
        borderColor = if (state.isCameraStreaming) NeonRed.copy(alpha = 0.6f) else SurfaceBorder
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
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                            .border(1.dp, GlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📸",
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Fotoğraf Seçimi Bekleniyor",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cihazınızın galerisinden bir fotoğraf seçin veya canlı kamerayı başlatın",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
