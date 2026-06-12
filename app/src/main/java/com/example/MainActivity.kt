package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuraViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val auraViewModel: AuraViewModel = viewModel()
                val navController = rememberNavController()

                val userMode by auraViewModel.userMode.collectAsState()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Intelligent dynamic Navigation Bar with dual mode considerations
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .navigationBarsPadding()
                        ) {
                            // Dual-sided Workspace toggle floating header (Feature 45 switcher)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Active Realm Workspace",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (userMode == "Buyer") MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .clickable {
                                                auraViewModel.setUserMode("Buyer")
                                                navController.navigate("discover") {
                                                    popUpTo(0)
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Collector",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (userMode == "Buyer") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (userMode == "Artist") MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .clickable {
                                                auraViewModel.setUserMode("Artist")
                                                navController.navigate("studio") {
                                                    popUpTo(0)
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Artist Studio",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (userMode == "Artist") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Horizontal Navigation Items Row based on active mode context
                            NavigationBar(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp
                            ) {
                                if (userMode == "Buyer") {
                                    NavigationBarItem(
                                        selected = currentRoute == "discover",
                                        onClick = { navController.navigate("discover") { launchSingleTop = true } },
                                        icon = { Icon(imageVector = if (currentRoute == "discover") Icons.Default.Explore else Icons.Default.Explore, contentDescription = "Feed") },
                                        label = { Text("Discover", fontSize = 10.sp) }
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == "search",
                                        onClick = { navController.navigate("search") { launchSingleTop = true } },
                                        icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                                        label = { Text("Aesthetics", fontSize = 10.sp) }
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == "auctions",
                                        onClick = { navController.navigate("auctions") { launchSingleTop = true } },
                                        icon = { Icon(imageVector = Icons.Default.Gavel, contentDescription = "Auctions") },
                                        label = { Text("Bidding", fontSize = 10.sp) }
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == "cart",
                                        onClick = { navController.navigate("cart") { launchSingleTop = true } },
                                        icon = { Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Cart") },
                                        label = { Text("Checkout", fontSize = 10.sp) }
                                    )
                                } else {
                                    // Custom Artist Navigation panel
                                    NavigationBarItem(
                                        selected = currentRoute == "studio",
                                        onClick = { navController.navigate("studio") { launchSingleTop = true } },
                                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Studio Hub") },
                                        label = { Text("Console", fontSize = 10.sp) }
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == "discover",
                                        onClick = { navController.navigate("discover") { launchSingleTop = true } },
                                        icon = { Icon(imageVector = Icons.Default.Preview, contentDescription = "View Feed") },
                                        label = { Text("Exit Studio", fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "discover",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable("discover") {
                            DiscoverFeedScreen(
                                viewModel = auraViewModel,
                                onNavigateToDetail = { artworkId ->
                                    navController.navigate("detail/$artworkId")
                                },
                                onNavigateToAuctions = {
                                    navController.navigate("auctions")
                                }
                            )
                        }

                        composable("search") {
                            SearchDiscoveryScreen(viewModel = auraViewModel)
                        }

                        composable("auctions") {
                            AuctionHouseScreen(
                                viewModel = auraViewModel,
                                onNavigateToDetail = { artworkId ->
                                    navController.navigate("detail/$artworkId")
                                }
                            )
                        }

                        composable("cart") {
                            CheckoutCartScreen(
                                viewModel = auraViewModel,
                                onNavigateBack = {
                                    navController.navigateUp()
                                }
                            )
                        }

                        composable("studio") {
                            ArtistStudioScreen(viewModel = auraViewModel)
                        }

                        composable(
                            route = "detail/{artworkId}",
                            arguments = listOf(navArgument("artworkId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val artId = backStackEntry.arguments?.getString("artworkId") ?: ""
                            ArtworkDetailScreen(
                                artworkId = artId,
                                viewModel = auraViewModel,
                                onNavigateBack = {
                                    navController.navigateUp()
                                },
                                onNavigateToCart = {
                                    navController.navigate("cart")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
