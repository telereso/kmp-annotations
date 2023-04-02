package io.telereso.annotations.client.app

import io.telereso.annotations.client.DatabaseDriverFactory
import io.telereso.annotations.client.AnnotationsClientManager.Companion.client
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.telereso.annotations.models.RocketLaunch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.telereso.kmp.core.Config
import okhttp3.Interceptor

class SampleViewModel(application: Application) : AndroidViewModel(application) {

    private val clientManager = client(DatabaseDriverFactory(application)) {
        withConfig(Config.builder(application.applicationInfo.loadLabel(application.packageManager).toString(),BuildConfig.VERSION_NAME) {
            shouldLogHttpRequests(true)
            withInterceptors(
                listOf<Interceptor>(ChuckerInterceptor.Builder(application).build())
            )
        })
    }

    // TODO Update sample
    // Expose screen UI state
    private val _rocketLauncherState = MutableLiveData<List<RocketLaunch>?>(null)
    val rocketLauncherState: LiveData<List<RocketLaunch>?> = _rocketLauncherState

    // TODO Update sample
    // Handle business logic
    fun fetchData() {
        clientManager.fetchLaunchRockets(true).onSuccess {
            _rocketLauncherState.postValue(it.toList())
        }.onFailure {
            println("failed to fetch")
        }
    }
}