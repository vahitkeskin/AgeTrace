package com.vahitkeskin.agetrace.presentation.mvi

import androidx.compose.ui.graphics.ImageBitmap
import com.vahitkeskin.agetrace.domain.model.AnalysisMetrics
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.domain.model.DetectionMode
import com.vahitkeskin.agetrace.domain.model.ImageSource
import com.vahitkeskin.agetrace.domain.model.PixelBuffer

object AgeTraceContract {

    data class State(
        val selectedMode: DetectionMode = DetectionMode.IMAGE_UPLOAD,
        val currentImageSource: ImageSource? = null,
        val pixelBuffer: PixelBuffer? = null,
        val displayBitmap: ImageBitmap? = null,
        val detectedFaces: List<DetectedFace> = emptyList(),
        val selectedFace: DetectedFace? = null,
        val metrics: AnalysisMetrics = AnalysisMetrics(),
        val isAnalyzing: Boolean = false,
        val isCameraStreaming: Boolean = false,
        val sampleFaces: List<ImageSource.PreloadedSample> = emptyList(),
        val errorMessage: String? = null
    )

    sealed interface Intent {
        data class ChangeMode(val mode: DetectionMode) : Intent
        data class SelectSample(val sample: ImageSource.PreloadedSample) : Intent
        data class OnFileUploaded(val name: String, val bytes: ByteArray) : Intent
        data object TriggerFilePicker : Intent
        data object ToggleCameraStream : Intent
        data class SelectFace(val face: DetectedFace) : Intent
        data object ClearCurrentAnalysis : Intent
    }

    sealed interface Effect {
        data object OpenNativeFilePicker : Effect
        data class Notify(val message: String) : Effect
    }
}
