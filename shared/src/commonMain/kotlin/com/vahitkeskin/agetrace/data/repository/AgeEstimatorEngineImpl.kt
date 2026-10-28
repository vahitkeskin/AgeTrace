package com.vahitkeskin.agetrace.data.repository

import com.vahitkeskin.agetrace.data.cv.BiometricAgeEstimator
import com.vahitkeskin.agetrace.domain.model.BoundingBox
import com.vahitkeskin.agetrace.domain.model.PixelBuffer
import com.vahitkeskin.agetrace.domain.repository.AgeEstimatorEngine
import com.vahitkeskin.agetrace.domain.repository.RawAgeEstimation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AgeEstimatorEngineImpl(
    private val estimator: BiometricAgeEstimator = BiometricAgeEstimator()
) : AgeEstimatorEngine {

    override suspend fun estimateAge(
        fullBuffer: PixelBuffer,
        faceBox: BoundingBox
    ): RawAgeEstimation = withContext(Dispatchers.Default) {
        estimator.estimate(fullBuffer, faceBox)
    }
}
