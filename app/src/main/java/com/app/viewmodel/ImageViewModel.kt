package com.app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class ImageViewModel(application: Application) : AndroidViewModel(application) {
    val imageUriLiveData: MutableLiveData<Uri> = MutableLiveData<Uri>()
}