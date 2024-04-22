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
import java.io.Console
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var arViewModel: ArViewModel
    private lateinit var arFragment: ArFragment

    private var lastHitResult: HitResult? = null

    private val PICK_MODEL_REQUEST_CODE = 1

    // Constants for better maintainability
    private  val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1
    private  val PERMISSION_GRANTED_MESSAGE = "Permiso de escritura concedido"
    private  val PERMISSION_DENIED_MESSAGE = "Permiso de escritura denegado"


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
        if (openGlVersion.toDouble() < ArViewModel.MIN_OPENGL_VERSION) {
            Toast.makeText(this, getString(R.string.open_gl_version_not_supported), Toast.LENGTH_SHORT).show()
            finish()
            return false
        }
        if (!arePermissionsGranted()) {
            requestPermissions()
        }
        return true
    }

    private fun arePermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE),
            WRITE_EXTERNAL_STORAGE_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            val message = if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PERMISSION_GRANTED_MESSAGE
            } else {
                PERMISSION_DENIED_MESSAGE
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
        intent.type = "*/*" // Filtrar todos los tipos de archivos
        //intent.type = "model/*" // Filtrar solo modelos 3D
        startActivityForResult(intent, PICK_MODEL_REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_MODEL_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val hitResult = lastHitResult // Obtener el HitResult almacenado
                if (hitResult != null) {
                    loadModelFromUri(uri, applicationContext, hitResult, arFragment)
                } else {
                    Toast.makeText(applicationContext, "No se encontró el HitResult", Toast.LENGTH_SHORT).show()
                    Log.e("MainActivity", "No se encontró el HitResult al cargar el modelo desde URI")
                }
            }
        }
    }


    fun loadModelFromUri(uri: Uri, context: Context, hitResult: HitResult, arFragment: ArFragment) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            // Create a temporary file
            val tempFile = File.createTempFile("model", ".glb", context.cacheDir)
            // Use FileOutputStream to write the InputStream to the temporary file
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            // Check if the file exists
            if (tempFile.exists()) {
                // Create an Anchor from the HitResult
                val anchor = hitResult.createAnchor()
                // Load the model from the temporary file
                ModelRenderable.builder()
                    .setSource(context, Uri.parse(tempFile.absolutePath))
                    .build()
                    .thenAccept { modelRenderable ->
                        // Add the model to the scene using the created Anchor
                        arViewModel.addModel(anchor, modelRenderable, arFragment)
                    }
                    .exceptionally { throwable ->
                        Toast.makeText(context, "Error loading model: ${throwable.message}", Toast.LENGTH_SHORT).show()
                        Log.d("TAG", "Error loading model: ${throwable.message}")
                        null
                    }
            } else {
                Toast.makeText(context, "File not found at: ${uri.path}", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "File not found at: ${uri.path}")
            }
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
            lastHitResult = hitResult // Almacenar el HitResult globalmente
            arViewModel.handleTap(hitResult, arFragment)
        }
    }
}