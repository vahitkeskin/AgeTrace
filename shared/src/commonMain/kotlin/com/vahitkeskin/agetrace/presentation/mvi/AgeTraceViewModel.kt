package com.vahitkeskin.agetrace.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vahitkeskin.agetrace.data.platform.PlatformImageBridge
import com.vahitkeskin.agetrace.di.ServiceLocator
import com.vahitkeskin.agetrace.domain.model.DetectionMode
import com.vahitkeskin.agetrace.domain.model.ImageSource
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import com.vahitkeskin.agetrace.domain.repository.ImageProcessorRepository
import com.vahitkeskin.agetrace.domain.usecase.AnalyzeImageUseCase
import com.vahitkeskin.agetrace.domain.usecase.GetSampleFacesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AgeTraceViewModel(
    private val analyzeImageUseCase: AnalyzeImageUseCase = ServiceLocator.analyzeImageUseCase,
    private val getSampleFacesUseCase: GetSampleFacesUseCase = ServiceLocator.getSampleFacesUseCase,
    private val imageProcessorRepository: ImageProcessorRepository = ServiceLocator.imageProcessorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AgeTraceContract.State())
    val state: StateFlow<AgeTraceContract.State> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AgeTraceContract.Effect>()
    val effects: SharedFlow<AgeTraceContract.Effect> = _effects.asSharedFlow()

    private var cameraStreamJob: Job? = null
    private var liveFrameCounter = 0L

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                val samples = getSampleFacesUseCase()
                _state.update { it.copy(sampleFaces = samples) }

                // Automatically load and analyze a realistic benchmark portrait on launch
                val defaultSample = samples.find { it.id == "sample_young_adult" } ?: samples.firstOrNull()
                if (defaultSample != null) {
                    processImageSource(defaultSample, defaultSample.bytes)
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Başlangıç verisi yüklenemedi: ${e.message}") }
            }
        }
    }

    fun handleIntent(intent: AgeTraceContract.Intent) {
        when (intent) {
            is AgeTraceContract.Intent.ChangeMode -> {
                if (intent.mode == DetectionMode.LIVE_CAMERA) {
                    _state.update { it.copy(selectedMode = intent.mode) }
                    startCameraStream()
                } else {
                    stopCameraStream()
                    _state.update { it.copy(selectedMode = intent.mode) }
                }
            }

            is AgeTraceContract.Intent.SelectSample -> {
                stopCameraStream()
                processImageSource(intent.sample, intent.sample.bytes)
            }

            is AgeTraceContract.Intent.OnFileUploaded -> {
                stopCameraStream()
                val fileSource = ImageSource.LocalFile(intent.name, intent.bytes)
                processImageSource(fileSource, intent.bytes)
            }

            AgeTraceContract.Intent.TriggerFilePicker -> {
                viewModelScope.launch {
                    _effects.emit(AgeTraceContract.Effect.OpenNativeFilePicker)
                }
            }

            AgeTraceContract.Intent.ToggleCameraStream -> {
                if (_state.value.isCameraStreaming) {
                    stopCameraStream()
                } else {
                    startCameraStream()
                }
            }

            is AgeTraceContract.Intent.SelectFace -> {
                _state.update { it.copy(selectedFace = intent.face) }
            }

            AgeTraceContract.Intent.ClearCurrentAnalysis -> {
                stopCameraStream()
                _state.update {
                    it.copy(
                        currentImageSource = null,
                        pixelBuffer = null,
                        displayBitmap = null,
                        detectedFaces = emptyList(),
                        selectedFace = null,
                        metrics = it.metrics.copy(
                            isFaceDetected = false,
                            statusMessage = "Görsel temizlendi"
                        )
                    )
                }
            }
        }
    }

    private fun processImageSource(source: ImageSource, bytes: ByteArray) {
        viewModelScope.launch {
            _state.update { it.copy(isAnalyzing = true, errorMessage = null) }
            try {
                val buffer = imageProcessorRepository.decodeImage(bytes)
                if (buffer == null) {
                    _state.update {
                        it.copy(
                            isAnalyzing = false,
                            errorMessage = "Görsel formatı okunamadı veya dosya bozuk."
                        )
                    }
                    return@launch
                }

                val imageBitmap = PlatformImageBridge.pixelBufferToImageBitmap(buffer)
                val analysisResult = analyzeImageUseCase(buffer)

                _state.update {
                    it.copy(
                        currentImageSource = source,
                        pixelBuffer = buffer,
                        displayBitmap = imageBitmap,
                        detectedFaces = analysisResult.faces,
                        selectedFace = analysisResult.faces.firstOrNull(),
                        metrics = analysisResult.metrics,
                        isAnalyzing = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "Analiz sırasında hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    private fun startCameraStream() {
        stopCameraStream()
        _state.update { it.copy(isCameraStreaming = true) }
        cameraStreamJob = viewModelScope.launch {
            while (isActive) {
                liveFrameCounter++
                try {
                    val frameBuffer = imageProcessorRepository.captureOrSimulateCameraFrame(liveFrameCounter)
                    val frameBitmap = PlatformImageBridge.pixelBufferToImageBitmap(frameBuffer)
                    val analysisResult = analyzeImageUseCase(frameBuffer)

                    _state.update {
                        it.copy(
                            currentImageSource = ImageSource.LiveCameraFrame(
                                frameIndex = liveFrameCounter,
                                timestampMs = liveFrameCounter * 120L,
                                bytes = ByteArray(0)
                            ),
                            pixelBuffer = frameBuffer,
                            displayBitmap = frameBitmap,
                            detectedFaces = analysisResult.faces,
                            selectedFace = analysisResult.faces.firstOrNull(),
                            metrics = analysisResult.metrics.copy(
                                statusMessage = "Canlı Kamera Akışı • Canlı Yüz Takibi Aktif"
                            ),
                            isAnalyzing = false
                        )
                    }
                } catch (e: Exception) {
                    // Skip single frame error in stream
                }
                delay(120) // ~8-10 FPS real-time live edge analysis
            }
        }
    }

    private fun stopCameraStream() {
        cameraStreamJob?.cancel()
        cameraStreamJob = null
        _state.update { it.copy(isCameraStreaming = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stopCameraStream()
    }
}
