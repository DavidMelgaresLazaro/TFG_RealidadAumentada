package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada


/*
import android.Manifest
import android.app.ActivityManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.view.PixelCopy
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

const val MIN_OPENGL_VERSION = 3.0
const val MAX_TAP_NUMBER = 1
const val REQUEST_CODE_PERMISSIONS = 101

class MainActivity : AppCompatActivity() {

    private var arFragment: ArFragment? = null
    private var tapNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val takePhotoButton = findViewById<Button>(R.id.btnTakePhoto)

        if (checkSystemSupport()) {
            setOnTapInPlane()
            setupTakePhotoButton()
        }

        takePhotoButton.setOnClickListener {
            takePhoto()
        }

    }

    private fun checkSystemSupport(): Boolean {
        val openGlVersion: String = (getSystemService(Context.ACTIVITY_SERVICE)
                as ActivityManager).deviceConfigurationInfo.glEsVersion
        return if (openGlVersion.toDouble() >= MIN_OPENGL_VERSION) {
            if (!arePermissionsGranted()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_PERMISSIONS
                )
            }
            true
        } else {
            Toast.makeText(
                this,
                getString(R.string.open_gl_version_not_supported),
                Toast.LENGTH_SHORT
            ).show()
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

    private fun setupTakePhotoButton() {
        val takePhotoButton = findViewById<Button>(R.id.btnTakePhoto)
        takePhotoButton.setOnClickListener {
            takePhoto()
        }
    }



    private fun takePhoto() {

        val arView = arFragment?.arSceneView

        val context = applicationContext

        if (arView != null) {

            val bitmap = Bitmap.createBitmap(arView.width, arView.height, Bitmap.Config.ARGB_8888)
            val handlerThread = HandlerThread("PixelCopier")
            handlerThread.start()
            PixelCopy.request(arView, bitmap, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {

                    savePhotoToGallery(bitmap)
                } else {
                    Toast.makeText(context, "Failed to take photo", Toast.LENGTH_SHORT).show()
                }
                handlerThread.quitSafely()
            }, Handler(handlerThread.looper))
        } else {
            Toast.makeText(context, "AR view is not available", Toast.LENGTH_SHORT).show()
        }
    }



    private fun savePhotoToGallery(bitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    Toast.makeText(
                        this,
                        "Photo saved to Gallery",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this,
                    "Failed to save photo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }




    private fun setOnTapInPlane() {
        arFragment = supportFragmentManager.findFragmentById(
            R.id.activity_main__container__camera_area
        ) as ArFragment
        arFragment?.setOnTapArPlaneListener { hitResult, _, _ ->
            tapNumber++
            if (tapNumber == MAX_TAP_NUMBER) {
                createModelRenderable(hitResult)
            }
        }
    }

    private fun createModelRenderable(hitResult: HitResult) {
        val anchor: Anchor = hitResult.createAnchor()
        ModelRenderable.builder()
            .setSource(this, R.raw.chess)
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { modelRenderable: ModelRenderable? ->
                addModel(
                    anchor,
                    modelRenderable
                )
            }.exceptionally { throwable: Throwable ->
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setMessage(
                    getString(R.string.something_is_not_right)
                            + throwable.message
                ).show()
                null
            }
    }

    private fun addModel(anchor: Anchor, modelRenderable: ModelRenderable?) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment?.arSceneView?.scene)

        val transformableNode = TransformableNode(arFragment?.transformationSystem)
        transformableNode.setParent(anchorNode)

        transformableNode.renderable = modelRenderable
        transformableNode.select()
    }
}
*/