package com.enlightenment.ai.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.model.ChildProfile
import com.enlightenment.ai.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            val profile = profileRepository.getProfile()
            _uiState.value = if (profile != null) {
                ProfileUiState.HasProfile(profile.toDisplayModel())
            } else {
                ProfileUiState.NoProfile
            }
        }
    }
    
    fun createProfile(name: String, age: Int) {
        viewModelScope.launch {
            val profile = ChildProfile(
                id = UUID.randomUUID().toString(),
                name = name,
                age = age
            )
            profileRepository.saveProfile(profile)
            _uiState.value = ProfileUiState.HasProfile(profile.toDisplayModel())
        }
    }
    
    fun updateInterests(interests: List<String>) {
        viewModelScope.launch {
            profileRepository.updateInterests(interests)
            loadProfile() // Reload to get updated profile
        }
    }
    
    private fun ChildProfile.toDisplayModel(): ProfileDisplayModel {
        return ProfileDisplayModel(
            id = id,
            name = name,
            age = age,
            interests = interests,
            learningLevel = learningLevel.toDisplayString(),
            daysLearned = 1, // TODO: Calculate from actual data
            storiesCompleted = 0 // TODO: Calculate from actual data
        )
    }
    
    private fun com.enlightenment.ai.domain.model.LearningLevel.toDisplayString(): String {
        return when (this) {
            com.enlightenment.ai.domain.model.LearningLevel.BEGINNER -> "初学者"
            com.enlightenment.ai.domain.model.LearningLevel.NORMAL -> "进阶学习者"
            com.enlightenment.ai.domain.model.LearningLevel.ADVANCED -> "小专家"
        }
    }
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    object NoProfile : ProfileUiState()
    data class HasProfile(val profile: ProfileDisplayModel) : ProfileUiState()
}

data class ProfileDisplayModel(
    val id: String,
    val name: String,
    val age: Int,
    val interests: List<String>,
    val learningLevel: String,
    val daysLearned: Int,
    val storiesCompleted: Int
)