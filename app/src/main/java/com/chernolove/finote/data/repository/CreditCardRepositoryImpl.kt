package com.chernolove.finote.data.repository
// 📍 Адрес файла в проекте.
// data → потому что это слой данных.
// repository → потому что это реализация репозитория.

import com.chernolove.finote.domain.model.CreditCard
// Подключаем доменную модель карты.
// Data может знать про Domain.

import com.chernolove.finote.domain.repository.CreditCardRepository
// Подключаем интерфейс из Domain.
// Мы будем его реализовывать.

import kotlinx.coroutines.flow.Flow
// Тип "поток данных" из Coroutines.
// Используется для наблюдения за списком карт.

import kotlinx.coroutines.flow.flow
// Функция-конструктор для создания Flow вручную.

class CreditCardRepositoryImpl : CreditCardRepository {
    // Реализация интерфейса.
    // Двоеточие означает: "реализую контракт".

    override suspend fun addCard(card: CreditCard) {
        // Метод добавления карты.
        // suspend → может выполняться асинхронно.
        // Пока пусто — заглушка.
    }

    override suspend fun deleteCard(card: CreditCard) {
        // Метод удаления карты.
        // Тоже временно без логики.
    }

    override fun getAllCards(): Flow<List<CreditCard>> {
        // Метод возвращает поток списка карт.
        // Flow → поток.
        // List<CreditCard> → список карт.

        return flow {
            // Создаём поток вручную.

            emit(emptyList())
            // Отправляем пустой список.
            // Это временные данные.
        }
    }
}