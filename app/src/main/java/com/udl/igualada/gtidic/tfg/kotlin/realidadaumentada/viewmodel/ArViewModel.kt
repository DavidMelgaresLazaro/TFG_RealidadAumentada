package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.ARModel
import kotlinx.coroutines.*
import java.io.File

class ArViewModel(application: Application) : AndroidViewModel(application) {

    private var tapNumber = 0
    val arState = MutableLiveData<ARModel>()

    private val context: Context
        get() = getApplication<Application>().applicationContext

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
        val scaleFactor = 0.01f
        val anchor: Anchor = hitResult.createAnchor()
        ModelRenderable.builder()
            .setSource(arFragment.requireContext(), R.raw.sas__cs2_agent_model_green)
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { modelRenderable: ModelRenderable? ->
                addModel(
                    anchor,
                    modelRenderable,
                    arFragment,
                )
            }.exceptionally { throwable: Throwable ->
                Toast.makeText(context, context.getString(R.string.something_is_not_right) + throwable.message, Toast.LENGTH_LONG).show()
                null
            }
    }


    fun addModel(anchor: Anchor, renderable: ModelRenderable?, arFragment: ArFragment) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment.arSceneView.scene)
        val modelNode = TransformableNode(arFragment.transformationSystem)
        modelNode.renderable = renderable
        modelNode.setParent(anchorNode)
        modelNode.select()
        arFragment.arSceneView.scene.addChild(modelNode)
    }

    fun savePhotoToGallery(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        }

        val resolver = context.contentResolver
        var uri: Uri? = null

        try {
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let { imageUri ->
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

                    // Update arState value on the main thread
                    Handler(context.mainLooper).post {
                        arState.value = arState.value?.copy(isPhotoSaved = true)
                    }

                    Log.d("ArViewModel", "Photo saved successfully: $imageUri")
                    Toast.makeText(context, "Photo saved successfully", Toast.LENGTH_SHORT).show()
                }
            } ?: Log.e("ArViewModel", "Failed to create image file")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
            Log.e("ArViewModel", "Failed to save photo", e)
        }
    }

    private val modelScope = MainScope()

    fun changeModel(filePath: String, arFragment: ArFragment) {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e("ArViewModel", "Model file does not exist: $filePath")
            return
        }

        modelScope.launch {
            val model = ModelRenderable.builder()
                .setSource(arFragment.requireContext(), Uri.fromFile(file))
                .setIsFilamentGltf(true)
                .build()

            model.thenAccept { modelRenderable: ModelRenderable? ->
                Log.d("ArViewModel", "ModelRenderable instance: $modelRenderable")
                Log.d("ArViewModel", arFragment.arSceneView.scene.children.toString())
                arFragment.arSceneView.scene.children.forEach { node ->
                    if (node is TransformableNode) {
                        node.renderable = modelRenderable
                        //node.localScale = Vector3(100000000f,100000000f,100000000f)
                        Log.d("ArViewModel", "TransformableNode updated with new renderable: ${node.renderable}")
                    }
                }
            }.exceptionally { throwable: Throwable ->
                Log.e("ArViewModel", "Error loading model", throwable)
                Toast.makeText(context,
                    context.getString(R.string.something_is_not_right)
                            + throwable.message, Toast.LENGTH_LONG).show()
                null
            }
        }
    }

    fun moveModel(arFragment: ArFragment, translation: Vector3) {
        arFragment.arSceneView.scene.children.forEach { node ->
            if (node is TransformableNode) {
                try {
                    val parentNode = node.parent
                    if (parentNode is AnchorNode) {
                        // Move the model
                        node.localPosition = Vector3.add(node.localPosition, translation)
                    }
                } catch (e: Exception) {
                    // Handle any exception that may occur while moving the model
                    Log.e("ArViewModel", "Error moving the model", e)
                }
            }
        }
    }






    fun increaseModelSize(arFragment: ArFragment) {
        adjustModelSize(arFragment, true)
    }

    fun decreaseModelSize(arFragment: ArFragment) {
        adjustModelSize(arFragment, false)
    }

    private fun adjustModelSize(arFragment: ArFragment, increaseSize: Boolean) {
        val scaleFactor = if (increaseSize) 1.2f else 0.8f // Factor de escala para ajustar el tamaño del modelo
        val minScale = 0.1f // Escala mínima permitida
        val maxScale = 10.0f // Escala máxima permitida

        arFragment.arSceneView.scene.children.forEach { node ->
            if (node is TransformableNode) {
                val newScale = Vector3(
                    node.localScale.x * scaleFactor,
                    node.localScale.y * scaleFactor,
                    node.localScale.z * scaleFactor
                ).clamp(minScale, maxScale)

                // Aplicar interpolación suave
                node.localScale = Vector3.lerp(node.localScale, newScale, 0.1f)

                val adjustmentType = if (increaseSize) "increased" else "decreased"
                Log.d("ArViewModel", "Model size $adjustmentType successfully") // Registro del ajuste de tamaño del modelo
            }
        }
    }

    // Extensión para limitar la escala dentro de un rango específico
    private fun Vector3.clamp(min: Float, max: Float): Vector3 {
        return Vector3(
            x.coerceIn(min, max),
            y.coerceIn(min, max),
            z.coerceIn(min, max)
        )
    }


}