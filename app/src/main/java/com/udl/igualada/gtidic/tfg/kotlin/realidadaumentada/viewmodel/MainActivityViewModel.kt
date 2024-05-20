package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.TransformableNode
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.ModelSource

class MainActivityViewModel: ViewModel() {

    //@TODO el anchorNode y el transformableNode no sirven para actualizar la vista, de momento,
    // se pueden quita y guardar en variables locales, o mantenerlas si se quiere hacer algo con ellas
    // en un futuro

    val modelSource: MutableLiveData<ModelSource> = MutableLiveData()
    val anchorNode: MutableLiveData<AnchorNode> = MutableLiveData()
    val transformableNode: MutableLiveData<TransformableNode> = MutableLiveData()



}