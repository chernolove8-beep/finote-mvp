package com.chernolove.finote.data.local.database
// пакет — место где хранится логика базы данных

import androidx.room.Database
// аннотация — объявляем базу данных Room

import androidx.room.RoomDatabase
// базовый класс — от него наследуются все базы Room

import androidx.room.TypeConverters
// подключение конвертеров для нестандартных типов

import com.chernolove.finote.data.local.dao.FinanceDao
// DAO — интерфейс для работы с данными

import com.chernolove.finote.data.local.entity.CreditCardEntity
// таблица кредитных карт

import com.chernolove.finote.data.local.entity.TransactionEntity
// таблица транзакций

@Database(
    entities = [
        CreditCardEntity::class,
        TransactionEntity::class
    ],
    // список таблиц, которые будут внутри базы
    // аналогия: список папок в архиве

    version = 2
    // версия базы данных
    // увеличивается при изменении структуры таблиц
)
@TypeConverters(Converters::class)
// подключаем конвертеры типов
// аналогия: переводчик форматов данных для базы

abstract class FinoteDatabase : RoomDatabase() {
    // класс базы данных
    // аналогия: сам архив / хранилище документов

    abstract fun financeDao(): FinanceDao
    // метод получения DAO
    // аналогия: выдаём сотрудника архива, через которого работаем с документами
}
