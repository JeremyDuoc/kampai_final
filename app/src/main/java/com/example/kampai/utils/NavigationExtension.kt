package com.example.kampai.utils

import androidx.navigation.NavController

/**
 * Extensión para navegar el bug de pantalla negra al hacer doble click al volver atrás
 */
fun NavController.navigateSafe(route: String) {
    if (currentBackStackEntry?.destination?.route != route) {
        try {
            navigate(route)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * Extensión para hacer popBackStack de forma segura
 */
fun NavController.popBackStackSafe(): Boolean {
    return try {
        if (previousBackStackEntry != null) {
            popBackStack()
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}