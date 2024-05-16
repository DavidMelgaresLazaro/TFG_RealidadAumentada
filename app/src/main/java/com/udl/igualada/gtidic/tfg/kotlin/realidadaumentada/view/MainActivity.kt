package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.ar.core.Plane
import com.google.ar.sceneform.ux.ArFragment
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.helpers.ArModelHelper
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.helpers.FileHelper
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.ModelSource
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel.MainActivityViewModel

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PICK_MODEL_REQUEST_CODE = 2
        private const val TAG = "MainActivity"
        private const val MAX_ALLOWED_TAPS = 1
    }

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var arFragment: ArFragment
    private lateinit var arModelHelper: ArModelHelper
    private lateinit var fileHelper: FileHelper
    private var tapCount = 0

    // @TODO: No veo necesario los botones +,-, ARCore ya te permite con dos dedos hacer zoom y rotar
    // Si lo quieres implementar lo revisamos luego
    // Esto es una propuesta que aún se puede mejorar, pero tiene ya los conceptos separados

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        arFragment = supportFragmentManager.findFragmentById(
            R.id.activity_main__container__camera_area
        ) as ArFragment

        arModelHelper = ArModelHelper(arFragment)
        fileHelper = FileHelper(this)

        viewModel.modelSource.value = ModelSource.ResourceId(R.raw.sas__cs2_agent_model_green)

        arFragment.setOnTapArPlaneListener { hitResult, plane, _ ->
            if (tapCount >= MAX_ALLOWED_TAPS) {
                return@setOnTapArPlaneListener
            }
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }
            val anchor = hitResult.createAnchor()
            val anchorNode = arModelHelper.createAnchorNode(anchor)
            anchorNode?.let {
                viewModel.anchorNode.value = it
                viewModel.modelSource.value?.let { source ->
                    arModelHelper.placeObject(it, source).thenAccept { triple ->
                        val (_, transformableNode, _) = triple
                        viewModel.transformableNode.value = transformableNode
                    }
                }
            }
            tapCount++
        }

        setupSelectModelButton()

        viewModel.modelSource.observe(this, Observer { modelSource ->
            modelSource?.let { source ->
                viewModel.anchorNode.value?.let { anchorNode ->
                    viewModel.transformableNode.value?.let { transformableNode ->
                        arModelHelper.removeTransformableNode(transformableNode)
                    }
                    arModelHelper.placeObject(anchorNode, source).thenAccept { triple ->
                        val (_, newTransformableNode, _) = triple
                        viewModel.transformableNode.value = newTransformableNode
                    }
                }
            }
        })

    }

    private fun setupSelectModelButton() {
        val selectModelButton = findViewById<Button>(R.id.btnSelectModel)
        selectModelButton.setOnClickListener {
            startActivityForResult(fileHelper.openFileSelector(), PICK_MODEL_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_MODEL_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val fp = fileHelper.getFilePathFromUri(uri)
                viewModel.modelSource.value = ModelSource.UriSource(Uri.parse(fp))
            }
        }
    }


}