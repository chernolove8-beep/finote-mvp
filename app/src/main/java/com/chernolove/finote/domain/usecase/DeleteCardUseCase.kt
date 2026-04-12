package com.chernolove.finote.domain.usecase

import com.chernolove.finote.domain.repository.FinanceRepository

class DeleteCardUseCase(
    private val repository: FinanceRepository
) {
    suspend fun execute(cardId: Long) {
        require(cardId > 0) { "Card id must be positive" }
        repository.deleteCard(cardId)
    }
}
