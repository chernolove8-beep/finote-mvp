package com.chernolove.finote.domain.usecase

import com.chernolove.finote.domain.model.CreditCard
import com.chernolove.finote.domain.repository.FinanceRepository

class AddCardUseCase(
    private val repository: FinanceRepository
) {

    suspend fun execute(card: CreditCard): Long {
        require(card.name.isNotBlank()) { "Card name cannot be blank" }
        require(card.creditLimit > 0) { "Credit limit must be positive" }
        require(card.currentDebt >= 0) { "Debt cannot be negative" }
        require(card.currentDebt <= card.creditLimit) { "Debt cannot exceed credit limit" }

        return repository.addCard(card)
    }
}
