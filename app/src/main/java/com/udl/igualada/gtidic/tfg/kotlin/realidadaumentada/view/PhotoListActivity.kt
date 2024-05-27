package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.adapter.PhotoAdapter
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.Photo

class PhotoListActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var photoList: MutableList<Photo>
    private lateinit var photoKeys: MutableList<String>
    private lateinit var adapter: PhotoAdapter
    private val database = FirebaseDatabase.getInstance().reference.child("photos")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_list)

        listView = findViewById(R.id.photoListView)
        photoList = mutableListOf()
        photoKeys = mutableListOf()

        adapter = PhotoAdapter(this, photoList, photoKeys)
        listView.adapter = adapter

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                photoList.clear()
                photoKeys.clear()
                for (photoSnapshot in snapshot.children) {
                    val photo = photoSnapshot.getValue(Photo::class.java)
                    photo?.let {
                        photoList.add(it)
                        photoKeys.add(photoSnapshot.key ?: "")
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PhotoListActivity, "Failed to load photos", Toast.LENGTH_SHORT).show()
            }
        })

        listView.setOnItemClickListener { _, _, position, _ ->
            val photoKey = photoKeys[position]
            showDeleteDialog(photoKey, position)
        }
    }

    private fun showDeleteDialog(photoKey: String, position: Int) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete Photo")
            .setMessage("Are you sure you want to delete this photo?")
            .setPositiveButton("Yes") { _, _ ->
                deletePhoto(photoKey, position)
            }
            .setNegativeButton("No", null)
            .create()

        dialog.show()
    }

    private fun deletePhoto(photoKey: String, position: Int) {
        database.child(photoKey).removeValue().addOnSuccessListener {
            Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show()
            photoList.removeAt(position)
            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to delete photo", Toast.LENGTH_SHORT).show()
        }
    }


}

