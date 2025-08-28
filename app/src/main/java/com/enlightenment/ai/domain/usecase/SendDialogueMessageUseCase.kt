package com.enlightenment.ai.domain.usecase

import com.enlightenment.ai.domain.model.ConversationContext
import com.enlightenment.ai.domain.model.DialogueMessage
import com.enlightenment.ai.domain.repository.DialogueRepository
import com.enlightenment.ai.domain.repository.ProfileRepository
import javax.inject.Inject

class SendDialogueMessageUseCase @Inject constructor(
    private val dialogueRepository: DialogueRepository,
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        message: String,
        conversationId: String
    ): Result<DialogueMessage> {
        val profile = profileRepository.getProfile()
        val history = dialogueRepository.getConversationHistory(conversationId)
        
        val context = ConversationContext(
            conversationId = conversationId,
            childAge = profile?.age ?: 4,
            recentMessages = history.takeLast(5)
        )
        
        return dialogueRepository.sendMessage(message, context)
    }
}