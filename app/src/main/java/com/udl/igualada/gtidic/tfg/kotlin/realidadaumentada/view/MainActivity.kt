package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.ar.core.HitResult
import com.google.ar.sceneform.ux.ArFragment
import android.Manifest
import android.content.Intent
import android.net.Uri
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel.ArViewModel
import java.io.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val PICK_MODEL_REQUEST_CODE = 2
    }

    private lateinit var arViewModel: ArViewModel
    private lateinit var arFragment: ArFragment

    private var lastHitResult: HitResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(
            R.id.activity_main__container__camera_area) as ArFragment

        arViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(ArViewModel::class.java)

        if (checkSystemSupport()) {
            setOnTapInPlane()
            setupTakePhotoButton()
            setupSelectModelButton()
        }
    }


    private fun checkSystemSupport(): Boolean {
        val openGlVersion = (
                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                ).deviceConfigurationInfo.glEsVersion.toDouble()
        if (openGlVersion < ArViewModel.MIN_OPENGL_VERSION) {
            showOpenGlVersionNotSupportedMessage()
            finish()
            return false
        }
        if (!hasCameraPermission()) {
            requestCameraPermission()
        }
        if (!hasReadExternalStoragePermission()) {
            requestReadExternalStoragePermission()
        }
        return true
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE)
    }

    private fun hasReadExternalStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
    }

    private fun showOpenGlVersionNotSupportedMessage() {
        Toast.makeText(this,
            getString(R.string.open_gl_version_not_supported),
            Toast.LENGTH_SHORT
        ).show()
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
                val fp = getFilePathFromUri(uri)
               arViewModel.changeModel(fp, arFragment)
            }
        }
    }


    @Throws(IOException::class)
    private fun getFilePathFromUri(uri: Uri): String {
        val resolver = contentResolver
        val inputStream = resolver.openInputStream(uri)
        val tempFile = File.createTempFile("temp", ".glb", cacheDir)
        val outputStream = FileOutputStream(tempFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return tempFile.absolutePath
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