package atm;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import atm.dispatcher.MessageDispatcher;
import atm.exceptions.InvalidAmountException;
import atm.exceptions.InvalidPinFormatException;
import atm.session.Session;
import atm.session.transactions.ATMTransactionFactory;
import atm.ui.panels.MainPanel;
import atm.utils.FormatChecker;
import bank.transactions.utils.AccountType;
import bank.transactions.utils.TransactionResult;
import bank.transactions.utils.TransactionType;

class SessionTest {
	private Session session;
	private final FormatChecker formatChecker = new FormatChecker();

	@BeforeEach
	void setUp() {
		MainPanel mainPanel = Mockito.mock(MainPanel.class);
		MessageDispatcher dispatcher = Mockito.mock(MessageDispatcher.class);
		Mockito.when(dispatcher.performTransaction(Mockito.anyString(), Mockito.any(char[].class),
				Mockito.any())).thenReturn(new TransactionResult(true, "", 0, new double[] {5000}));

		session = new Session(mainPanel, dispatcher);
		session.insertCard();
		session.addCard("4111111111111111");
		session.addPin(new char[] {'1', '2', '3', '4', '5'});
		session.setTransaction(ATMTransactionFactory.createTransaction(TransactionType.Withdrawal));
		session.setFromAccount(AccountType.Chequing);
	}

	@ParameterizedTest
	@ValueSource(ints = {20, 40, 50, 60, 70, 100, 980, 1000})
	void acceptsWithdrawalAmountsDispensableByTwentyOrFifty(int amount) {
		assertDoesNotThrow(() -> session.setAmount(amount));
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, 0, 1, 10, 19, 21, 30, 49, 51, 59, 999, 1001, 1020})
	void rejectsWithdrawalAmountsOutsideTheDailyLimitOrNotDispensable(int amount) {
		assertThrows(InvalidAmountException.class, () -> session.setAmount(amount));
	}

	@ParameterizedTest
	@ValueSource(strings = {"00000", "12345", "99999"})
	void acceptsFiveDigitPins(String pin) {
		assertDoesNotThrow(() -> formatChecker.checkPinFormat(pin.toCharArray()));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "1234", "123456", "12a45", "12:45"})
	void rejectsInvalidPinFormats(String pin) {
		assertThrows(InvalidPinFormatException.class,
				() -> formatChecker.checkPinFormat(pin.toCharArray()));
	}
}
