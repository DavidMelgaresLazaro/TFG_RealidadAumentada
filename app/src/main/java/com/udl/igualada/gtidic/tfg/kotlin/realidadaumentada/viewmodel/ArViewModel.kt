package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.ar.core.Anchor
import com.google.ar.sceneform.rendering.ModelRenderable
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.ARModel

class ArViewModel : ViewModel() {
    private val _tapNumber = MutableLiveData(0)
    val tapNumber: LiveData<Int> = _tapNumber

    private val _model = MutableLiveData<ARModel?>()
    val model: LiveData<ARModel?> = _model

    private val MAX_TAP_NUMBER = 1 // Definir el máximo número de toques aquí

    fun incrementTapNumber() {
        _tapNumber.value = (_tapNumber.value ?: 0) + 1
    }

    fun setModel(anchor: Anchor, modelRenderable: ModelRenderable?) {
        _model.value = ARModel(anchor, modelRenderable)
    }

    fun resetTapNumber() {
        _tapNumber.value = 0
    }

    fun isTapLimitReached(): Boolean {
        return _tapNumber.value ?: 0 >= MAX_TAP_NUMBER
    }
}



