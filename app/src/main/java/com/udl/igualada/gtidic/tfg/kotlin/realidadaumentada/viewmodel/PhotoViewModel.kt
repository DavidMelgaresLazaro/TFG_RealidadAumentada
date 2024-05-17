package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import java.io.OutputStream

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    fun savePhotoToGallery(context: Context, bitmap: Bitmap) {
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

                    // Update UI on the main thread
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Photo saved successfully", Toast.LENGTH_SHORT).show()
                    }

                    Log.d("PhotoViewModel", "Photo saved successfully: $imageUri")
                }
            } ?: Log.e("PhotoViewModel", "Failed to create image file")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
            Log.e("PhotoViewModel", "Failed to save photo", e)
        }
    }
}
