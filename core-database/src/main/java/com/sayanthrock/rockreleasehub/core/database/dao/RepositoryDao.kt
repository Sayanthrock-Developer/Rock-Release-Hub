package com.sayanthrock.rockreleasehub.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sayanthrock.rockreleasehub.core.database.entity.RepositoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepositoryDao {
    @Query("SELECT * FROM repositories ORDER BY updatedAt DESC")
    fun getRepositories(): Flow<List<RepositoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositories(repositories: List<RepositoryEntity>)

    @Query("DELETE FROM repositories")
    suspend fun clearAll()
}
