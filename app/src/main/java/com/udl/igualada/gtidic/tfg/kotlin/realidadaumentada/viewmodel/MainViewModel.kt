package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.ARModel
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.PhotoModel
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.ModelRenderer

class MainViewModel(private val context: Context) : ViewModel() {

    private val arModel = ARModel(context)
    private val photoModel = PhotoModel(context)
    private val modelRenderer = ModelRenderer(context)

    fun checkSystemSupport(): Boolean {
        return arModel.checkSystemSupport()
    }

    fun arePermissionsGranted(): Boolean {
        return arModel.arePermissionsGranted()
    }

    fun takePhoto(view: View): Bitmap? {
        return photoModel.takePhoto(view)
    }

    fun savePhotoToGallery(bitmap: Bitmap) {
        photoModel.savePhotoToGallery(bitmap)
    }
}

