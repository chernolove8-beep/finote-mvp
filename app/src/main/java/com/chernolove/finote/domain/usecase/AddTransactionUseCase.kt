package com.chernolove.finote.domain.usecase

import com.chernolove.finote.domain.model.Transaction
import com.chernolove.finote.domain.repository.FinanceRepository

// UseCase отвечает за добавление операции
// Это бизнес-действие пользователя
class AddTransactionUseCase(

    // Repository передаётся через конструктор
    // UseCase НЕ знает где хранятся данные
    private val repository: FinanceRepository

) {

    // execute — стандартное имя метода UseCase
    // мы выполняем действие
    suspend fun execute(transaction: Transaction) {

        // здесь может быть бизнес-логика
        // например проверка суммы
        if (transaction.amount <= 0) {
            throw IllegalArgumentException("Amount must be positive")
        }

        // передаём операцию в Repository
        // Repository уже сохранит её в БД
        repository.addTransaction(transaction)
    }

}
