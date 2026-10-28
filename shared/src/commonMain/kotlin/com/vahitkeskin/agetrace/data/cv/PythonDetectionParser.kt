package com.vahitkeskin.agetrace.data.cv

import com.vahitkeskin.agetrace.domain.model.AgeClassification
import com.vahitkeskin.agetrace.domain.model.BoundingBox
import com.vahitkeskin.agetrace.domain.model.DetectedFace
import com.vahitkeskin.agetrace.domain.model.FacialBiometrics

/**
 * Robust zero-dependency JSON parser for Python AgeTrace engine outputs.
 */
object PythonDetectionParser {

    data class PythonAnalysisResponse(
        val success: Boolean,
        val engine: String,
        val inferenceTimeMs: Long,
        val faces: List<DetectedFace>
    )

    fun parse(jsonStr: String): PythonAnalysisResponse? {
        return try {
            if (!jsonStr.contains("\"success\": true") && !jsonStr.contains("\"success\":true")) {
                return null
            }

            val inferenceTimeMs = extractLong(jsonStr, "\"inference_time_ms\":") ?: 35L
            val engine = extractString(jsonStr, "\"engine\":") ?: "Python Vision Core"

            val facesList = mutableListOf<DetectedFace>()

            // Find all face objects inside "faces": [ ... ]
            val facesBlockStart = jsonStr.indexOf("\"faces\":")
            if (facesBlockStart != -1) {
                val arrayStart = jsonStr.indexOf('[', facesBlockStart)
                val arrayEnd = jsonStr.lastIndexOf(']')
                if (arrayStart != -1 && arrayEnd > arrayStart) {
                    val arrayContent = jsonStr.substring(arrayStart + 1, arrayEnd)
                    val faceChunks = splitJsonObjects(arrayContent)

                    for ((index, chunk) in faceChunks.withIndex()) {
                        val id = extractString(chunk, "\"id\":") ?: "py_face_${index + 1}"
                        val conf = extractFloat(chunk, "\"confidence\":") ?: 0.85f
                        val age = extractInt(chunk, "\"age\":") ?: 25

                        val x = extractFloat(chunk, "\"x\":") ?: 0.2f
                        val y = extractFloat(chunk, "\"y\":") ?: 0.15f
                        val w = extractFloat(chunk, "\"w\":") ?: 0.6f
                        val h = extractFloat(chunk, "\"h\":") ?: 0.7f

                        val cranio = extractFloat(chunk, "\"craniofacial_ratio\":") ?: 0.85f
                        val wrinkle = extractFloat(chunk, "\"wrinkle_density\":") ?: 0.25f
                        val texture = extractFloat(chunk, "\"skin_texture_variance\":") ?: 0.20f
                        val symmetry = extractFloat(chunk, "\"symmetry_score\":") ?: 0.92f

                        val classification = AgeClassification.fromAge(age)

                        facesList.add(
                            DetectedFace(
                                id = id,
                                boundingBox = BoundingBox(
                                    normalizedX = x.coerceIn(0f, 1f),
                                    normalizedY = y.coerceIn(0f, 1f),
                                    normalizedWidth = w.coerceIn(0.05f, 1f),
                                    normalizedHeight = h.coerceIn(0.05f, 1f)
                                ),
                                confidenceScore = conf.coerceIn(0.5f, 0.99f),
                                estimatedAge = age,
                                classification = classification,
                                biometrics = FacialBiometrics(
                                    craniofacialRatio = cranio,
                                    wrinkleDensityScore = wrinkle,
                                    skinTextureVariance = texture,
                                    bilateralSymmetryScore = symmetry,
                                    chrominanceCentroid = Pair(110f, 150f)
                                )
                            )
                        )
                    }
                }
            }

            PythonAnalysisResponse(
                success = true,
                engine = engine,
                inferenceTimeMs = inferenceTimeMs,
                faces = facesList
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun splitJsonObjects(content: String): List<String> {
        val objects = mutableListOf<String>()
        var depth = 0
        var start = -1

        for ((idx, char) in content.withIndex()) {
            if (char == '{') {
                if (depth == 0) start = idx
                depth++
            } else if (char == '}') {
                depth--
                if (depth == 0 && start != -1) {
                    objects.add(content.substring(start, idx + 1))
                    start = -1
                }
            }
        }
        return objects
    }

    private fun extractString(json: String, key: String): String? {
        val idx = json.indexOf(key)
        if (idx == -1) return null
        val quoteStart = json.indexOf('"', idx + key.length)
        if (quoteStart == -1) return null
        val quoteEnd = json.indexOf('"', quoteStart + 1)
        if (quoteEnd == -1) return null
        return json.substring(quoteStart + 1, quoteEnd)
    }

    private fun extractFloat(json: String, key: String): Float? {
        val idx = json.indexOf(key)
        if (idx == -1) return null
        val afterKey = json.substring(idx + key.length).trim()
        val numStr = afterKey.takeWhile { it.isDigit() || it == '.' || it == '-' }
        return numStr.toFloatOrNull()
    }

    private fun extractInt(json: String, key: String): Int? {
        val idx = json.indexOf(key)
        if (idx == -1) return null
        val afterKey = json.substring(idx + key.length).trim()
        val numStr = afterKey.takeWhile { it.isDigit() || it == '-' }
        return numStr.toIntOrNull()
    }

    private fun extractLong(json: String, key: String): Long? {
        val idx = json.indexOf(key)
        if (idx == -1) return null
        val afterKey = json.substring(idx + key.length).trim()
        val numStr = afterKey.takeWhile { it.isDigit() || it == '-' }
        return numStr.toLongOrNull()
    }
}
