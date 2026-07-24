package com.sayanthrock.rockreleasehub.feature.repositories

import com.sayanthrock.rockreleasehub.core.model.Repository
import com.sayanthrock.rockreleasehub.core.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RepoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: RepoViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RepoViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsSuccessWithMockRepos() {
        val state = viewModel.uiState.value

        assertTrue(state is RepoState.Success)

        val successState = state as RepoState.Success
        assertEquals(2, successState.repos.size)

        val firstRepo = successState.repos[0]
        assertEquals(1, firstRepo.id)
        assertEquals("Repo 1", firstRepo.name)
        assertEquals("testuser/Repo 1", firstRepo.fullName)
        assertEquals("Mock Repo", firstRepo.description)

        val expectedUser = User(1, "testuser", "", "Test User", "User")
        assertEquals(expectedUser, firstRepo.owner)

        assertEquals("Kotlin", firstRepo.language)
        assertEquals(10, firstRepo.stargazersCount)
        assertEquals(false, firstRepo.isPrivate)
        assertEquals("2024-01-01", firstRepo.updatedAt)

        val secondRepo = successState.repos[1]
        assertEquals(2, secondRepo.id)
        assertEquals("Repo 2", secondRepo.name)
        assertEquals("testuser/Repo 2", secondRepo.fullName)
        assertEquals("Another Mock", secondRepo.description)
        assertEquals(expectedUser, secondRepo.owner)
        assertEquals("Java", secondRepo.language)
        assertEquals(5, secondRepo.stargazersCount)
        assertEquals(true, secondRepo.isPrivate)
        assertEquals("2024-01-02", secondRepo.updatedAt)
    }
}
