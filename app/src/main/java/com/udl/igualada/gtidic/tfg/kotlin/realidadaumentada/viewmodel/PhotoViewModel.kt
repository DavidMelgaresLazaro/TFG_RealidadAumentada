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
import com.google.ar.core.Frame
import com.google.ar.sceneform.ux.TransformableNode
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

    /**
     * Saves a photo to the gallery and uploads it to Firebase Storage along with metadata.
     *
     * @param context The context to use for accessing resources.
     * @param bitmap The bitmap to be saved and uploaded.
     * @param transformableNode The node associated with the photo (can be null).
     * @param modelName The name of the model being captured (can be null).
     * @param devicePosition The device position when the photo was taken.
     */
    fun savePhotoToGallery(context: Context, bitmap: Bitmap, transformableNode: TransformableNode?, modelName: String?, devicePosition: Map<String, Float>?) {
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

                    // Get model position before uploading photo to Firebase Storage
                    val modelPosition = getModelPosition(transformableNode)

                    // Save to Firebase Storage
                    savePhotoToFirebase(bitmap, filename, modelName, transformableNode, modelPosition, devicePosition)

                    // Update UI on the main thread
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Photo saved successfully", Toast.LENGTH_SHORT).show()
                    }

                    Log.d("PhotoViewModel", "Photo saved successfully: $imageUri")
                }
            } ?: Log.e("PhotoViewModel", "Failed to create image file")
        } catch (e: Exception) {
            Log.e("PhotoViewModel", "Failed to save photo", e)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePhotoToFirebase(bitmap: Bitmap, filename: String, modelName: String?, transformableNode: TransformableNode?, modelPosition: Map<String, Float>?, devicePosition: Map<String, Float>?) {
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
                savePhotoMetadataToDatabase(filename, photoUrl, modelName, transformableNode, modelPosition, devicePosition)
            }
            Log.d("PhotoViewModel", "Photo uploaded to Firebase successfully: ${taskSnapshot.metadata?.path}")
        }
    }

    private fun savePhotoMetadataToDatabase(filename: String, url: String, modelName: String?, transformableNode: TransformableNode?, modelPosition: Map<String, Float>?, devicePosition: Map<String, Float>?) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())
        val modelSize = getModelSize(transformableNode)
        val horizontalDistance = getHorizontalDistanceToModel(devicePosition, modelPosition)

        val photoMetadata = mutableMapOf(
            "filename" to filename,
            "url" to url,
            "time" to timestamp,
            "modelName" to modelName,  // Add modelName to metadata
            "size" to modelSize,
            "position" to modelPosition,
            "distance" to horizontalDistance
        )

        photosRef.push().setValue(photoMetadata)
            .addOnSuccessListener {
                Log.d("PhotoViewModel", "Photo metadata saved to database successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("PhotoViewModel", "Failed to save photo metadata to database", exception)
            }
    }

    private fun getModelSize(transformableNode: TransformableNode?): Map<String, Float> {
        return transformableNode?.localScale?.let { size ->
            mapOf(
                "width" to size.x,
                "height" to size.y,
                "depth" to size.z
            )
        } ?: emptyMap()
    }

    private fun getModelPosition(transformableNode: TransformableNode?): Map<String, Float>? {
        return transformableNode?.worldPosition?.let { position ->
            mapOf(
                "x" to position.x,
                "y" to position.y,
                "z" to position.z
            )
        }
    }

    private fun getHorizontalDistanceToModel(devicePosition: Map<String, Float>?, modelPosition: Map<String, Float>?): Float {
        if (devicePosition == null || modelPosition == null) {
            return 0f
        }

        val deviceX = devicePosition["x"] ?: 0f
        val deviceZ = devicePosition["z"] ?: 0f
        val modelX = modelPosition["x"] ?: 0f
        val modelZ = modelPosition["z"] ?: 0f

        return kotlin.math.sqrt((deviceX - modelX) * (deviceX - modelX) + (deviceZ - modelZ) * (deviceZ - modelZ))
    }

    fun getDevicePosition(arFrame: Frame?): Map<String, Float>? {
        return arFrame?.camera?.pose?.let { cameraPose ->
            val translation = cameraPose.translation
            mapOf(
                "x" to translation[0].toFloat(),
                "y" to translation[1].toFloat(),
                "z" to translation[2].toFloat()
            )
        }
    }
}
