package com.sayanthrock.rockreleasehub.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.sayanthrock.rockreleasehub.core.database.AppDatabase
import com.sayanthrock.rockreleasehub.core.database.entity.RepositoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RepositoryDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: RepositoryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.repositoryDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetRepositories() = runTest {
        val repo1 = RepositoryEntity(
            id = 1L,
            name = "repo1",
            fullName = "owner1/repo1",
            ownerLogin = "owner1",
            updatedAt = "2023-01-01T00:00:00Z"
        )
        val repo2 = RepositoryEntity(
            id = 2L,
            name = "repo2",
            fullName = "owner2/repo2",
            ownerLogin = "owner2",
            updatedAt = "2023-01-02T00:00:00Z"
        )

        dao.insertRepositories(listOf(repo1, repo2))

        val repositories = dao.getRepositories().first()

        assertEquals(2, repositories.size)
        // Check ordering by updatedAt DESC
        assertEquals(repo2, repositories[0])
        assertEquals(repo1, repositories[1])
    }

    @Test
    fun insertOnConflictReplace() = runTest {
        val repo1 = RepositoryEntity(
            id = 1L,
            name = "repo1",
            fullName = "owner1/repo1",
            ownerLogin = "owner1",
            updatedAt = "2023-01-01T00:00:00Z"
        )

        dao.insertRepositories(listOf(repo1))

        val repo1Updated = RepositoryEntity(
            id = 1L, // Same ID
            name = "repo1_updated",
            fullName = "owner1/repo1_updated",
            ownerLogin = "owner1",
            updatedAt = "2023-01-02T00:00:00Z" // Newer date
        )

        dao.insertRepositories(listOf(repo1Updated))

        val repositories = dao.getRepositories().first()

        assertEquals(1, repositories.size)
        assertEquals(repo1Updated, repositories[0])
    }

    @Test
    fun clearAllRepositories() = runTest {
        val repo1 = RepositoryEntity(
            id = 1L,
            name = "repo1",
            fullName = "owner1/repo1",
            ownerLogin = "owner1",
            updatedAt = "2023-01-01T00:00:00Z"
        )

        dao.insertRepositories(listOf(repo1))

        var repositories = dao.getRepositories().first()
        assertEquals(1, repositories.size)

        dao.clearAll()

        repositories = dao.getRepositories().first()
        assertTrue(repositories.isEmpty())
    }
}
