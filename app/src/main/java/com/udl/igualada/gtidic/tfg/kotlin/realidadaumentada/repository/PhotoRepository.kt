package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.Photo
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.PhotoDao
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.PhotoDatabase

class PhotoRepository(context: Context) {

    private val photoDao: PhotoDao = PhotoDatabase.getDatabase(context).photoDao()
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getAllPhotos(): LiveData<List<Photo>> {
        return photoDao.getAllPhotos()
    }

    suspend fun insert(photo: Photo) {
        photoDao.insert(photo)
        savePhotoToFirebase(photo)
    }

    suspend fun delete(photo: Photo) {
        photoDao.delete(photo)
        deletePhotoFromFirebase(photo)
    }

    private fun savePhotoToFirebase(photo: Photo) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userEmail = user.email?.replace(".", "_") ?: return
        val userPhotosRef = database.getReference("photos").child(userEmail).child(photo.filename.replace(".", "_"))

        userPhotosRef.setValue(photo).addOnSuccessListener {
            // Foto subida correctamente a Firebase Database
        }.addOnFailureListener {
            // Error al subir la foto a Firebase Database
        }
    }

    private fun deletePhotoFromFirebase(photo: Photo) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userEmail = user.email?.replace(".", "_") ?: return
        val userPhotosRef = database.getReference("photos").child(userEmail).child(photo.filename.replace(".", "_"))

        userPhotosRef.removeValue().addOnSuccessListener {
            // Foto eliminada correctamente de Firebase Database
        }.addOnFailureListener {
            // Error al eliminar la foto de Firebase Database
        }
    }
}
