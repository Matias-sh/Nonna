@file:Suppress("DEPRECATION")

package com.cocido.nonna.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

data class NonnaCropRequest(
    val sourceUri: Uri,
    val aspectRatio: Float,
    val title: String,
    val lockAspectRatio: Boolean = true
)

class NonnaCropContract : ActivityResultContract<NonnaCropRequest, Uri?>() {
    private val delegate = CropImageContract()

    override fun createIntent(context: Context, input: NonnaCropRequest): Intent {
        val ratioY = 1000
        val ratioX = (input.aspectRatio * ratioY).toInt().coerceAtLeast(1)
        val options = CropImageOptions().apply {
            fixAspectRatio = input.lockAspectRatio
            if (input.lockAspectRatio) {
                aspectRatioX = ratioX
                aspectRatioY = ratioY
            }
            outputCompressFormat = android.graphics.Bitmap.CompressFormat.JPEG
            outputCompressQuality = 92
            allowRotation = true
            allowFlipping = false
            autoZoomEnabled = true
            multiTouchEnabled = true
            showCropOverlay = true
            showProgressBar = true
            guidelines = CropImageView.Guidelines.ON
            showCropLabel = true
            cropMenuCropButtonTitle = "Aplicar"
            activityTitle = input.title
            activityBackgroundColor = android.graphics.Color.parseColor("#0F0F12")
            toolbarColor = android.graphics.Color.parseColor("#1A1A1E")
            toolbarBackButtonColor = android.graphics.Color.WHITE
            toolbarTintColor = android.graphics.Color.parseColor("#D86F4D")
            activityMenuIconColor = android.graphics.Color.parseColor("#D86F4D")
        }
        return delegate.createIntent(context, CropImageContractOptions(input.sourceUri, options))
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return delegate.parseResult(resultCode, intent).uriContent
    }
}
