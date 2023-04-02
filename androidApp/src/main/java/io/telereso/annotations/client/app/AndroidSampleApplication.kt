package io.telereso.annotations.client.app

import android.app.Application
import io.telereso.kmp.core.CoreClient

class AndroidSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        /**
         * Initialize logs for debbugging
         */
        CoreClient.debugLogger()
    }
}