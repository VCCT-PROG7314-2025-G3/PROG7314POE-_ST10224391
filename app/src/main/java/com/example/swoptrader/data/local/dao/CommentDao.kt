package com.example.swoptrader.data.local.dao

import androidx.room.*
import com.example.swoptrader.data.model.CommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    
    @Query("SELECT * FROM comments WHERE itemId = :itemId ORDER BY createdAt ASC")
    fun getCommentsByItem(itemId: String): Flow<List<CommentEntity>>
    
    @Query("SELECT * FROM comments WHERE itemId = :itemId ORDER BY createdAt ASC")
    suspend fun getCommentsByItemSync(itemId: String): List<CommentEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)
    
    @Update
    suspend fun updateComment(comment: CommentEntity)
    
    @Delete
    suspend fun deleteComment(comment: CommentEntity)
    
    @Query("DELETE FROM comments WHERE itemId = :itemId")
    suspend fun deleteCommentsByItem(itemId: String)
    
    @Query("DELETE FROM comments")
    suspend fun deleteAllComments()
}

