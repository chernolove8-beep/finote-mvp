package com.chernolove.finote.data.local.entity
// пакет — адрес расположения модели данных в проекте

import androidx.room.Entity
// аннотация Room — говорим что это таблица в базе данных

import androidx.room.PrimaryKey
// аннотация — указываем главный уникальный ключ строки

@Entity(tableName = "credit_cards")
// создаём таблицу "credit_cards" в базе
// аналогия: отдельная папка в архиве для кредитных карт

data class CreditCardEntity(
    // data class — модель данных
    // аналогия: бланк карточки клиента

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // уникальный идентификатор записи
    // autoGenerate — база сама присваивает номер
    // аналогия: номер дела в архиве

    val name: String,
    // название кредитной карты
    // аналогия: имя клиента или название продукта

    val creditLimit: Double,
    // кредитный лимит
    // аналогия: максимально доступная сумма

    val currentDebt: Double,
    // текущий долг
    // аналогия: сколько уже потрачено

    val dueDate: Long,
    // дата платежа (в миллисекундах)
    // аналогия: крайний срок оплаты по договору

    val createdAt: Long
    // дата создания записи
    // аналогия: когда карточку завели в систему
)