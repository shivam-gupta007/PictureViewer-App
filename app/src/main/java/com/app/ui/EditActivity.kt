package com.app.ui

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.app.R
import com.app.databinding.ActivityEditBinding
import com.app.viewmodel.ImageViewModel
import com.app.viewmodel.ImageViewModelSingleton
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import java.io.File
import java.io.FileOutputStream

class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private lateinit var imageViewModel: ImageViewModel
    private var rotationValue: Float = 90.0F
    private val myStorageRequestCode = 101
    private lateinit var cropImg: ActivityResultLauncher<CropImageContractOptions>
    private var originalImgUri: Uri? = null
    private val list = arrayListOf(90.0F, 180.0F, 270.0F, 360.0F)
    private var i = 0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit)

        imageViewModel = ImageViewModelSingleton.getInstance(this.application)

        imageViewModel.imageUriLiveData.observe(this) {
            if (it != null)
                binding.image.setImageURI(it)
        }


        cropImg = registerForActivityResult(CropImageContract()) {
            val uriContent: Uri
            if (it.isSuccessful) {
                uriContent = it.uriContent!!
                imageViewModel.imageUriLiveData.postValue(uriContent)
                Toast.makeText(this, "Picture cropped", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("Exception", it.error.toString())
            }
        }

        binding.rotateBtn.setOnClickListener {
            if (imageViewModel.imageUriLiveData.value != null) {
                rotateImage(list[i])
                i.inc()
            }
        }

        binding.cropBtn.setOnClickListener {
            cropImage()
        }

        binding.undoBtn.setOnClickListener {
            rotateImage(0.0F)
            if (originalImgUri != null) {
                binding.image.setImageURI(originalImgUri)
                imageViewModel.imageUriLiveData.postValue(originalImgUri)
            }
        }

        binding.saveBtn.setOnClickListener {
            requestStoragePermission()
            saveImageToGallery()
        }

    }

    private fun cropImage() {

        val imageUri: Uri? = imageViewModel.imageUriLiveData.value

        if (imageUri != null) {
            originalImgUri = imageUri

            cropImg.launch(options(uri = imageUri) {
                setGuidelines(CropImageView.Guidelines.ON)
                setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                setCropMenuCropButtonIcon(R.drawable.ic_crop)
                setAllowRotation(false)
                setAllowFlipping(false)
                setActivityTitle("Crop Image")
            })

        }
    }

    private fun saveImageToGallery() {
        val imagePath =
            File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS)

        if (!imagePath.exists())
            imagePath.mkdir()

        val bitmapDrawable = binding.image.drawable as BitmapDrawable
        var bitmap = bitmapDrawable.bitmap
        if (rotationValue > 0.0F)
            bitmap = rotateBitmap(bitmap)

        val newFile = File(imagePath, "${System.currentTimeMillis()}.png")

        try {
            val fileOutputStream = FileOutputStream(newFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            Toast.makeText(this, "Picture saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.d("msg", "Exception: ${e.message}")
        }

    }

    private fun rotateBitmap(bitmap: Bitmap?): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(rotationValue)
        return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun rotateImage(value: Float) {
        originalImgUri = imageViewModel.imageUriLiveData.value

        val bitmapDrawable = binding.image.drawable as BitmapDrawable
        var bitmap = bitmapDrawable.bitmap
        val matrix = Matrix()
        matrix.postRotate(value)
        bitmap = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
        binding.image.setImageBitmap(bitmap)

        rotationValue += 90.0F
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestStoragePermission() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                Array(1) { android.Manifest.permission.WRITE_EXTERNAL_STORAGE },
                myStorageRequestCode
            )
        }
    }
}