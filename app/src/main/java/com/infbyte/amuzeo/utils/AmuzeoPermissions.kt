package com.infbyte.amuzeo.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object AmuzeoPermissions {
    fun isReadPermissionGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context
                .checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) ==
                PackageManager.PERMISSION_GRANTED
        }

        return context
            .checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
    }
}
