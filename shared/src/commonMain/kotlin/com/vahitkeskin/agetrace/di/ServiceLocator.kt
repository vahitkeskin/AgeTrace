package com.vahitkeskin.agetrace.di

import com.vahitkeskin.agetrace.data.repository.AgeEstimatorEngineImpl
import com.vahitkeskin.agetrace.data.repository.FaceDetectorEngineImpl
import com.vahitkeskin.agetrace.data.repository.ImageProcessorRepositoryImpl
import com.vahitkeskin.agetrace.domain.repository.AgeEstimatorEngine
import com.vahitkeskin.agetrace.domain.repository.FaceDetectorEngine
import com.vahitkeskin.agetrace.domain.repository.ImageProcessorRepository
import com.vahitkeskin.agetrace.domain.usecase.AnalyzeImageUseCase
import com.vahitkeskin.agetrace.domain.usecase.GetSampleFacesUseCase

/**
 * Dependency Injection Service Locator.
 * Maintains clean inversion of control across Presentation, Domain, and Data layers.
 */
object ServiceLocator {

    val faceDetectorEngine: FaceDetectorEngine by lazy {
        FaceDetectorEngineImpl()
    }

    val ageEstimatorEngine: AgeEstimatorEngine by lazy {
        AgeEstimatorEngineImpl()
    }

    val imageProcessorRepository: ImageProcessorRepository by lazy {
        ImageProcessorRepositoryImpl()
    }

    val analyzeImageUseCase: AnalyzeImageUseCase by lazy {
        AnalyzeImageUseCase(
            faceDetectorEngine = faceDetectorEngine,
            ageEstimatorEngine = ageEstimatorEngine
        )
    }

    val getSampleFacesUseCase: GetSampleFacesUseCase by lazy {
        GetSampleFacesUseCase(
            imageProcessorRepository = imageProcessorRepository
        )
    }
}
