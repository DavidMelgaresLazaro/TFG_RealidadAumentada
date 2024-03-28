package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

// MainActivity.kt
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel.ArViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: ArViewModel by viewModels()

    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(
            R.id.activity_main__container__camera_area
        ) as ArFragment

        viewModel.tapNumber.observe(this, Observer { tapNumber ->
            if (viewModel.isTapLimitReached()) {
                // Handle tap logic
                viewModel.resetTapNumber() // Reset tapNumber
            }
        })

        viewModel.model.observe(this, Observer { arModel ->
            arModel?.let { addModel(it.anchor, it.modelRenderable) }
        })

        setupArTapListener()
        setupTakePhotoButton()
    }

    private fun setupArTapListener() {
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            viewModel.incrementTapNumber()
            createModelRenderable(hitResult)
        }
    }

    private fun createModelRenderable(hitResult: HitResult) {
        if (!viewModel.isTapLimitReached()) {
            val anchor: Anchor = hitResult.createAnchor()
            ModelRenderable.builder()
                .setSource(this, R.raw.chess)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept { modelRenderable: ModelRenderable? ->
                    viewModel.setModel(anchor, modelRenderable)
                }.exceptionally { throwable: Throwable ->
                    // Handle exception
                    null
                }
        }
    }

    private fun addModel(anchor: Anchor, modelRenderable: ModelRenderable?) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment.arSceneView.scene)

        val transformableNode = TransformableNode(arFragment.transformationSystem)
        transformableNode.setParent(anchorNode)

        transformableNode.renderable = modelRenderable
        transformableNode.select()
    }

    private fun setupTakePhotoButton() {
        val takePhotoButton = findViewById<Button>(R.id.btnTakePhoto)
        takePhotoButton.setOnClickListener {
            // Logic for taking photo
        }
    }
}



