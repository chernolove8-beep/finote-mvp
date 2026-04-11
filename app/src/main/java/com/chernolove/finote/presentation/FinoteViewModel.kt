package com.chernolove.finote.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chernolove.finote.data.repository.ExchangeRateInfo
import com.chernolove.finote.data.repository.CurrencyRepository
import com.chernolove.finote.domain.model.CreditCard
import com.chernolove.finote.domain.model.Transaction
import com.chernolove.finote.domain.model.TransactionType
import com.chernolove.finote.domain.usecase.AddCardUseCase
import com.chernolove.finote.domain.usecase.AddTransactionUseCase
import com.chernolove.finote.domain.usecase.GetCardsUseCase
import com.chernolove.finote.domain.usecase.GetTransactionsByCardUseCase
import com.chernolove.finote.domain.usecase.UpdateCardUseCase
import com.chernolove.finote.domain.usecase.UpdateTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

// Общее состояние экранов.
// UI читает именно этот объект и на его основе решает, что показать пользователю.
data class FinoteUiState(
    val isLoading: Boolean = false,
    val isRateLoading: Boolean = false,
    val cards: List<CreditCard> = emptyList(),
    val selectedCard: CreditCard? = null,
    val selectedTransaction: Transaction? = null,
    val transactions: List<Transaction> = emptyList(),
    val usdToRubRate: Double? = null,
    val rateActualDate: String? = null,
    val message: FinoteMessage? = null
)

// Внутренние сигналы для UI.
// Текст хранится не здесь, а в ресурсах локализации.
enum class FinoteMessage {
    CARDS_LOAD_FAILED,
    CARD_OPEN_FAILED,
    INVALID_CARD_INPUT,
    CARD_ADDED,
    CARD_UPDATED,
    CARD_SAVE_FAILED,
    INVALID_TRANSACTION_INPUT,
    TRANSACTION_ADDED,
    TRANSACTION_UPDATED,
    TRANSACTION_SAVE_FAILED
}

class FinoteViewModel(
    private val addCardUseCase: AddCardUseCase,
    private val updateCardUseCase: UpdateCardUseCase,
    private val getCardsUseCase: GetCardsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getTransactionsByCardUseCase: GetTransactionsByCardUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    // Внутреннее изменяемое состояние.
    private val _uiState = MutableStateFlow(FinoteUiState(isLoading = true))
    // UI подписывается на это состояние и перерисовывает экран при изменениях.
    val uiState: StateFlow<FinoteUiState> = _uiState.asStateFlow()

    init {
        // При старте загружаем локальные данные карт и курс USD/RUB из внешнего API.
        loadCards()
        loadUsdRate()
    }

    fun loadCards() {
        // Загружаем список карт для главного экрана.
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            // Идём не напрямую в базу, а через UseCase.
            runCatching { getCardsUseCase.execute() }
                .onSuccess { cards ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cards = cards,
                        selectedCard = _uiState.value.selectedCard?.let { selected ->
                            cards.firstOrNull { it.id == selected.id }
                        } ?: _uiState.value.selectedCard,
                        selectedTransaction = _uiState.value.selectedTransaction?.let { selected ->
                            _uiState.value.transactions.firstOrNull { it.id == selected.id }
                        }
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = FinoteMessage.CARDS_LOAD_FAILED
                    )
                }
        }
    }

    fun loadCardDetails(cardId: Long) {
        // Загружаем одну карту и все операции по ней.
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            runCatching {
                // Находим саму карту.
                val cards = getCardsUseCase.execute()
                val card = cards.firstOrNull { it.id == cardId }
                    ?: throw IllegalArgumentException("Карта не найдена")

                // Затем получаем список операций только для этой карты.
                val transactions = getTransactionsByCardUseCase.execute(cardId)
                Triple(cards, card, transactions)
            }.onSuccess { (cards, card, transactions) ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    cards = cards,
                    selectedCard = card,
                    transactions = transactions,
                    selectedTransaction = _uiState.value.selectedTransaction?.let { selected ->
                        transactions.firstOrNull { it.id == selected.id }
                    }
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = FinoteMessage.CARD_OPEN_FAILED
                )
            }
        }
    }

    fun addCard(
        cardId: Long = 0,
        name: String,
        creditLimitText: String,
        currentDebtText: String,
        dueDateText: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // ViewModel получает сырые строки из UI и переводит их в нужные типы.
            val creditLimit = creditLimitText.toDoubleOrNull()
            val currentDebt = currentDebtText.toDoubleOrNull()
            val dueDate = parseInputDate(dueDateText)

            // Базовая проверка введённых данных.
            if (name.isBlank() || creditLimit == null || currentDebt == null || dueDate == null) {
                _uiState.value = _uiState.value.copy(message = FinoteMessage.INVALID_CARD_INPUT)
                return@launch
            }

            runCatching {
                // Здесь формируется доменная модель карты, которую дальше получает UseCase.
                val card = CreditCard(
                    id = cardId,
                    name = name.trim(),
                    creditLimit = creditLimit,
                    currentDebt = currentDebt,
                    dueDate = dueDate,
                    createdAt = if (cardId == 0L) System.currentTimeMillis() else (
                        _uiState.value.cards.firstOrNull { it.id == cardId }?.createdAt
                            ?: System.currentTimeMillis()
                    )
                )

                // Один и тот же метод работает и для создания новой карты, и для редактирования.
                if (cardId == 0L) {
                    addCardUseCase.execute(card)
                } else {
                    updateCardUseCase.execute(card)
                }
            }.onSuccess {
                // После сохранения обновляем список на главном экране.
                loadCards()
                _uiState.value = _uiState.value.copy(
                    message = if (cardId == 0L) FinoteMessage.CARD_ADDED else FinoteMessage.CARD_UPDATED
                )
                onSuccess()
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    message = FinoteMessage.CARD_SAVE_FAILED
                )
            }
        }
    }

    fun addTransaction(
        transactionId: Long = 0,
        cardId: Long,
        amountText: String,
        dueDateText: String,
        type: TransactionType,
        comment: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // Аналогично: UI отдаёт строки, а ViewModel подготавливает данные для бизнес-слоя.
            val amount = amountText.toDoubleOrNull()
            val date = parseInputDate(dueDateText)

            // Проверяем, что сумма и дата введены корректно.
            if (amount == null || date == null) {
                _uiState.value = _uiState.value.copy(message = FinoteMessage.INVALID_TRANSACTION_INPUT)
                return@launch
            }

            runCatching {
                // Формируем доменную модель операции.
                val transaction = Transaction(
                    id = transactionId,
                    cardId = cardId,
                    amount = amount,
                    type = type,
                    date = date,
                    comment = comment.ifBlank { null }
                )

                // transactionId = 0 -> новая операция.
                // transactionId != 0 -> редактирование существующей.
                if (transactionId == 0L) {
                    addTransactionUseCase.execute(transaction)
                } else {
                    updateTransactionUseCase.execute(transaction)
                }
            }.onSuccess {
                // После изменения операции обновляем детали карты:
                // там меняется и список операций, и долг по карте.
                loadCardDetails(cardId)
                _uiState.value = _uiState.value.copy(
                    message = if (transactionId == 0L) {
                        FinoteMessage.TRANSACTION_ADDED
                    } else {
                        FinoteMessage.TRANSACTION_UPDATED
                    }
                )
                onSuccess()
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    message = FinoteMessage.TRANSACTION_SAVE_FAILED
                )
            }
        }
    }

    fun clearMessage() {
        // Очищаем одноразовое сообщение после показа Toast.
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun selectTransaction(transactionId: Long) {
        // Запоминаем выбранную операцию перед переходом на экран редактирования.
        _uiState.value = _uiState.value.copy(
            selectedTransaction = _uiState.value.transactions.firstOrNull { it.id == transactionId }
        )
    }

    fun loadUsdRate() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRateLoading = true)
            // Курс берём из внешнего API и сохраняем в UI State для показа на главном экране.
            runCatching { currencyRepository.getUsdToRubRate() }
                .onSuccess { rateInfo ->
                    _uiState.value = _uiState.value.copy(
                        isRateLoading = false,
                        usdToRubRate = rateInfo.rubRate,
                        rateActualDate = rateInfo.actualDate
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isRateLoading = false,
                        usdToRubRate = null,
                        rateActualDate = null
                    )
                }
        }
    }
}
