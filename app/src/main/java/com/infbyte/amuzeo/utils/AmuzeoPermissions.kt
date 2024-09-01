package com.infbyte.amuzeo.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment

object AmuzeoPermissions {
    fun isReadPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            context
                .checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
        }
    }
}
