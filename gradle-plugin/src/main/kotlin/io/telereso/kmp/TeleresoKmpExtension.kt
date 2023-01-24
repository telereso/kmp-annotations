package io.telereso.kmp


import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject


open class TeleresoKmpExtension @Inject constructor(
    objects: ObjectFactory
) {

    /**
     * set to true to stop copying generated model files with json converters extensions into src dir
     */
    var disableJsonConverters: Boolean = false
    /**
     * set to true to stop copying generated reactNative files react native dir
     */
    var disableReactExport: Boolean = false
    /**
     * set to true to stop copying generated flutter files into flutter dir
     */
    var disableFlutterExport: Boolean = false

    companion object {
        fun Project.teleresoKmp(): TeleresoKmpExtension {
            return extensions.create("teleresoKmp", TeleresoKmpExtension::class.java)
        }
    }
}