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
import com.google.ar.sceneform.AnchorNode
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val photosRef: DatabaseReference = database.getReference("photos")
    private val coordinatesRef: DatabaseReference = database.getReference("coordinates")

    fun savePhotoToGallery(context: Context, bitmap: Bitmap, modelAnchorNode: AnchorNode?) {
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

                    // Save to Firebase Storage
                    savePhotoToFirebase(bitmap, filename, modelAnchorNode)

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

    private fun savePhotoToFirebase(bitmap: Bitmap, filename: String, modelAnchorNode: AnchorNode?) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imagesRef: StorageReference = storageRef.child("images/$filename")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnFailureListener { exception ->
            Log.e("PhotoViewModel", "Failed to upload photo to Firebase", exception)
        }.addOnSuccessListener { taskSnapshot ->
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                val photoUrl = uri.toString()
                savePhotoMetadataToDatabase(filename, photoUrl, modelAnchorNode)
            }
            Log.d("PhotoViewModel", "Photo uploaded to Firebase successfully: ${taskSnapshot.metadata?.path}")
        }
    }

    private fun savePhotoMetadataToDatabase(filename: String, url: String, modelAnchorNode: AnchorNode?) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())

        val photoMetadata = mutableMapOf(
            "filename" to filename,
            "url" to url,
            "timestamp" to timestamp
        )

        modelAnchorNode?.worldPosition?.let { position ->
            photoMetadata["x"] = position.x.toString()
            photoMetadata["y"] = position.y.toString()
            photoMetadata["z"] = position.z.toString()
        }

        photosRef.push().setValue(photoMetadata)
            .addOnSuccessListener {
                Log.d("PhotoViewModel", "Photo metadata saved to database successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("PhotoViewModel", "Failed to save photo metadata to database", exception)
            }
    }
}


