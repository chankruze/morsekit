package `in`.geekofia.morsekit

/**
 * Which icon colour the system bars need so they stay readable over the app's own colours.
 * The status bar sits over the top app bar; the navigation bar sits over the bottom navigation. Computed from the actual theme colours, so it's right for any theme.
 */
data class SystemBarAppearance(
    val lightStatusBarIcons: Boolean,
    val lightNavigationBarIcons: Boolean,
)
