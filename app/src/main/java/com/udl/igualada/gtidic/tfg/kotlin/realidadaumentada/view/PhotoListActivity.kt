package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.adapter.PhotoAdapter

class PhotoListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var photoList: MutableList<Map<String, Any>>
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_list)

        recyclerView = findViewById(R.id.recyclerViewPhotos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        photoList = mutableListOf()
        photoAdapter = PhotoAdapter(this, photoList)
        recyclerView.adapter = photoAdapter

        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        if (userEmail != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("photos").child(userEmail.replace(".", "_"))
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    photoList.clear()
                    for (snapshot in dataSnapshot.children) {
                        val photoData = snapshot.value as? Map<String, Any>
                        if (photoData != null) {
                            photoList.add(photoData)
                        }
                    }
                    photoAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@PhotoListActivity, "Failed to load photos", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
