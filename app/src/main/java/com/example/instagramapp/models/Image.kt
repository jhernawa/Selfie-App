package com.example.instagramapp.models

import android.graphics.Bitmap
import java.io.Serializable

data class Image(
    var imagePath: String? = null,
    var key : String? = null
) : Serializable