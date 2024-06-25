package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.Photo
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.repository.PhotoRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhotoRepository = PhotoRepository(application)
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    fun savePhotoToGallery(
        context: Context,
        bitmap: Bitmap,
        anchorNode: AnchorNode,
        modelName: String?,
        devicePosition: Map<String, Double>,
        comment: String
    ) {
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

                    val photo = Photo(
                        filename = filename,
                        url = imageUri.toString(),
                        time = getCurrentDateTimeInSpain(), // Updated here
                        modelName = modelName,
                        size = getModelSize(anchorNode),
                        position = devicePosition.mapValues { it.value.toFloat() },
                        distance = getHorizontalDistanceToModel(devicePosition, modelPosition),
                        comment = comment,
                        localUri = imageUri.toString()
                    )

                    viewModelScope.launch {
                        try {
                            repository.insert(photo)
                            savePhotoToFirebase(photo)
                            Log.d("PhotoViewModel", "Photo metadata saved to Firebase successfully")
                        } catch (e: Exception) {
                            Log.e("PhotoViewModel", "Failed to save photo metadata to Firebase", e)
                        }
                    }

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

    private fun getModelSize(anchorNode: AnchorNode): Map<String, Float> {
        val transformableNode = anchorNode.children.firstOrNull { it is TransformableNode } as? TransformableNode
        return transformableNode?.let {
            val size = it.localScale
            mapOf("width" to size.x)
        } ?: emptyMap()
    }

    private fun getModelPosition(anchorNode: AnchorNode?): Map<String, Float> {
        return if (anchorNode == null) {
            emptyMap()
        } else {
            val position = anchorNode.worldPosition
            mapOf(
                "x" to position.x,
                "z" to position.z
            )
        }
    }

    fun getDeviceLocation(callback: (Map<String, Double>?) -> Unit) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val devicePosition = mapOf(
                            "latitude" to location.latitude,
                            "longitude" to location.longitude
                        )
                        callback(devicePosition)
                    } else {
                        callback(null)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("PhotoViewModel", "Failed to get device location", exception)
                    callback(null)
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
            callback(null)
        }
    }

    private fun getHorizontalDistanceToModel(
        devicePosition: Map<String, Double>?,
        modelPosition: Map<String, Float>?
    ): Float? {
        if (devicePosition == null || modelPosition == null) {
            return null
        }

        // Assuming device position is in lat/lng and model position is in x/z (meters)
        val deviceLat = devicePosition["latitude"] ?: return null
        val deviceLng = devicePosition["longitude"] ?: return null
        val modelX = modelPosition["x"] ?: return null
        val modelZ = modelPosition["z"] ?: return null

        // Calculate distance in meters between two points in 2D space
        val distance = Math.sqrt((modelX * modelX + modelZ * modelZ).toDouble()).toFloat()
        return distance
    }

    private fun savePhotoToFirebase(photo: Photo) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email?.replace(".", "_") ?: return
            val databaseReference = FirebaseDatabase.getInstance().getReference("photos").child(userEmail)
            databaseReference.child(photo.filename.replace(".", "_")).setValue(photo)
                .addOnSuccessListener {
                    Log.d("PhotoViewModel", "Photo metadata saved to Firebase successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("PhotoViewModel", "Failed to save photo metadata to Firebase", exception)
                }
        } else {
            Log.e("PhotoViewModel", "User not authenticated, cannot save photo metadata to Firebase")
        }
    }
}


fun getCurrentDateTimeInSpain(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    sdf.timeZone = TimeZone.getDefault()
    return sdf.format(Date())
}
