package com.chernolove.finote.data.local.database
// пакет — логика работы с базой данных

import androidx.room.TypeConverter
// аннотация Room — говорит что это функция преобразования типов

import java.time.LocalDate
// тип даты из Java Time API

class Converters {
    // класс конвертеров
    // аналогия: переводчик между "языком приложения" и "языком базы данных"

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? {
        // преобразуем LocalDate → Long
        // база не понимает сложные типы, только простые числа и строки
        // аналогия: переводим дату в числовой код

        return date?.toEpochDay()
        // переводим дату в количество дней от 1970 года
    }

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? {
        // обратное преобразование Long → LocalDate
        // аналогия: восстанавливаем нормальную дату из числового кода

        return epochDay?.let { LocalDate.ofEpochDay(it) }
    }
}