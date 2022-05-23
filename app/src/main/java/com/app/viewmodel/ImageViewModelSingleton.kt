package com.app.viewmodel

import android.app.Application

object ImageViewModelSingleton {
    private lateinit var instance: ImageViewModel

    fun getInstance(application: Application): ImageViewModel {
        instance = if (::instance.isInitialized) instance else ImageViewModel(application)
        return instance
    }
}