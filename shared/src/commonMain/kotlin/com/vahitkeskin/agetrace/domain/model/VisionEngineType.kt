package com.vahitkeskin.agetrace.domain.model

enum class VisionEngineType(
    val title: String,
    val icon: String,
    val description: String
) {
    PYTHON_CORE(
        title = "InsightFace Deep AI",
        icon = "🧠",
        description = "deepinsight/insightface SCRFD & GenderAge ONNX Derin Öğrenme Motoru"
    ),
    EDGE_AI_CORE(
        title = "Edge AI Core",
        icon = "⚡",
        description = "Tamamen yerel bağımsız yerleşik motor"
    )
}
