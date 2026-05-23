package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.database.AppDatabase
import com.example.repository.ChatRepository
import com.example.ui.MainScreen
import com.example.ui.theme.AIHubTheme
import com.example.viewmodel.MainViewModel
import com.example.webview.WebViewManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "aihub-db").build()
        val repo = ChatRepository(db.chatDao())
        
        // Use singleton WebViewManager
        WebViewManager.currentActivityContext = this
        val viewModel = MainViewModel(repo) // Updated constructor

        setContent {
            AIHubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(viewModel) // Updated call
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (WebViewManager.currentActivityContext == this) {
            WebViewManager.dismissAllDialogs()
            WebViewManager.currentActivityContext = null
        }
    }
}

