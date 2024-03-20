package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model

import android.content.Context
import android.net.Uri
import com.google.ar.sceneform.rendering.ModelRenderable

class ModelRenderer(private val context: Context) {

    fun createModelRenderable(uri: Uri): ModelRenderable? {
        return ModelRenderable.builder()
            .setSource(context, uri)
            .build()
            .exceptionally {
                // Handle the exception
                null
            }
            .get()
    }
}

