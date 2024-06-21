package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.adapter.PhotoAdapter
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.Photo

class PhotoListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter
    private lateinit var photoList: MutableList<Photo>
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_list)

        // Configurar la Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Configurar el título centrado
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        toolbarTitle.text = "Lista de Fotografías"

        // Cambiar el color de la flecha de volver atrás
        toolbar.navigationIcon?.setTint(getColor(android.R.color.black))

        // Manejar evento de la flecha de volver atrás
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

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
                        val photo = snapshot.getValue(Photo::class.java)
                        if (photo != null) {
                            photoList.add(photo)
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
