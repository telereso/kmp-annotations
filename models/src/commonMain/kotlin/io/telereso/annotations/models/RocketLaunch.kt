package io.telereso.annotations.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * TODO Remove sample
 * The RocketLaunch class is marked with the @Serializable annotation,
 * so that the kotlinx.serialization plugin can automatically generate a default serializer for it.
 */
@Serializable
@OptIn(ExperimentalJsExport::class)
@JsExport
data class RocketLaunch(
    /**
     * The @SerialName annotation allows you to redefine field names,
     * making it possible to declare properties in data classes with more readable names.
     */
    @SerialName("flight_number")
    val flightNumber: Int? = null,

    // we dont need to set the SerialName to avoid double work.
    val mission_name: String? = null,

    @SerialName("launch_year")
    val launchYear: Int? = null,
    @SerialName("launch_date_utc")
    val launchDateUTC: String? = null,
    @SerialName("rocket")
    val rocket: Rocket?=null,
    @SerialName("details")
    val details: String?=null,
    @SerialName("launch_success")
    val launchSuccess: Boolean? = null,
    @SerialName("links")
    val links: Links? = null,
)

@Serializable
data class RocketLaunchList(val list: List<RocketLaunch>)

@Serializable
@OptIn(ExperimentalJsExport::class)
@JsExport
data class Rocket(
    @SerialName("rocket_id")
    val id: String? = null,
    @SerialName("rocket_name")
    val name: String? = null,
    @SerialName("rocket_type")
    val type: String? = null,
)

@Serializable
@OptIn(ExperimentalJsExport::class)
@JsExport
data class Links(
    @SerialName("mission_patch")
    val missionPatchUrl: String?,
    @SerialName("article_link")
    val articleUrl: String?
)
