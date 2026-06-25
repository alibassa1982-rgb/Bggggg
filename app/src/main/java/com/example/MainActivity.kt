package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppDatabase
import com.example.data.MineModRepository
import com.example.ui.CreateEditScreen
import com.example.ui.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MineModViewModel
import com.example.viewmodel.MineModViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable true edge-to-edge drawing
        enableEdgeToEdge()

        // Initialize Local Database Persistence
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MineModRepository(database.mineModDao())
        
        // ViewModel Injection with simple factory
        val viewModel: MineModViewModel by viewModels {
            MineModViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        // 1. Home list page of all custom items
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToCreate = {
                                    navController.navigate("create")
                                },
                                onNavigateToEdit = { modId ->
                                    navController.navigate("edit/$modId")
                                }
                            )
                        }

                        // 2. New creation form
                        composable("create") {
                            CreateEditScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 3. Edit pre-filled form
                        composable(
                            route = "edit/{modId}",
                            arguments = listOf(
                                navArgument("modId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val modId = backStackEntry.arguments?.getInt("modId") ?: 0
                            
                            // Query & pre-fill fields when entering the screen
                            LaunchedEffect(modId) {
                                val mod = repository.getModById(modId)
                                viewModel.resetForm(mod)
                            }

                            CreateEditScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
