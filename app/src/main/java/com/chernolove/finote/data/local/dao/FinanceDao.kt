package com.chernolove.finote.data.local.dao // 📍 адрес файла в проекте

import androidx.room.Dao // 🧠 Говорим Room: "это интерфейс доступа к базе"
import androidx.room.Insert // 🧠 Аннотация для сохранения данных
import androidx.room.Query // 🧠 Аннотация для SQL запроса
import androidx.room.Delete // 🧠 Аннотация для удаления
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.chernolove.finote.data.local.entity.CreditCardEntity // 📦 таблица кредитных карт
import com.chernolove.finote.data.local.entity.TransactionEntity // 📦 таблица операций

@Dao // 🧠 Room понимает — здесь будут методы работы с базой
interface FinanceDao { // 👩‍💼 Сотрудник склада данных

    // -----------------------------
    // 💳 CREDIT CARD
    // -----------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCard(card: CreditCardEntity): Long
    // 🧠 Сохраняем новую кредитную карту в таблицу credit_cards
    // suspend = можно вызывать только из корутины (не блокирует UI)

    @Query("SELECT * FROM credit_cards ORDER BY dueDate ASC")
    suspend fun getAllCards(): List<CreditCardEntity>
    // 🧠 Получить все кредитные карты из базы

    @Query("SELECT * FROM credit_cards WHERE id = :cardId LIMIT 1")
    suspend fun getCardById(cardId: Long): CreditCardEntity?
    // 🧠 Получить одну карту по её id

    @Update
    suspend fun updateCreditCard(card: CreditCardEntity)
    // 🧠 Обновить данные конкретной карты

    @Delete
    suspend fun deleteCreditCard(card: CreditCardEntity)
    // 🧠 Удалить конкретную карту

    // -----------------------------
    // 💸 TRANSACTION
    // -----------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    // 🧠 Сохранить новую операцию

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionById(transactionId: Long): TransactionEntity?
    // 🧠 Получить одну операцию по её id

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    // 🧠 Обновить конкретную операцию

    @Query("SELECT * FROM transactions WHERE cardId = :cardId ORDER BY date DESC")
    suspend fun getTransactionsByCardId(cardId: Long): List<TransactionEntity>
    // 🧠 Получить операции по конкретной карте
    // :cardId — это параметр запроса (подставляется из функции)

    @Query("DELETE FROM transactions WHERE cardId = :cardId")
    suspend fun deleteTransactionsByCardId(cardId: Long)
    // 🧠 Удалить все операции карты (например если карту удалили)

}
