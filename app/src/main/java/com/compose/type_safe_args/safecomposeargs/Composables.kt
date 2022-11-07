package com.compose.type_safe_args.safecomposeargs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun DemoScreen() {
    val navController = rememberNavController()
    val graph = remember(navController) {
        NavigationGraph(navController)
    }

    NavHost(
        navController = navController,
        startDestination = HomePage.getDestination()
    ) {
        composable(
            route = HomePage.route,
            arguments = HomePage.argumentList
        ) { backStackEntry ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "This is home page", textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            graph.openUserPage(
                                null,
                                false,
                                intArrayOf(1, 2, 5),
                                arrayListOf("user1, user2"),
                                arrayListOf(
                                    User(22, "user22"),
                                    User(33, "User33")
                                )
                            )
                        }
                    ) {
                        Text(text = "Go to user page", textAlign = TextAlign.Center)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { graph.openEndScreen("This is end screen") }) {
                        Text(text = "Go to end screen page", textAlign = TextAlign.Center)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { graph.openTncPage() }) {
                        Text(text = "Go to tnc page", textAlign = TextAlign.Center)
                    }
                }
            }
        }

        composable(
            route = UserPage.route,
            arguments = UserPage.argumentList
        ) { backStackEntry ->
            val (userId, isLoggedIn, userIds, uniqueUser, uniqueUsers, userNames, phone) = remember {
                UserPage.parseArguments(backStackEntry)
            }
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "This is user page with userId: $userId",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Is user logged in $isLoggedIn", textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "User ids is ${userIds.joinToString { it.toString() }}",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "User names is ${userNames.joinToString { it }}",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Unique User is ${uniqueUser?.id} ${uniqueUser?.name}",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Unique Users are ${uniqueUsers.joinToString(separator = " -- ") { it.id.toString() + it.name }}",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Phone number is $phone",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        composable(
            route = EndScreen.route,
            arguments = EndScreen.argumentList
        ) { backStackEntry ->
            val (endScreenText) = EndScreen.parseArguments(backStackEntry)
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "This is end page with endText: $endScreenText",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        composable(
            route = TncPage.route,
            arguments = TncPage.argumentList
        ) { backStackEntry ->
            val (tncUrl) = TncPage.parseArguments(backStackEntry)
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "This is tnc page with tncUrl: $tncUrl",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
