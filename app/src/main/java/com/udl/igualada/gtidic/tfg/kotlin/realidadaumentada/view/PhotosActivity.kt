package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.adapters.PhotosAdapter
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel.PhotosViewModel
import androidx.lifecycle.ViewModelProvider

class PhotosActivity : AppCompatActivity() {

    private lateinit var photosViewModel: PhotosViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photos)

        recyclerView = findViewById(R.id.recyclerViewPhotos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        photosViewModel = ViewModelProvider(this).get(PhotosViewModel::class.java)

        val adapter = PhotosAdapter()
        recyclerView.adapter = adapter

        photosViewModel.photos.observe(this, { photos ->
            photos?.let {
                adapter.submitList(it)
            }
        })

        photosViewModel.loadPhotos()
    }
}
