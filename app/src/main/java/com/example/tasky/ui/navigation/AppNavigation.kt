package com.example.tasky.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.tasky.ui.auth.LoginRoute
import com.example.tasky.ui.auth.LoginViewModel
import com.example.tasky.domain.usecase.LoginUseCase
import com.example.tasky.data.repository.AuthRepositoryImpl
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
                    navController.navigate("job_detail/7f72b1f4-7596-4c2b-b659-c68b1305d438") {                        popUpTo("login") { inclusive = true }
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
                JobDetailViewModel(repository, useCase)
            }

            JobDetailScreen(jobId = jobId, viewModel = detailViewModel)
        }

        composable("home") {
            com.example.tasky.ui.home.HomeScreen()
        }
    }
}