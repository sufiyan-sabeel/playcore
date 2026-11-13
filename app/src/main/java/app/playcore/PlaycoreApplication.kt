package app.playcore

import android.app.Application
import app.playcore.di.AppContainer

class PlaycoreApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
