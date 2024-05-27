package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.helpers

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.widget.Toast
import com.google.ar.core.Frame
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel.PhotoViewModel

class PhotoHelper(
    private val arFragment: ArFragment,
    private val photoViewModel: PhotoViewModel
) {

    fun takePhoto(context: Context, anchorNode: AnchorNode, modelName: String, comment: String) {
        val arView = arFragment.arSceneView

        if (arView != null) {
            val bitmap = Bitmap.createBitmap(arView.width, arView.height, Bitmap.Config.ARGB_8888)
            val handlerThread = HandlerThread("PixelCopier")
            handlerThread.start()
            PixelCopy.request(arView, bitmap, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    val arFrame = arView.arFrame

                    val devicePosition = getDevicePosition(arFrame)

                    if (devicePosition != null) {
                        photoViewModel.savePhotoToGallery(context, bitmap, anchorNode, modelName, devicePosition, comment)
                    } else {
                        Toast.makeText(context, "Failed to get device position", Toast.LENGTH_SHORT).show()
                    }

                }
                handlerThread.quitSafely()
            }, Handler(handlerThread.looper))
        } else {
            Toast.makeText(context, "AR view is not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDevicePosition(arFrame: Frame?): Map<String, Float>? {
        val cameraPose = arFrame?.camera?.pose
        val cameraPosition = cameraPose?.let {
            val translation = it.translation
            mapOf(
                "x" to translation[0].toFloat(),
                "y" to translation[1].toFloat(),
                "z" to translation[2].toFloat()
            )
        }
        return cameraPosition
    }
}
