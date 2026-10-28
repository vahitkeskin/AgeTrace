package com.vahitkeskin.agetrace.domain.model

sealed interface ImageSource {
    val displayName: String

    data class LocalFile(
        val name: String,
        val bytes: ByteArray,
        val path: String? = null
    ) : ImageSource {
        override val displayName: String get() = name

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is LocalFile) return false
            return name == other.name && bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int = 31 * name.hashCode() + bytes.contentHashCode()
    }

    data class PreloadedSample(
        val id: String,
        val title: String,
        val description: String,
        val expectedAgeBracket: String,
        val bytes: ByteArray
    ) : ImageSource {
        override val displayName: String get() = title

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PreloadedSample) return false
            return id == other.id && bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int = 31 * id.hashCode() + bytes.contentHashCode()
    }

    data class LiveCameraFrame(
        val frameIndex: Long,
        val timestampMs: Long,
        val bytes: ByteArray
    ) : ImageSource {
        override val displayName: String get() = "Canlı Kamera (Frame #$frameIndex)"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is LiveCameraFrame) return false
            return frameIndex == other.frameIndex && bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int = 31 * frameIndex.hashCode() + bytes.contentHashCode()
    }
}
