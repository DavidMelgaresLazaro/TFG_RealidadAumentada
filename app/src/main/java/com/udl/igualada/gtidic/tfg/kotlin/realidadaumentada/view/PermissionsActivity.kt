package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R

class PermissionsActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private const val MAX_PERMISSION_REQUEST_ATTEMPTS = 3
        internal const val MIN_OPENGL_VERSION = 3.0
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var permissionMessage: TextView
    private var permissionRequestCount = mutableMapOf(
        Manifest.permission.CAMERA to 0,
        Manifest.permission.READ_EXTERNAL_STORAGE to 0,
        Manifest.permission.ACCESS_FINE_LOCATION to 0
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progressBar = findViewById(R.id.progressBar)
        permissionMessage = findViewById(R.id.permissionMessage)
    }

    override fun onResume() {
        super.onResume()
        checkSystemSupport()
    }

    private fun checkSystemSupport() {
        progressBar.visibility = ProgressBar.VISIBLE
        permissionMessage.visibility = TextView.VISIBLE

        val openGlVersion = (
                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                ).deviceConfigurationInfo.glEsVersion.toDouble()
        if (openGlVersion < MIN_OPENGL_VERSION) {
            showOpenGlVersionNotSupportedMessage()
            finish()
            return
        }
        if (!hasAllPermissions()) {
            requestAllPermissions()
        } else {
            progressBar.visibility = ProgressBar.GONE
            permissionMessage.visibility = TextView.GONE
            startMainActivity()
        }
    }

    private fun hasAllPermissions(): Boolean {
        return hasCameraPermission() && hasReadExternalStoragePermission() && hasLocationPermission()
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasReadExternalStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAllPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (!hasCameraPermission()) permissionsToRequest.add(Manifest.permission.CAMERA)
        if (!hasReadExternalStoragePermission()) permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        if (!hasLocationPermission()) permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showOpenGlVersionNotSupportedMessage() {
        Toast.makeText(this,
            getString(R.string.open_gl_version_not_supported),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showPermissionsRequiredMessage(missingPermissions: List<String>) {
        val permissionsText = missingPermissions.joinToString { it }
        Toast.makeText(this,
            "Se deben aceptar todos los permisos para poder utilizar la aplicación: $permissionsText",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = mutableListOf<String>()
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i])
                    permissionRequestCount[permissions[i]] = permissionRequestCount[permissions[i]]!! + 1
                }
            }
            if (deniedPermissions.isEmpty()) {
                checkSystemSupport()
            } else {
                progressBar.visibility = ProgressBar.GONE
                permissionMessage.visibility = TextView.GONE
                showPermissionsRequiredMessage(deniedPermissions)
                if (shouldRequestPermissionsAgain(deniedPermissions)) {
                    requestAllPermissions()
                } else {
                    showFinalPermissionDeniedMessage(deniedPermissions)
                }
            }
        }
    }

    private fun shouldRequestPermissionsAgain(deniedPermissions: List<String>): Boolean {
        for (permission in deniedPermissions) {
            if (permissionRequestCount[permission]!! < MAX_PERMISSION_REQUEST_ATTEMPTS &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true
            }
        }
        return false
    }

    private fun showFinalPermissionDeniedMessage(deniedPermissions: List<String>) {
        val permissionsText = deniedPermissions.joinToString { it }
        Toast.makeText(this,
            "Los siguientes permisos son necesarios: $permissionsText. Habilítelos manualmente en la configuración.",
            Toast.LENGTH_LONG
        ).show()
    }
}

