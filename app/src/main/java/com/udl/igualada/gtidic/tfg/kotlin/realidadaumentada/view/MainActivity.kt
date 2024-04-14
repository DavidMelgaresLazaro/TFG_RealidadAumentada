package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.view.PixelCopy
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel.ArViewModel
import java.io.File
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var arViewModel: ArViewModel
    private lateinit var arFragment: ArFragment

    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1001
    private val PICK_MODEL_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.activity_main__container__camera_area) as ArFragment
        arViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(ArViewModel::class.java)


        if (checkSystemSupport()) {
            setOnTapInPlane()
            setupTakePhotoButton()
            setupSelectModelButton()
        }
    }

    private fun checkSystemSupport(): Boolean {
        val openGlVersion: String = (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).deviceConfigurationInfo.glEsVersion
        return if (openGlVersion.toDouble() >= ArViewModel.MIN_OPENGL_VERSION) {
            if (!arePermissionsGranted()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE
                )
            }
            true
        } else {
            Toast.makeText(this, getString(R.string.open_gl_version_not_supported), Toast.LENGTH_SHORT).show()
            finish()
            false
        }
    }

    private fun arePermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de escritura concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso de escritura denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupTakePhotoButton() {
        val takePhotoButton = findViewById<Button>(R.id.btnTakePhoto)
        takePhotoButton.setOnClickListener {
            takePhoto()
        }
    }
    private fun setupSelectModelButton() {
        val selectModelButton = findViewById<Button>(R.id.btnSelectModel)
        selectModelButton.setOnClickListener {
            openFileSelector()
        }
    }
    private fun openFileSelector() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "model/*" // Filtrar solo modelos 3D
        startActivityForResult(intent, PICK_MODEL_REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_MODEL_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                loadModelFromUri(uri)
            }
        }
    }

    private fun loadModelFromUri(uri: Uri) {
        val file = File(uri.path) // Obtener el archivo desde la URI
        if (file.exists()) {
            ModelRenderable.builder()
                .setSource(this, Uri.parse(file.toURI().toString())) // Convertir la URI del archivo en una URI válida
                .build()
                .thenAccept { modelRenderable ->
                    arViewModel.addModelToScene(modelRenderable, arFragment)
                }
                .exceptionally { throwable ->
                    Toast.makeText(this, "Error loading model: ${throwable.message}", Toast.LENGTH_SHORT).show()
                    null
                }
        } else {
            Toast.makeText(this, "File not found at: ${uri.path}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun takePhoto() {
        val arView = arFragment.arSceneView
        val context = applicationContext

        if (arView != null) {
            val bitmap = Bitmap.createBitmap(arView.width, arView.height, Bitmap.Config.ARGB_8888)
            val handlerThread = HandlerThread("PixelCopier")
            handlerThread.start()
            PixelCopy.request(arView, bitmap, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    arViewModel.savePhotoToGallery(bitmap)
                } else {
                    Toast.makeText(context, "Failed to take photo", Toast.LENGTH_SHORT).show()
                }
                handlerThread.quitSafely()
            }, Handler(handlerThread.looper))
        } else {
            Toast.makeText(context, "AR view is not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setOnTapInPlane() {
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            arViewModel.handleTap(hitResult, arFragment)
        }
    }
}