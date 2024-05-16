package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.helpers

import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.ModelSource
import java.util.concurrent.CompletableFuture

class ArModelHelper(private val fragment: ArFragment) {
    companion object {
        private const val TAG = "ArModelHelper"
    }


    fun placeObject(anchorNode: AnchorNode, model: ModelSource):
            CompletableFuture<Triple<AnchorNode, TransformableNode, ModelRenderable>> {
        val renderableFuture: CompletableFuture<ModelRenderable>? = when (model) {
            is ModelSource.ResourceId -> ModelRenderable.builder()
                .setSource(fragment.requireContext(), model.id)
                .setIsFilamentGltf(true)
                .build()
            is ModelSource.UriSource -> ModelRenderable.builder()
                .setSource(fragment.requireContext(), model.uri)
                .setIsFilamentGltf(true)
                .build()
        }

        return renderableFuture?.thenCompose { modelRenderable ->
            CompletableFuture.completedFuture(addNodeToScene(anchorNode, modelRenderable))
        }?.exceptionally { throwable ->
            Log.e(TAG, "Error creating model renderable", throwable)
            throw RuntimeException("Error creating model renderable", throwable)
        } ?: CompletableFuture.completedFuture(null)
    }

    private fun addNodeToScene(anchorNode: AnchorNode, model: ModelRenderable):
            Triple<AnchorNode, TransformableNode, ModelRenderable> {
        val transformableNode = TransformableNode(fragment.transformationSystem).apply {
            renderable = model
            setParent(anchorNode)
            select()
        }
        fragment.arSceneView.scene.addChild(anchorNode)
        Log.d(TAG, "Node added to scene successfully")
        Log.d(TAG, "Node scale: ${transformableNode.localScale}")
        Log.d(TAG, "Node position: ${transformableNode.localPosition}")
        return Triple(anchorNode, transformableNode, model)
    }


    fun createAnchorNode(anchor: Anchor?): AnchorNode {
        return AnchorNode(anchor)
    }

    fun removeTransformableNode(transformableNode: TransformableNode) {
        transformableNode.parent?.removeChild(transformableNode)
        Log.d(TAG, "The transformableNode is removed from scene successfully")
    }

}

