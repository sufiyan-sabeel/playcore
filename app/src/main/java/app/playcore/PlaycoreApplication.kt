package app.playcore

import android.app.Application
import app.playcore.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PlaycoreApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // A session left open by a crash, a kill or a reboot is restored at startup.
        appScope.launch {
            runCatching { container.restorationEngine.recoverStaleSessions() }
        }
    }
}
