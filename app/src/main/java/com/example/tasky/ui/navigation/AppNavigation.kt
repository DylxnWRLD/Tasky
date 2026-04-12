package com.example.tasky.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.example.tasky.ui.auth.LoginRoute
import com.example.tasky.ui.auth.LoginViewModel
import com.example.tasky.domain.usecase.LoginUseCase
import com.example.tasky.data.repository.AuthRepositoryImpl
import com.example.tasky.ui.home.HomeScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {

            val viewModel = remember {
                val repository = AuthRepositoryImpl()
                val useCase = LoginUseCase(repository)
                LoginViewModel(useCase)
            }

            val state = viewModel.state

            LaunchedEffect(state.user) {
                if (state.user != null) {
                    println("LOGIN EXITOSO: ${state.user}") // 👈 debug

                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }

            LoginRoute(viewModel = viewModel)
        }

        composable("home") {
            HomeScreen()
        }
    }
}