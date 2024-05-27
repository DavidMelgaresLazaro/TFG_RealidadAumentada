package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*

class PhotosViewModel : ViewModel() {

    private val _photos = MutableLiveData<List<String>>()
    val photos: LiveData<List<String>> get() = _photos

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val photosRef: DatabaseReference = database.getReference("photos")

    fun loadPhotos() {
        photosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val photoUrls = snapshot.children.mapNotNull { it.child("url").getValue(String::class.java) }
                _photos.value = photoUrls
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
}
