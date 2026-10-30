package com.vahitkeskin.agetrace.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.vahitkeskin.agetrace.domain.model.CustomBlurRegion
import com.vahitkeskin.agetrace.domain.model.DetectionMode
import com.vahitkeskin.agetrace.presentation.mvi.AgeTraceContract
import com.vahitkeskin.agetrace.presentation.mvi.AgeTraceViewModel
import com.vahitkeskin.agetrace.presentation.theme.AccentBlue
import com.vahitkeskin.agetrace.presentation.theme.AmbientGlassBackdrop
import com.vahitkeskin.agetrace.presentation.theme.BackgroundDark
import com.vahitkeskin.agetrace.presentation.theme.GlassBorder
import com.vahitkeskin.agetrace.presentation.theme.GlassSpecularBorderBrush
import com.vahitkeskin.agetrace.presentation.theme.GlassSpecularBorderBrushSelected
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.vahitkeskin.agetrace.presentation.ui.components.CustomBlurOverlay
import com.vahitkeskin.agetrace.presentation.ui.components.DetectedFaceAvatarsStrip
import com.vahitkeskin.agetrace.presentation.ui.components.FacebookShimmerSkeletonViewport
import com.vahitkeskin.agetrace.presentation.ui.components.FacebookShimmerViewportOverlay
import com.vahitkeskin.agetrace.presentation.ui.components.FaceOverlayCanvas
import com.vahitkeskin.agetrace.presentation.ui.components.FileUploadDropZone
import com.vahitkeskin.agetrace.presentation.ui.components.FullscreenZoomableImageViewer
import com.vahitkeskin.agetrace.presentation.ui.components.MetricsSidePanel
import com.vahitkeskin.agetrace.presentation.ui.components.PlatformBackHandler
import com.vahitkeskin.agetrace.presentation.ui.components.rememberCameraCaptureLauncher
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

    var isFullscreenOpen by remember { mutableStateOf(false) }

    // Android System Back Navigation Handler:
    // 1. If Fullscreen Zoom Viewer is open, first back press closes it.
    // 2. If an image/analysis is active, back press clears the image.
    // 3. If image is clear, back press allows normal app exit.
    val hasActiveImage = state.displayBitmap != null || state.currentImageSource != null || state.detectedFaces.isNotEmpty()
    PlatformBackHandler(enabled = isFullscreenOpen || hasActiveImage) {
        if (isFullscreenOpen) {
            isFullscreenOpen = false
        } else {
            viewModel.handleIntent(AgeTraceContract.Intent.ClearCurrentAnalysis)
        }
    }

    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(2500)
            toastMessage = null
        }
    }

    // Multiplatform Native Gallery Launcher (Android PhotoPicker, iOS Photos, Desktop FileDialog)
    val galleryLauncher = rememberGalleryPickerLauncher { pickedFiles ->
        if (pickedFiles.isNotEmpty()) {
            viewModel.handleIntent(AgeTraceContract.Intent.OnMultipleFilesUploaded(pickedFiles))
        }
    }

    // Multiplatform Native Camera Launcher with Runtime Permission Dialog
    val cameraLauncher = rememberCameraCaptureLauncher { name, bytes ->
        viewModel.handleIntent(AgeTraceContract.Intent.OnFileUploaded(name, bytes))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AgeTraceContract.Effect.OpenNativeFilePicker -> {
                    galleryLauncher()
                }
                AgeTraceContract.Effect.OpenNativeCameraPicker -> {
                    cameraLauncher()
                }
                is AgeTraceContract.Effect.Notify -> {
                    toastMessage = effect.message
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
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // 1. Floating Frosted Glass Header
            MinimalHeader(
                isAnalyzing = state.isAnalyzing,
                selectedMode = state.selectedMode,
                isCameraStreaming = state.isCameraStreaming,
                onModeChange = { mode -> viewModel.handleIntent(AgeTraceContract.Intent.ChangeMode(mode)) },
                onCameraClick = { cameraLauncher() },
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
                                onPageChange = { index -> viewModel.handleIntent(AgeTraceContract.Intent.SelectActivePage(index)) },
                                onFaceClick = { face ->
                                    viewModel.handleIntent(AgeTraceContract.Intent.ToggleFaceBlur(face.id))
                                    viewModel.handleIntent(AgeTraceContract.Intent.SelectFace(face))
                                },
                                onRotateClick = { viewModel.handleIntent(AgeTraceContract.Intent.RotateCurrentImage) },
                                onToggleBlurClick = { viewModel.handleIntent(AgeTraceContract.Intent.ToggleAllBlur) },
                                onToggleAgeBalloonClick = { viewModel.handleIntent(AgeTraceContract.Intent.ToggleAgeBalloon) },
                                onToggleCustomBlurMode = { viewModel.handleIntent(AgeTraceContract.Intent.ToggleCustomBlurMode) },
                                onAddCustomBlur = { normX, normY -> viewModel.handleIntent(AgeTraceContract.Intent.AddCustomBlurRegion(normX, normY)) },
                                onUpdateCustomBlur = { id, normX, normY, normW, normH ->
                                    viewModel.handleIntent(AgeTraceContract.Intent.UpdateCustomBlurRegion(CustomBlurRegion(id, normX, normY, normW, normH)))
                                },
                                onRemoveCustomBlur = { id -> viewModel.handleIntent(AgeTraceContract.Intent.RemoveCustomBlurRegion(id)) },
                                onPickFileClick = { galleryLauncher() },
                                onCameraClick = { cameraLauncher() },
                                onImageClick = { isFullscreenOpen = true },
                                onExportClick = { format -> viewModel.handleIntent(AgeTraceContract.Intent.ExportCurrentImage(format)) },
                                onExportAllClick = { format -> viewModel.handleIntent(AgeTraceContract.Intent.ExportAllImages(format)) }
                            )

                            if (state.detectedFaces.isNotEmpty()) {
                                DetectedFaceAvatarsStrip(
                                    modifier = Modifier.fillMaxWidth(),
                                    faces = state.detectedFaces,
                                    pixelBuffer = state.pixelBuffer,
                                    selectedFace = state.selectedFace,
                                    blurredFaceIds = state.blurredFaceIds,
                                    isExpanded = state.isFaceListExpanded,
                                    onToggleExpand = { viewModel.handleIntent(AgeTraceContract.Intent.ToggleFaceListExpanded) },
                                    onFaceClick = { face ->
                                        viewModel.handleIntent(AgeTraceContract.Intent.ToggleFaceBlur(face.id))
                                        viewModel.handleIntent(AgeTraceContract.Intent.SelectFace(face))
                                    },
                                    onToggleFaceBlur = { faceId -> viewModel.handleIntent(AgeTraceContract.Intent.ToggleFaceBlur(faceId)) }
                                )
                            }

                            if (state.selectedMode == DetectionMode.IMAGE_UPLOAD) {
                                FileUploadDropZone(
                                    modifier = Modifier.fillMaxWidth(),
                                    sampleFaces = state.sampleFaces,
                                    currentImageSource = state.currentImageSource,
                                    onPickFileClick = { galleryLauncher() },
                                    onCameraClick = { cameraLauncher() },
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
                            onPageChange = { index -> viewModel.handleIntent(AgeTraceContract.Intent.SelectActivePage(index)) },
                            onFaceClick = { face ->
                                viewModel.handleIntent(AgeTraceContract.Intent.ToggleFaceBlur(face.id))
                                viewModel.handleIntent(AgeTraceContract.Intent.SelectFace(face))
                            },
                            onRotateClick = { viewModel.handleIntent(AgeTraceContract.Intent.RotateCurrentImage) },
                            onToggleBlurClick = { viewModel.handleIntent(AgeTraceContract.Intent.ToggleAllBlur) },
                            onToggleAgeBalloonClick = { viewModel.handleIntent(AgeTraceContract.Intent.ToggleAgeBalloon) },
                            onToggleCustomBlurMode = { viewModel.handleIntent(AgeTraceContract.Intent.ToggleCustomBlurMode) },
                            onAddCustomBlur = { normX, normY -> viewModel.handleIntent(AgeTraceContract.Intent.AddCustomBlurRegion(normX, normY)) },
                            onUpdateCustomBlur = { id, normX, normY, normW, normH ->
                                viewModel.handleIntent(AgeTraceContract.Intent.UpdateCustomBlurRegion(CustomBlurRegion(id, normX, normY, normW, normH)))
                            },
                            onRemoveCustomBlur = { id -> viewModel.handleIntent(AgeTraceContract.Intent.RemoveCustomBlurRegion(id)) },
                            onPickFileClick = { galleryLauncher() },
                            onCameraClick = { cameraLauncher() },
                            onImageClick = { isFullscreenOpen = true },
                            onExportClick = { format -> viewModel.handleIntent(AgeTraceContract.Intent.ExportCurrentImage(format)) },
                            onExportAllClick = { format -> viewModel.handleIntent(AgeTraceContract.Intent.ExportAllImages(format)) }
                        )

                        if (state.detectedFaces.isNotEmpty()) {
                            DetectedFaceAvatarsStrip(
                                modifier = Modifier.fillMaxWidth(),
                                faces = state.detectedFaces,
                                pixelBuffer = state.pixelBuffer,
                                selectedFace = state.selectedFace,
                                blurredFaceIds = state.blurredFaceIds,
                                isExpanded = state.isFaceListExpanded,
                                onToggleExpand = { viewModel.handleIntent(AgeTraceContract.Intent.ToggleFaceListExpanded) },
                                onFaceClick = { face ->
                                    viewModel.handleIntent(AgeTraceContract.Intent.ToggleFaceBlur(face.id))
                                    viewModel.handleIntent(AgeTraceContract.Intent.SelectFace(face))
                                },
                                onToggleFaceBlur = { faceId -> viewModel.handleIntent(AgeTraceContract.Intent.ToggleFaceBlur(faceId)) }
                            )
                        }

                        if (state.selectedMode == DetectionMode.IMAGE_UPLOAD) {
                            FileUploadDropZone(
                                modifier = Modifier.fillMaxWidth(),
                                sampleFaces = state.sampleFaces,
                                currentImageSource = state.currentImageSource,
                                onPickFileClick = { galleryLauncher() },
                                onCameraClick = { cameraLauncher() },
                                onSelectSample = { sample -> viewModel.handleIntent(AgeTraceContract.Intent.SelectSample(sample)) }
                            )
                        }

                        MetricsSidePanel(
                            modifier = Modifier.fillMaxWidth(),
                            metrics = state.metrics,
                            selectedFace = state.selectedFace
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // 3. Fullscreen Interactive Zoom Viewer Overlay
        AnimatedVisibility(
            visible = isFullscreenOpen && state.displayBitmap != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            FullscreenZoomableImageViewer(
                state = state,
                onFaceClick = { face -> viewModel.handleIntent(AgeTraceContract.Intent.SelectFace(face)) },
                onDismiss = { isFullscreenOpen = false }
            )
        }

        // 4. Floating Feedback Toast Pill
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xF00F172A))
                    .border(1.dp, GlassSpecularBorderBrushSelected, RoundedCornerShape(999.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = toastMessage ?: "",
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
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
    onCameraClick: () -> Unit,
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
                                .clickable {
                                    if (mode == DetectionMode.LIVE_CAMERA) {
                                        onCameraClick()
                                    } else {
                                        onModeChange(mode)
                                    }
                                }
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
    onPageChange: (Int) -> Unit,
    onFaceClick: (com.vahitkeskin.agetrace.domain.model.DetectedFace) -> Unit,
    onRotateClick: () -> Unit,
    onToggleBlurClick: () -> Unit,
    onToggleAgeBalloonClick: () -> Unit,
    onToggleCustomBlurMode: () -> Unit,
    onAddCustomBlur: (normX: Float, normY: Float) -> Unit,
    onUpdateCustomBlur: (id: String, normX: Float, normY: Float, normW: Float, normH: Float) -> Unit,
    onRemoveCustomBlur: (id: String) -> Unit,
    onPickFileClick: () -> Unit,
    onCameraClick: () -> Unit,
    onImageClick: () -> Unit,
    onExportClick: (AgeTraceContract.ExportFormat) -> Unit,
    onExportAllClick: (AgeTraceContract.ExportFormat) -> Unit
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
        val pages = state.pages
        if (pages.isNotEmpty()) {
            val pagerState = rememberPagerState(
                initialPage = state.activePageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0)),
                pageCount = { pages.size }
            )

            LaunchedEffect(pagerState.currentPage) {
                if (pagerState.currentPage != state.activePageIndex) {
                    onPageChange(pagerState.currentPage)
                }
            }

            LaunchedEffect(state.activePageIndex) {
                if (state.activePageIndex in 0 until pages.size && pagerState.currentPage != state.activePageIndex) {
                    pagerState.animateScrollToPage(state.activePageIndex)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
            ) {
                // HorizontalPager for smooth left/right swiping between uploaded images
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    val page = pages.getOrNull(pageIndex) ?: return@HorizontalPager
                    val displayBitmap = page.displayBitmap
                    val pixelBuffer = page.pixelBuffer

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onImageClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (displayBitmap != null) {
                            Image(
                                bitmap = displayBitmap,
                                contentDescription = "Page Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        FaceOverlayCanvas(
                            modifier = Modifier.fillMaxSize(),
                            faces = page.detectedFaces,
                            imageWidth = pixelBuffer.width,
                            imageHeight = pixelBuffer.height,
                            selectedFace = page.selectedFace,
                            onFaceClick = onFaceClick,
                            isScanning = state.isCameraStreaming || page.isAnalyzing,
                            isAgeBalloonVisible = state.isAgeBalloonVisible
                        )

                        CustomBlurOverlay(
                            modifier = Modifier.fillMaxSize(),
                            customBlurRegions = page.customBlurRegions,
                            imageWidth = pixelBuffer.width,
                            imageHeight = pixelBuffer.height,
                            isAddingMode = state.isAddingCustomBlur,
                            onAddRegion = onAddCustomBlur,
                            onUpdateRegion = onUpdateCustomBlur,
                            onRemoveRegion = onRemoveCustomBlur
                        )

                        if (page.isAnalyzing) {
                            FacebookShimmerViewportOverlay()
                        }
                    }
                }

                // Sleek Floating Control Header across top of viewport
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Action Group: Rotate + Fullscreen Zoom + Page Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // Frosted Glass Rotate Button
                        GlassmorphicIconButton(
                            icon = "🔄",
                            onClick = onRotateClick,
                            size = 32.dp
                        )

                        // Frosted Glass Fullscreen Zoom Button
                        GlassmorphicIconButton(
                            icon = "⛶",
                            onClick = onImageClick,
                            size = 32.dp
                        )

                        // Multi-page Counter & Add Image Button
                        if (pages.size > 1) {
                            Box(
                                modifier = Modifier
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xCC0F172A))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(999.dp))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${pagerState.currentPage + 1}/${pages.size}",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Add More Images Button
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0x990F172A))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(999.dp))
                                .clickable { onPickFileClick() }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "➕ Ekle",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Right Toggle Group: Custom Blur + Universal Blur + Age Balloon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // Custom Blur Region Toggle Button
                        Row(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    brush = if (state.isAddingCustomBlur) {
                                        Brush.verticalGradient(
                                            listOf(Color(0xD90284C7), Color(0xB30369A1))
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(Color(0xD90F172A), Color(0xB30F172A))
                                        )
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (state.isAddingCustomBlur) AccentBlue else Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(999.dp)
                                )
                                .clickable { onToggleCustomBlurMode() }
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "➕", fontSize = 10.sp)
                            Text(
                                text = "Sansür",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.isAddingCustomBlur) Color.White else TextSecondary
                            )
                        }

                        val isBlurActive = state.isBlurAllEnabled || state.isMinorBlurEnabled
                        val blurThumbOffset by animateDpAsState(
                            targetValue = if (isBlurActive) 13.dp else 2.dp,
                            label = "blur_switch"
                        )

                        val ageBalloonThumbOffset by animateDpAsState(
                            targetValue = if (state.isAgeBalloonVisible) 13.dp else 2.dp,
                            label = "age_balloon_switch"
                        )

                        // 1. Universal Face Blur Switch
                        Row(
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    brush = if (isBlurActive) {
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
                                    brush = if (isBlurActive) {
                                        Brush.verticalGradient(
                                            listOf(MinorColor.copy(alpha = 0.8f), MinorColor.copy(alpha = 0.3f))
                                        )
                                    } else {
                                        GlassSpecularBorderBrush
                                    },
                                    shape = RoundedCornerShape(999.dp)
                                )
                                .clickable { onToggleBlurClick() }
                                .padding(horizontal = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = if (isBlurActive) "🛡️ Bulanık" else "👁️ Net",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isBlurActive) MinorColor else TextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )

                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .height(15.dp)
                                    .clip(CircleShape)
                                    .background(if (isBlurActive) MinorColor else Color(0xFF334155))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = blurThumbOffset, top = 2.dp)
                                        .size(11.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }

                        // 2. Age Balloon & Frame Switch
                        Row(
                            modifier = Modifier
                                .height(32.dp)
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
                                .padding(horizontal = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "🎈 Yaş",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.isAgeBalloonVisible) Color(0xFF38BDF8) else TextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )

                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .height(15.dp)
                                    .clip(CircleShape)
                                    .background(if (state.isAgeBalloonVisible) Color(0xFF38BDF8) else Color(0xFF334155))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = ageBalloonThumbOffset, top = 2.dp)
                                        .size(11.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }

                // Adding-mode floating banner at bottom-center
                if (state.isAddingCustomBlur) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 44.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xE60F172A))
                            .border(1.dp, AccentBlue, RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "📍 Bulanıklık için görsele dokun (büyüt/küçült/taşı)",
                            color = AccentBlue,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Sleek Floating Export Buttons at bottom-end of viewport
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Single JPG
                    Row(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xD90F172A), Color(0xB30F172A))))
                            .border(1.dp, GlassSpecularBorderBrush, RoundedCornerShape(999.dp))
                            .clickable { onExportClick(AgeTraceContract.ExportFormat.JPEG) }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(text = "⬇️", fontSize = 9.sp)
                        Text(text = "JPG", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    // Single PNG
                    Row(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xD90F172A), Color(0xB30F172A))))
                            .border(1.dp, GlassSpecularBorderBrush, RoundedCornerShape(999.dp))
                            .clickable { onExportClick(AgeTraceContract.ExportFormat.PNG) }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(text = "⬇️", fontSize = 9.sp)
                        Text(text = "PNG", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                    }

                    // Batch Export when multiple images are present
                    if (pages.size > 1) {
                        Row(
                            modifier = Modifier
                                .height(28.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Brush.verticalGradient(listOf(Color(0xD90284C7), Color(0xB30369A1))))
                                .border(1.dp, AccentBlue, RoundedCornerShape(999.dp))
                                .clickable { onExportAllClick(AgeTraceContract.ExportFormat.JPEG) }
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(text = "📦", fontSize = 9.sp)
                            Text(text = "Tümü", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        } else if (state.isAnalyzing) {
            // Wavy Facebook Shimmer effect while image is loading / analyzing
            FacebookShimmerSkeletonViewport()
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

                    // Polite Action Buttons inside empty state: [ 📷 Fotoğraf Çek ] [ 🖼️ Galeri ]
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassmorphicPill(
                            isSelected = true,
                            selectedColor = AccentBlue,
                            onClick = onCameraClick
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "📷", fontSize = 13.sp)
                                Text(
                                    text = "Fotoğraf Çek",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        GlassmorphicPill(
                            isSelected = false,
                            selectedColor = AccentBlue,
                            onClick = onPickFileClick
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "🖼️", fontSize = 13.sp)
                                Text(
                                    text = "Galeri",
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
}
