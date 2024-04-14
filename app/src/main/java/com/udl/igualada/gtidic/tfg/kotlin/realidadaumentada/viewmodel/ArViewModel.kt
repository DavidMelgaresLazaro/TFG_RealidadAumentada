package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.ARModel


class ArViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private var tapNumber = 0
    val arState = MutableLiveData<ARModel>()

    init {
        arState.value = ARModel()
    }
    companion object {
        internal const val MIN_OPENGL_VERSION = 3.0
        private const val MAX_TAP_NUMBER = 1
    }

    fun handleTap(hitResult: HitResult?, arFragment: ArFragment) {
        hitResult?.let {
            tapNumber++
            if (tapNumber == MAX_TAP_NUMBER) {
                createModelRenderable(hitResult, arFragment)
            }
        }
    }

    private fun createModelRenderable(hitResult: HitResult, arFragment: ArFragment) {
        val anchor: Anchor = hitResult.createAnchor()
        ModelRenderable.builder()
            .setSource(context, R.raw.chess)
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { modelRenderable: ModelRenderable? ->
                addModel(
                    anchor,
                    modelRenderable,
                    arFragment
                )
            }.exceptionally { throwable: Throwable ->
                Toast.makeText(context, context.getString(R.string.something_is_not_right) + throwable.message, Toast.LENGTH_LONG).show()
                null
            }
    }


    private fun addModel(anchor: Anchor, modelRenderable: ModelRenderable?, arFragment: ArFragment) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment.arSceneView.scene)

        val transformableNode = TransformableNode(arFragment.transformationSystem)
        transformableNode.setParent(anchorNode)

        transformableNode.renderable = modelRenderable
        transformableNode.select()
    }

    fun savePhotoToGallery(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    arState.value = arState.value?.copy(isPhotoSaved = true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun addModelToScene(modelRenderable: ModelRenderable, arFragment: ArFragment) {
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            val transformableNode = TransformableNode(arFragment.transformationSystem)
            transformableNode.setParent(anchorNode)
            transformableNode.renderable = modelRenderable
            transformableNode.select()
        }
    }
}