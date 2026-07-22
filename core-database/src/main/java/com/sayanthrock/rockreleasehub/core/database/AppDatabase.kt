package com.sayanthrock.rockreleasehub.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sayanthrock.rockreleasehub.core.database.dao.RepositoryDao
import com.sayanthrock.rockreleasehub.core.database.entity.RepositoryEntity

@Database(entities = [RepositoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun repositoryDao(): RepositoryDao
}
