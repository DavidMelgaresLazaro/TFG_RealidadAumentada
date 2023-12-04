package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada



import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


const val MIN_OPENGL_VERSION = 3.0
const val MAX_TAP_NUMBER = 1

class MainActivity : AppCompatActivity() {

    private var arFragment: ArFragment? = null
    private var tapNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (checkSystemSupport()) {
            setOnTapInPlane()
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

    private fun checkSystemSupport(): Boolean {
        val openGlVersion: String = (getSystemService(Context.ACTIVITY_SERVICE)
                as ActivityManager).deviceConfigurationInfo.glEsVersion
        return if (openGlVersion.toDouble() >= MIN_OPENGL_VERSION) {
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

    private fun addModel(anchor: Anchor, modelRenderable: ModelRenderable?) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment?.arSceneView?.scene)

        val transformableNode = TransformableNode(arFragment?.transformationSystem)
        transformableNode.setParent(anchorNode)

        transformableNode.renderable = modelRenderable
        transformableNode.select()
    }
}