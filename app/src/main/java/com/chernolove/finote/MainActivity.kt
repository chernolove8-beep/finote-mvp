package com.chernolove.finote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.chernolove.finote.presentation.FinoteApp
import com.chernolove.finote.ui.theme.FinoteTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Вызываем стандартную инициализацию Android Activity.
        super.onCreate(savedInstanceState)

        // setContent = вместо XML мы сразу описываем интерфейс через Kotlin + Compose.
        setContent {
            // Single Activity: всё приложение работает внутри одного Activity,
            // а переключение экранов происходит уже через Compose Navigation.
            FinoteTheme {
                // Тема приложения:
                // цвета, шрифты и общее визуальное оформление.

                // ViewModel получаем через DI (Koin), чтобы не создавать её вручную.
                // То есть готовый объект уже передаётся сюда из контейнера зависимостей.
                val viewModel = koinViewModel<com.chernolove.finote.presentation.FinoteViewModel>()

                // Корневой Compose-экран приложения.
                // Отсюда начинается дерево интерфейса и навигация между экранами.
                FinoteApp(viewModel = viewModel)
            }
        }
    }
}
