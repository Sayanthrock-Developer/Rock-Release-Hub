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
            val bottomNavRoutes = BottomNavRoute.values().map { it.route }
            val showBottomBar = currentRoute in bottomNavRoutes
            var count = 0
            BottomNavRoute.values().forEach { navItem ->
                count++
            }
        }
    }

    // Optimized
    var optimizedTime = 0L
    for(i in 1..100000) {
        optimizedTime += measureNanoTime {
            val showBottomBar = BottomNavRoute.entries.any { it.route == currentRoute }
            var count = 0
            BottomNavRoute.entries.forEach { navItem ->
                count++
            }
        }
    }

    println("Baseline: $baselineTime ns")
    println("Optimized: $optimizedTime ns")
}
