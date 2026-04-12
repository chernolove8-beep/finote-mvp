package com.chernolove.finote.data.repository

import com.chernolove.finote.data.local.dao.FinanceDao
import com.chernolove.finote.data.local.entity.CreditCardEntity
import com.chernolove.finote.data.local.entity.TransactionEntity
import com.chernolove.finote.domain.model.CreditCard
import com.chernolove.finote.domain.model.Transaction
import com.chernolove.finote.domain.model.TransactionType
import com.chernolove.finote.domain.repository.FinanceRepository
import java.time.LocalDate

class FinanceRepositoryImpl(private val financeDao: FinanceDao) : FinanceRepository {

    override suspend fun addCard(card: CreditCard): Long {
        return financeDao.insertCreditCard(card.toEntity())
    }

    override suspend fun updateCard(card: CreditCard) {
        financeDao.updateCreditCard(card.toEntity())
    }

    override suspend fun deleteCard(cardId: Long) {
        val currentCard = financeDao.getCardById(cardId)
            ?: throw IllegalArgumentException("Card not found")

        financeDao.deleteCreditCard(currentCard)
    }

    override suspend fun getCards(): List<CreditCard> {
        return financeDao.getAllCards().map { it.toDomain() }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        val currentCard = financeDao.getCardById(transaction.cardId)
            ?: throw IllegalArgumentException("Card not found")

        val newDebt = when (transaction.type) {
            TransactionType.EXPENSE -> currentCard.currentDebt + transaction.amount
            TransactionType.PAYMENT -> currentCard.currentDebt - transaction.amount
        }

        require(newDebt >= 0) { "Debt cannot be negative" }
        require(newDebt <= currentCard.creditLimit) { "Debt cannot exceed credit limit" }

        financeDao.insertTransaction(transaction.toEntity())
        financeDao.updateCreditCard(currentCard.copy(currentDebt = newDebt))
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        val currentCard = financeDao.getCardById(transaction.cardId)
            ?: throw IllegalArgumentException("Card not found")
        val oldTransaction = financeDao.getTransactionById(transaction.id)
            ?: throw IllegalArgumentException("Transaction not found")

        val oldEffect = when (TransactionType.valueOf(oldTransaction.type)) {
            TransactionType.EXPENSE -> oldTransaction.amount
            TransactionType.PAYMENT -> -oldTransaction.amount
        }
        val newEffect = when (transaction.type) {
            TransactionType.EXPENSE -> transaction.amount
            TransactionType.PAYMENT -> -transaction.amount
        }

        val newDebt = currentCard.currentDebt - oldEffect + newEffect

        require(newDebt >= 0) { "Debt cannot be negative" }
        require(newDebt <= currentCard.creditLimit) { "Debt cannot exceed credit limit" }

        financeDao.updateTransaction(transaction.toEntity())
        financeDao.updateCreditCard(currentCard.copy(currentDebt = newDebt))
    }

    override suspend fun deleteTransaction(transactionId: Long) {
        val transaction = financeDao.getTransactionById(transactionId)
            ?: throw IllegalArgumentException("Transaction not found")
        val currentCard = financeDao.getCardById(transaction.cardId)
            ?: throw IllegalArgumentException("Card not found")

        val debtDelta = when (TransactionType.valueOf(transaction.type)) {
            TransactionType.EXPENSE -> -transaction.amount
            TransactionType.PAYMENT -> transaction.amount
        }

        val newDebt = currentCard.currentDebt + debtDelta

        require(newDebt >= 0) { "Debt cannot be negative" }
        require(newDebt <= currentCard.creditLimit) { "Debt cannot exceed credit limit" }

        financeDao.deleteTransaction(transaction)
        financeDao.updateCreditCard(currentCard.copy(currentDebt = newDebt))
    }

    override suspend fun getTransactionsByCard(cardId: Long): List<Transaction> {
        return financeDao.getTransactionsByCardId(cardId).map { it.toDomain() }
    }

    private fun CreditCardEntity.toDomain(): CreditCard {
        return CreditCard(
            id = id,
            name = name,
            creditLimit = creditLimit,
            currentDebt = currentDebt,
            dueDate = LocalDate.ofEpochDay(dueDate),
            createdAt = createdAt
        )
    }

    private fun CreditCard.toEntity(): CreditCardEntity {
        return CreditCardEntity(
            id = id,
            name = name,
            creditLimit = creditLimit,
            currentDebt = currentDebt,
            dueDate = dueDate.toEpochDay(),
            createdAt = createdAt
        )
    }

    private fun TransactionEntity.toDomain(): Transaction {
        return Transaction(
            id = id,
            cardId = cardId,
            amount = amount,
            type = TransactionType.valueOf(type),
            date = LocalDate.ofEpochDay(date),
            comment = comment
        )
    }

    private fun Transaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            cardId = cardId,
            amount = amount,
            type = type.name,
            date = date.toEpochDay(),
            comment = comment
        )
    }
}
