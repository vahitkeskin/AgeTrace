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
        assertEquals(1, candidates.size, "Single portrait must yield exactly 1 face candidate without duplicate nested boxes")
        val primary = candidates.first()
        assertTrue(primary.confidence > 0.60f, "Face detection confidence should be > 60%")
        assertTrue(primary.boundingBox.normalizedWidth in 0.15f..0.85f)
        assertTrue(primary.boundingBox.normalizedHeight in 0.20f..0.90f)
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

    @Test
    fun testMultiPersonGroupPortraitDetection() = runBlocking {
        val groupPortrait = SampleFaceFactory.generateGroupPortrait()
        val analyzeUseCase = AnalyzeImageUseCase(
            faceDetectorEngine = FaceDetectorEngineImpl(),
            ageEstimatorEngine = AgeEstimatorEngineImpl()
        )
        val result = analyzeUseCase(groupPortrait)
        assertTrue(result.faces.size >= 2, "Multiple faces must be detected in group portrait")
        for (face in result.faces) {
            assertTrue(face.badgeLabel.startsWith("%"))
            assertTrue(face.badgeLabel.endsWith("Age"))
            assertTrue(face.confidencePercentage in 60..99)
        }
    }

    @Test
    fun testPythonDetectionParser() {
        val sampleJson = """
        {
          "success": true,
          "engine": "AgeTrace Python Computer Vision Core (PIL)",
          "inference_time_ms": 42,
          "image": { "width": 640, "height": 480 },
          "faces_count": 1,
          "faces": [
            {
              "id": "py_face_1",
              "bbox": { "x": 0.25, "y": 0.15, "w": 0.50, "h": 0.65 },
              "confidence": 0.89,
              "age": 31,
              "classification": "18+ Adult",
              "badge": "%89 - 31 Age",
              "biometrics": {
                "craniofacial_ratio": 0.82,
                "wrinkle_density": 0.26,
                "skin_texture_variance": 0.19,
                "symmetry_score": 0.94
              }
            }
          ]
        }
        """.trimIndent()

        val parsed = com.vahitkeskin.agetrace.data.cv.PythonDetectionParser.parse(sampleJson)
        assertNotNull(parsed)
        assertTrue(parsed.success)
        assertEquals("AgeTrace Python Computer Vision Core (PIL)", parsed.engine)
        assertEquals(1, parsed.faces.size)

        val face = parsed.faces.first()
        assertEquals(31, face.estimatedAge)
        assertEquals("%89 - 31 Age", face.badgeLabel)
        assertEquals(AgeClassification.ADULT, face.classification)
        assertEquals(0.25f, face.boundingBox.normalizedX)
    }

    @Test
    fun testLivePythonDetectionIntegration() = runBlocking {
        if (com.vahitkeskin.agetrace.data.platform.PlatformPythonBridge.isPythonSupported()) {
            val portrait = SampleFaceFactory.generatePortrait(25)
            val bmpBytes = com.vahitkeskin.agetrace.data.cv.BmpEncoder.encode(portrait)
            val json = com.vahitkeskin.agetrace.data.platform.PlatformPythonBridge.analyzeWithPython(bmpBytes)
            assertNotNull(json, "Python execution must return valid json output")
            val parsed = com.vahitkeskin.agetrace.data.cv.PythonDetectionParser.parse(json)
            assertNotNull(parsed)
            assertTrue(parsed.faces.isNotEmpty(), "Python detector must identify face")
            val face = parsed.faces.first()
            assertTrue(face.confidencePercentage >= 85, "Confidence percentage should be >= 85%")
            assertTrue(face.badgeLabel.contains("Age"), "Badge must contain Age keyword")
        }
    }

    @Test
    fun testToggleAgeBalloonState() {
        val state = com.vahitkeskin.agetrace.presentation.mvi.AgeTraceContract.State()
        assertTrue(state.isAgeBalloonVisible, "Age balloon should be visible by default")
        val toggled = state.copy(isAgeBalloonVisible = !state.isAgeBalloonVisible)
        assertTrue(!toggled.isAgeBalloonVisible, "Age balloon should toggle to false")
    }

    @Test
    fun testInitialStateIsEmptyWithoutDefaultImage() {
        val state = com.vahitkeskin.agetrace.presentation.mvi.AgeTraceContract.State()
        assertEquals(null, state.displayBitmap, "Initial state must have null displayBitmap (empty image area)")
        assertEquals(null, state.pixelBuffer, "Initial state must have null pixelBuffer")
        assertEquals(null, state.currentImageSource, "Initial state must have null currentImageSource")
        assertTrue(state.detectedFaces.isEmpty(), "Initial state must have empty detectedFaces")
    }

    @Test
    fun testClearImageBackNavigationBehavior() {
        val face = DetectedFace(
            id = "face_test",
            boundingBox = BoundingBox(0.1f, 0.1f, 0.4f, 0.4f),
            confidenceScore = 0.9f,
            estimatedAge = 25,
            classification = AgeClassification.YOUNG_ADULT,
            biometrics = FacialBiometrics(0.8f, 0.2f, 0.2f, 0.9f, Pair(100f, 100f))
        )
        val loadedState = com.vahitkeskin.agetrace.presentation.mvi.AgeTraceContract.State(
            detectedFaces = listOf(face),
            selectedFace = face
        )
        val hasActiveImage = loadedState.displayBitmap != null || loadedState.currentImageSource != null || loadedState.detectedFaces.isNotEmpty()
        assertTrue(hasActiveImage, "BackHandler should be enabled when an image or detected faces are present")

        val clearedState = loadedState.copy(
            displayBitmap = null,
            pixelBuffer = null,
            currentImageSource = null,
            currentImageBytes = null,
            detectedFaces = emptyList(),
            selectedFace = null
        )
        val hasActiveImageAfterClear = clearedState.displayBitmap != null || clearedState.currentImageSource != null || clearedState.detectedFaces.isNotEmpty()
        assertTrue(!hasActiveImageAfterClear, "BackHandler should be disabled when image area is empty, enabling system app exit")
    }

    @Test
    fun testFacialHairPreventsMinorClassification() {
        val estimator = BiometricAgeEstimator()
        val adultPortrait = SampleFaceFactory.generatePortrait(36)
        // Simulate dark facial hair / beard stubble across lower-face chin region
        val pixels = adultPortrait.pixels.copyOf()
        val w = adultPortrait.width
        val h = adultPortrait.height
        val startY = (h * 0.55f).toInt()
        val endY = (h * 0.75f).toInt()
        val startX = (w * 0.35f).toInt()
        val endX = (w * 0.65f).toInt()
        for (y in startY..endY) {
            for (x in startX..endX) {
                val idx = y * w + x
                val noise = if ((x + y) % 2 == 0) 20 else 0
                pixels[idx] = (0xFF shl 24) or ((35 + noise) shl 16) or ((28 + noise) shl 8) or (22 + noise)
            }
        }
        val beardedBuffer = com.vahitkeskin.agetrace.domain.model.PixelBuffer(w, h, pixels)
        val faceBox = BoundingBox(0.2f, 0.15f, 0.6f, 0.7f)
        val estimation = estimator.estimate(beardedBuffer, faceBox)
        println("TEST ESTIMATION: age=${estimation.estimatedAge}, class=${estimation.classification}, biometrics=${estimation.biometrics}")
        assertTrue(
            estimation.estimatedAge >= 25,
            "An adult portrait with facial hair/beard must never be classified under 25 (got ${estimation.estimatedAge})"
        )
        assertTrue(
            estimation.classification != AgeClassification.MINOR,
            "Classification must be an adult category, never MINOR"
        )
    }

    @Test
    fun testPlatformOnnxEstimatorSafety() {
        val dummyFloats = FloatArray(3 * 96 * 96)
        // If not initialized, estimate safely returns null without throwing exceptions
        if (!com.vahitkeskin.agetrace.data.cv.PlatformOnnxEstimator.isSupported()) {
            val res = com.vahitkeskin.agetrace.data.cv.PlatformOnnxEstimator.estimate(dummyFloats)
            assertEquals(null, res)
        }
    }

    @Test
    fun testUniversalBlurAndIndividualOverride() {
        val portrait = SampleFaceFactory.generatePortrait(28)
        val faceBox1 = BoundingBox(0.1f, 0.2f, 0.3f, 0.4f)
        val faceBox2 = BoundingBox(0.6f, 0.2f, 0.3f, 0.4f)

        // 1. Universal blur: all faces blurred
        val allBlurredBuffer = portrait.withBlurredFaces(listOf(faceBox1, faceBox2))
        assertNotNull(allBlurredBuffer)
        assertTrue(!portrait.pixels.contentEquals(allBlurredBuffer.pixels), "Pixels must change when blurred")

        // 2. Individual face override: only face2 blurred (face1 unblurred)
        val singleFaceBlurred = portrait.withBlurredFaces(listOf(faceBox2))
        assertNotNull(singleFaceBlurred)

        // Face1 center area in singleFaceBlurred should match original portrait
        val face1CenterIdx = ((faceBox1.centerY * portrait.height).toInt() * portrait.width) + (faceBox1.centerX * portrait.width).toInt()
        assertEquals(portrait.pixels[face1CenterIdx], singleFaceBlurred.pixels[face1CenterIdx], "Unblurred face1 must retain original pixels")

        // Face2 center area in singleFaceBlurred should be blurred (different from original)
        val face2CenterIdx = ((faceBox2.centerY * portrait.height).toInt() * portrait.width) + (faceBox2.centerX * portrait.width).toInt()
        assertTrue(portrait.pixels[face2CenterIdx] != singleFaceBlurred.pixels[face2CenterIdx], "Blurred face2 must have blurred pixels")
    }
}
