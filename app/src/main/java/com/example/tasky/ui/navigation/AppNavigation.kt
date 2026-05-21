package com.example.tasky.ui.navigation

import android.widget.Toast
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
import com.example.tasky.data.remote.SupabaseClient
import com.example.tasky.data.repository.UserRepositoryImpl
import com.example.tasky.domain.model.GestorDeSacudidas
import com.example.tasky.domain.usecase.AcceptApplicantUseCase
import com.example.tasky.ui.jobs.detail.WorkerProfileScreen
import com.example.tasky.ui.jobs.detail.WorkerProfileViewModel
import com.example.tasky.ui.profile.UserProfileRoute
import com.example.tasky.ui.profile.UserProfileViewModel
import com.example.tasky.domain.usecase.GetUserProfileUseCase
import com.example.tasky.domain.usecase.GetWorkerProfileUseCase
import com.example.tasky.domain.usecase.UpdateUserProfileUseCase
import com.example.tasky.ui.report.ReportScreen
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Aca se define como es que va a ejecutarse todo lo del sensor de movimiento
    GestorDeSacudidas(
        onShake = {
            // Se extrae la ruta actual para saber dónde chingados está el usuario
            val rutaActual = navController.currentDestination?.route

            val pantallasBloqueadas = listOf("login", "register", "forgot_password", "pantalla_reporte")

            if (rutaActual !in pantallasBloqueadas) {
                val usuarioLogueado = SupabaseClient.client.auth.currentUserOrNull()

                if (usuarioLogueado != null) {
                    navController.navigate("pantalla_reporte")
                }
            }
        }
    )


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
                    Toast.makeText(
                        context,
                        "¡Bienvenido ${state.user?.name}!",
                        Toast.LENGTH_SHORT
                    ).show()

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
                    Toast.makeText(
                        context,
                        "¡Registro exitoso! Ahora puedes iniciar sesión",
                        Toast.LENGTH_SHORT
                    ).show()

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
                    Toast.makeText(
                        context,
                        "Contraseña cambiada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
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
                },
                onEditClick = {
                    navController.navigate("edit_job/$jobId") // Brinca a editar
                },
                onDeleteSuccess = {
                    Toast.makeText(contextoDetalle, "Tarea Eliminada", Toast.LENGTH_SHORT).show()
                    navController.popBackStack() // Saca al wey a la pantalla anterior
                }
            )
        }

        // --- RUTA PARA EDITAR TRABAJO ---
        composable(
            route = "edit_job/{jobId}",
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            val contexto = LocalContext.current

            val createViewModel = remember { CreateJobViewModel(JobRepositoryImpl(contexto)) }

            CreateJobScreen(
                viewModel = createViewModel,
                jobIdToEdit = jobId, // Se le inyecta el ID para que sepa que es edición
                onNavigateBack = { navController.popBackStack() },
                onJobSaved = { mensajeToast ->
                    // Se recicla el Toast tanto para creación como para edición
                    Toast.makeText(contexto, mensajeToast, Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            )
        }

        // Lista de postulantes
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
                onNavigateBack = { navController.popBackStack() },
                onApplicantClick = { workerId ->
                    navController.navigate("worker_profile/$workerId/$jobId")
                }
            )
        }

        // Detalles del trabajador seleccionado
        composable(
            route = "worker_profile/{workerId}/{jobId}",
            arguments = listOf(
                navArgument("workerId") { type = NavType.StringType },
                navArgument("jobId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val workerId = backStackEntry.arguments?.getString("workerId") ?: ""
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            val contexto = LocalContext.current

            val profileViewModel = remember {
                val repository = JobRepositoryImpl(contexto)
                val getProfileUC = GetWorkerProfileUseCase(repository)
                val acceptUC = AcceptApplicantUseCase(repository)
                WorkerProfileViewModel(getProfileUC, acceptUC) // Le pasamos ambos
            }

            WorkerProfileScreen(
                workerId = workerId,
                jobId = jobId,
                viewModel = profileViewModel,
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
                jobIdToEdit = null, // Se indica nulo al tratarse de una creación
                onNavigateBack = { navController.popBackStack() },
                onJobSaved = { mensaje ->
                    // Se lanza el madrazo visual y se regresa a la pantalla anterior
                    Toast.makeText(
                        contexto,
                        mensaje,
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.popBackStack()
                }
            )
        }


        composable("profile") {
            val contexto = LocalContext.current
            val viewModel = remember {
                val authRepository = AuthRepositoryImpl()
                val jobRepository = JobRepositoryImpl(contexto)
                val userRepository = UserRepositoryImpl()
                val updateProfileUseCase = UpdateUserProfileUseCase(userRepository)
                val getProfileUseCase = GetUserProfileUseCase(authRepository)

                UserProfileViewModel(
                    getUserProfileUseCase = getProfileUseCase,
                    jobRepository = jobRepository,
                    updateUserProfileUseCase = updateProfileUseCase
                )
            }

            viewModel.setOnSaveSuccessCallback {
                Toast.makeText(contexto, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
            }

            viewModel.setOnSaveErrorCallback { errorMsg ->
                Toast.makeText(contexto, "Error: $errorMsg", Toast.LENGTH_SHORT).show()
            }

            UserProfileRoute(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditProfile = {
                    viewModel.toggleEditing()
                },
                onJobClick = { jobId ->
                    navController.navigate("job_detail/$jobId")
                }
            )
        }

        // Formulario de Reporte
        composable("pantalla_reporte") {
            val contexto = LocalContext.current
            val scope = rememberCoroutineScope()

            val jobRepository = remember { JobRepositoryImpl(contexto) }

            ReportScreen(
                onBack = { navController.popBackStack() },
                onEnviar = { textoReporte ->
                    scope.launch {
                        try {
                            val user = SupabaseClient.client.auth.currentUserOrNull()

                            if (user != null) {
                                jobRepository.enviarReporte(user.id, textoReporte)
                                Toast.makeText(contexto, "Reporte enviado exitosamente", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(contexto, "Inicia sesión primero para reportar", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(contexto, "Error en el reporte: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            // Se saca al usuario de la pantalla de reportes a huevo
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
    }
}