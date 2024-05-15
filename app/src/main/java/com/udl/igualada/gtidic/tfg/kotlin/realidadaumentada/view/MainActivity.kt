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
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.TransformableNode
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel.ArViewModel
import java.io.*
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {

    sealed class ModelSource {
        data class ResourceId(val id: Int) : ModelSource()
        data class UriSource(val uri: Uri) : ModelSource()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val PICK_MODEL_REQUEST_CODE = 2
    }

    private lateinit var arViewModel: ArViewModel
    private lateinit var arFragment: ArFragment

    private var lastHitResult: HitResult? = null

    private var currentAnchorNode: AnchorNode? = null
    private var currentTransformableNode: TransformableNode? = null

    private lateinit var selectedObject: ModelSource
    private lateinit var selectedObject2: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(
            R.id.activity_main__container__camera_area
        ) as ArFragment

        arViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(ArViewModel::class.java)

        selectedObject = ModelSource.ResourceId(R.raw.sas__cs2_agent_model_green)



        /*if (checkSystemSupport()) {
            setOnTapInPlane()
            setupSelectModelButton()
            setupIncreaseSizeButton()
            setupDecreaseSizeButton()
        }*/
        arFragment.setOnTapArPlaneListener { hitResult, plane, _ ->
            //If surface is not horizontal and upward facing
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                //return for the callback
                return@setOnTapArPlaneListener
            }
            //create a new anchor
            val anchor = hitResult.createAnchor()
            placeObject(arFragment, anchor, selectedObject as ModelSource)
        }

        setupSelectModelButton()
        setupIncreaseSizeButton()
        setupDecreaseSizeButton()

    }

    private fun placeObject(fragment: ArFragment, anchor: Anchor, model: ModelSource) {
        Log.d("ArViewModel", "Selected 3D model: $model")
        val context = fragment.requireContext()

        val renderableFuture: CompletableFuture<ModelRenderable>? = when (model) {
            is ModelSource.ResourceId ->
                ModelRenderable.builder().setSource(context, model.id).setIsFilamentGltf(true).build()
            is ModelSource.UriSource -> ModelRenderable.builder().setSource(context, model.uri).setIsFilamentGltf(true).build()
        }

        renderableFuture?.thenAccept { renderable ->
            Log.d("ArViewModel", "ModelRenderable instance: $renderable")
            addNodeToScene(fragment, anchor, renderable)
        }?.exceptionally { throwable ->
            Log.d("ArViewModel", "Error creating model renderable", throwable)
            Toast.makeText(context, context.getString(R.string.something_is_not_right)
                    + throwable.message, Toast.LENGTH_LONG).show()
            null
        }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, model: ModelRenderable) {
        Log.d("ArViewModel", "Anchor: $anchor")
        Log.d("ArViewModel", "ModelRenderable: $model")
        try {
            val anchorNode = AnchorNode(anchor)
            val transformableNode = TransformableNode(fragment.transformationSystem)
            transformableNode.renderable = model
            Log.d("ArViewModel", "TransformableNode instance: $transformableNode")
            transformableNode.setParent(anchorNode)
            fragment.arSceneView.scene.addChild(anchorNode)
            transformableNode.select()
            Log.d("ArViewModel", "Node added to scene successfully")
            Log.d("ArViewModel", "Node scale: ${transformableNode.localScale}")
            Log.d("ArViewModel", "Node position: ${transformableNode.localPosition}")

            currentAnchorNode = anchorNode
            currentTransformableNode = transformableNode

        } catch (e: Exception) {
            Log.e("ArViewModel", "Error adding node to scene", e)
        }
    }




    private fun setupIncreaseSizeButton() {
        val increaseSizeButton = findViewById<Button>(R.id.btnIncreaseSize)
        increaseSizeButton.setOnClickListener {
            //arViewModel.increaseModelSize(arFragment)
            increaseModelSize()
        }
    }

    private fun setupDecreaseSizeButton() {
        val decreaseSizeButton = findViewById<Button>(R.id.btnDecreaseSize)
        decreaseSizeButton.setOnClickListener {
            //arViewModel.decreaseModelSize(arFragment)
            decreaseModelSize()
        }
    }

    private fun increaseModelSize() {
        currentTransformableNode?.let { transformableNode ->
            val currentScale = transformableNode.localScale
            val newScale = Vector3(currentScale.x * 1.1f, currentScale.y * 1.1f, currentScale.z * 1.1f)
            transformableNode.localScale = newScale
        }
    }

    private fun decreaseModelSize() {
        currentTransformableNode?.let { transformableNode ->
            val currentScale = transformableNode.localScale
            val newScale = Vector3(currentScale.x * 0.9f, currentScale.y * 0.9f, currentScale.z * 0.9f)
            transformableNode.localScale = newScale
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

    private fun setModelPath(modelFileName: String) {
        selectedObject2 = Uri.parse(modelFileName)
        val toast = Toast.makeText(applicationContext, modelFileName, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun updateCurrentSceneWithNewModel(model: ModelSource){

        // Remove the existing TransformableNode from its parent AnchorNode
        currentTransformableNode?.let {
            it.parent?.let { parent ->
                parent.removeChild(it)
            }
        }

        // Create a new TransformableNode with the new model and add it to the same AnchorNode
        currentAnchorNode?.let { anchorNode ->
            anchorNode.anchor?.let { placeObject(arFragment, it, model) }
        }

    }


    private fun setupSelectModelButton() {
        val selectModelButton = findViewById<Button>(R.id.btnSelectModel)
        selectModelButton.setOnClickListener {
            openFileSelector()
            //selectedObject = ModelSource.ResourceId(R.raw.lamp)
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
                selectedObject = ModelSource.UriSource(Uri.parse(fp))
                updateCurrentSceneWithNewModel(selectedObject)

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

    private fun setupGestureDetector() {
        val gestureDetector =
            GestureDetector(this@MainActivity, object : GestureDetector.SimpleOnGestureListener() {
                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    // Calcula el desplazamiento en la escena AR
                    val translation = Vector3(distanceX, 0f, distanceY)
                    // Llama a la función moveModel con el desplazamiento calculado
                    arViewModel.moveModel(arFragment, translation)
                    return true
                }
            })

        // Obtiene la vista raíz del fragmento AR
        val rootView = arFragment.view

        // Asigna el detector de gestos a la vista raíz del fragmento
        rootView?.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true // Devuelve true para indicar que se ha gestionado el evento
        }
    }




}