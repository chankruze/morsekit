plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    idea
}

// The landing page (web/) is a separate npm project. Android Studio applies these excludes on
// every Gradle sync, so it never indexes the JavaScript dependencies or the site's build output.
idea {
    module {
        excludeDirs.addAll(listOf(file("web/node_modules"), file("web/dist")))
    }
}
