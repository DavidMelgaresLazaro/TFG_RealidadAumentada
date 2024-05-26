package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model

data class Photo(
    val filename: String = "",
    val url: String = "",
    val time: String = "",
    val modelName: String? = null,
    val size: Map<String, Float>? = null,
    val position: Map<String, Float>? = null,
    val distance: Float? = null,
    val comment: String? = null
)
