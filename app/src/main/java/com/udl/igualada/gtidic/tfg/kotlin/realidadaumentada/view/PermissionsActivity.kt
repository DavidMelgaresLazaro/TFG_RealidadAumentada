package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.view

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.R


class PermissionsActivity : AppCompatActivity() {

    /*
     * @TODO: La idea david es poner un loading bar, que muestre los mensajes de que se estan pidieno permisos, revisando la compatibilidad,...
     */

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        internal const val MIN_OPENGL_VERSION = 3.0
        private const val REQUEST_LOCATION_PERMISSION = 1

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (checkSystemSupport()) {
            startMainActivity()
        }
    }

    private fun checkSystemSupport(): Boolean {
        val openGlVersion = (
                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                ).deviceConfigurationInfo.glEsVersion.toDouble()
        if (openGlVersion < MIN_OPENGL_VERSION) {
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
            this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE
        )
    }

    private fun hasReadExternalStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }
    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun showOpenGlVersionNotSupportedMessage() {
        Toast.makeText(this,
            getString(R.string.open_gl_version_not_supported),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}