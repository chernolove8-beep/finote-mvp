package com.chernolove.finote.domain.repository

import com.chernolove.finote.domain.model.CreditCard
import com.chernolove.finote.domain.model.Transaction

interface FinanceRepository {
    suspend fun addCard(card: CreditCard): Long
    suspend fun updateCard(card: CreditCard)
    suspend fun getCards(): List<CreditCard>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun getTransactionsByCard(cardId: Long): List<Transaction>
}
