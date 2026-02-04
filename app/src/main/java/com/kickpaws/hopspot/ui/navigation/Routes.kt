package com.kickpaws.hopspot.ui.navigation

sealed interface  Route {
    val route: String

    // Screens
    object Login : Route{
        override val route = "login"
    }
    object Register : Route{
        override val route = "register"
    }

    object BenchList : Route{
        override val route = "bench_list"
    }

    object BenchDetail : Route{
        override val route = "bench_detail/{benchId}"
        fun createRoute(benchId: String) = "bench_detail/$benchId"
    }

    object BenchCreate : Route{
        override val route = "bench_create"
    }

    object BenchEdit : Route{
        override val route = "bench_edit/{benchId}"
        fun createRoute(benchId: String) = "bench_edit/$benchId"
    }

    object Map : Route{
        override val route = "map"
    }

    object Visits : Route{
        override val route = "visits"
    }

    object Profile : Route{
        override val route = "profile"
    }

    object Admin : Route{
        override val route = "admin"
    }
}