package com.kickpaws.hopspot.ui.navigation

sealed interface  Route {
    val route: String

    // Screens
    object Splash : Route {
        override val route = "splash"
    }

    object Login : Route{
        override val route = "login"
    }
    object Register : Route{
        override val route = "register"
    }

    object SpotList : Route{
        override val route = "spot_list"
    }

    object SpotDetail : Route{
        override val route = "spot_detail/{spotId}"
        fun createRoute(spotId: String) = "spot_detail/$spotId"
    }

    object SpotCreate : Route{
        override val route = "spot_create"
    }

    object SpotEdit : Route{
        override val route = "spot_edit/{spotId}"
        fun createRoute(spotId: String) = "spot_edit/$spotId"
    }

    object Map : Route{
        override val route = "map"
    }

    object Visits : Route{
        override val route = "visits"
    }

    object Favorites : Route{
        override val route = "favorites"
    }

    object Profile : Route{
        override val route = "profile"
    }

    object Admin : Route{
        override val route = "admin"
    }

    object ActivityFeed : Route{
        override val route = "activity_feed"
    }
}
