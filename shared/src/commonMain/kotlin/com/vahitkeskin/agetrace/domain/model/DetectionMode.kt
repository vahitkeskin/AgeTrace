package com.vahitkeskin.agetrace.domain.model

enum class DetectionMode(val title: String, val subtitle: String) {
    IMAGE_UPLOAD(
        title = "Görsel Yükle",
        subtitle = "Fotoğraf yükle veya hazır portre seç"
    ),
    LIVE_CAMERA(
        title = "Canlı Kamera",
        subtitle = "Gerçek zamanlı yüz & yaş analizi"
    )
}
