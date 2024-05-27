package com.udl.igualada.gtidic.tfg.kotlin.realidadaumentada.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileHelper(private val context: Context) {

    @Throws(IOException::class)
    fun getFilePathFromUri(uri: Uri): String {
        val resolver = context.contentResolver
        val inputStream = resolver.openInputStream(uri)
        val tempFile = File.createTempFile("temp", ".glb", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return tempFile.absolutePath
    }

    fun openFileSelector(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        return intent
    }
}
