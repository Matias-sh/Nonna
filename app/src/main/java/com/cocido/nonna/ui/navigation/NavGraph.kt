package com.cocido.nonna.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cocido.nonna.ui.components.NonnaMotion
import com.cocido.nonna.ui.components.NonnaTab
import com.cocido.nonna.ui.screens.auth.AuthScreen
import com.cocido.nonna.ui.screens.cofres.CofreDetailScreen
import com.cocido.nonna.ui.screens.cofres.CofresListScreen
import com.cocido.nonna.ui.screens.cofres.CreateCofreScreen
import com.cocido.nonna.ui.screens.cofres.EditCofreScreen
import com.cocido.nonna.ui.screens.home.HomeScreen
import com.cocido.nonna.ui.screens.memory.AddMemoryScreen
import com.cocido.nonna.ui.screens.memory.EditMemoryScreen
import com.cocido.nonna.ui.screens.memory.MemoryDetailScreen
import com.cocido.nonna.ui.screens.memory.SelectCofreScreen
import com.cocido.nonna.ui.screens.onboarding.OnboardingScreen
import com.cocido.nonna.ui.screens.profile.ProfileScreen
import com.cocido.nonna.ui.screens.profile.ProfileSettingsScreen
import com.cocido.nonna.ui.screens.tree.AddPersonScreen
import com.cocido.nonna.ui.screens.tree.FamilyTreeScreen
import com.cocido.nonna.ui.screens.welcome.WelcomeScreen
import com.cocido.nonna.ui.viewmodel.FamilyTreeViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

sealed class Screen(val route: String) {
    // Auth flow
    data object Welcome : Screen("welcome")
    data object Auth : Screen("auth/{mode}") {
        fun createRoute(mode: String) = "auth/$mode"
    }
    data object Onboarding : Screen("onboarding")
    
    // Main tabs
    data object Home : Screen("home")
    data object Cofres : Screen("cofres")
    data object FamilyTree : Screen("tree")
    data object Profile : Screen("profile")
    data object ProfileSettings : Screen("profile/settings")
    
    // Detail screens
    data object CofreDetail : Screen("cofre/{cofreId}") {
        fun createRoute(cofreId: String) = "cofre/$cofreId"
    }
    data object MemoryDetail : Screen("memory/{memoryId}") {
        fun createRoute(memoryId: String) = "memory/$memoryId"
    }
    data object EditMemory : Screen("memory/{memoryId}/edit") {
        fun createRoute(memoryId: String) = "memory/$memoryId/edit"
    }
    
    // Creation screens
    data object CreateCofre : Screen("create-cofre")
    data object EditCofre : Screen("cofre/{cofreId}/edit") {
        fun createRoute(cofreId: String) = "cofre/$cofreId/edit"
    }
    data object AddMemory : Screen("add-memory?cofreId={cofreId}") {
        fun createRoute(cofreId: String? = null) = if (cofreId != null) {
            "add-memory?cofreId=$cofreId"
        } else {
            "add-memory"
        }
    }
    data object SelectCofre : Screen("select-cofre")
    data object AddPerson : Screen("add-person")
}

@Composable
fun NonnaNavHost(
    navController: NavHostController = rememberNavController(),
    isLoggedIn: Boolean = false,
    onLogout: () -> Unit = {},
    startDestination: String? = null
) {
    val effectiveStartDestination = startDestination ?: if (isLoggedIn) Screen.Home.route else Screen.Welcome.route

    NavHost(
        navController = navController,
        startDestination = effectiveStartDestination,
        enterTransition = {
            fadeIn(animationSpec = NonnaMotion.screenFadeIn) + scaleIn(
                initialScale = 0.985f,
                animationSpec = NonnaMotion.navSpring
            )
        },
        exitTransition = {
            fadeOut(animationSpec = NonnaMotion.screenFadeOut) + scaleOut(
                targetScale = 0.995f,
                animationSpec = NonnaMotion.navSpring
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = NonnaMotion.screenFadeIn) + scaleIn(
                initialScale = 0.99f,
                animationSpec = NonnaMotion.navSpring
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = NonnaMotion.screenFadeOut) + scaleOut(
                targetScale = 0.995f,
                animationSpec = NonnaMotion.navSpring
            )
        }
    ) {
        // Welcome Screen
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onCreateCofre = {
                    navController.navigate(Screen.Auth.createRoute("signup"))
                },
                onLogin = {
                    navController.navigate(Screen.Auth.createRoute("login"))
                }
            )
        }
        
        // Auth Screen
        composable(
            route = Screen.Auth.route,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "login"
            AuthScreen(
                mode = if (mode == "login") AuthMode.Login else AuthMode.Signup,
                onBack = { navController.popBackStack() },
                onAuth = { _, _ ->
                    // Entrar a la app (Home) y limpiar pila para no volver a Welcome/Auth
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Onboarding Screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                onTabSelected = { tab ->
                    when (tab) {
                        NonnaTab.Inicio -> { /* Already here */ }
                        NonnaTab.Cofres -> navController.navigate(Screen.Cofres.route)
                        NonnaTab.Perfil -> navController.navigate(Screen.Profile.route)
                    }
                },
                onCreateCofre = { navController.navigate(Screen.CreateCofre.route) },
                onAddMemory = { navController.navigate(Screen.SelectCofre.route) },
                onContinueCofre = { cofreId ->
                    navController.navigate(Screen.CofreDetail.createRoute(cofreId))
                }
            )
        }
        
        // Cofres List Screen
        composable(Screen.Cofres.route) {
            CofresListScreen(
                onTabSelected = { tab ->
                    when (tab) {
                        NonnaTab.Inicio -> navController.navigate(Screen.Home.route)
                        NonnaTab.Cofres -> { /* Already here */ }
                        NonnaTab.Perfil -> navController.navigate(Screen.Profile.route)
                    }
                },
                onCofreClick = { cofreId ->
                    navController.navigate(Screen.CofreDetail.createRoute(cofreId))
                },
                onCreateCofre = { navController.navigate(Screen.CreateCofre.route) }
            )
        }
        
        // Family Tree Screen
        composable(Screen.FamilyTree.route) {
            val viewModel: FamilyTreeViewModel = hiltViewModel()
            val uiState = viewModel.state.collectAsStateWithLifecycle().value

            FamilyTreeScreen(
                onTabSelected = { tab ->
                    when (tab) {
                        NonnaTab.Inicio -> navController.navigate(Screen.Home.route)
                        NonnaTab.Cofres -> navController.navigate(Screen.Cofres.route)
                        NonnaTab.Perfil -> navController.navigate(Screen.Profile.route)
                    }
                },
                onNodeClick = { nodeId, cofreId ->
                    if (cofreId != null) {
                        navController.navigate(Screen.CofreDetail.createRoute(cofreId))
                    }
                },
                onAddNode = { navController.navigate(Screen.AddPerson.route) },
                nodes = uiState.nodes,
                isLoading = uiState.isLoading
            )
        }
        
        // Add Person Screen
        composable(Screen.AddPerson.route) {
            // Compartimos el mismo ViewModel del árbol familiar para que
            // tenga acceso al estado actual y pueda refrescar luego.
            val parentEntry = remember(navController) {
                navController.getBackStackEntry(Screen.FamilyTree.route)
            }
            val familyTreeViewModel: FamilyTreeViewModel = hiltViewModel(parentEntry)
            val existingMembers = familyTreeViewModel.getAllPersonNames()

            AddPersonScreen(
                onBack = { navController.popBackStack() },
                onAddPerson = { name, relation, birthDate, deathDate, notes, createCofre ->
                    familyTreeViewModel.addPerson(
                        fullName = name,
                        selectedRelationName = relation,
                        birthDate = birthDate,
                        deathDate = deathDate,
                        notes = notes,
                        createCofre = createCofre
                    ) { success, _ ->
                        if (success) {
                            // Volvemos al árbol; el backend ya se encarga de crear el cofre
                            // cuando createCofre = true.
                            navController.popBackStack()
                        }
                    }
                },
                existingMembers = existingMembers
            )
        }
        
        // Profile Screen
        composable(Screen.Profile.route) {
            ProfileScreen(
                onTabSelected = { tab ->
                    when (tab) {
                        NonnaTab.Inicio -> navController.navigate(Screen.Home.route)
                        NonnaTab.Cofres -> navController.navigate(Screen.Cofres.route)
                        NonnaTab.Perfil -> { /* Already here */ }
                    }
                },
                onOpenSettings = { navController.navigate(Screen.ProfileSettings.route) },
                onLogout = {
                    onLogout()
                    // key(authState) en MainActivity recrea el NavHost con startDestination=Welcome
                }
            )
        }

        // Profile Settings Screen
        composable(Screen.ProfileSettings.route) {
            ProfileSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        // Cofre Detail Screen
        composable(
            route = Screen.CofreDetail.route,
            arguments = listOf(
                navArgument("cofreId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cofreId = backStackEntry.arguments?.getString("cofreId") ?: ""
            CofreDetailScreen(
                cofreId = cofreId,
                onBack = { navController.popBackStack() },
                onAddMemory = {
                    navController.navigate(Screen.AddMemory.createRoute(cofreId))
                },
                onMemoryClick = { memoryId ->
                    navController.navigate(Screen.MemoryDetail.createRoute(memoryId))
                },
                onEditCofre = { id ->
                    navController.navigate(Screen.EditCofre.createRoute(id))
                }
            )
        }

        // Edit Cofre Screen
        composable(
            route = Screen.EditCofre.route,
            arguments = listOf(
                navArgument("cofreId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cofreId = backStackEntry.arguments?.getString("cofreId") ?: ""
            EditCofreScreen(
                cofreId = cofreId,
                onBack = { navController.popBackStack() },
                onUpdated = { }
            )
        }
        
        // Create Cofre Screen
        composable(Screen.CreateCofre.route) {
            CreateCofreScreen(
                onBack = { navController.popBackStack() },
                onCreate = { _ ->
                    navController.popBackStack()
                }
            )
        }
        
        // Select Cofre Screen
        composable(Screen.SelectCofre.route) {
            SelectCofreScreen(
                onBack = { navController.popBackStack() },
                onCofreSelected = { cofreId ->
                    navController.navigate(Screen.AddMemory.createRoute(cofreId)) {
                        popUpTo(Screen.SelectCofre.route) { inclusive = true }
                    }
                },
                onCreateCofre = { navController.navigate(Screen.CreateCofre.route) }
            )
        }
        
        // Add Memory Screen
        composable(
            route = Screen.AddMemory.route,
            arguments = listOf(
                navArgument("cofreId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val cofreId = backStackEntry.arguments?.getString("cofreId")
            AddMemoryScreen(
                cofreId = cofreId,
                onBack = { navController.popBackStack() },
                onSave = {
                    navController.popBackStack()
                }
            )
        }
        
        // Memory Detail Screen
        composable(
            route = Screen.MemoryDetail.route,
            arguments = listOf(
                navArgument("memoryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val memoryId = backStackEntry.arguments?.getString("memoryId") ?: ""
            MemoryDetailScreen(
                memoryId = memoryId,
                onBack = { navController.popBackStack() },
                onDelete = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.EditMemory.createRoute(id)) }
            )
        }

        composable(
            route = Screen.EditMemory.route,
            arguments = listOf(
                navArgument("memoryId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val memoryId = backStackEntry.arguments?.getString("memoryId") ?: ""
            EditMemoryScreen(
                memoryId = memoryId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}

enum class AuthMode {
    Login, Signup
}
