package com.vahitkeskin.agetrace.presentation.mvi

import androidx.compose.ui.graphics.ImageBitmap
import com.vahitkeskin.agetrace.domain.model.AnalysisMetrics
import com.vahitkeskin.agetrace.domain.model.CustomBlurRegion
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.domain.model.DetectionMode
import com.vahitkeskin.agetrace.domain.model.ImageSource
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import com.vahitkeskin.agetrace.domain.model.VisionEngineType

object AgeTraceContract {

    enum class ExportFormat {
        JPEG,
        PNG
    }

    data class ImagePageData(
        val id: String,
        val name: String = "Görsel",
        val bytes: ByteArray = ByteArray(0),
        val pixelBuffer: PixelBuffer,
        val cleanBitmap: ImageBitmap? = null,
        val activePixelBuffer: PixelBuffer = pixelBuffer,
        val displayBitmap: ImageBitmap? = cleanBitmap,
        val detectedFaces: List<DetectedFace> = emptyList(),
        val selectedFace: DetectedFace? = null,
        val blurredFaceIds: Set<String> = emptySet(),
        val customBlurRegions: List<CustomBlurRegion> = emptyList(),
        val metrics: AnalysisMetrics = AnalysisMetrics(),
        val isAnalyzing: Boolean = false
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ImagePageData) return false
            return id == other.id &&
                name == other.name &&
                pixelBuffer == other.pixelBuffer &&
                activePixelBuffer == other.activePixelBuffer &&
                displayBitmap == other.displayBitmap &&
                detectedFaces == other.detectedFaces &&
                selectedFace == other.selectedFace &&
                blurredFaceIds == other.blurredFaceIds &&
                customBlurRegions == other.customBlurRegions &&
                metrics == other.metrics &&
                isAnalyzing == other.isAnalyzing
        }

        override fun hashCode(): Int = id.hashCode()
    }

    data class State(
        val selectedMode: DetectionMode = DetectionMode.IMAGE_UPLOAD,
        val selectedEngine: VisionEngineType = VisionEngineType.PYTHON_CORE,
        val currentImageSource: ImageSource? = null,
        val pages: List<ImagePageData> = emptyList(),
        val activePageIndex: Int = 0,
        val isMinorBlurEnabled: Boolean = false,
        val isBlurAllEnabled: Boolean = false,
        val isAgeBalloonVisible: Boolean = true,
        val isAddingCustomBlur: Boolean = false,
        val isFaceListExpanded: Boolean = false,
        val isCameraStreaming: Boolean = false,
        val sampleFaces: List<ImageSource.PreloadedSample> = emptyList(),
        val errorMessage: String? = null
    ) {
        val currentPage: ImagePageData? get() = pages.getOrNull(activePageIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0)))
        val currentImageBytes: ByteArray? get() = currentPage?.bytes
        val pixelBuffer: PixelBuffer? get() = currentPage?.pixelBuffer
        val activePixelBuffer: PixelBuffer? get() = currentPage?.activePixelBuffer
        val cleanDisplayBitmap: ImageBitmap? get() = currentPage?.cleanBitmap
        val blurredDisplayBitmap: ImageBitmap? get() = currentPage?.displayBitmap
        val displayBitmap: ImageBitmap? get() = currentPage?.displayBitmap
        val detectedFaces: List<DetectedFace> get() = currentPage?.detectedFaces ?: emptyList()
        val selectedFace: DetectedFace? get() = currentPage?.selectedFace
        val blurredFaceIds: Set<String> get() = currentPage?.blurredFaceIds ?: emptySet()
        val customBlurRegions: List<CustomBlurRegion> get() = currentPage?.customBlurRegions ?: emptyList()
        val metrics: AnalysisMetrics get() = currentPage?.metrics ?: AnalysisMetrics()
        val isAnalyzing: Boolean get() = currentPage?.isAnalyzing == true || pages.any { it.isAnalyzing }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is State) return false
            return selectedMode == other.selectedMode &&
                selectedEngine == other.selectedEngine &&
                currentImageSource == other.currentImageSource &&
                pages == other.pages &&
                activePageIndex == other.activePageIndex &&
                isMinorBlurEnabled == other.isMinorBlurEnabled &&
                isBlurAllEnabled == other.isBlurAllEnabled &&
                isAgeBalloonVisible == other.isAgeBalloonVisible &&
                isAddingCustomBlur == other.isAddingCustomBlur &&
                isFaceListExpanded == other.isFaceListExpanded &&
                isCameraStreaming == other.isCameraStreaming &&
                sampleFaces == other.sampleFaces &&
                errorMessage == other.errorMessage
        }

        override fun hashCode(): Int = selectedMode.hashCode()
    }

    sealed interface Intent {
        data class ChangeMode(val mode: DetectionMode) : Intent
        data class ChangeEngine(val engine: VisionEngineType) : Intent
        data class SelectSample(val sample: ImageSource.PreloadedSample) : Intent
        data class OnFileUploaded(val name: String, val bytes: ByteArray) : Intent
        data class OnMultipleFilesUploaded(val files: List<Pair<String, ByteArray>>) : Intent
        data class SelectActivePage(val index: Int) : Intent
        data class RemovePage(val index: Int) : Intent
        data object TriggerFilePicker : Intent
        data object TriggerCameraPicker : Intent
        data object ToggleCameraStream : Intent
        data class SelectFace(val face: DetectedFace) : Intent
        data object ClearCurrentAnalysis : Intent
        data object RerunAnalysis : Intent
        data object RotateCurrentImage : Intent
        data object ToggleMinorBlur : Intent
        data object ToggleAllBlur : Intent
        data class ToggleFaceBlur(val faceId: String) : Intent
        data object ToggleCustomBlurMode : Intent
        data class AddCustomBlurRegion(val normX: Float, val normY: Float) : Intent
        data class UpdateCustomBlurRegion(val region: CustomBlurRegion) : Intent
        data class RemoveCustomBlurRegion(val regionId: String) : Intent
        data object ToggleFaceListExpanded : Intent
        data class ExportCurrentImage(val format: ExportFormat) : Intent
        data class ExportAllImages(val format: ExportFormat) : Intent
        data object ToggleAgeBalloon : Intent
    }

    sealed interface Effect {
        data object OpenNativeFilePicker : Effect
        data object OpenNativeCameraPicker : Effect
        data class Notify(val message: String) : Effect
    }
}
