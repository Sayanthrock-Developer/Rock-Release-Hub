package com.sayanthrock.rockreleasehub.feature.repositories

import androidx.lifecycle.ViewModel
import com.sayanthrock.rockreleasehub.core.model.Repository
import com.sayanthrock.rockreleasehub.core.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RepoViewModel @Inject constructor() : ViewModel() {
    private val mockUser = User(1, "testuser", "", "Test User", "User")
    private val mockRepos = listOf(
        Repository(1, "Repo 1", "testuser/Repo 1", "Mock Repo", mockUser, "Kotlin", 10, false, "2024-01-01"),
        Repository(2, "Repo 2", "testuser/Repo 2", "Another Mock", mockUser, "Java", 5, true, "2024-01-02")
    )

    private val _uiState = MutableStateFlow<RepoState>(RepoState.Success(mockRepos))
    val uiState: StateFlow<RepoState> = _uiState.asStateFlow()
}

sealed interface RepoState {
    data object Loading : RepoState
    data class Success(val repos: List<Repository>) : RepoState
    data class Error(val message: String) : RepoState
}
