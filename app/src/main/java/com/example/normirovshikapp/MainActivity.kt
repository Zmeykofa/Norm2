package com.example.normirovshikapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.normirovshikapp.data.AppDatabase
import com.example.normirovshikapp.ui.MainScreen
import com.example.normirovshikapp.ui.theme.NormirovshikAppTheme
import com.example.normirovshikapp.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получаем DAO из базы
        val dao = AppDatabase.getDatabase(applicationContext).appDao()

        // Создаём ViewModel через фабрику
        val viewModel: MainViewModel by viewModels {
            MainViewModel.Factory(dao, applicationContext)
        }

        // Оборачиваем UI в тему
        setContent {
            NormirovshikAppTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
