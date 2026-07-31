import kotlin.system.measureNanoTime

enum class BottomNavRoute(val route: String) {
    HOME("home"),
    REPOSITORIES("repositories"),
    WORKFLOWS("workflows"),
    DOWNLOADS("downloads"),
    SETTINGS("settings")
}

fun main() {
    val currentRoute = "settings"

    // Baseline
    var baselineTime = 0L
    for(i in 1..100000) {
        baselineTime += measureNanoTime {
            val bottomNavRoutes = BottomNavRoute.entries.map { it.route }
            val showBottomBar = currentRoute in bottomNavRoutes
            val count = BottomNavRoute.entries.size
        }
    }

    // Optimized
    var optimizedTime = 0L
    for(i in 1..100000) {
        optimizedTime += measureNanoTime {
            val showBottomBar = BottomNavRoute.entries.any { it.route == currentRoute }
            val count = BottomNavRoute.entries.size
        }
    }

    println("Baseline: $baselineTime ns")
    println("Optimized: $optimizedTime ns")
}
