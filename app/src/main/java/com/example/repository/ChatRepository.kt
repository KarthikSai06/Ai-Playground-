package com.example.repository

import com.example.database.ChatDao
import com.example.models.ChatMessage
import com.example.models.ChatSession
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun getMessages(sessionId: Long): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun saveSession(title: String): Long {
        return chatDao.insertSession(ChatSession(title = title))
    }

    suspend fun saveMessage(sessionId: Long, prompt: String, platform: String, response: String) {
        chatDao.insertMessage(
            ChatMessage(
                sessionId = sessionId,
                prompt = prompt,
                platform = platform,
                response = response
            )
        )
    }

    suspend fun updateMessageResponse(sessionId: Long, platform: String, response: String) {
        chatDao.updateMessageResponse(sessionId, platform, response)
    }

    suspend fun clearHistory() {
        chatDao.clearHistory()
    }
}
