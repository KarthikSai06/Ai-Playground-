package com.example.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.models.ChatMessage
import com.example.models.ChatSession

@Database(entities = [ChatSession::class, ChatMessage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
