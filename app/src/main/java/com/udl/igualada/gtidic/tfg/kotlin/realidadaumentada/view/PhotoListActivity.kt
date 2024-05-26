package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.adapter.PhotoAdapter
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.Photo

class PhotoListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var photosRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_list)

        recyclerView = findViewById(R.id.recyclerViewPhotos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        photoAdapter = PhotoAdapter()
        recyclerView.adapter = photoAdapter

        database = FirebaseDatabase.getInstance()
        photosRef = database.getReference("photos")

        loadPhotosFromFirebase()
    }

    private fun loadPhotosFromFirebase() {
        photosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val photos = mutableListOf<Photo>()
                snapshot.children.forEach {
                    val photo = it.getValue(Photo::class.java)
                    if (photo != null) {
                        photos.add(photo)
                    }
                }
                photoAdapter.submitList(photos)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }
}
