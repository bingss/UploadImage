package com.example.uploadimagetest.data

import android.net.Uri

data class CameraPhoto(
    val fileUri: Uri? = null,
    val filePath: String = ""
)
