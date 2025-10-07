package com.example.swoptrader.ui.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    object Login : Screen("login")
    
    object Home : Screen("home")
    
    object Profile : Screen("profile")
    
    object Settings : Screen("settings")
    
    object ListItem : Screen("list_item")
    
    object ViewItem : Screen(
        route = "view_item/{itemId}",
        arguments = listOf(
            navArgument("itemId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(itemId: String) = "view_item/$itemId"
    }
    
    object ChatList : Screen("chat_list")
    
    object Chat : Screen(
        route = "chat/{chatId}",
        arguments = listOf(
            navArgument("chatId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(chatId: String) = "chat/$chatId"
    }
    
    object Meetup : Screen(
        route = "meetup/{meetupId}",
        arguments = listOf(
            navArgument("meetupId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(meetupId: String) = "meetup/$meetupId"
    }
    
    object PitchOffer : Screen(
        route = "pitch_offer/{itemId}",
        arguments = listOf(
            navArgument("itemId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(itemId: String) = "pitch_offer/$itemId"
    }
    
    object OfferDetails : Screen(
        route = "offer_details/{offerId}",
        arguments = listOf(
            navArgument("offerId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(offerId: String) = "offer_details/$offerId"
    }
    
    object FirestoreTest : Screen("firestore_test")
}

