package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model

import android.net.Uri


sealed class ModelSource {
    data class ResourceId(val id: Int) : ModelSource()
    data class UriSource(val uri: Uri) : ModelSource()
}


