package com.olapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.olapp.ui.screen.ContactScreen
import com.olapp.ui.screen.DiscoveryScreen
import com.olapp.ui.screen.MatchesScreen
import com.olapp.ui.screen.OlasScreen
import com.olapp.ui.screen.ProfileScreen
import com.olapp.ui.screen.SetupScreen

private data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val navItems = listOf(
    NavItem("discovery", "Nearby", Icons.Default.WifiTethering, Icons.Outlined.WifiTethering),
    NavItem("olas",      "Waves",  Icons.Default.MailOutline,   Icons.Outlined.MailOutline),
    NavItem("matches",   "Vibes",   Icons.Default.People,        Icons.Outlined.PeopleOutline),
    NavItem("profile",   "Profile", Icons.Default.Person,       Icons.Outlined.Person),
)

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    var editingProfile  by remember { mutableStateOf(false) }
    var showingContact  by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = androidx.compose.ui.unit.Dp.Unspecified
            ) {
                navItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "discovery",
            modifier = Modifier.padding(padding)
        ) {
            composable("discovery") { DiscoveryScreen() }
            composable("olas")      { OlasScreen() }
            composable("matches")   { MatchesScreen() }
            composable("profile") {
                when {
                    editingProfile -> SetupScreen(onSaved = { editingProfile = false })
                    showingContact -> ContactScreen(onBack = { showingContact = false })
                    else -> ProfileScreen(
                        onEdit = { editingProfile = true },
                        onContact = { showingContact = true }
                    )
                }
            }
        }
    }
}
