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
import com.vahitkeskin.agetrace.data.platform.PlatformImageBridge
import com.vahitkeskin.agetrace.domain.model.DetectionMode
import com.vahitkeskin.agetrace.domain.model.VisionEngineType
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
            // 1. Executive Top Header (Clean, balanced, non-squeezing)
            ExecutiveHeader(
                isAnalyzing = state.isAnalyzing,
                selectedEngine = state.selectedEngine,
                onClearClick = { viewModel.handleIntent(AgeTraceContract.Intent.ClearCurrentAnalysis) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Master Control Deck (Mode + Engine selectors separated cleanly)
            ControlDeck(
                selectedMode = state.selectedMode,
                isCameraStreaming = state.isCameraStreaming,
                selectedEngine = state.selectedEngine,
                onModeChange = { mode -> viewModel.handleIntent(AgeTraceContract.Intent.ChangeMode(mode)) },
                onEngineChange = { engine -> viewModel.handleIntent(AgeTraceContract.Intent.ChangeEngine(engine)) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Responsive Workspace: Viewport & Overlay + Live Metrics Panel
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

                                DirectPathInputRow(
                                    onPathSubmit = { path ->
                                        val bytes = PlatformImageBridge.readBytesFromPath(path)
                                        if (bytes != null) {
                                            val fileName = path.substringAfterLast('/').substringAfterLast('\\')
                                            viewModel.handleIntent(
                                                AgeTraceContract.Intent.OnFileUploaded(fileName, bytes)
                                            )
                                        }
                                    }
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

                            DirectPathInputRow(
                                onPathSubmit = { path ->
                                    val bytes = PlatformImageBridge.readBytesFromPath(path)
                                    if (bytes != null) {
                                        val fileName = path.substringAfterLast('/').substringAfterLast('\\')
                                        viewModel.handleIntent(
                                            AgeTraceContract.Intent.OnFileUploaded(fileName, bytes)
                                        )
                                    }
                                }
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
private fun ExecutiveHeader(
    isAnalyzing: Boolean,
    selectedEngine: VisionEngineType,
    onClearClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Identity
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "◈",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AgeTrace",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x1F3B82F6))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AI VISION",
                            color = Color(0xFF38BDF8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Text(
                    text = "Biyometrik Yüz Algılama & Yaş Tahmini",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Actions & Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color(0xFF38BDF8),
                    strokeWidth = 2.dp
                )
            }

            // Status Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x1410B981))
                    .border(1.dp, Color(0x3310B981), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(GreenSuccess)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedEngine == VisionEngineType.PYTHON_CORE) "Python ML" else "Edge AI",
                        color = GreenSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Reset Action Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceCard)
                    .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                    .clickable { onClearClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Sıfırla",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ControlDeck(
    selectedMode: DetectionMode,
    isCameraStreaming: Boolean,
    selectedEngine: VisionEngineType,
    onModeChange: (DetectionMode) -> Unit,
    onEngineChange: (VisionEngineType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mode Selector (Segmented Pill)
        Row(
            modifier = Modifier
                .weight(1.2f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard.copy(alpha = 0.90f))
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            DetectionMode.entries.forEach { mode ->
                val isSelected = (mode == selectedMode)
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF26334A) else Color.Transparent,
                    label = "mode_bg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(bgColor)
                        .clickable { onModeChange(mode) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (mode == DetectionMode.LIVE_CAMERA && isCameraStreaming) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NeonRed))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (mode == DetectionMode.IMAGE_UPLOAD) "Fotoğraf Yükle" else "Canlı Kamera",
                            color = if (isSelected) TextPrimary else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Engine Selector (Segmented Pill)
        Row(
            modifier = Modifier
                .weight(1.0f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard.copy(alpha = 0.90f))
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            VisionEngineType.entries.forEach { engine ->
                val isSelected = (engine == selectedEngine)
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF26334A) else Color.Transparent,
                    label = "eng_bg"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(bgColor)
                        .clickable { onEngineChange(engine) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = engine.title.substringBefore(" "),
                        color = if (isSelected) TextPrimary else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectPathInputRow(
    onPathSubmit: (String) -> Unit
) {
    var pathText by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard.copy(alpha = 0.85f))
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = pathText,
            onValueChange = { pathText = it },
            placeholder = { Text("Doğrudan görsel dosya yolu (/Users/.../foto.jpg)", color = TextMuted, fontSize = 11.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (pathText.isNotBlank()) Color(0xFF2563EB) else Color(0x22FFFFFF))
                .clickable(enabled = pathText.isNotBlank()) {
                    onPathSubmit(pathText.trim().removeSurrounding("\"").removeSurrounding("'"))
                }
                .padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Text(
                text = "Yükle",
                color = if (pathText.isNotBlank()) Color.White else TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
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

