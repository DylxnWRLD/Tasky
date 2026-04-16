package com.example.tasky.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.tasky.ui.auth.LoginRoute
import com.example.tasky.ui.auth.LoginViewModel
import com.example.tasky.domain.usecase.LoginUseCase
import com.example.tasky.data.repository.AuthRepositoryImpl
// Importa tus nuevas clases de Jobs
import com.example.tasky.ui.jobs.detail.JobDetailScreen
import com.example.tasky.ui.jobs.detail.JobDetailViewModel
import com.example.tasky.domain.usecase.ApplyToJobUseCase
import com.example.tasky.data.repository.JobRepositoryImpl

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

                    navController.navigate("job_detail/1") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }

            LoginRoute(viewModel = viewModel)
        }

        composable(
            route = "job_detail/{jobId}",
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""

            val detailViewModel = remember {
                val repository = JobRepositoryImpl()
                val useCase = ApplyToJobUseCase(repository)
                JobDetailViewModel(useCase)
            }

            JobDetailScreen(viewModel = detailViewModel)
        }

        composable("home") {
            com.example.tasky.ui.home.HomeScreen()
        }
    }
}