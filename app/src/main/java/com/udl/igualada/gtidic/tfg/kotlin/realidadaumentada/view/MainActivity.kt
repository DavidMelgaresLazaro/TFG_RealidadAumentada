package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import com.google.android.material.navigation.NavigationView
import com.google.ar.core.Plane
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.auth.FirebaseAuth
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.helpers.ArModelHelper
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.helpers.FileHelper
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.helpers.PhotoHelper
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model.ModelSource
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel.MainActivityViewModel
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.viewmodel.PhotoViewModel
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    companion object {
        private const val PICK_MODEL_REQUEST_CODE = 2
        private const val TAG = "MainActivity"
        private const val MAX_ALLOWED_TAPS = 1
    }

    private val viewModel: MainActivityViewModel by viewModels()
    private val photoViewModel: PhotoViewModel by viewModels()
    private lateinit var arFragment: ArFragment
    private lateinit var arModelHelper: ArModelHelper
    private lateinit var fileHelper: FileHelper
    private var tapCount = 0
    private lateinit var photoHelper: PhotoHelper
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.activity_main__container__camera_area) as ArFragment
        arModelHelper = ArModelHelper(arFragment)
        fileHelper = FileHelper(this)
        photoHelper = PhotoHelper(arFragment, photoViewModel)

        setupPhotoButton()

        viewModel.updateModelSource(ModelSource.ResourceId(R.raw.sas__cs2_agent_model_green))

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
                viewModel.updateAnchorNode(it)
                viewModel.modelSource.value?.let { source ->
                    arModelHelper.placeObject(it, source).thenAccept { modelInfo ->
                        viewModel.updateTransformableNode(modelInfo.transformableNode)
                        viewModel.updateModelName(modelInfo.name)
                    }
                }
            }
            tapCount++
        }

        setupNavigationDrawer()

        viewModel.modelSource.observe(this, Observer { modelSource ->
            modelSource?.let { source ->
                viewModel.anchorNode.value?.let { anchorNode ->
                    viewModel.transformableNode.value?.let { transformableNode ->
                        arModelHelper.removeTransformableNode(transformableNode)
                    }
                    arModelHelper.placeObject(anchorNode, source).thenAccept { modelInfo ->
                        viewModel.updateTransformableNode(modelInfo.transformableNode)
                        viewModel.updateModelName(modelInfo.name)
                    }
                }
            }
        })
    }

    private fun setupPhotoButton() {
        val photoButton = findViewById<ImageButton>(R.id.btnTakePhoto)
        photoButton.setOnClickListener {
            val anchorNode = viewModel.anchorNode.value
            val modelName = viewModel.modelName.value
            if (anchorNode != null && modelName != null) {
                showCommentDialog(this) { comment ->
                    photoHelper.takePhoto(applicationContext, anchorNode, modelName, comment)
                }
            } else {
                Toast.makeText(this, "AnchorNode or ModelName not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCommentDialog(context: Context, onCommentSubmitted: (String) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_comment, null)
        val editTextComment = dialogView.findViewById<EditText>(R.id.editTextComment)
        val buttonSubmit = dialogView.findViewById<Button>(R.id.buttonSubmit)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        buttonSubmit.setOnClickListener {
            val comment = editTextComment.text.toString()
            onCommentSubmitted(comment)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupNavigationDrawer() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_select_model -> {
                startActivityForResult(fileHelper.openFileSelector(), PICK_MODEL_REQUEST_CODE)
            }
            R.id.nav_view_photos -> {
                val intent = Intent(this, PhotoListActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawers()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_MODEL_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val filePath = fileHelper.getFilePathFromUri(uri)
                    viewModel.updateModelSource(ModelSource.UriSource(Uri.fromFile(File(filePath))))
                } catch (e: IOException) {
                    Toast.makeText(this, "Failed to load model", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
    }
}
