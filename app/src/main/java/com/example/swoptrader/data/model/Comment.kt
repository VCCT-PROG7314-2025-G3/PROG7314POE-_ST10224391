package com.example.swoptrader.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("itemId")
    val itemId: String,
    
    @SerializedName("authorId")
    val authorId: String,
    
    @SerializedName("authorName")
    val authorName: String,
    
    @SerializedName("authorProfileImageUrl")
    val authorProfileImageUrl: String? = null,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @SerializedName("isEdited")
    val isEdited: Boolean = false,
    
    @SerializedName("likes")
    val likes: Int = 0,
    
    @SerializedName("replies")
    val replies: List<Comment> = emptyList(),
    
    @SerializedName("parentCommentId")
    val parentCommentId: String? = null
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey
    val id: String,
    val itemId: String,
    val authorId: String,
    val authorName: String,
    val authorProfileImageUrl: String? = null,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false,
    val likes: Int = 0,
    val parentCommentId: String? = null
)

// Extension functions to convert between Comment and CommentEntity
fun Comment.toEntity(): CommentEntity {
    return CommentEntity(
        id = id,
        itemId = itemId,
        authorId = authorId,
        authorName = authorName,
        authorProfileImageUrl = authorProfileImageUrl,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isEdited = isEdited,
        likes = likes,
        parentCommentId = parentCommentId
    )
}

fun CommentEntity.toComment(): Comment {
    return Comment(
        id = id,
        itemId = itemId,
        authorId = authorId,
        authorName = authorName,
        authorProfileImageUrl = authorProfileImageUrl,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isEdited = isEdited,
        likes = likes,
        replies = emptyList(), // Replies would be loaded separately
        parentCommentId = parentCommentId
    )
}
