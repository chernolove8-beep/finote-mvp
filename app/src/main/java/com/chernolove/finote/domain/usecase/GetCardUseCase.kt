package com.chernolove.finote.domain.usecase // пакет = адрес файла в проекте

import com.chernolove.finote.domain.model.CreditCard // импортируем модель карты
import com.chernolove.finote.domain.repository.FinanceRepository // импортируем контракт доступа к данным

class GetCardsUseCase( // UseCase = действие пользователя "получить карты"

    private val repository: FinanceRepository // зависимость → через него будем получать данные

) {

    suspend fun execute(): List<CreditCard> { // функция действия → возвращает список карт

        return repository.getCards()
        // делегируем получение данных repository
        // UseCase НЕ знает откуда берутся данные (Room / API / файл)

    }

}
