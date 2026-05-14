package com.cocido.nonna.ui.navigation

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavHostController
import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.compose.ui.platform.LocalContext
import com.cocido.nonna.ui.components.NonnaMotion
import com.cocido.nonna.ui.components.NonnaTab
import com.cocido.nonna.ui.screens.auth.AuthScreen
import com.cocido.nonna.ui.screens.auth.ForgotPasswordScreen
import com.cocido.nonna.ui.screens.auth.VerifyEmailScreen
import com.cocido.nonna.ui.screens.cofres.CofreDetailScreen
import com.cocido.nonna.ui.screens.cofres.CofresListRoute
import com.cocido.nonna.ui.screens.cofres.CreateCofreScreen
import com.cocido.nonna.ui.screens.cofres.EditCofreScreen
import com.cocido.nonna.ui.screens.home.HomeRoute
import com.cocido.nonna.ui.screens.memory.AddMemoryScreen
import com.cocido.nonna.ui.screens.memory.EditMemoryScreen
import com.cocido.nonna.ui.screens.memory.MemoryDetailScreen
import com.cocido.nonna.ui.screens.memory.SelectCofreScreen
import com.cocido.nonna.ui.screens.onboarding.OnboardingScreen
import com.cocido.nonna.ui.screens.profile.ProfileRoute
import com.cocido.nonna.ui.screens.profile.ProfileSettingsScreen
import com.cocido.nonna.ui.screens.profile.InvitationsScreen
import com.cocido.nonna.ui.screens.profile.SubscriptionCenterScreen
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
    data object ForgotPassword : Screen("forgot-password")
    data object VerifyEmail : Screen("verify-email")
    data object Onboarding : Screen("onboarding")
    
    // Main tabs
    data object Home : Screen("home")
    data object Cofres : Screen("cofres")
    data object FamilyTree : Screen("tree")
    data object Profile : Screen("profile")
    data object ProfileSettings : Screen("profile/settings")
    data object ProfileEdit : Screen("profile/edit")
    data object SubscriptionCenter : Screen(
        "profile/subscription?status={status}&collectionStatus={collectionStatus}&paymentId={paymentId}"
    ) {
        fun createRoute(
            status: String? = null,
            collectionStatus: String? = null,
            paymentId: String? = null
        ): String {
            val s = Uri.encode(status.orEmpty())
            val cs = Uri.encode(collectionStatus.orEmpty())
            val pid = Uri.encode(paymentId.orEmpty())
            return "profile/subscription?status=$s&collectionStatus=$cs&paymentId=$pid"
        }
    }
    data object Invitations : Screen("profile/invitations?invitationId={invitationId}") {
        fun createRoute(invitationId: String? = null): String =
            if (invitationId.isNullOrBlank()) {
                "profile/invitations"
            } else {
                "profile/invitations?invitationId=$invitationId"
            }
    }
    
    // Detail screens
    data object CofreDetail : Screen("cofre/{cofreId}") {
        fun createRoute(cofreId: String) = "cofre/$cofreId"
    }
    data object MemoryDetail : Screen("memory/{memoryId}?cofreContext={cofreContext}&cofreCreator={cofreCreator}") {
        fun createRoute(
            memoryId: String,
            cofreContext: String? = null,
            cofreCreator: String? = null
        ): String {
            val ctx = Uri.encode(cofreContext.orEmpty())
            val creator = Uri.encode(cofreCreator.orEmpty())
            return "memory/$memoryId?cofreContext=$ctx&cofreCreator=$creator"
        }
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
    startDestination: String? = null,
    onEmailVerified: () -> Unit = {}
) {
    val context = LocalContext.current
    val homePrefs = remember(context) {
        context.getSharedPreferences("nonna_home_hints", android.content.Context.MODE_PRIVATE)
    }
    val navigateToCofreDetail: (String) -> Unit = { cofreId ->
        homePrefs.edit().putString("last_viewed_cofre_id", cofreId).apply()
        navController.navigate(Screen.CofreDetail.createRoute(cofreId))
    }

    val effectiveStartDestination = startDestination ?: if (isLoggedIn) Screen.Home.route else Screen.Welcome.route

    val activity = LocalContext.current as? ComponentActivity
    LaunchedEffect(navController, activity, effectiveStartDestination) {
        val intent: Intent = activity?.intent ?: return@LaunchedEffect
        val data = intent.data
        if (intent.action == Intent.ACTION_VIEW && data != null) {
            val handledByGraph = runCatching { navController.handleDeepLink(intent) }.getOrDefault(false)
            if (!handledByGraph) {
                val host = data.host.orEmpty()
                val path = data.path.orEmpty()
                val isPaymentsCallback =
                    (host == "apinonna.pushsoftware.com.ar" && path.contains("/pagos-suscripcion")) ||
                        (data.scheme == "nonna" && host == "pagos-suscripcion")
                if (isPaymentsCallback) {
                    val status = data.getQueryParameter("status")
                    val collectionStatus = data.getQueryParameter("collectionStatus")
                        ?: data.getQueryParameter("collection_status")
                    val paymentId = data.getQueryParameter("paymentId")
                        ?: data.getQueryParameter("payment_id")
                    navController.navigate(
                        Screen.SubscriptionCenter.createRoute(
                            status = status,
                            collectionStatus = collectionStatus,
                            paymentId = paymentId
                        )
                    )
                }
            }
        }
    }

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
                onForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onAuth = { user ->
                    val route = if (user.isEmailVerified()) Screen.Home.route else Screen.VerifyEmail.route
                    navController.navigate(route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onCompleted = { navController.popBackStack() }
            )
        }

        composable(Screen.VerifyEmail.route) {
            VerifyEmailScreen(
                onVerified = {
                    onEmailVerified()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.VerifyEmail.route) { inclusive = true }
                    }
                },
                onLogout = {
                    onLogout()
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
            HomeRoute(
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
                    navigateToCofreDetail(cofreId)
                },
                onOpenInvitations = { navController.navigate(Screen.Invitations.createRoute()) }
            )
        }
        
        // Cofres List Screen
        composable(Screen.Cofres.route) {
            CofresListRoute(
                onTabSelected = { tab ->
                    when (tab) {
                        NonnaTab.Inicio -> navController.navigate(Screen.Home.route)
                        NonnaTab.Cofres -> { /* Already here */ }
                        NonnaTab.Perfil -> navController.navigate(Screen.Profile.route)
                    }
                },
                onCofreClick = { cofreId ->
                    navigateToCofreDetail(cofreId)
                },
                onCreateCofre = { navController.navigate(Screen.CreateCofre.route) }
            )
        }
        
        // Family Tree Screen
        composable(Screen.FamilyTree.route) {
            val viewModel: FamilyTreeViewModel = hiltViewModel()
            val uiState by viewModel.state.collectAsStateWithLifecycle()

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
                        navigateToCofreDetail(cofreId)
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
            ProfileRoute(
                onTabSelected = { tab ->
                    when (tab) {
                        NonnaTab.Inicio -> navController.navigate(Screen.Home.route)
                        NonnaTab.Cofres -> navController.navigate(Screen.Cofres.route)
                        NonnaTab.Perfil -> { /* Already here */ }
                    }
                },
                onEditProfile = { navController.navigate(Screen.ProfileEdit.route) },
                onOpenSubscriptionCenter = { navController.navigate(Screen.SubscriptionCenter.createRoute()) },
                onOpenInvitations = { navController.navigate(Screen.Invitations.createRoute()) },
                onLogout = {
                    onLogout()
                    // key(authState) en MainActivity recrea el NavHost con startDestination=Welcome
                }
            )
        }

        composable(
            route = Screen.Invitations.route,
            arguments = listOf(
                navArgument("invitationId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://apinonna.pushsoftware.com.ar/invitaciones/{invitationId}"
                },
                navDeepLink {
                    uriPattern = "nonna://invitaciones/{invitationId}"
                }
            )
        ) { backStackEntry ->
            val invitationId = backStackEntry.arguments?.getString("invitationId")
            InvitationsScreen(
                onBack = { navController.popBackStack() },
                deepLinkedInvitationId = invitationId
            )
        }

        // Profile Edit Screen
        composable(Screen.ProfileEdit.route) {
            ProfileSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SubscriptionCenter.route,
            arguments = listOf(
                navArgument("status") { type = NavType.StringType; defaultValue = "" },
                navArgument("collectionStatus") { type = NavType.StringType; defaultValue = "" },
                navArgument("paymentId") { type = NavType.StringType; defaultValue = "" }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "nonna://pagos-suscripcion/callback?status={status}&collectionStatus={collectionStatus}&paymentId={paymentId}"
                },
                navDeepLink {
                    uriPattern = "nonna://pagos-suscripcion/callback?status={status}&collection_status={collectionStatus}&payment_id={paymentId}"
                },
                navDeepLink {
                    uriPattern = "https://apinonna.pushsoftware.com.ar/pagos-suscripcion/callback?status={status}&collection_status={collectionStatus}&payment_id={paymentId}"
                }
            )
        ) { backStackEntry ->
            val status = backStackEntry.arguments?.getString("status")?.takeIf { it.isNotBlank() }
            val collectionStatus = backStackEntry.arguments?.getString("collectionStatus")?.takeIf { it.isNotBlank() }
            val paymentId = backStackEntry.arguments?.getString("paymentId")?.takeIf { it.isNotBlank() }
            SubscriptionCenterScreen(
                onBack = { navController.popBackStack() },
                checkoutStatus = status,
                checkoutCollectionStatus = collectionStatus,
                checkoutPaymentId = paymentId
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
                onMemoryClick = { memoryId, cofreName, cofreCreator ->
                    navController.navigate(
                        Screen.MemoryDetail.createRoute(memoryId, cofreName, cofreCreator)
                    )
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
                navArgument("memoryId") { type = NavType.StringType },
                navArgument("cofreContext") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("cofreCreator") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val memoryId = backStackEntry.arguments?.getString("memoryId") ?: ""
            val cofreContext = backStackEntry.arguments?.getString("cofreContext")
                ?.takeIf { it.isNotBlank() }
            val cofreCreator = backStackEntry.arguments?.getString("cofreCreator")
                ?.takeIf { it.isNotBlank() }
            MemoryDetailScreen(
                memoryId = memoryId,
                cofreContextName = cofreContext,
                cofreCreatorDisplayName = cofreCreator,
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
