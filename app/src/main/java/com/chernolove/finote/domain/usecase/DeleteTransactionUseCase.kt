package com.chernolove.finote.domain.usecase

import com.chernolove.finote.domain.repository.FinanceRepository

class DeleteTransactionUseCase(
    private val repository: FinanceRepository
) {
    suspend fun execute(transactionId: Long) {
        require(transactionId > 0) { "Transaction id must be positive" }
        repository.deleteTransaction(transactionId)
    }
}
