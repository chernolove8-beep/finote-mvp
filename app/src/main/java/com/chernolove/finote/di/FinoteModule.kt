package com.chernolove.finote.di

import androidx.room.Room
import com.chernolove.finote.data.local.database.FinoteDatabase
import com.chernolove.finote.data.remote.api.CurrencyApiService
import com.chernolove.finote.data.repository.CurrencyRepository
import com.chernolove.finote.data.repository.FinanceRepositoryImpl
import com.chernolove.finote.domain.repository.FinanceRepository
import com.chernolove.finote.domain.usecase.AddCardUseCase
import com.chernolove.finote.domain.usecase.AddTransactionUseCase
import com.chernolove.finote.domain.usecase.GetCardsUseCase
import com.chernolove.finote.domain.usecase.GetTransactionsByCardUseCase
import com.chernolove.finote.domain.usecase.UpdateCardUseCase
import com.chernolove.finote.domain.usecase.UpdateTransactionUseCase
import com.chernolove.finote.presentation.FinoteViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val finoteModule = module {
    // DI создаёт и хранит единственный экземпляр локальной базы Room.
    single {
        Room.databaseBuilder(
            get(),
            FinoteDatabase::class.java,
            "finote_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<FinoteDatabase>().financeDao() }
    single<FinanceRepository> { FinanceRepositoryImpl(get()) }

    // Внешний API курса валют: open.er-api.com
    single {
        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CurrencyApiService::class.java)
    }

    single { CurrencyRepository(get()) }

    // UseCase и ViewModel тоже отдаются через DI,
    // чтобы слои приложения были слабо связаны между собой.
    factory { AddCardUseCase(get()) }
    factory { UpdateCardUseCase(get()) }
    factory { GetCardsUseCase(get()) }
    factory { AddTransactionUseCase(get()) }
    factory { GetTransactionsByCardUseCase(get()) }
    factory { UpdateTransactionUseCase(get()) }

    viewModel {
        FinoteViewModel(
            addCardUseCase = get(),
            updateCardUseCase = get(),
            getCardsUseCase = get(),
            addTransactionUseCase = get(),
            getTransactionsByCardUseCase = get(),
            updateTransactionUseCase = get(),
            currencyRepository = get()
        )
    }
}
