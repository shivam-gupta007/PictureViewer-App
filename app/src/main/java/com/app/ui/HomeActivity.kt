package com.app.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import com.app.R
import com.app.databinding.ActivityHomeBinding
import com.app.viewmodel.ImageViewModel
import com.app.viewmodel.ImageViewModelSingleton
import java.io.File
import java.io.FileOutputStream

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var imageViewModel: ImageViewModel
    private val myCameraRequestCode = 100

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        imageViewModel = ImageViewModelSingleton.getInstance(this.application)

        imageViewModel.imageUriLiveData.observe(this) {
            if (it != null)
                startActivity(Intent(this, EditActivity::class.java))
        }


        val getImageFromGallery = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            if (it != null) {
                binding.image.setImageURI(it)
                imageViewModel.imageUriLiveData.postValue(it)
            }
        }

        val getImageFromCamera = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (it.data != null) {
                    val imageBitmap = it.data?.extras?.get("data") as Bitmap
                    Log.d("shivam", "Uri: ${it.data!!.data}")
                    binding.image.setImageBitmap(imageBitmap)
                    imageViewModel.imageUriLiveData.postValue(getUriFromBitmap(imageBitmap))
                }
            }
        }

        binding.galleryBtn.setOnClickListener {
            getImageFromGallery.launch("image/*")
        }

        binding.takeSelfieBtn.setOnClickListener {
            requestCameraPermission()
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            getImageFromCamera.launch(cameraIntent)
        }
    }

    private fun getUriFromBitmap(bitmap: Bitmap): Uri {
        val imageFolder = File(cacheDir, "images")
        var uri: Uri? = null
        try {
            if (!imageFolder.exists())
                imageFolder.mkdir()

            val file = File(imageFolder, "demo.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            uri = FileProvider.getUriForFile(this, "com.app.fileprovider", file)
        } catch (e: Exception) {
            Log.d("Exception",e.message.toString())
        }

        return uri!!
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestCameraPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                Array(1) { android.Manifest.permission.CAMERA },
                myCameraRequestCode
            )
        }
    }

}