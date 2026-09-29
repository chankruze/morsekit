package `in`.geekofia.morsekit

import android.app.Activity
import android.app.Application
import android.os.Bundle
import `in`.geekofia.morsekit.platform.androidPlatformServices
import java.lang.ref.WeakReference

/** Owns the app-scoped [AppContainer] so it survives Activity recreation (e.g. rotation). */
class MorseKitApplication : Application() {
    /** The resumed Activity, for APIs that need one. Weak, so a destroyed Activity is never kept. */
    private var resumedActivity: WeakReference<Activity>? = null

    val container: AppContainer by lazy {
        AppContainer(androidPlatformServices(this, currentActivity = { resumedActivity?.get() }))
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                resumedActivity = WeakReference(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                if (resumedActivity?.get() === activity) resumedActivity = null
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }
}
