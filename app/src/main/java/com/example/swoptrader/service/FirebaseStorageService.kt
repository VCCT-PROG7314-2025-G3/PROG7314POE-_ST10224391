package com.example.swoptrader.service

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageService @Inject constructor() {
    
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    
    /**
     * Upload an image to Firebase Storage
     * @param imageUri The URI of the image to upload
     * @param folder The folder to upload to (e.g., "items", "profiles")
     * @return The download URL of the uploaded image
     */
    suspend fun uploadImage(imageUri: Uri, folder: String = "items"): Result<String> {
        return try {
            // Create a unique filename
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child("$folder/$fileName")
            
            // Upload the image with metadata
            val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()
            
            val uploadTask = imageRef.putFile(imageUri, metadata).await()
            
            val downloadUrl = imageRef.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: com.google.firebase.storage.StorageException) {
            when (e.errorCode) {
                com.google.firebase.storage.StorageException.ERROR_OBJECT_NOT_FOUND -> {
                    Result.failure(Exception("Image not found. Please try selecting a different image."))
                }
                com.google.firebase.storage.StorageException.ERROR_NOT_AUTHENTICATED -> {
                    Result.failure(Exception("Permission denied. Please check your account permissions."))
                }
                com.google.firebase.storage.StorageException.ERROR_QUOTA_EXCEEDED -> {
                    Result.failure(Exception("Storage quota exceeded. Please try again later."))
                }
                else -> {
                    Result.failure(Exception("Upload failed: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Upload failed: ${e.message}"))
        }
    }
    
    /**
     * Upload multiple images to Firebase Storage
     * @param imageUris List of image URIs to upload
     * @param folder The folder to upload to
     * @return List of download URLs
     */
    suspend fun uploadImages(imageUris: List<Uri>, folder: String = "items"): Result<List<String>> {
        return try {
            val downloadUrls = mutableListOf<String>()
            
            for (imageUri in imageUris) {
                val result = uploadImage(imageUri, folder)
                result.fold(
                    onSuccess = { url -> downloadUrls.add(url) },
                    onFailure = { throw it }
                )
            }
            
            Result.success(downloadUrls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete an image from Firebase Storage
     * @param imageUrl The download URL of the image to delete
     * @return Success or failure result
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
