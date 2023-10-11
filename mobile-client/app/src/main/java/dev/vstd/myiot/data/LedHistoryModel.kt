package dev.vstd.myiot.data

import com.google.gson.GsonBuilder
import java.io.Serializable

data class LedHistoryModel(
    val which: Int,
    val on: Boolean,
) : Serializable {
    companion object {
        fun fromJson(json: String): LedHistoryModel =
            GsonBuilder().create().fromJson(json, LedHistoryModel::class.java)
    }
}