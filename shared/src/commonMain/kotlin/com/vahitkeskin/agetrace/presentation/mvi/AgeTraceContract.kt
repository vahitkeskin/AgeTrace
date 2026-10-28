package com.vahitkeskin.agetrace.presentation.mvi

import androidx.compose.ui.graphics.ImageBitmap
import com.vahitkeskin.agetrace.domain.model.AnalysisMetrics
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.domain.model.DetectionMode
import com.vahitkeskin.agetrace.domain.model.ImageSource
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import com.vahitkeskin.agetrace.domain.model.VisionEngineType

object AgeTraceContract {

    data class State(
        val selectedMode: DetectionMode = DetectionMode.IMAGE_UPLOAD,
        val selectedEngine: VisionEngineType = VisionEngineType.PYTHON_CORE,
        val currentImageSource: ImageSource? = null,
        val currentImageBytes: ByteArray? = null,
        val pixelBuffer: PixelBuffer? = null,
        val displayBitmap: ImageBitmap? = null,
        val detectedFaces: List<DetectedFace> = emptyList(),
        val selectedFace: DetectedFace? = null,
        val metrics: AnalysisMetrics = AnalysisMetrics(),
        val isAnalyzing: Boolean = false,
        val isCameraStreaming: Boolean = false,
        val sampleFaces: List<ImageSource.PreloadedSample> = emptyList(),
        val errorMessage: String? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is State) return false
            return selectedMode == other.selectedMode &&
                selectedEngine == other.selectedEngine &&
                currentImageSource == other.currentImageSource &&
                pixelBuffer == other.pixelBuffer &&
                displayBitmap == other.displayBitmap &&
                detectedFaces == other.detectedFaces &&
                selectedFace == other.selectedFace &&
                metrics == other.metrics &&
                isAnalyzing == other.isAnalyzing &&
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
        data object TriggerFilePicker : Intent
        data object ToggleCameraStream : Intent
        data class SelectFace(val face: DetectedFace) : Intent
        data object ClearCurrentAnalysis : Intent
        data object RerunAnalysis : Intent
    }

    sealed interface Effect {
        data object OpenNativeFilePicker : Effect
        data class Notify(val message: String) : Effect
    }
}
