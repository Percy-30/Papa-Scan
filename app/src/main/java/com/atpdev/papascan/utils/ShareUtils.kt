package com.atpdev.papascan.utils

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.atpdev.papascan.R

// Función de extensión para Fragment
fun Fragment.sharePapaScanApp() {
    val shareText = getString(R.string.share_app_message) + "\n\n" + AppConstants.APP_SHARE_URL
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app_title))
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    startActivity(Intent.createChooser(intent, getString(R.string.choose_app_to_share)))
}

// Opcional: si deseas usarlo también desde una Activity
fun Context.sharePapaScanApp() {
    val shareText = getString(R.string.share_app_message) + "\n\n" + AppConstants.APP_SHARE_URL
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app_title))
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    startActivity(Intent.createChooser(intent, getString(R.string.choose_app_to_share)))
}
