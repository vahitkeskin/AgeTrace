package com.vahitkeskin.agetrace

import com.vahitkeskin.agetrace.data.cv.BiometricAgeEstimator
import com.vahitkeskin.agetrace.data.cv.EdgeAiFaceDetector
import com.vahitkeskin.agetrace.data.cv.SampleFaceFactory
import com.vahitkeskin.agetrace.data.repository.AgeEstimatorEngineImpl
import com.vahitkeskin.agetrace.data.repository.FaceDetectorEngineImpl
import com.vahitkeskin.agetrace.domain.model.AgeClassification
import com.vahitkeskin.agetrace.domain.model.BoundingBox
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.domain.model.FacialBiometrics
import com.vahitkeskin.agetrace.domain.usecase.AnalyzeImageUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AgeTraceCleanArchitectureTest {

    @Test
    fun testExactBadgeLabelFormat() {
        val face = DetectedFace(
            id = "face_1",
            boundingBox = BoundingBox(0.2f, 0.2f, 0.5f, 0.6f),
            confidenceScore = 0.874f,
            estimatedAge = 32,
            classification = AgeClassification.ADULT,
            biometrics = FacialBiometrics(
                craniofacialRatio = 0.85f,
                wrinkleDensityScore = 0.35f,
                skinTextureVariance = 0.28f,
                bilateralSymmetryScore = 0.92f,
                chrominanceCentroid = Pair(112f, 152f)
            )
        )

        // Verifies exact format required in prompt: "%87 - 32 Age"
        assertEquals("%87 - 32 Age", face.badgeLabel)
        assertEquals(87, face.confidencePercentage)
    }

    @Test
    fun testAgeClassificationBrackets() {
        assertEquals(AgeClassification.MINOR, AgeClassification.fromAge(8))
        assertEquals(AgeClassification.MINOR, AgeClassification.fromAge(17))
        assertEquals(AgeClassification.YOUNG_ADULT, AgeClassification.fromAge(18))
        assertEquals(AgeClassification.YOUNG_ADULT, AgeClassification.fromAge(25))
        assertEquals(AgeClassification.ADULT, AgeClassification.fromAge(35))
        assertEquals(AgeClassification.MATURE, AgeClassification.fromAge(55))
        assertEquals(AgeClassification.SENIOR, AgeClassification.fromAge(72))

        assertTrue(AgeClassification.ADULT.isAdult)
        assertTrue(!AgeClassification.MINOR.isAdult)
    }

    @Test
    fun testFaceDetectionOnRealPortraitPixels() {
        val portraitBuffer = SampleFaceFactory.generatePortrait(28)
        val detector = EdgeAiFaceDetector()
        val candidates = detector.detect(portraitBuffer)

        assertTrue(candidates.isNotEmpty(), "At least one face candidate must be detected in portrait")
        val primary = candidates.first()
        assertTrue(primary.confidence > 0.60f, "Face detection confidence should be > 60%")
        assertTrue(primary.boundingBox.normalizedWidth in 0.20f..0.85f)
        assertTrue(primary.boundingBox.normalizedHeight in 0.25f..0.90f)
    }

    @Test
    fun testBiometricAgeEstimatorOnYoungVsSenior() {
        val youngPortrait = SampleFaceFactory.generatePortrait(8)
        val seniorPortrait = SampleFaceFactory.generatePortrait(71)

        val estimator = BiometricAgeEstimator()
        val youngBox = BoundingBox(0.2f, 0.15f, 0.6f, 0.7f)
        val seniorBox = BoundingBox(0.2f, 0.15f, 0.6f, 0.7f)

        val youngEstimation = estimator.estimate(youngPortrait, youngBox)
        val seniorEstimation = estimator.estimate(seniorPortrait, seniorBox)

        assertTrue(
            youngEstimation.estimatedAge < seniorEstimation.estimatedAge,
            "Young portrait age (${youngEstimation.estimatedAge}) must be less than senior portrait age (${seniorEstimation.estimatedAge})"
        )

        assertTrue(
            seniorEstimation.biometrics.wrinkleDensityScore > youngEstimation.biometrics.wrinkleDensityScore,
            "Senior portrait must exhibit higher wrinkle density than child"
        )
    }

    @Test
    fun testEndToEndAnalyzeImageUseCasePipeline() = runBlocking {
        val analyzeUseCase = AnalyzeImageUseCase(
            faceDetectorEngine = FaceDetectorEngineImpl(),
            ageEstimatorEngine = AgeEstimatorEngineImpl()
        )

        val portrait = SampleFaceFactory.generatePortrait(35)
        val result = analyzeUseCase(portrait)

        assertTrue(result.faces.isNotEmpty(), "Faces should be identified")
        assertNotNull(result.metrics.primaryAge)
        assertTrue(result.metrics.inferenceTimeMs >= 0)
        assertTrue(result.metrics.isFaceDetected)

        val primaryFace = result.faces.first()
        assertTrue(primaryFace.badgeLabel.startsWith("%"))
        assertTrue(primaryFace.badgeLabel.endsWith("Age"))
    }
}
