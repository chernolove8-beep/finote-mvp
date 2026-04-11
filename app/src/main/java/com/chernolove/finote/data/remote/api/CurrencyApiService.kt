package com.chernolove.finote.data.remote.api

import com.chernolove.finote.data.remote.dto.ExchangeRatesResponse
import retrofit2.http.GET

interface CurrencyApiService {
    @GET("v6/latest/USD")
    suspend fun getLatestUsdRates(): ExchangeRatesResponse
}
