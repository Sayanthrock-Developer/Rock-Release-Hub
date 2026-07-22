package com.sayanthrock.rockreleasehub.feature.releases

import androidx.lifecycle.ViewModel
import com.sayanthrock.rockreleasehub.core.model.Release
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ReleaseViewModel @Inject constructor() : ViewModel() {
    private val mockReleases = listOf(
        Release(1, "v1.0.0", "v1.0.0", "Initial Release", "2024-01-01", "2024-01-01", false, false, emptyList()),
        Release(2, "v1.1.0-beta", "v1.1.0", "Beta Release", "2024-01-02", "2024-01-02", false, true, emptyList())
    )

    private val _uiState = MutableStateFlow<ReleaseState>(ReleaseState.Success(mockReleases))
    val uiState: StateFlow<ReleaseState> = _uiState.asStateFlow()
}

sealed interface ReleaseState {
    data object Loading : ReleaseState
    data class Success(val releases: List<Release>) : ReleaseState
    data class Error(val message: String) : ReleaseState
}
