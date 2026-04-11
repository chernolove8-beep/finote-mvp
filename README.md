# Finote

`Finote` is an Android MVP application for tracking credit cards, debts, limits, payment dates, and transactions.

Проект выполнен как дипломный `MVP`: на минимально достаточном уровне, но с реализацией основных требований курса и защиты.

## Что реализовано

- список кредитных карт на главном экране
- добавление и редактирование карты
- экран деталей карты
- добавление и редактирование операций
- локальная база данных `Room`
- архитектура `MVVM + Clean Architecture`
- навигация между экранами
- `Dependency Injection` через `Koin`
- работа с внешним `API`
- блок курса `USD/RUB` с датой актуальности и источником данных
- локализация `RU / EN`
- интерфейс на `Jetpack Compose`

## Технологии

- `Kotlin`
- `Jetpack Compose`
- `Room`
- `StateFlow`
- `Coroutines`
- `Navigation Compose`
- `Retrofit`
- `Gson`
- `Koin`

## Архитектура

Поток данных в проекте:

`UI -> ViewModel -> UseCase -> Repository -> Room / API`

Основные слои:

- `presentation/` — экраны, навигация, `ViewModel`
- `domain/` — модели и `UseCase`
- `data/` — `Room`, `DAO`, `Entity`, `Repository`, `API`
- `di/` — зависимости приложения

## Внешний API

Для блока курса валют используется сервис:

- `https://open.er-api.com/`

## Как запустить

1. Открыть проект в `Android Studio`
2. Дождаться `Gradle Sync`
3. Запустить приложение на эмуляторе или Android-устройстве

## Параметры проекта

- `Min SDK: 29`
- `Target SDK: 35`
- `Java/Kotlin target: 17`

## Статус

Проект готов к демонстрации и защите в формате дипломного `MVP`.
