package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (viewModel.checkSystemSupport()) {
            setupTakePhotoButton()
        } else {
            Toast.makeText(
                this,
                getString(R.string.open_gl_version_not_supported),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    private fun setupTakePhotoButton() {
        val takePhotoButton = findViewById<Button>(R.id.btnTakePhoto)
        takePhotoButton.setOnClickListener {
            val rootView = window.decorView.rootView
            val bitmap = viewModel.takePhoto(rootView)
            bitmap?.let {
                viewModel.savePhotoToGallery(it)
                Toast.makeText(
                    this,
                    getString(R.string.photo_saved_to_gallery),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
