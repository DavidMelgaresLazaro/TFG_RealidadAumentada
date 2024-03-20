package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.model

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.ar.core.Session

class ARModel(private val context: Context) {

    private var session: Session? = null

    fun checkSystemSupport(): Boolean {
        val openGlVersion: String = (context.getSystemService(Context.ACTIVITY_SERVICE)
                as ActivityManager).deviceConfigurationInfo.glEsVersion
        return openGlVersion.toDouble() >= MIN_OPENGL_VERSION
    }

    fun arePermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val MIN_OPENGL_VERSION = 3.0
    }
}
