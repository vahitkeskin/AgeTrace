package com.vahitkeskin.agetrace.domain.usecase

import com.vahitkeskin.agetrace.domain.model.ImageSource
import com.vahitkeskin.agetrace.domain.repository.ImageProcessorRepository

class GetSampleFacesUseCase(
    private val imageProcessorRepository: ImageProcessorRepository
) {
    suspend operator fun invoke(): List<ImageSource.PreloadedSample> {
        return imageProcessorRepository.getPreloadedSamples()
    }
}
