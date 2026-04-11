package com.chernolove.finote.domain.model
// 📍 Domain слой.
// Здесь нет Android и Room.
// Это чистая бизнес-модель.

import java.time.LocalDate
// Используем LocalDate, потому что в Domain
// мы работаем с реальной датой, а не с числом.

data class Transaction(
    val id: Long,                      // Уникальный идентификатор операции
    val cardId: Long,                  // ID карты, к которой относится операция
    val amount: Double,                // Сумма операции
    val type: TransactionType,         // Тип операции (EXPENSE / PAYMENT)
    val date: LocalDate,               // Дата операции
    val comment: String?               // Комментарий к операции
)
