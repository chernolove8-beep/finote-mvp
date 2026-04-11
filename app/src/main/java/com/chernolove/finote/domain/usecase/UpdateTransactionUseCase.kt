package com.chernolove.finote.domain.usecase

import com.chernolove.finote.domain.model.Transaction
import com.chernolove.finote.domain.repository.FinanceRepository

class UpdateTransactionUseCase(
    private val repository: FinanceRepository
) {
    suspend fun execute(transaction: Transaction) {
        require(transaction.amount > 0) { "Amount must be positive" }
        repository.updateTransaction(transaction)
    }
}
