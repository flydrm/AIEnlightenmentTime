package com.enlightenment.ai.domain.model

data class ChildProfile(
    val id: String,
    val name: String,
    val age: Int,
    val gender: String? = null,
    val interests: List<String> = emptyList(),
    val learningLevel: LearningLevel = LearningLevel.NORMAL,
    val preferredLearningStyle: LearningStyle = LearningStyle.MIXED,
    val recentTopics: List<String> = emptyList(),
    val companionPersonality: CompanionPersonality = CompanionPersonality()
)

enum class LearningLevel {
    BEGINNER,
    NORMAL,
    ADVANCED
}

enum class LearningStyle {
    VISUAL,
    AUDITORY,
    KINESTHETIC,
    MIXED
}

data class CompanionPersonality(
    val name: String = "小熊猫",
    val traits: List<String> = listOf("friendly", "encouraging", "patient"),
    val speechStyle: String = "warm_and_gentle"
)