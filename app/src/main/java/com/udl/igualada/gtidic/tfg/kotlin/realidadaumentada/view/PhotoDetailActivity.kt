package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R

class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textViewComment: TextView
    private lateinit var textViewModelName: TextView
    private lateinit var textViewFilename: TextView
    private lateinit var textViewTimestamp: TextView
    private lateinit var textViewSize: TextView
    private lateinit var textViewPosition: TextView
    private lateinit var textViewDistance: TextView
    private lateinit var deleteButton: Button
    private lateinit var photoKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)

        imageView = findViewById(R.id.photoDetailImageView)
        textViewComment = findViewById(R.id.photoDetailComment)
        textViewModelName = findViewById(R.id.photoDetailModelName)
        textViewFilename = findViewById(R.id.photoDetailFilename)
        textViewTimestamp = findViewById(R.id.photoDetailTimestamp)
        textViewSize = findViewById(R.id.photoDetailSize)
        textViewPosition = findViewById(R.id.photoDetailPosition)
        textViewDistance = findViewById(R.id.photoDetailDistance)
        deleteButton = findViewById(R.id.deletePhotoButton)

        photoKey = intent.getStringExtra("photoKey") ?: ""

        loadPhotoDetails()

        deleteButton.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun loadPhotoDetails() {
        val database = FirebaseDatabase.getInstance().reference.child("photos").child(photoKey)
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val photoUrl = snapshot.child("url").getValue(String::class.java)
                val photoComment = snapshot.child("comment").getValue(String::class.java)
                val photoModelName = snapshot.child("modelName").getValue(String::class.java)
                val filename = snapshot.child("filename").getValue(String::class.java)
                val timestamp = snapshot.child("time").getValue(String::class.java)

                val sizeIndicator = object : GenericTypeIndicator<Map<String, Float>>() {}
                val positionIndicator = object : GenericTypeIndicator<Map<String, Float>>() {}
                val size = snapshot.child("size").getValue(sizeIndicator)
                val position = snapshot.child("position").getValue(positionIndicator)
                val distance = snapshot.child("distance").getValue(Double::class.java)

                photoUrl?.let {
                    Glide.with(this@PhotoDetailActivity).load(it).into(imageView)
                }
                textViewComment.text = "Comment: $photoComment"
                textViewModelName.text = "Model Name: $photoModelName"
                textViewFilename.text = "Filename: $filename"
                textViewTimestamp.text = "Timestamp: $timestamp"
                textViewSize.text = "Size: $size"
                textViewPosition.text = "Position: $position"
                textViewDistance.text = "Distance: $distance"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PhotoDetailActivity, "Failed to load photo details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete Photo")
            .setMessage("Are you sure you want to delete this photo?")
            .setPositiveButton("Yes") { _, _ ->
                deletePhoto()
            }
            .setNegativeButton("No", null)
            .create()

        dialog.show()
    }

    private fun deletePhoto() {
        val database = FirebaseDatabase.getInstance().reference.child("photos").child(photoKey)
        database.removeValue().addOnSuccessListener {
            Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show()
            finish() // Close the activity after deletion
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to delete photo", Toast.LENGTH_SHORT).show()
        }
    }
}
