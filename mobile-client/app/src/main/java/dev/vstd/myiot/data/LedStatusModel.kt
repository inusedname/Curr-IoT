package dev.vstd.myiot.data

import java.io.Serializable

data class LedStatusModel(
    val which: Int,
    val on: Boolean,
) : Serializable {
    companion object {
        fun from(key: String, on: Boolean): LedStatusModel {
            return when (key) {
                "l1" -> LedStatusModel(1, on)
                "l2" -> LedStatusModel(2, on)
                else -> throw IllegalArgumentException("Unknown key: $key")
            }
        }
    }
}