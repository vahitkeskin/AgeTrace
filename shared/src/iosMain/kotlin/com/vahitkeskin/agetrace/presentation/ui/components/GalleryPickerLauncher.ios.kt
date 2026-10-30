package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerConfigurationAssetRepresentationModeCurrent
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

private var activeGalleryDelegate: GalleryPickerDelegate? = null

@OptIn(ExperimentalForeignApi::class)
private class GalleryPickerDelegate(
    private val onImagesPicked: (List<Pair<String, ByteArray>>) -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true) {
            activeGalleryDelegate = null
        }

        val results = didFinishPicking.filterIsInstance<PHPickerResult>()
        if (results.isEmpty()) return

        val pickedImages = mutableListOf<Pair<String, ByteArray>>()
        val totalCount = results.size
        var completedCount = 0

        for ((index, result) in results.withIndex()) {
            val itemProvider = result.itemProvider
            val typeId = "public.image"
            if (itemProvider.hasItemConformingToTypeIdentifier(typeId)) {
                itemProvider.loadDataRepresentationForTypeIdentifier(typeId) { nsData, _ ->
                    dispatch_async(dispatch_get_main_queue()) {
                        if (nsData != null) {
                            val length = nsData.length.toInt()
                            if (length > 0) {
                                val bytes = ByteArray(length)
                                bytes.usePinned { pinned ->
                                    memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
                                }
                                val fileName = "galeri_${index + 1}.jpg"
                                pickedImages.add(Pair(fileName, bytes))
                            }
                        }
                        completedCount++
                        if (completedCount >= totalCount && pickedImages.isNotEmpty()) {
                            onImagesPicked(pickedImages)
                        }
                    }
                }
            } else {
                dispatch_async(dispatch_get_main_queue()) {
                    completedCount++
                    if (completedCount >= totalCount && pickedImages.isNotEmpty()) {
                        onImagesPicked(pickedImages)
                    }
                }
            }
        }
    }
}

private fun getTopViewController(): UIViewController? {
    val keyWindow = UIApplication.sharedApplication.keyWindow
        ?: UIApplication.sharedApplication.windows.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow
        ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
    var topController = keyWindow?.rootViewController
    while (topController?.presentedViewController != null) {
        topController = topController.presentedViewController
    }
    return topController
}

@Composable
actual fun rememberGalleryPickerLauncher(
    onImagesPicked: (List<Pair<String, ByteArray>>) -> Unit
): () -> Unit {
    val currentOnImagesPicked = rememberUpdatedState(onImagesPicked)
    return {
        val config = PHPickerConfiguration().apply {
            selectionLimit = 30
            filter = PHPickerFilter.imagesFilter()
            preferredAssetRepresentationMode = PHPickerConfigurationAssetRepresentationModeCurrent
        }
        val picker = PHPickerViewController(configuration = config)
        val delegate = GalleryPickerDelegate { images ->
            currentOnImagesPicked.value(images)
        }
        activeGalleryDelegate = delegate
        picker.delegate = delegate

        val topVc = getTopViewController()
        topVc?.presentViewController(picker, animated = true, completion = null)
    }
}
