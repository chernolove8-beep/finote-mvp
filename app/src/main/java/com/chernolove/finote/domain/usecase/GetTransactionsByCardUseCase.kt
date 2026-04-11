package com.chernolove.finote.domain.usecase

// импорт модели операции
// это просто класс Transaction из domain.model
import com.chernolove.finote.domain.model.Transaction

// импорт интерфейса Repository
// через него мы будем получать данные
import com.chernolove.finote.domain.repository.FinanceRepository


// объявляем класс UseCase
// он отвечает за получение списка операций по карте
class GetTransactionsByCardUseCase(

    // это параметр конструктора
    // сюда будет передан Repository
    // благодаря этому UseCase сможет получать данные
    private val repository: FinanceRepository

) {

    // функция execute — стандартное имя метода UseCase
    // cardId — это id карты, по которой хотим получить операции
    suspend fun execute(cardId: Long): List<Transaction> {

        // проверка бизнес-логики
        // карта не может иметь id <= 0
        if (cardId <= 0) {
            // если id неправильный — кидаем ошибку
            throw IllegalArgumentException("Invalid card id")
        }

        // просим Repository вернуть операции по карте
        // Repository сам решает:
        // из базы брать / из API / из кэша
        return repository.getTransactionsByCard(cardId)
    }

}
