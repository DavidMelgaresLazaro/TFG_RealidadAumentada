package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.content.ContentResolver
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R

class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var photoDetailImageView: ImageView
    private lateinit var photoDetailComment: TextView
    private lateinit var photoDetailModelName: TextView
    private lateinit var photoDetailFilename: TextView
    private lateinit var photoDetailTimestamp: TextView
    private lateinit var photoDetailSize: TextView
    private lateinit var photoDetailPosition: TextView
    private lateinit var photoDetailDistance: TextView
    private lateinit var deletePhotoButton: Button
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: FirebaseStorage
    private var localUri: String? = null
    private var storageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)

        // Configurar la Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = "Información de la Fotografía"

        // Cambiar el color de la flecha de volver atrás
        toolbar.navigationIcon?.setTint(getColor(android.R.color.black))

        // Manejar evento de la flecha de volver atrás
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        photoDetailImageView = findViewById(R.id.photoDetailImageView)
        photoDetailComment = findViewById(R.id.photoDetailComment)
        photoDetailModelName = findViewById(R.id.photoDetailModelName)
        photoDetailFilename = findViewById(R.id.photoDetailFilename)
        photoDetailTimestamp = findViewById(R.id.photoDetailTimestamp)
        photoDetailSize = findViewById(R.id.photoDetailSize)
        photoDetailPosition = findViewById(R.id.photoDetailPosition)
        photoDetailDistance = findViewById(R.id.photoDetailDistance)
        deletePhotoButton = findViewById(R.id.deletePhotoButton)

        val filename = intent.getStringExtra("filename")
        val userEmail = FirebaseAuth.getInstance().currentUser?.email

        if (filename != null && userEmail != null) {
            val userPhotosRef = FirebaseDatabase.getInstance().getReference("photos").child(userEmail.replace(".", "_")).child(filename.replace(".", "_"))
            userPhotosRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val photoData = dataSnapshot.value as? Map<String, Any>
                    if (photoData != null) {
                        storageUrl = photoData["url"] as? String
                        val comment = photoData["comment"] as? String
                        val modelName = photoData["modelName"] as? String
                        val timestamp = photoData["time"] as? String
                        val size = photoData["size"] as? Map<String, Float>
                        val position = photoData["position"] as? Map<String, Float>
                        val distance = photoData["distance"] as? Double
                        localUri = photoData["localUri"] as? String

                        if (storageUrl != null) {
                            Glide.with(this@PhotoDetailActivity).load(storageUrl).into(photoDetailImageView)
                        }

                        photoDetailFilename.text = applyStyle("Nombre fichero:", filename)
                        photoDetailComment.text = applyStyle("Comentario:", comment)
                        photoDetailModelName.text = applyStyle("Nombre modelo:", modelName)
                        photoDetailTimestamp.text = applyStyle("Fecha y hora:", timestamp)
                        photoDetailSize.text = applyStyle("Tamaño:", "${size?.toString() ?: "N/A"} %")
                        photoDetailPosition.text = applyStyle("Posición:", position?.toString())
                        photoDetailDistance.text = applyStyle("Distancia:", "${distance?.toString() ?: "N/A"} m")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@PhotoDetailActivity, "Failed to load photo details", Toast.LENGTH_SHORT).show()
                }
            })
        }

        deletePhotoButton.setOnClickListener {
            if (filename != null && userEmail != null) {
                showDeleteConfirmationDialog(userEmail.replace(".", "_"), filename.replace(".", "_"))
            }
        }
    }

    private fun applyStyle(label: String, content: String?): SpannableString {
        val spannableString = SpannableString("$label ${content ?: "N/A"}")
        spannableString.setSpan(StyleSpan(Typeface.BOLD), 0, label.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    private fun showDeleteConfirmationDialog(userEmail: String, filename: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("¿Estás seguro de que deseas eliminar esta foto?")
            .setPositiveButton("Eliminar") { dialog, id ->
                deletePhoto(userEmail, filename)
            }
            .setNegativeButton("Cancelar") { dialog, id ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun deletePhoto(userEmail: String, filename: String) {
        val userPhotosRef = FirebaseDatabase.getInstance().getReference("photos").child(userEmail).child(filename)
        userPhotosRef.removeValue().addOnSuccessListener {
            Toast.makeText(this, "Photo metadata deleted from database", Toast.LENGTH_SHORT).show()
            if (localUri != null) {
                deletePhotoFromDevice(localUri!!)
            }

            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to delete photo metadata from database", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deletePhotoFromDevice(uri: String) {
        val resolver: ContentResolver = contentResolver
        try {
            val photoUri = Uri.parse(uri)
            val deletedRows = resolver.delete(photoUri, null, null)
            if (deletedRows > 0) {
                Toast.makeText(this, "Photo deleted from device", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to delete photo from device. No rows deleted.", Toast.LENGTH_SHORT).show()
                Log.e("PhotoDetailActivity", "Failed to delete photo from device. No rows deleted.")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to delete photo from device", Toast.LENGTH_SHORT).show()
            Log.e("PhotoDetailActivity", "Failed to delete photo from device", e)
        }
    }
}
