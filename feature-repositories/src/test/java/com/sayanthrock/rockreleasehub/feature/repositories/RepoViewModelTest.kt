package com.sayanthrock.rockreleasehub.feature.repositories

import com.sayanthrock.rockreleasehub.core.model.Repository
import com.sayanthrock.rockreleasehub.core.model.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RepoViewModelTest {

    @Test
    fun `initial uiState is Success with mock repositories`() = runTest {
        val viewModel = RepoViewModel()

        val state = viewModel.uiState.value

        assertTrue(state is RepoState.Success)

        val successState = state as RepoState.Success
        val expectedUser = User(1, "testuser", "", "Test User", "User")
        val expectedRepos = listOf(
            Repository(1, "Repo 1", "testuser/Repo 1", "Mock Repo", expectedUser, "Kotlin", 10, false, "2024-01-01"),
            Repository(2, "Repo 2", "testuser/Repo 2", "Another Mock", expectedUser, "Java", 5, true, "2024-01-02")
        )

        assertEquals(expectedRepos, successState.repos)
    }
}
