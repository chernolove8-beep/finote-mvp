package com.chernolove.finote.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExchangeRatesResponse(
    @SerializedName("result")
    val result: String,
    @SerializedName("time_last_update_utc")
    val timeLastUpdateUtc: String?,
    @SerializedName("rates")
    val rates: Map<String, Double>
)
