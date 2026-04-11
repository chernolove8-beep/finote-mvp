package com.chernolove.finote.presentation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chernolove.finote.R
import com.chernolove.finote.domain.model.CreditCard
import com.chernolove.finote.domain.model.Transaction
import com.chernolove.finote.domain.model.TransactionType
import java.time.LocalDate

private const val HOME_ROUTE = "home"
private const val ADD_CARD_ROUTE = "add_card"
private const val EDIT_CARD_ROUTE = "edit_card"
private const val DETAILS_ROUTE = "details"
private const val ADD_TRANSACTION_ROUTE = "add_transaction"
private const val EDIT_TRANSACTION_ROUTE = "edit_transaction"

@Composable
fun FinoteApp(viewModel: FinoteViewModel) {
    // Контроллер навигации управляет переходами между Compose-экранами.
    val navController = rememberNavController()

    // Получаем текущее состояние приложения из ViewModel.
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val messageText = state.message?.let { it.asText() }

    // Если появилось сообщение от ViewModel, показываем короткий Toast.
    LaunchedEffect(messageText) {
        messageText?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    // NavHost — центральная точка навигации между экранами Compose.
    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE
    ) {
        // Главный экран со списком карт и блоком курса валют.
        composable(HOME_ROUTE) {
            HomeScreen(
                state = state,
                onAddCardClick = { navController.navigate(ADD_CARD_ROUTE) },
                onCardClick = { cardId -> navController.navigate("$DETAILS_ROUTE/$cardId") },
                onReload = viewModel::loadCards
            )
        }

        // Экран создания новой карты.
        composable(ADD_CARD_ROUTE) {
            AddCardScreen(
                screenTitle = stringResource(R.string.new_card_title),
                actionText = stringResource(R.string.save_card),
                onBack = { navController.popBackStack() },
                onSave = { name, limit, debt, dueDate ->
                    viewModel.addCard(
                        name = name,
                        creditLimitText = limit,
                        currentDebtText = debt,
                        dueDateText = dueDate,
                        onSuccess = { navController.popBackStack() }
                    )
                }
            )
        }

        // Экран редактирования уже существующей карты.
        composable(
            route = "$EDIT_CARD_ROUTE/{cardId}",
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: 0L
            val existingCard = state.cards.firstOrNull { it.id == cardId }
            AddCardScreen(
                screenTitle = stringResource(R.string.edit_card_title),
                actionText = stringResource(R.string.save_changes),
                initialName = existingCard?.name.orEmpty(),
                initialLimit = existingCard?.creditLimit?.trimmedNumber().orEmpty(),
                initialDebt = existingCard?.currentDebt?.trimmedNumber().orEmpty(),
                initialDay = existingCard?.dueDate?.dayPart().orEmpty(),
                initialMonth = existingCard?.dueDate?.monthPart().orEmpty(),
                initialYear = existingCard?.dueDate?.yearPart().orEmpty(),
                onBack = { navController.popBackStack() },
                onSave = { name, limit, debt, dueDate ->
                    viewModel.addCard(
                        cardId = cardId,
                        name = name,
                        creditLimitText = limit,
                        currentDebtText = debt,
                        dueDateText = dueDate,
                        onSuccess = { navController.popBackStack() }
                    )
                }
            )
        }

        // Экран деталей выбранной карты.
        composable(
            route = "$DETAILS_ROUTE/{cardId}",
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: 0L
            // При открытии экрана сразу подгружаем актуальные данные карты.
            LaunchedEffect(cardId) { viewModel.loadCardDetails(cardId) }
            CardDetailsScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onEditCard = { navController.navigate("$EDIT_CARD_ROUTE/$cardId") },
                onAddTransaction = { navController.navigate("$ADD_TRANSACTION_ROUTE/$cardId") },
                onEditTransaction = { transactionId ->
                    viewModel.selectTransaction(transactionId)
                    navController.navigate("$EDIT_TRANSACTION_ROUTE/$cardId/$transactionId")
                }
            )
        }

        // Экран добавления новой операции к карте.
        composable(
            route = "$ADD_TRANSACTION_ROUTE/{cardId}",
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: 0L
            AddTransactionScreen(
                screenTitle = stringResource(R.string.new_transaction_title),
                actionText = stringResource(R.string.save_transaction),
                initialAmount = "",
                initialDay = LocalDate.now().dayPart(),
                initialMonth = LocalDate.now().monthPart(),
                initialYear = LocalDate.now().yearPart(),
                initialComment = "",
                initialType = TransactionType.EXPENSE,
                onBack = { navController.popBackStack() },
                onSave = { amount, date, type, comment ->
                    viewModel.addTransaction(
                        cardId = cardId,
                        amountText = amount,
                        dueDateText = date,
                        type = type,
                        comment = comment,
                        onSuccess = { navController.popBackStack() }
                    )
                }
            )
        }

        // Экран редактирования существующей операции.
        composable(
            route = "$EDIT_TRANSACTION_ROUTE/{cardId}/{transactionId}",
            arguments = listOf(
                navArgument("cardId") { type = NavType.LongType },
                navArgument("transactionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: 0L
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            val existingTransaction = state.transactions.firstOrNull { it.id == transactionId }
            AddTransactionScreen(
                screenTitle = stringResource(R.string.edit_transaction_title),
                actionText = stringResource(R.string.save_changes),
                initialAmount = existingTransaction?.amount?.trimmedNumber().orEmpty(),
                initialDay = existingTransaction?.date?.dayPart() ?: LocalDate.now().dayPart(),
                initialMonth = existingTransaction?.date?.monthPart() ?: LocalDate.now().monthPart(),
                initialYear = existingTransaction?.date?.yearPart() ?: LocalDate.now().yearPart(),
                initialComment = existingTransaction?.comment.orEmpty(),
                initialType = existingTransaction?.type ?: TransactionType.EXPENSE,
                onBack = { navController.popBackStack() },
                onSave = { amount, date, type, comment ->
                    viewModel.addTransaction(
                        transactionId = transactionId,
                        cardId = cardId,
                        amountText = amount,
                        dueDateText = date,
                        type = type,
                        comment = comment,
                        onSuccess = { navController.popBackStack() }
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    state: FinoteUiState,
    onAddCardClick: () -> Unit,
    onCardClick: (Long) -> Unit,
    onReload: () -> Unit
) {
    // Единый фирменный фон приложения.
    FinoteScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Верхняя панель с названием приложения и слоганом.
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.app_tagline),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddCardClick,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.add_card))
                    }
                    TextButton(onClick = onReload) {
                        Text(stringResource(R.string.refresh))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ExchangeRateCard(
                    rate = state.usdToRubRate,
                    actualDate = state.rateActualDate,
                    isLoading = state.isRateLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    state.isLoading -> {
                        CircularProgressIndicator()
                    }

                    state.cards.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.home_empty_state),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.cards, key = { it.id }) { card ->
                                CardItem(card = card, onClick = { onCardClick(card.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinoteScreenBackground(
    content: @Composable () -> Unit
) {
    // Общая фоновая обёртка для экранов.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)
                    )
                )
            )
    ) {
        MoneyPatternOverlay()
        content()
    }
}

@Composable
private fun MoneyPatternOverlay() {
    // Декоративный паттерн со знаками рубля.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(5) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = when (rowIndex % 3) {
                    0 -> Arrangement.SpaceBetween
                    1 -> Arrangement.SpaceAround
                    else -> Arrangement.SpaceEvenly
                }
            ) {
                repeat(3) { columnIndex ->
                    val isAccent = (rowIndex + columnIndex) % 2 == 0
                    val useGoldAccent = (rowIndex * 3 + columnIndex) % 4 == 0
                    Text(
                        text = "₽",
                        modifier = Modifier.rotate(if (isAccent) -12f else 10f),
                        style = if (isAccent) {
                            MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black)
                        } else {
                            MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                        },
                        color = if (useGoldAccent) {
                            MaterialTheme.colorScheme.secondary.copy(
                                alpha = if (isAccent) 0.12f else 0.08f
                            )
                        } else {
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = if (isAccent) 0.07f else 0.04f
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExchangeRateCard(
    rate: Double?,
    actualDate: String?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    // Карточка данных, пришедших из внешнего API.
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.exchange_rate_title),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            when {
                isLoading -> Text(stringResource(R.string.exchange_rate_loading))
                rate != null -> {
                    Text(stringResource(R.string.exchange_rate_value, rate.formatMoney()))
                    actualDate?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(stringResource(R.string.exchange_rate_actual_date, it))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.exchange_rate_source),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> Text(stringResource(R.string.exchange_rate_unavailable))
            }
        }
    }
}

@Composable
private fun CardItem(card: CreditCard, onClick: () -> Unit) {
    // Доступный остаток рассчитываем для отображения на экране.
    val availableLimit = card.creditLimit - card.currentDebt

    // Одна карточка из списка карт на HomeScreen.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = card.name, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.card_debt, card.currentDebt.formatMoney()))
            Text(stringResource(R.string.card_limit, card.creditLimit.formatMoney()))
            Text(stringResource(R.string.card_available, availableLimit.formatMoney()))
            Text(stringResource(R.string.card_due_date, card.dueDate.toInputDate()))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCardScreen(
    screenTitle: String,
    actionText: String,
    initialName: String = "",
    initialLimit: String = "",
    initialDebt: String = "",
    initialDay: String = "",
    initialMonth: String = "",
    initialYear: String = "",
    onBack: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    // Переиспользуемая форма:
    // и для добавления карты, и для её редактирования.
    var name by remember { mutableStateOf(initialName) }
    var limit by remember { mutableStateOf(initialLimit) }
    var debt by remember { mutableStateOf(initialDebt) }
    var dueDay by remember { mutableStateOf(initialDay) }
    var dueMonth by remember { mutableStateOf(initialMonth) }
    var dueYear by remember { mutableStateOf(initialYear) }

    FinoteScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(screenTitle) },
                    navigationIcon = {
                        TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.card_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = finoteTextFieldColors()
                )
                OutlinedTextField(
                    value = limit,
                    onValueChange = { limit = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text(stringResource(R.string.credit_limit_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(18.dp),
                    colors = finoteTextFieldColors()
                )
                OutlinedTextField(
                    value = debt,
                    onValueChange = { debt = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text(stringResource(R.string.current_debt_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(18.dp),
                    colors = finoteTextFieldColors()
                )
                Text(
                    text = stringResource(R.string.payment_date_label),
                    style = MaterialTheme.typography.bodyMedium
                )
                DatePartsInput(
                    day = dueDay,
                    month = dueMonth,
                    year = dueYear,
                    onDayChange = { dueDay = it },
                    onMonthChange = { dueMonth = it },
                    onYearChange = { dueYear = it }
                )
                Button(
                    onClick = { onSave(name, limit, debt, buildDateInput(dueDay, dueMonth, dueYear)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(actionText)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardDetailsScreen(
    state: FinoteUiState,
    onBack: () -> Unit,
    onEditCard: () -> Unit,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit
) {
    val card = state.selectedCard

    // Экран с полной информацией по карте и списком операций.
    FinoteScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(card?.name ?: stringResource(R.string.card_details_title)) },
                    navigationIcon = {
                        TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                    return@Column
                }

                if (card == null) {
                    Text(stringResource(R.string.card_not_found))
                    return@Column
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.card_debt, card.currentDebt.formatMoney()), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.card_limit, card.creditLimit.formatMoney()))
                        Text(stringResource(R.string.card_available, (card.creditLimit - card.currentDebt).formatMoney()))
                        Text(stringResource(R.string.card_due_date, card.dueDate.toInputDate()))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onEditCard,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(stringResource(R.string.edit_card))
                    }
                    Button(
                        onClick = onAddTransaction,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(stringResource(R.string.add_transaction))
                    }
                }

                Text(stringResource(R.string.transactions_title), style = MaterialTheme.typography.titleMedium)

                if (state.transactions.isEmpty()) {
                    Text(stringResource(R.string.transactions_empty_state))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.transactions, key = { it.id }) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onEdit = { onEditTransaction(transaction.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onEdit: () -> Unit
) {
    // Отдельная карточка одной операции.
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (transaction.type == TransactionType.EXPENSE) {
                    stringResource(R.string.transaction_type_expense)
                } else {
                    stringResource(R.string.transaction_type_payment)
                },
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(R.string.transaction_amount, transaction.amount.formatMoney()))
            Text(stringResource(R.string.transaction_date, transaction.date.toInputDate()))
            Text(stringResource(R.string.transaction_comment, transaction.comment ?: "—"))
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onEdit) {
                Text(stringResource(R.string.edit_transaction))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionScreen(
    screenTitle: String,
    actionText: String,
    initialAmount: String,
    initialDay: String,
    initialMonth: String,
    initialYear: String,
    initialComment: String,
    initialType: TransactionType,
    onBack: () -> Unit,
    onSave: (String, String, TransactionType, String) -> Unit
) {
    // Переиспользуемая форма:
    // и для создания операции, и для её редактирования.
    var amount by remember { mutableStateOf(initialAmount) }
    var transactionDay by remember { mutableStateOf(initialDay) }
    var transactionMonth by remember { mutableStateOf(initialMonth) }
    var transactionYear by remember { mutableStateOf(initialYear) }
    var comment by remember { mutableStateOf(initialComment) }
    var type by remember { mutableStateOf(initialType) }

    FinoteScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(screenTitle) },
                    navigationIcon = {
                        TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { type = TransactionType.EXPENSE },
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            if (type == TransactionType.EXPENSE) {
                                stringResource(R.string.expense_selected)
                            } else {
                                stringResource(R.string.transaction_type_expense)
                            }
                        )
                    }
                    Button(
                        onClick = { type = TransactionType.PAYMENT },
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            if (type == TransactionType.PAYMENT) {
                                stringResource(R.string.payment_selected)
                            } else {
                                stringResource(R.string.transaction_type_payment)
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text(stringResource(R.string.amount_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(18.dp),
                    colors = finoteTextFieldColors()
                )
                Text(
                    text = stringResource(R.string.transaction_date_label),
                    style = MaterialTheme.typography.bodyMedium
                )
                DatePartsInput(
                    day = transactionDay,
                    month = transactionMonth,
                    year = transactionYear,
                    onDayChange = { transactionDay = it },
                    onMonthChange = { transactionMonth = it },
                    onYearChange = { transactionYear = it }
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text(stringResource(R.string.comment_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = finoteTextFieldColors()
                )

                Button(
                    onClick = {
                        onSave(
                            amount,
                            buildDateInput(transactionDay, transactionMonth, transactionYear),
                            type,
                            comment
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(actionText)
                }
            }
        }
    }
}

@Composable
private fun DatePartsInput(
    day: String,
    month: String,
    year: String,
    onDayChange: (String) -> Unit,
    onMonthChange: (String) -> Unit,
    onYearChange: (String) -> Unit
) {
    // Отдельные поля день / месяц / год удобнее для ручного ввода на защите.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = day,
            onValueChange = { onDayChange(it.filter(Char::isDigit).take(2)) },
            label = { Text(stringResource(R.string.day_label)) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = finoteTextFieldColors()
        )
        OutlinedTextField(
            value = month,
            onValueChange = { onMonthChange(it.filter(Char::isDigit).take(2)) },
            label = { Text(stringResource(R.string.month_label)) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = finoteTextFieldColors()
        )
        OutlinedTextField(
            value = year,
            onValueChange = { onYearChange(it.filter(Char::isDigit).take(4)) },
            label = { Text(stringResource(R.string.year_label)) },
            modifier = Modifier.weight(1.3f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = finoteTextFieldColors()
        )
    }
}

@Composable
private fun finoteTextFieldColors() = OutlinedTextFieldDefaults.colors(
    // Единые цвета для всех полей ввода.
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun FinoteMessage.asText(): String = when (this) {
    // Переводим внутренний enum в локализованный текст для пользователя.
    FinoteMessage.CARDS_LOAD_FAILED -> stringResource(R.string.message_cards_load_failed)
    FinoteMessage.CARD_OPEN_FAILED -> stringResource(R.string.message_card_open_failed)
    FinoteMessage.INVALID_CARD_INPUT -> stringResource(R.string.message_invalid_card_input)
    FinoteMessage.CARD_ADDED -> stringResource(R.string.message_card_added)
    FinoteMessage.CARD_UPDATED -> stringResource(R.string.message_card_updated)
    FinoteMessage.CARD_SAVE_FAILED -> stringResource(R.string.message_card_save_failed)
    FinoteMessage.INVALID_TRANSACTION_INPUT -> stringResource(R.string.message_invalid_transaction_input)
    FinoteMessage.TRANSACTION_ADDED -> stringResource(R.string.message_transaction_added)
    FinoteMessage.TRANSACTION_UPDATED -> stringResource(R.string.message_transaction_updated)
    FinoteMessage.TRANSACTION_SAVE_FAILED -> stringResource(R.string.message_transaction_save_failed)
}

// Вспомогательные функции для красивого отображения чисел.
private fun Double.formatMoney(): String = String.format("%.2f", this)
private fun Double.trimmedNumber(): String =
    if (this % 1.0 == 0.0) toInt().toString() else toString()
