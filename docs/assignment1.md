# Assignment 1 Test Cases

Inside this document are the session testing records

## Session testing

### Technique and assumptions

Robust Worst-Case Boundary Value Analysis was used for the PIN format and withdrawal amount inputs using the single fault assumption. 
For each input is the input is changed around one boundary while the remaining setup is held valid.

Since for the PIN format needs exactly 5 digits. THe tested boundaries are 4,5,6 characters, as well as invalid non digit characters for rubust out of range values.

For the withdrawal amounts the ATM can only dispense twenty and fifty dollar bills. The amounts selected at valid denomination and those immediately outside of those combination ranges. Since we have account with balance of 5000 dollars and a daily withdraw of 1000 dollars so 1000 dollars and values near by are used as the upper limit boundary.

### Test cases

#### PIN format

| ID | Input | Expected result |
|---|---|---|
| PIN-01 | `00000` | Accepted |
| PIN-02 | `12345` | Accepted |
| PIN-03 | `99999` | Accepted |
| PIN-04 | Empty PIN | `InvalidPinFormatException` |
| PIN-05 | `1234` | `InvalidPinFormatException` |
| PIN-06 | `123456` | `InvalidPinFormatException` |
| PIN-07 | `12a45` | `InvalidPinFormatException` |
| PIN-08 | `12:45` | `InvalidPinFormatException` |

#### Withdrawal amount

| ID | Input | Expected result |
|---|---:|---|
| AMT-01 | 20 | Accepted |
| AMT-02 | 40 | Accepted |
| AMT-03 | 50 | Accepted |
| AMT-04 | 60 | Accepted |
| AMT-05 | 70 | Accepted |
| AMT-06 | 100 | Accepted |
| AMT-07 | 980 | Accepted |
| AMT-08 | 1,000 | Accepted |
| AMT-09 | -1 | `InvalidAmountException` |
| AMT-10 | 0 | `InvalidAmountException` |
| AMT-11 | 1 | `InvalidAmountException` |
| AMT-12 | 10 | `InvalidAmountException` |
| AMT-13 | 19 | `InvalidAmountException` |
| AMT-14 | 21 | `InvalidAmountException` |
| AMT-15 | 30 | `InvalidAmountException` |
| AMT-16 | 49 | `InvalidAmountException` |
| AMT-17 | 51 | `InvalidAmountException` |
| AMT-18 | 59 | `InvalidAmountException` |
| AMT-19 | 999 | `InvalidAmountException` |
| AMT-20 | 1,001 | `InvalidAmountException` |
| AMT-21 | 1,020 | `InvalidAmountException` |

### Test results

The JUnit implementation is in
`src/test/java/atm/SessionTest.java`. 

| Test group | Result against current implementation | Reason |
|---|---|---|
| PIN-01 through PIN-03 | Fail | Right now we expect 4 characters instead of 5 |
| PIN-04 through PIN-08 | Pass | Rejected by the current format check |
| AMT-01, AMT-02, AMT-03, AMT-05, AMT-06, AMT-08 | Pass | These amounts pass the denomination validator |
| AMT-04 and AMT-07 | Fail | The validator rejects valid combinations of $20 and $50 bills such as $60 and $980 |
| AMT-09, AMT-11 through AMT-20 | Pass | These invalid amounts are rejected by the current validator |
| AMT-10 and AMT-21 | Fail | The current validator accepts zero and the withdrawal does not keep inside the $1,000 limit for $1,020 |

### Fault localization

| Failing test(s) | Suspected fault (file, method, condition) | One-line justification |
|---|---|---|
| PIN-01 through PIN-03 | `src/main/java/atm/utils/FormatChecker.java`, `checkPinFormat` | The method checks `pin.length == 4`, although it requires five digits |
| AMT-04 and AMT-07 | `src/main/java/atm/utils/CashValidator.java`, `validateWithdrawal` | The validation rejects valid combined bill amounts such as $60 and $980. |
| AMT-10 | `src/main/java/atm/utils/CashValidator.java`, `validateWithdrawal` | The method has no positive lower bound check so amount `0` is accepted |
| AMT-21 | `src/main/java/atm/session/Session.java`, `setAmount` | The withdrawal allows denominations but never rejects an amount above the $1,000 daily limit |
