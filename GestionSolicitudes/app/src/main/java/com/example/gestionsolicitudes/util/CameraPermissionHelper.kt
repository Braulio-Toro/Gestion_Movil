package com.example.gestionsolicitudes.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object CameraPermissionHelper {
    fun tienePermisoCamara(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
}
