package com.vahitkeskin.agetrace.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

private var activeCameraDelegate: CameraPickerDelegate? = null

@OptIn(ExperimentalForeignApi::class)
private class CameraPickerDelegate(
    private val onImageCaptured: (name: String, bytes: ByteArray) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        picker.dismissViewControllerAnimated(true) {
            activeCameraDelegate = null
        }
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        if (image != null) {
            val jpegData = UIImageJPEGRepresentation(image, 0.95)
            if (jpegData != null) {
                val length = jpegData.length.toInt()
                if (length > 0) {
                    val bytes = ByteArray(length)
                    bytes.usePinned { pinned ->
                        memcpy(pinned.addressOf(0), jpegData.bytes, jpegData.length)
                    }
                    val timestamp = platform.posix.time(null)
                    dispatch_async(dispatch_get_main_queue()) {
                        onImageCaptured("kamera_$timestamp.jpg", bytes)
                    }
                }
            }
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true) {
            activeCameraDelegate = null
        }
    }
}

private fun getTopViewControllerForCamera(): UIViewController? {
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
actual fun rememberCameraCaptureLauncher(
    onImageCaptured: (name: String, bytes: ByteArray) -> Unit
): () -> Unit {
    val currentOnImageCaptured = rememberUpdatedState(onImageCaptured)
    return {
        val picker = UIImagePickerController()
        val sourceType = if (UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        } else {
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        }
        picker.sourceType = sourceType
        val delegate = CameraPickerDelegate { name, bytes ->
            currentOnImageCaptured.value(name, bytes)
        }
        activeCameraDelegate = delegate
        picker.delegate = delegate

        val topVc = getTopViewControllerForCamera()
        topVc?.presentViewController(picker, animated = true, completion = null)
    }
}
