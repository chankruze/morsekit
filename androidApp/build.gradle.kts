import com.android.build.api.artifact.SingleArtifact
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

// --- Versioning -------------------------------------------------------------------------------
// version.properties (repo root) is shared with the iOS app via iosApp/Configuration/Config.xcconfig.
// Read through `providers` so the configuration cache is invalidated when the file changes.

val appName = "MorseKit"

private val semVerRegex = Regex("""(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)""")

private val versionProperties = Properties().apply {
    val file = rootProject.layout.projectDirectory.file("version.properties")
    load(providers.fileContents(file).asText.get().reader())
}

val appVersionName: String = versionProperties.getProperty("VERSION_NAME")?.trim()
    ?.takeIf(semVerRegex::matches)
    ?: error("version.properties: VERSION_NAME must be MAJOR.MINOR.PATCH (e.g. 0.0.1)")

val appVersionCode: Int = versionProperties.getProperty("VERSION_CODE")?.trim()?.toIntOrNull()
    ?.takeIf { it > 0 }
    ?: error("version.properties: VERSION_CODE must be a positive integer")

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "in.geekofia.morsekit"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "in.geekofia.morsekit"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            // Allows debug and release builds to be installed side by side.
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

// --- Named artifacts --------------------------------------------------------------------------
// After `assemble<Variant>` / `bundle<Variant>`, copy the APK / AAB to build/dist/ as
// `MorseKit-v<versionName>-<versionCode>-<variant>.<ext>`, e.g. MorseKit-v0.0.1-1-release.apk.
// AGP 9 removed the old `applicationVariants` rename hook, so this uses the public artifacts API.
// The variant name includes the product flavor automatically if flavors are added later.

androidComponents {
    onVariants { variant ->
        val taskSuffix = variant.name.replaceFirstChar { it.uppercase() }
        val baseName = "$appName-v$appVersionName-$appVersionCode-${variant.name}"
        val distDir = layout.buildDirectory.dir("dist")

        val copyApk = tasks.register<Copy>("copy${taskSuffix}NamedApk") {
            group = "distribution"
            description = "Copies the ${variant.name} APK to build/dist/$baseName.apk"
            // Assumes a single APK per variant (no ABI/density splits configured).
            from(variant.artifacts.get(SingleArtifact.APK)) {
                include("*.apk")
                rename { "$baseName.apk" }
            }
            into(distDir)
        }

        val copyBundle = tasks.register<Copy>("copy${taskSuffix}NamedBundle") {
            group = "distribution"
            description = "Copies the ${variant.name} AAB to build/dist/$baseName.aab"
            from(variant.artifacts.get(SingleArtifact.BUNDLE)) {
                rename { "$baseName.aab" }
            }
            into(distDir)
        }

        tasks.matching { it.name == "assemble$taskSuffix" }.configureEach { finalizedBy(copyApk) }
        tasks.matching { it.name == "bundle$taskSuffix" }.configureEach { finalizedBy(copyBundle) }
    }
}
