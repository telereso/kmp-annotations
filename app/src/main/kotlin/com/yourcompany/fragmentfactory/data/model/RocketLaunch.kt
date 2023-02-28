package com.yourcompany.fragmentfactory.data.model

import io.telereso.kmp.annotations.Builder
import io.telereso.kmp.annotations.FlutterExport
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The RocketLaunch class is marked with the @Serializable annotation,
 * so that the kotlinx.serialization plugin can automatically generate a default serializer for it.
 */
@Serializable
@FlutterExport
@Builder
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
    val array: Array<Pokemon>? = null,
    val list: List<Links>? = null,
)

@Serializable
@FlutterExport
@Builder
data class Rocket(
    @SerialName("rocket_id")
    var id: String? = null,
    @SerialName("rocket_name")
    var name: String? = null,
    @SerialName("rocket_type")
    var type: String? = null,
    var array: Array<String>? = null,
    var list: List<String>? = null,
)

@Serializable
@FlutterExport
data class Links(
    @SerialName("mission_patch")
    val missionPatchUrl: String?,
    @SerialName("article_link")
    val articleUrl: String?
)