package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.helpers

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.widget.Toast
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel.PhotoViewModel

class PhotoHelper(
    private val arFragment: ArFragment,
    private val photoViewModel: PhotoViewModel
) {

    /**
     * Captures a photo of the current AR scene and saves it to the gallery.
     *
     * @param context The context to use for accessing resources.
     * @param anchorNode The anchor node to capture in the photo (can be null).
     * @param modelName The name of the model being captured (can be null).
     */
    fun takePhoto(context: Context, anchorNode: AnchorNode, modelName: String?) {
        val arView = arFragment.arSceneView

        if (arView != null) {
            // Create a bitmap with the same dimensions as the AR view
            val bitmap = Bitmap.createBitmap(arView.width, arView.height, Bitmap.Config.ARGB_8888)
            val handlerThread = HandlerThread("PixelCopier")
            handlerThread.start()

            // Request a PixelCopy to capture the AR view into the bitmap
            PixelCopy.request(arView, bitmap, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    val arFrame = arView.arFrame

                    if (arFrame != null) {
                        val devicePosition = photoViewModel.getDevicePosition(arFrame)
                        val transformableNode = anchorNode.children.firstOrNull { it is TransformableNode } as? TransformableNode

                        if (devicePosition != null) {
                            // Save the photo to the gallery using the ViewModel
                            photoViewModel.savePhotoToGallery(context, bitmap, transformableNode, modelName, devicePosition)
                            Toast.makeText(context, "Photo saved successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to get device position", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "AR frame is not available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to capture photo", Toast.LENGTH_SHORT).show()
                }
                handlerThread.quitSafely()
            }, Handler(handlerThread.looper))
        } else {
            Toast.makeText(context, "AR view is not available", Toast.LENGTH_SHORT).show()
        }
    }
}
