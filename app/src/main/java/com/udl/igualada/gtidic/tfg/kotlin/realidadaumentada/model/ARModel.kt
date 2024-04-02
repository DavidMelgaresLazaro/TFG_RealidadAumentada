package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model

import com.google.ar.core.Anchor
import com.google.ar.sceneform.rendering.ModelRenderable


data class ARModel(
    val anchor: Anchor,
    val modelRenderable: ModelRenderable?
)

