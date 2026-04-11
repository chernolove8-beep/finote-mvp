package com.chernolove.finote.domain.model

import java.time.LocalDate

data class CreditCard(
    val id: Long,
    val name: String,
    val creditLimit: Double,
    val currentDebt: Double,
    val dueDate: LocalDate,
    val createdAt: Long
)
