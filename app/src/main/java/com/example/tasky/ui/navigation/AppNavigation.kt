package com.example.tasky.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.tasky.ui.auth.LoginRoute
import com.example.tasky.ui.auth.LoginViewModel
import com.example.tasky.ui.auth.RegisterRoute
import com.example.tasky.ui.auth.RegisterViewModel
import com.example.tasky.ui.auth.ForgotPasswordRoute
import com.example.tasky.ui.auth.ForgotPasswordViewModel
import com.example.tasky.domain.usecase.LoginUseCase
import com.example.tasky.domain.usecase.RegisterUseCase
import com.example.tasky.domain.usecase.ResetPasswordUseCase
import com.example.tasky.domain.usecase.VerifyOtpUseCase
import com.example.tasky.domain.usecase.UpdatePasswordUseCase
import com.example.tasky.data.repository.AuthRepositoryImpl
import com.example.tasky.data.repository.ForgotPasswordRepositoryImpl
import com.example.tasky.ui.jobs.detail.JobDetailScreen
import com.example.tasky.ui.jobs.detail.JobDetailViewModel
import com.example.tasky.ui.jobs.detail.JobApplicantsScreen
import com.example.tasky.ui.jobs.detail.JobApplicantsViewModel
import com.example.tasky.domain.usecase.ApplyToJobUseCase
import com.example.tasky.domain.usecase.GetApplicantsUseCase
import com.example.tasky.data.repository.JobRepositoryImpl
import com.example.tasky.ui.HomeViewModel
import com.example.tasky.ui.create.CreateJobScreen
import com.example.tasky.ui.create.CreateJobViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // --- AUTENTICACIÓN ---
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
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
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
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("forgot_password") {
            val viewModel = remember {
                val repository = ForgotPasswordRepositoryImpl()
                val resetUseCase = ResetPasswordUseCase(repository)
                val verifyUseCase = VerifyOtpUseCase(repository)
                val updateUseCase = UpdatePasswordUseCase(repository)
                ForgotPasswordViewModel(resetUseCase, verifyUseCase, updateUseCase)
            }

            ForgotPasswordRoute(
                viewModel = viewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onPasswordUpdated = {
                    navController.navigate("login") {
                        popUpTo("forgot_password") { inclusive = true }
                    }
                }
            )
        }

        // --- FLUJO DE TRABAJOS ---

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

            JobDetailScreen(
                jobId = jobId,
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() },
                goToApplicants = { id ->
                    navController.navigate("applicants/$id")
                }
            )
        }

        composable(
            route = "applicants/{jobId}",
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            val contextoApplicants = LocalContext.current

            val applicantsViewModel = remember {
                val repository = JobRepositoryImpl(contextoApplicants)
                val useCase = GetApplicantsUseCase(repository)
                JobApplicantsViewModel(useCase)
            }

            JobApplicantsScreen(
                jobId = jobId,
                jobTitle = "Lista de Postulantes",
                viewModel = applicantsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("home") {
            val contextoHome = LocalContext.current
            val homeViewModel = remember {
                HomeViewModel(JobRepositoryImpl(contextoHome))
            }

            com.example.tasky.ui.HomeScreen(
                viewModel = homeViewModel,
                onNavigateToCreateJob = { navController.navigate("create_job") },
                onNavigateToProfile = { navController.navigate("profile") },
                onJobClick = { jobId -> navController.navigate("job_detail/$jobId") }
            )
        }

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
                        onSuccess = { navController.popBackStack() }
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