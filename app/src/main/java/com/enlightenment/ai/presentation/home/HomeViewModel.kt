package com.enlightenment.ai.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            val profile = profileRepository.getProfile()
            _uiState.update { currentState ->
                currentState.copy(
                    greeting = if (profile != null) {
                        "嗨，${profile.name}！今天想学什么呢？"
                    } else {
                        "嗨，小朋友！让我们开始今天的学习吧！"
                    },
                    pandaMood = "happy"
                )
            }
        }
    }
}

data class HomeUiState(
    val greeting: String = "嗨，小朋友！",
    val pandaMood: String = "happy",
    val isLoading: Boolean = false
)