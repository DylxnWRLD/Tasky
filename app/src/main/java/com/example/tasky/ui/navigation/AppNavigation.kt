package com.example.tasky.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.tasky.ui.auth.LoginRoute
import com.example.tasky.ui.auth.LoginViewModel
import com.example.tasky.ui.auth.RegisterRoute
import com.example.tasky.ui.auth.RegisterViewModel
import com.example.tasky.domain.usecase.LoginUseCase
import com.example.tasky.domain.usecase.RegisterUseCase
import com.example.tasky.data.repository.AuthRepositoryImpl
import com.example.tasky.ui.jobs.detail.JobDetailScreen
import com.example.tasky.ui.jobs.detail.JobDetailViewModel
import com.example.tasky.domain.usecase.ApplyToJobUseCase
import com.example.tasky.data.repository.JobRepositoryImpl
import com.example.tasky.ui.HomeViewModel
import com.example.tasky.ui.create.CreateJobScreen
import com.example.tasky.ui.create.CreateJobViewModel
import org.osmdroid.util.GeoPoint

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
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }

            LoginRoute(
                viewModel = viewModel,
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            val viewModel = remember {
                val repository = AuthRepositoryImpl()
                val useCase = RegisterUseCase(repository)
                RegisterViewModel(useCase)
            }

            RegisterRoute(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "job_detail/{jobId}",
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            val contextoDetalle = LocalContext.current

            val detailViewModel = remember {
                val repository = JobRepositoryImpl(contextoDetalle)
                val useCase = ApplyToJobUseCase(repository)
                JobDetailViewModel(repository, useCase)
            }

            JobDetailScreen(jobId = jobId, viewModel = detailViewModel, onNavigateBack = { navController.popBackStack() })
        }

        // Aquí se inyectan las rutas de navegación al HomeScreen
        composable("home") {
            val contextoHome = LocalContext.current
            val homeViewModel = remember {
                HomeViewModel(JobRepositoryImpl(contextoHome))
            }

            // Asegurar que el import apunte a com.example.tasky.ui.HomeScreen
            com.example.tasky.ui.HomeScreen(
                viewModel = homeViewModel,
                onNavigateToCreateJob = {
                    navController.navigate("create_job")
                },
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onJobClick = { jobId ->
                    navController.navigate("job_detail/$jobId")
                }
            )
        }

        // RUTA CREAR TRABAJO
        composable("create_job") {
            val contexto = LocalContext.current
            val createViewModel = remember {
                CreateJobViewModel(JobRepositoryImpl(contexto))
            }

            CreateJobScreen(
                viewModel = createViewModel,
                onNavigateBack = { navController.popBackStack() },
                onJobCreated = { imageUri, location, title, category, payment, description, date, time ->
                    createViewModel.publicarChamba(
                        imageUri, location, title, category, payment, description, date, time,
                        onSuccess = {
                            navController.popBackStack()
                        }
                    )
                }
            )
        }

        composable("profile") {
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Text("Pantalla de perfil en construcción...")
            }
        }
    }
}