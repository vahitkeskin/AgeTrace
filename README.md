# AgeTrace • Edge AI Biometric Face & Age Detection System

**AgeTrace** is a high-performance cross-platform application (Android, iOS, Desktop) built with **Compose Multiplatform** and structured following strict **Clean Architecture** principles. It performs local, offline Edge AI face detection and biometric biological age estimation directly on image pixels without relying on any cloud APIs.

---

## 🎯 Features & Architectural Highlights

1. **Precision Edge AI Face Detection & Overlay**:
   - Analyzes raw pixel buffers using skin chrominance clustering (YCbCr/HSV) and facial landmark valley verification.
   - Dynamically calculates coordinates `(x, y, w, h)` and renders a signature **Red Bounding Box (`#FF1744`)** with cyber corner brackets.
   - Displays a floating badge directly above the face box with the exact format:
     $$\mathbf{\%87 - 32\ Age}$$
     *(Exact format: Confidence percentage, dash, and estimated age).*

2. **Real-Time Biometric Age Estimation**:
   - **Anthropometric Craniofacial Morphology**: Measures the ratio of forehead height to lower facial expansion.
   - **High-Frequency Wrinkle Gradient Integration**: Applies Sobel filters to detect forehead expression lines and periorbital crow's feet.
   - **Skin Texture Roughness & Local Variance**: Quantifies micro-variances across cheek regions.
   - **Biometric Classification**: Categorizes detected individuals into `18- Minor` vs `18+ Adult` (with Young Adult, Adult, Mature, Senior cohorts).

3. **Multi-Mode Operation**:
   - **Image Upload**: Native file picker (PNG, JPG, WEBP) and drag & drop support.
   - **Live Camera**: Real-time HUD scanner with sweeping laser reticle and instant telemetry.
   - **Calibrated Benchmark Portraits**: Five curated biological benchmark portraits across distinct age cohorts (8yo, 24yo, 35yo, 52yo, 71yo).

4. **Modern Glassmorphic Dark UI**:
   - Minimalist, cyber-grade dark aesthetic with frosted translucent cards, glowing neon accents, and responsive layout for mobile and desktop.

---

## 🏛️ Clean Architecture Structure

```
com.vahitkeskin.agetrace/
├── domain/                               # Enterprise Business Logic (Framework Agnostic)
│   ├── model/
│   │   ├── BoundingBox.kt                # Normalized & absolute spatial coordinates
│   │   ├── DetectedFace.kt               # Face entity with "%87 - 32 Age" badgeLabel
│   │   ├── AgeClassification.kt         # 18- Minor vs 18+ Adult bracket enum
│   │   ├── FacialBiometrics.kt           # Anthropometry, wrinkles, texture, symmetry
│   │   ├── AnalysisMetrics.kt            # Latency (ms), resolution, status
│   │   ├── PixelBuffer.kt                # Pure Kotlin 2D raster image buffer
│   │   ├── ImageSource.kt                # LocalFile, PreloadedSample, LiveCameraFrame
│   │   └── DetectionMode.kt              # Image Upload vs Live Camera
│   ├── repository/                       # Port Interfaces
│   │   ├── FaceDetectorEngine.kt
│   │   ├── AgeEstimatorEngine.kt
│   │   └── ImageProcessorRepository.kt
│   └── usecase/                          # Use Cases / Interactors
│       ├── AnalyzeImageUseCase.kt        # End-to-end detection & estimation pipeline
│       └── GetSampleFacesUseCase.kt      # Benchmark portrait retrieval
│
├── data/                                 # Data & Computer Vision Engine
│   ├── cv/
│   │   ├── ColorSpaceConverter.kt        # YCbCr / RGB skin chrominance segmentation
│   │   ├── ConvolutionKernels.kt         # Sobel horizontal & 2D gradient filters
│   │   ├── EdgeAiFaceDetector.kt         # Multi-scale face detection & NMS
│   │   ├── BiometricAgeEstimator.kt      # Real anthropometry & wrinkle regression
│   │   ├── SampleFaceFactory.kt          # Biometric portrait synthesis
│   │   └── BmpEncoder.kt                 # Pure Kotlin 32-bit BMP raster encoder
│   ├── platform/
│   │   └── PlatformImageBridge.kt        # Expect/actual bridge for JVM, Android, iOS
│   └── repository/                       # Port Implementations
│       ├── FaceDetectorEngineImpl.kt
│       ├── AgeEstimatorEngineImpl.kt
│       └── ImageProcessorRepositoryImpl.kt
│
├── presentation/                         # Presentation Layer (MVI + Compose UI)
│   ├── theme/
│   │   ├── Color.kt                      # Dark OLED & Neon Red palette
│   │   └── GlassmorphicCard.kt           # Frosted glass card composable
│   ├── mvi/
│   │   ├── AgeTraceContract.kt           # State, Intent, Effect single source of truth
│   │   └── AgeTraceViewModel.kt          # Coroutines StateFlow business coordinator
│   └── ui/
│       ├── AgeTraceScreen.kt             # Responsive adaptive screen
│       └── components/
│           ├── FaceOverlayCanvas.kt      # Red bounding box & floating label canvas
│           ├── MetricsSidePanel.kt       # Live accuracy, 18+ Adult status, biometrics
│           ├── ModeSelectorBar.kt        # Mode switcher
│           └── FileUploadDropZone.kt     # File picker & benchmark gallery
│
└── di/
    └── ServiceLocator.kt                 # Decoupled Dependency Injection container
```

---

## 🚀 Running the Application

### Desktop (macOS / Linux / Windows)
```bash
./gradlew :desktopApp:run
```

### Android
```bash
./gradlew :androidApp:assembleDebug
```

### Run All Unit & Multiplatform Tests
```bash
./gradlew :shared:allTests
```