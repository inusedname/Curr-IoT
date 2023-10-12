package dev.vstd.myiot.screen.home

import com.google.gson.GsonBuilder

data class SensorModel(
    val humid: Float,
    val temp: Float,
    val lux: Float,
    val dust: Int,
    val seconds: Long,
) {
    companion object {
        fun fromJson(json: String): SensorModel {
            val gson = GsonBuilder().create()
            return gson.fromJson(json, SensorModel::class.java)
        }
    }
}