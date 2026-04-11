package com.chernolove.finote.domain.repository

import com.chernolove.finote.domain.model.CreditCard
import kotlinx.coroutines.flow.Flow

interface CreditCardRepository {
    suspend fun addCard(card: CreditCard)
    suspend fun deleteCard(card: CreditCard)
    fun getAllCards(): Flow<List<CreditCard>>
}
