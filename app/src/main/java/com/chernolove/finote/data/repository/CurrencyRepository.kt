package com.chernolove.finote.data.repository

import com.chernolove.finote.data.remote.api.CurrencyApiService
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

data class ExchangeRateInfo(
    val rubRate: Double?,
    val actualDate: String?
)

class CurrencyRepository(
    private val apiService: CurrencyApiService
) {
    suspend fun getUsdToRubRate(): ExchangeRateInfo {
        val response = apiService.getLatestUsdRates()
        val actualDate = response.timeLastUpdateUtc?.let { rawDate ->
            runCatching {
                ZonedDateTime.parse(rawDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault()))
            }.getOrNull()
        }

        return ExchangeRateInfo(
            rubRate = response.rates["RUB"],
            actualDate = actualDate
        )
    }
}
