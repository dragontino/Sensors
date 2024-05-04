package com.sensors.app.util

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

object RequestCodes {
    const val REQUEST_LOCATION_PERMISSION = 1
    const val REQUEST_PHONE_STATE_PERMISSION = 2
    const val REQUEST_EXTERNAL_STORAGE_PERMISSION = 3
}


inline fun Fragment.usePermissions(
    vararg permissions: String,
    requestCode: Int,
    use: () -> Unit
) {
    when {
        checkPermissions(*permissions) -> use()
        else -> requestPermissions(permissions = permissions, code = requestCode)
    }
}


fun Fragment.checkPermissions(vararg permissions: String): Boolean {
    return permissions.all { permission ->
        ActivityCompat.checkSelfPermission(
            /* context = */ requireContext(),
            /* permission = */ permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}


fun Fragment.requestPermissions(
    vararg permissions: String,
    code: Int
) {
    ActivityCompat.requestPermissions(
        /* activity = */ requireActivity(),
        /* permissions = */ permissions,
        /* requestCode = */ code
    )
}


inline fun Fragment.onRequestPermissionsResult(
    crossinline grantedPermissions: (permissions: List<String>) -> Unit,
    crossinline deniedPermissions: (permissions: List<String>) -> Unit
) {
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.isEmpty()) deniedPermissions(emptyList())

        result.toList().partition { it.second }.let { (granted, denied) ->
            granted.takeIf { it.isNotEmpty() }?.map { it.first }?.let(grantedPermissions)
            denied.takeIf { it.isNotEmpty() }?.map { it.first }?.let(deniedPermissions)
        }
    }
}