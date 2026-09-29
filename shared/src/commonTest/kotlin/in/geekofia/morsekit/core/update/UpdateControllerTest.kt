package `in`.geekofia.morsekit.core.update

import `in`.geekofia.morsekit.platform.AppUpdateService
import `in`.geekofia.morsekit.platform.InMemoryKeyValueStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

/** The "store": the test sets what's available and whether the user accepts. */
private class FakeAppUpdateService(override val isSupported: Boolean = true) : AppUpdateService {
    var available: AvailableUpdate? = null
    var userAccepts = true
    val started = mutableListOf<UpdateMode>()
    var completed = 0
    private var onReady: () -> Unit = {}

    override fun checkForUpdate(manual: Boolean, onResult: (AvailableUpdate?) -> Unit) = onResult(available)
    override fun startUpdate(mode: UpdateMode, onResult: (accepted: Boolean) -> Unit) {
        started += mode
        onResult(userAccepts)
    }
    override fun setOnReadyToInstall(listener: () -> Unit) {
        onReady = listener
    }
    override fun completeUpdate() {
        completed++
    }
    fun finishDownload() = onReady()
}

class UpdateControllerTest {
    private val service = FakeAppUpdateService()
    private var now = 1_000_000_000_000L
    private val controller = UpdateController(service, UpdatePrompter(InMemoryKeyValueStore(), nowMillis = { now }))

    @Test
    fun nothingAvailableDoesNothing() {
        controller.onAppResumed()
        assertEquals(emptyList(), service.started)
    }

    @Test
    fun offersAFlexibleUpdateOnResume() {
        service.available = update()
        controller.onAppResumed()
        assertEquals(listOf(UpdateMode.Flexible), service.started)
    }

    @Test
    fun offersAtMostOnceADay() {
        service.available = update()
        controller.onAppResumed()
        controller.onAppResumed()
        assertEquals(1, service.started.size)
        now += 1.days.inWholeMilliseconds
        controller.onAppResumed()
        assertEquals(2, service.started.size)
    }

    @Test
    fun declinedUpdateIsNotOfferedAgainTomorrow() {
        service.available = update()
        service.userAccepts = false
        controller.onAppResumed()
        now += 1.days.inWholeMilliseconds
        controller.onAppResumed()
        assertEquals(1, service.started.size)
    }

    @Test
    fun interruptedImmediateUpdateResumesEvenWhenACheckIsNotDue() {
        service.available = update()
        controller.onAppResumed() // uses today's check
        service.available = update(immediateInProgress = true)
        controller.onAppResumed()
        assertEquals(listOf(UpdateMode.Flexible, UpdateMode.Immediate), service.started)
    }

    @Test
    fun downloadedUpdateAsksToRestartAndInstalls() {
        assertFalse(controller.readyToInstall)
        service.finishDownload()
        assertTrue(controller.readyToInstall)
        controller.installNow()
        assertFalse(controller.readyToInstall)
        assertEquals(1, service.completed)
    }

    @Test
    fun laterKeepsTheDownloadWithoutInstalling() {
        service.finishDownload()
        controller.postponeInstall()
        assertFalse(controller.readyToInstall)
        assertEquals(0, service.completed)
    }

    @Test
    fun manualCheckReportsUpToDate() {
        controller.checkNow()
        assertEquals(UpdateController.ManualResult.UpToDate, controller.manualResult)
        controller.dismissManualResult()
        assertNull(controller.manualResult)
    }

    @Test
    fun manualCheckOffersEvenAfterAutomaticCheckAndSnooze() {
        service.available = update()
        service.userAccepts = false
        controller.onAppResumed() // declined -> snoozed, and today's check used
        controller.checkNow()
        assertEquals(2, service.started.size)
        assertEquals(UpdateController.ManualResult.Offered, controller.manualResult)
    }

    @Test
    fun unsupportedPlatformNeverChecks() {
        val none = FakeAppUpdateService(isSupported = false).apply { available = update() }
        val c = UpdateController(none, UpdatePrompter(InMemoryKeyValueStore(), nowMillis = { now }))
        c.onAppResumed()
        c.checkNow()
        assertEquals(emptyList(), none.started)
        assertNull(c.manualResult)
        assertFalse(c.isSupported)
    }
}
