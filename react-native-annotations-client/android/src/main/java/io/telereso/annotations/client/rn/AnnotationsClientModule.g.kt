package io.telereso.annotations.client.rn

import io.telereso.annotations.client.AnnotationsClientManager
import io.telereso.annotations.models.*
import io.telereso.annotations.client.getRocketLaunchesListFlow
import io.telereso.annotations.client.getRocketLaunchesArrayFlow
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import kotlin.js.ExperimentalJsExport
import io.telereso.kmp.core.Task


@OptIn(ExperimentalJsExport::class)
class AnnotationsClientModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val manager = AnnotationsClientManager.getInstance()

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun fetchLaunchRockets(forceReload: Boolean, promise: Promise) {
    manager.fetchLaunchRockets(forceReload).onSuccess {
      promise.resolve(RocketLaunch.toJson(it))
    }.onFailure {
      promise.reject(it)
    }
  }

  @ReactMethod
  fun testDefaultParam(param: String = "", promise: Promise) {
       try {
          promise.resolve(manager.testDefaultParam(param)) 
       } catch (e:Exception){
          promise.reject(e)
       }

  }

  @ReactMethod
  fun getFlow(param: String = "", promise: Promise) {
   Task.execute {
     val emitter =
       reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
     manager.getFlow(param).collect {
       emitter.emit("${NAME}_getFlow_1ps", it)
     }
     promise.resolve(true)
   }.onFailure {
     promise.reject(it)
   }
  }

  @ReactMethod
  fun getFirstRocketLaunchFlow(promise: Promise) {
   manager.getFirstRocketLaunchFlow().onSuccess { res -> 
     val emitter =
          reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
     Task.execute {
       res.collect {
           emitter.emit("${NAME}_getFirstRocketLaunchFlow_0", it?.toJson())
       }
       promise.resolve(true)
     }.onFailure {
       promise.reject(it)
     }
   }.onFailure {
     promise.reject(it)
   }
  }

  @ReactMethod
  fun getRocketLaunchesFlow(param: String = "", promise: Promise) {
   manager.getRocketLaunchesArrayFlow(param).onSuccess { res -> 
     val emitter =
          reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
     Task.execute {
       res.collect {
           emitter.emit("${NAME}_getRocketLaunchesFlow_1ps", it.toJson())
       }
       promise.resolve(true)
     }.onFailure {
       promise.reject(it)
     }
   }.onFailure {
     promise.reject(it)
   }
  }

  @ReactMethod
  fun getArrayRocketLaunchFlow(rocketLaunch: String, promise: Promise) {
   manager.getArrayRocketLaunchFlow(RocketLaunch.fromJson(rocketLaunch)).onSuccess { res -> 
     val emitter =
          reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
     Task.execute {
       res.collect {
           emitter.emit("${NAME}_getArrayRocketLaunchFlow_1rr", RocketLaunch.toJson(it))
       }
       promise.resolve(true)
     }.onFailure {
       promise.reject(it)
     }
   }.onFailure {
     promise.reject(it)
   }
  }

  companion object {
    const val NAME = "AnnotationsClient"
  }
}