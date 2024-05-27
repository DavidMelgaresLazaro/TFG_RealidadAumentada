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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val photosRef: DatabaseReference = database.getReference("photos")
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun savePhotoToGallery(context: Context, bitmap: Bitmap, anchorNode: AnchorNode, modelName: String?, devicePosition: Map<String, Float>?, comment: String) {
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

                    val modelPosition = getModelPosition(anchorNode)

                    savePhotoToFirebase(bitmap, filename, modelName, anchorNode, modelPosition, devicePosition, comment, imageUri.toString())

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

    private fun savePhotoToFirebase(bitmap: Bitmap, filename: String, modelName: String?, anchorNode: AnchorNode, modelPosition: Map<String, Float>?, devicePosition: Map<String, Float>?, comment: String, localUri: String) {
        val storageRef = storage.reference.child("images/$filename")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = storageRef.putBytes(data)
        uploadTask.addOnFailureListener { exception ->
            Log.e("PhotoViewModel", "Failed to upload photo to Firebase", exception)
        }.addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val photoUrl = uri.toString()
                savePhotoMetadataToDatabase(filename, photoUrl, modelName, anchorNode, modelPosition, devicePosition, comment, localUri)
            }
            Log.d("PhotoViewModel", "Photo uploaded to Firebase successfully: ${taskSnapshot.metadata?.path}")
        }
    }

    private fun savePhotoMetadataToDatabase(filename: String, url: String, modelName: String?, anchorNode: AnchorNode, modelPosition: Map<String, Float>?, devicePosition: Map<String, Float>?, comment: String, localUri: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())
        val modelSize = getModelSize(anchorNode)
        val horizontalDistance = getHorizontalDistanceToModel(devicePosition, modelPosition)

        val photoMetadata = mutableMapOf(
            "filename" to filename,
            "url" to url,
            "time" to timestamp,
            "modelName" to modelName,
            "size" to modelSize,
            "position" to modelPosition,
            "distance" to horizontalDistance,
            "comment" to comment,
            "localUri" to localUri
        )

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email
            if (userEmail != null) {
                val userPhotosRef = photosRef.child(userEmail.replace(".", "_")).child(filename.replace(".", "_"))
                userPhotosRef.setValue(photoMetadata)
                    .addOnSuccessListener {
                        Log.d("PhotoViewModel", "Photo metadata saved to database successfully")
                    }
                    .addOnFailureListener { exception ->
                        Log.e("PhotoViewModel", "Failed to save photo metadata to database", exception)
                    }
            } else {
                Log.e("PhotoViewModel", "User email is null, cannot save photo metadata")
            }
        } else {
            Log.e("PhotoViewModel", "User not authenticated, cannot save photo metadata")
        }
    }

    private fun getModelSize(anchorNode: AnchorNode?): Map<String, Float> {
        return if (anchorNode == null) {
            emptyMap()
        } else {
            val size = anchorNode.worldScale
            mapOf(
                "width" to size.x,
                "height" to size.y,
                "depth" to size.z
            )
        }
    }

    private fun getModelPosition(anchorNode: AnchorNode?): Map<String, Float> {
        return if (anchorNode == null) {
            emptyMap()
        } else {
            val position = anchorNode.worldPosition
            mapOf(
                "x" to position.x,
                "y" to position.y,
                "z" to position.z
            )
        }
    }

    private fun getHorizontalDistanceToModel(devicePosition: Map<String, Float>?, modelPosition: Map<String, Float>?): Float? {
        if (devicePosition == null || modelPosition == null) {
            return null
        }

        val deviceX = devicePosition["x"] ?: return null
        val deviceZ = devicePosition["z"] ?: return null
        val modelX = modelPosition["x"] ?: return null
        val modelZ = modelPosition["z"] ?: return null

        return kotlin.math.sqrt((deviceX - modelX) * (deviceX - modelX) + (deviceZ - modelZ) * (deviceZ - modelZ))
    }
}
