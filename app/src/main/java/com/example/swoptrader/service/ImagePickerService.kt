package com.example.swoptrader.service

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImagePickerService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun getCameraPermissionRationale(): String {
        return "Camera access is needed to take photos of your items for listing."
    }
    
    fun getStoragePermissionRationale(): String {
        return "Storage access is needed to select photos from your gallery for item listings."
    }
    
    fun createCameraIntent(): Intent {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        return intent
    }
    
    fun createGalleryIntent(): Intent {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        return intent
    }
    
    fun createImageChooserIntent(): Intent {
        val cameraIntent = createCameraIntent()
        val galleryIntent = createGalleryIntent()
        
        val chooserIntent = Intent.createChooser(galleryIntent, "Select Image Source")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        
        return chooserIntent
    }
    
    fun getImageUriFromFile(file: File): Uri? {
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }
    
    fun createTempImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.cacheDir, "images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File(storageDir, "IMG_${timeStamp}.jpg")
    }
}

