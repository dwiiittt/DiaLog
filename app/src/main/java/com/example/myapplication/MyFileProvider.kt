package com.example.myapplication

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// Ini adalah helper class untuk fitur "Ambil Foto" (Kamera)
class MyFileProvider : FileProvider(R.xml.file_paths) {
    companion object {
        fun getImageUri(context: Context): Uri {
            val directory = File(context.cacheDir, "images")
            directory.mkdirs()
            val file = File.createTempFile(
                "selected_image_",
                ".jpg",
                directory
            )
            val authority = context.packageName + ".fileprovider"
            return getUriForFile(
                context,
                authority,
                file
            )
        }
    }
}