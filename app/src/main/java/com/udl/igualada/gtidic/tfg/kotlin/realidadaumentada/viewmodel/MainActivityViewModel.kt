package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.TransformableNode
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.ModelSource

class MainActivityViewModel : ViewModel() {

    // LiveData to hold the source of the 3D model
    private val _modelSource: MutableLiveData<ModelSource> = MutableLiveData()
    val modelSource: LiveData<ModelSource> get() = _modelSource

    // LiveData to hold the anchor node in the AR scene
    private val _anchorNode: MutableLiveData<AnchorNode> = MutableLiveData()
    val anchorNode: LiveData<AnchorNode> get() = _anchorNode

    // LiveData to hold the transformable node in the AR scene
    private val _transformableNode: MutableLiveData<TransformableNode> = MutableLiveData()
    val transformableNode: LiveData<TransformableNode> get() = _transformableNode

    // LiveData to hold the name of the model
    private val _modelName: MutableLiveData<String> = MutableLiveData()
    val modelName: LiveData<String> get() = _modelName

    /**
     * Updates the source of the 3D model.
     *
     * @param source The new model source.
     */
    fun updateModelSource(source: ModelSource) {
        _modelSource.value = source
    }

    /**
     * Updates the anchor node in the AR scene.
     *
     * @param node The new anchor node.
     */
    fun updateAnchorNode(node: AnchorNode) {
        _anchorNode.value = node
    }

    /**
     * Updates the transformable node in the AR scene.
     *
     * @param node The new transformable node.
     */
    fun updateTransformableNode(node: TransformableNode) {
        _transformableNode.value = node
    }

    /**
     * Updates the name of the model.
     *
     * @param name The new model name.
     */
    fun updateModelName(name: String) {
        _modelName.value = name
    }
}
