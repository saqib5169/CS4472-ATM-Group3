# CS4472B — Assignment 1: Black-Box Testing

This document records the test-design approach, representative test cases, observed results, and suspected faults for the ATM system. **Expected outcomes come from the assignment specification, not from the existing implementation.** Source defects have intentionally not been fixed for Assignment 1.

## Testing scope and reported results

| Testing target | JUnit 5 test class | Required technique | Cases | Pass | Fail |
|---|---|---|---:|---:|---:|
| ATM Session: PIN format and withdrawal amount | `src/test/java/atm/SessionTest.java` | Robust Worst-Case Boundary Value Analysis, with single-fault assumption | 29 | 22 | 7 |
| Withdrawal fees | `src/test/java/bank/utils/WithdrawalFeesTest.java` | Robust Worst-Case Boundary Value Analysis | 9 | 3 | 6* |
| Deposit fees | `src/test/java/bank/utils/DepositFeeTest.java` | Weak Robust Equivalence Class Analysis | 12 | 1 | 11 |
| Transfer fees | `src/test/java/bank/utils/TransferFeesTest.java` | Decision Table Analysis | 16 | 0 | 16 |
| **Total reported** | | | **66** | **26** | **40** |

*W-09 exercises a negative balance. Its recorded JUnit failure is included in the count, but its expected result depends on an explicit assumption because the specification does not define invalid-balance handling. The figures above reproduce the individually documented results; the final integrated CI run must independently confirm the combined suite.*

## 1. Test design, process, and assumptions

### 1.1 ATM Session — Robust Worst-Case Boundary Value Analysis

**Target:** `atm.session.Session`, withdrawal transaction only (choice 1). The implementation is tested through PIN-format and withdrawal-amount validation, with fault localization in the Session class and supporting validators.

- A valid PIN contains **exactly five digits**. Tests include five-character valid representatives, four- and six-character cases, the empty string, and non-digit characters. Invalid PIN format is expected to throw `InvalidPinFormatException`.
- A valid withdrawal must be a positive amount that can be dispensed using any combination of **$20 and $50 bills**, and must respect the account's balance and daily withdrawal limit. Invalid amounts are expected to throw `InvalidAmountException`.
- The assignment requires an account with **$5,000 available balance** and **$1,000 daily withdrawal limit**; the $1,000 limit is the upper amount boundary. The JUnit fixture should be checked to ensure it creates these values.
- The recorded design changes one input or boundary condition while keeping the other setup valid, in keeping with the stated **single-fault assumption**. It includes representative near-boundary and invalid values, but does not document the full Cartesian product often associated with worst-case BVA. This is a coverage limitation, not a claim that unrun combinations passed.
- The results below are the team's reported observed outcomes. The uploaded report does not provide individual exception traces for each Session case.

### 1.2 Withdrawal fees — Robust Worst-Case Boundary Value Analysis

**Target:** `bank.utils.FeesCalculator.calculateWithdrawalFee(double amount, double accountBalance, boolean student, int dayOfWeek)`.

- Student and non-student customers are represented using both values of the `student` flag. For students, a **weekday** has a **0%** fee and a **weekend** has a **0.1%** fee.
- For non-students, the required balance tiers are **below $1,000 → 0.3%**, **$1,000 up to but not including $5,000 → 0.1%**, and **$5,000 or more → 0%**.
- Cases W-03–W-08 check monetary boundaries at **$999.99, $1,000.00, $1,000.01, $4,999.99, $5,000.00, and $5,000.01**. `Calendar.MONDAY` and `Calendar.SATURDAY` represent weekday and weekend respectively; the Java API takes a day integer rather than an `isWeekday` boolean.
- The withdrawal amount is held at **$100** for all nine cases to isolate the percentage rules. A $100 withdrawal is valid using $20/$50 bills.
- W-09 adds a **-$0.01 balance** as an exploratory robust case. Under a literal extension of the below-$1,000 fee rule the expected value would be $0.30, but negative-balance validity and rejection behavior are not specified. Its observed mismatch is recorded and **not treated as independently confirmed invalid-input behavior**.
- **Coverage limitation:** this suite varies account balance and student/day categories but not withdrawal amount or every possible combination of the inputs. Therefore it is a targeted boundary suite, not evidence of complete Robust Worst-Case BVA coverage.

### 1.3 Deposit fees — Weak Robust Equivalence Class Analysis

**Target:** `bank.utils.FeesCalculator.calculateDepositInterest(double amount, double accountBalance, boolean student)`.

The method name refers to *interest*, but the assignment specification defines a *deposit fee*; the expected values therefore follow the specification. Each expected fee is `deposit amount × fee rate` (for example, 0.5% is `0.005`). `student = true` denotes a student. The account balance is the balance **before** the deposit. All numeric comparisons follow the specified strict and inclusive thresholds; expected fees use a floating-point tolerance of `0.000001`.

The valid input space is partitioned by customer type and the applicable amount and balance thresholds. There are eight distinct valid fee-rule classes, represented by D01–D08. D09–D12 are additional valid representatives that help distinguish incorrect thresholds and rates.

#### Valid equivalence classes

| Class | Student | Deposit amount | Existing balance | Expected fee rate |
|---|---|---|---|---|
| V1 | Yes | > $50 | > $500 | 0.5% |
| V2 | Yes | > $50 | ≤ $500 | 0.25% |
| V3 | Yes | $0 < amount ≤ $50 | > $5,000 | 0.5% |
| V4 | Yes | $0 < amount ≤ $50 | ≤ $5,000 | 0% |
| V5 | No | > $250 | > $2,500 | 0.8% |
| V6 | No | > $250 | ≤ $2,500 | 0.4% |
| V7 | No | $0 < amount ≤ $250 | > $10,000 | 0% |
| V8 | No | $0 < amount ≤ $250 | ≤ $10,000 | 0.1% |

#### Identified invalid-input equivalence classes (not executed)

| Candidate class | Representative input | Limitation |
|---|---|---|
| Zero deposit | `amount = 0` | No deposit-specific rejection/return rule is given. |
| Negative deposit | `amount = -1` | No defined expected exception or result. |
| Negative account balance | `accountBalance = -0.01` | The fee specification does not define invalid-balance handling. |
| Non-finite numeric input | `NaN` or positive infinity | The API uses `double`, but the assignment gives no rule for these values. |

**Robustness limitation:** The executed 12-case suite covers **valid classes only**. These invalid classes have been identified but do not have a defensible pass/fail oracle under the supplied specification. The withdrawal-only `InvalidAmountException` requirement for ATM Session is not automatically a deposit validation rule. Full Weak Robust ECA coverage requires clarification or an explicitly approved assumption, followed by implementing and running those additional cases.

### 1.4 Transfer fees — Decision Table Analysis

**Target:** `bank.utils.FeesCalculator.calculateTransferFee(double amount, double fromAccountBalance, double toAccountBalance, boolean student)`.

- There are four binary decisions: student status, transfer amount relative to the appropriate cutoff, source balance relative to its cutoff, and destination balance relative to its cutoff. The complete decision table therefore contains **16 rules**, with one recorded case per rule.
- Student amount cutoff: **$200**; source/destination balance cutoffs: **$2,000/$1,000**.
- Non-student amount cutoff: **$100**. For amounts below $100, source/destination cutoffs are **$4,000/$2,000**; otherwise they are **$2,000/$1,000**.
- A value exactly at a cutoff belongs to the “or more” group. In particular, DT7 uses **$200**, so it follows the student $200-or-more rule.
- The chosen test values represent each logical rule rather than independently testing every threshold. Valid positive transfer amounts and non-negative balances are assumed; invalid-transfer behavior is not specified. Fees are `amount × fee rate`, with tolerance `0.0001` used by the reported tests.

#### Transfer decision table

`T` means the stated condition is true; `F` means false. For C2–C4, **the numeric cutoff depends on the customer type and transfer amount**, as shown below. `X` indicates the expected fee rate for that rule.

| | R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8 | R9 | R10 | R11 | R12 | R13 | R14 | R15 | R16 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| C1: Student? | T | T | T | T | T | T | T | T | F | F | F | F | F | F | F | F |
| C2: Amount < cutoff? | T | T | T | T | F | F | F | F | T | T | T | T | F | F | F | F |
| C3: From < cutoff? | T | T | F | F | T | T | F | F | T | T | F | F | T | T | F | F |
| C4: To < cutoff? | T | F | T | F | T | F | T | F | T | F | T | F | T | F | T | F |
| A1: 0.1% | X | | | | | | | | | X | | | | X | | |
| A2: 0.05% | | X | X | | X | | | | | | | | | | | |
| A3: 0.025% | | | | X | | X | | | | | | | | | | |
| A4: 0.25% | | | | | | | X | | | | | | | | | X |
| A5: 0.125% | | | | | | | | X | | | | | | | | |
| A6: 0.2% | | | | | | | | | X | | | | X | | | |
| A7: 1% | | | | | | | | | | | X | | | | | |
| A8: 0.5% | | | | | | | | | | | | X | | | X | |

#### Decision cutoffs by rule

| Rules | Amount | From-balance | To-balance |
|---|---|---|---|
| R1–R8 (student) | $200 | $2,000 | $1,000 |
| R9–R12 (non-student, amount < $100) | $100 | $4,000 | $2,000 |
| R13–R16 (non-student, amount ≥ $100) | $100 | $2,000 | $1,000 |

## 2. Test cases and observed results

### 2.1 ATM Session — PIN validation

| Test ID | Input | Expected result | Actual result (reported) | Status |
|---|---|---|---|---|
| PIN-01 | `00000` | Accepted | Rejected (four-character format check) | **FAIL** |
| PIN-02 | `12345` | Accepted | Rejected (four-character format check) | **FAIL** |
| PIN-03 | `99999` | Accepted | Rejected (four-character format check) | **FAIL** |
| PIN-04 | Empty PIN | `InvalidPinFormatException` | Rejected as invalid | PASS |
| PIN-05 | `1234` | `InvalidPinFormatException` | Rejected as invalid | PASS |
| PIN-06 | `123456` | `InvalidPinFormatException` | Rejected as invalid | PASS |
| PIN-07 | `12a45` | `InvalidPinFormatException` | Rejected as invalid | PASS |
| PIN-08 | `12:45` | `InvalidPinFormatException` | Rejected as invalid | PASS |

**Result:** 8 recorded cases, **5 passed** and **3 failed**. Valid five-digit PINs PIN-01–PIN-03 were rejected due to the implementation's four-character length check.

### 2.2 ATM Session — Withdrawal amount validation

| Test ID | Input | Expected result | Actual result (reported) | Status |
|---|---|---|---|---|
| AMT-01 | 20 | Accepted | Accepted | PASS |
| AMT-02 | 40 | Accepted | Accepted | PASS |
| AMT-03 | 50 | Accepted | Accepted | PASS |
| AMT-04 | 60 | Accepted | Rejected by denomination validation | **FAIL** |
| AMT-05 | 70 | Accepted | Accepted | PASS |
| AMT-06 | 100 | Accepted | Accepted | PASS |
| AMT-07 | 980 | Accepted | Rejected by denomination validation | **FAIL** |
| AMT-08 | 1,000 | Accepted | Accepted | PASS |
| AMT-09 | -1 | `InvalidAmountException` | Rejected as invalid | PASS |
| AMT-10 | 0 | `InvalidAmountException` | Accepted by validation | **FAIL** |
| AMT-11 | 1 | `InvalidAmountException` | Rejected as invalid | PASS |
| AMT-12 | 10 | `InvalidAmountException` | Rejected as invalid | PASS |
| AMT-13 | 19 | `InvalidAmountException` | Rejected as invalid | PASS |
| AMT-14 | 21 | `InvalidAmountException` | Rejected as invalid | PASS |
| AMT-15 | 30 | `InvalidAmountException` | Rejected as invalid | PASS |
| AMT-16 | 49 | `InvalidAmountException` | Rejected as invalid | PASS |
| AMT-17 | 51 | `InvalidAmountException` | Rejected as invalid | PASS |
| AMT-18 | 59 | `InvalidAmountException` | Rejected as invalid | PASS |
| AMT-19 | 999 | `InvalidAmountException` | Rejected as invalid | PASS |
| AMT-20 | 1,001 | `InvalidAmountException` | Rejected as invalid | PASS |
| AMT-21 | 1,020 | `InvalidAmountException` | Not rejected by daily-limit validation | **FAIL** |

**Result:** 21 recorded cases, **17 passed** and **4 failed**. AMT-04 ($60) and AMT-07 ($980) are dispensable but were rejected. AMT-10 ($0) was not rejected as invalid, and AMT-21 ($1,020) was not rejected by the required daily-limit validation.

**Session scope note:** The Session tables report qualitative actual outcomes from the teammate's documentation; the original report did not give actual exception messages or a raw per-case JUnit log. The test IDs `AMT-*` mean **withdrawal amount**, not “ATM.”

### 2.3 Withdrawal fee test cases and results

All fee amounts below are in dollars. The tested withdrawal amount is $100 in each case.

| Test ID | isStudent | Day (Calendar) | Balance  | Expected Fee | Actual Result | Status |
|---------|-----------|----------------|----------|--------------|---------------|--------|
| W-01    | True      | MONDAY (2)     | 500.00   | 0.00         | 0.10          | **FAIL** |
| W-02    | True      | SATURDAY (7)   | 500.00   | 0.10         | 0.00          | **FAIL** |
| W-03    | False     | MONDAY (2)     | 999.99   | 0.30         | 0.20          | **FAIL** |
| W-04    | False     | MONDAY (2)     | 1000.00  | 0.10         | 0.10          | PASS   |
| W-05    | False     | MONDAY (2)     | 1000.01  | 0.10         | 0.10          | PASS   |
| W-06    | False     | MONDAY (2)     | 4999.99  | 0.10         | 0.10          | PASS   |
| W-07    | False     | MONDAY (2)     | 5000.00  | 0.00         | 0.10          | **FAIL** |
| W-08    | False     | MONDAY (2)     | 5000.01  | 0.00         | 0.10          | **FAIL** |
| W-09    | False     | MONDAY (2)     | -0.01    | 0.30         | 0.20          | **FAIL** |

**Result:** 9 recorded cases: **3 passed** and **6 failed assertions**. W-09 is assumption-dependent for the reasons explained in §1.2, so its status should not be interpreted as a validated negative-balance requirement.

### 2.4 Deposit fee test cases and results

Deposit tests were run November 4, 2026 using `DepositFeeTest` (12 tests, 11 failures, 1 pass, 0 errors, 0 skipped). Values below were taken from the reported Maven execution. Test D03 passed; the other 11 cases failed their specification-based assertions.

| ID | Class | Student | Deposit ($) | Balance ($) | Rate | Expected ($) | Actual ($) | Result | Purpose |
|---|---|---|---:|---:|---:|---:|---:|---|---|
| D01 | V1 | Yes | 100 | 600 | 0.5% | 0.50 | 0.20 | **FAIL** | Student, >$50 and balance >$500 |
| D02 | V2 | Yes | 100 | 500 | 0.25% | 0.25 | 0.20 | **FAIL** | Student, balance exactly $500 |
| D03 | V3 | Yes | 50 | 6000 | 0.5% | 0.25 | 0.25 | **PASS** | Student, amount exactly $50; balance >$5,000 |
| D04 | V4 | Yes | 50 | 5000 | 0% | 0.00 | 0.10 | **FAIL** | Student, amount exactly $50; balance exactly $5,000 |
| D05 | V5 | No | 300 | 3000 | 0.8% | 2.40 | 0.00 | **FAIL** | Non-student, amount >$250 and balance >$2,500 |
| D06 | V6 | No | 300 | 2500 | 0.4% | 1.20 | 0.00 | **FAIL** | Non-student, balance exactly $2,500 |
| D07 | V7 | No | 250 | 11000 | 0% | 0.00 | 1.25 | **FAIL** | Non-student, amount exactly $250 and balance >$10,000 |
| D08 | V8 | No | 250 | 10000 | 0.1% | 0.25 | 0.00 | **FAIL** | Non-student, amount exactly $250 and balance exactly $10,000 |
| D09 | V1 extra | Yes | 75 | 750 | 0.5% | 0.375 | 0.15 | **FAIL** | Separates assignment cutoff $50 from implemented cutoff $100 |
| D10 | V1 extra | Yes | 150 | 1200 | 0.5% | 0.75 | 1.50 | **FAIL** | Tests the higher student rate path |
| D11 | V5 extra | No | 600 | 3000 | 0.8% | 4.80 | 3.00 | **FAIL** | Investigates the $2,500 versus $5,000 balance cutoffs |
| D12 | V5 extra | No | 600 | 6000 | 0.8% | 4.80 | 6.00 | **FAIL** | Investigates higher non-student percentage |

**Execution details:** The original test output showed compilation and JUnit execution succeeded, with assertion mismatches accounting for the strict-mode `BUILD FAILURE`. A Maven certificate/metadata warning did not prevent execution. A later informational run completed with `BUILD SUCCESS` while still reporting the 11 failures. Passing Checkstyle on the final, integrated `dev` branch must be verified separately; do not infer full-project success from this single test run. JaCoCo agent initialization alone is not proof that a coverage report was published.

### 2.5 Transfer fee test cases

| ID | Student | Amount | From | To | Rate | Expected fee |
|---|---|---|---|---|---|---|
| DT1 | Yes | $100 | $1,000 | $500 | 0.1% | $0.10 |
| DT2 | Yes | $150 | $1,500 | $1,100 | 0.05% | $0.075 |
| DT3 | Yes | $50 | $2,200 | $900 | 0.05% | $0.025 |
| DT4 | Yes | $175 | $2,500 | $1,300 | 0.025% | $0.04375 |
| DT5 | Yes | $250 | $1,500 | $500 | 0.05% | $0.125 |
| DT6 | Yes | $300 | $1,700 | $1,100 | 0.025% | $0.075 |
| DT7 | Yes | $200 | $2,500 | $700 | 0.25% | $0.50 |
| DT8 | Yes | $210 | $3,000 | $1,100 | 0.125% | $0.2625 |
| DT9 | No | $50 | $3,500 | $1,700 | 0.2% | $0.10 |
| DT10 | No | $75 | $3,750 | $2,600 | 0.1% | $0.075 |
| DT11 | No | $10 | $4,200 | $1,750 | 1% | $0.10 |
| DT12 | No | $35 | $4,500 | $2,700 | 0.5% | $0.175 |
| DT13 | No | $155 | $1,250 | $750 | 0.2% | $0.31 |
| DT14 | No | $101 | $150 | $1,555 | 0.1% | $0.101 |
| DT15 | No | $110 | $2,150 | $555 | 0.5% | $0.55 |
| DT16 | No | $200 | $3,000 | $1,200 | 0.25% | $0.50 |

### 2.6 Transfer fee results

| ID | Expected | Actual | Pass/Fail |
|---|---|---|---|
| DT1 | $0.10 | $2.50 | Fail |
| DT2 | $0.075 | $1.875 | Fail |
| DT3 | $0.025 | $2.50 | Fail |
| DT4 | $0.04375 | $2.1875 | Fail |
| DT5 | $0.125 | $6.25 | Fail |
| DT6 | $0.075 | $3.75 | Fail |
| DT7 | $0.50 | $5.00 | Fail |
| DT8 | $0.2625 | $2.625 | Fail |
| DT9 | $0.10 | $2.50 | Fail |
| DT10 | $0.075 | $3.75 | Fail |
| DT11 | $0.10 | $0.50 | Fail |
| DT12 | $0.175 | $1.75 | Fail |
| DT13 | $0.31 | $7.75 | Fail |
| DT14 | $0.101 | $0.505 | Fail |
| DT15 | $0.55 | $5.50 | Fail |
| DT16 | $0.50 | $11.00 | Fail |

**Result:** 16 recorded cases, **16 failures** against the current fee implementation. All expected amounts were calculated from the written decision-table rules. Individual fee mismatches can involve multiple incorrect conditions or rates.

## 3. Fault localization

Each row below links the affected **failing test IDs** to the relevant class and method and, where known, the specific condition or assignment. Several failing cases can expose a shared defect; conversely, multiple errors may contribute to one failure. These are suspected fault locations, not claims of uniquely proven root causes.

### 3.1 Session validation

| Failing case(s) | Suspected fault (file and method) | Explanation |
|---|---|---|
| PIN-01–PIN-03 | `src/main/java/atm/utils/FormatChecker.java` — `checkPinFormat`, four-character PIN check | The code expects a PIN length of four, although the specification requires five digits. |
| AMT-04, AMT-07 | `src/main/java/atm/utils/CashValidator.java` — `validateWithdrawal`, denomination logic | Valid amounts of $60 and $980 can be dispensed using $20 and $50 bills but were rejected by validation. |
| AMT-10 | `src/main/java/atm/utils/CashValidator.java` — `validateWithdrawal`, positivity check | The validator does not reject zero as an invalid withdrawal amount. |
| AMT-21 | `src/main/java/atm/session/Session.java` — `setAmount`, daily-limit enforcement | A $1,020 withdrawal was not rejected even though the account's daily limit is $1,000. |

### 3.2 Withdrawal fee calculation

All conditions below belong to `src/main/java/bank/utils/FeesCalculator.java`, method `calculateWithdrawalFee`.

| Failing case(s) | Suspected condition/statement | Explanation |
|---|---|---|
| W-01, W-02 | Weekend branch assigns `feePercentage = 0.0`, while the weekday path assigns `0.001` | Student fees are reversed: the specification requires no fee on weekdays and 0.1% on weekends. |
| W-03 (also W-09 under the stated assumption) | Non-student balance `< 1000.00` assigns `feePercentage = 0.002` | The non-student fee below $1,000 is 0.2% in the implementation rather than the required 0.3%. W-09 does not establish a separate invalid-balance contract. |
| W-07, W-08 | Non-student middle tier checks `accountBalance < 10000.00` | The tier should end below $5,000, so accounts at or above $5,000 incorrectly remain subject to the 0.1% charge. |

### 3.3 Deposit fee calculation

All entries below refer to `src/main/java/bank/utils/FeesCalculator.java`, method `calculateDepositInterest`.

| Fault ID | Relevant failing case(s) | Suspected condition or statement | One-sentence justification |
|---|---|---|---|
| F-DEP-01 | D01, D02, D09 | Student branch uses `amount > 100.00` instead of `amount > 50.00`. | Deposits of $75 and $100 should follow the above-$50 fee rules, but the implementation routes them through its low-amount branch. |
| F-DEP-02 | D04 (also affects D01, D02, D09 after misrouting) | Student low-amount/low-balance path assigns `interestPercentage = 0.002` rather than `0.0` for amounts ≤$50 and balance ≤$5,000. | D04 should have no fee, but the implementation charges $0.10 on a $50 deposit. |
| F-DEP-03 | D10 | Student high-amount/high-balance path uses `interestPercentage = 0.01` rather than `0.005`. | D10 returns $1.50 on $150, even though the required 0.5% fee is $0.75. |
| F-DEP-04 | D05, D06 | Non-student branch uses `amount > 500.00` instead of `amount > 250.00`. | Both $300 deposits are treated as small deposits even though they should follow the over-$250 rules. |
| F-DEP-05 | D07 | Non-student low-amount/high-balance path assigns `interestPercentage = 0.005` instead of `0.0`. | D07 incorrectly charges $1.25 although the specified fee for a $250 deposit with $11,000 balance is zero. |
| F-DEP-06 | D08 (also influences D05, D06 after misrouting) | Non-student low-amount/low-balance path assigns `interestPercentage = 0.0` instead of `0.001`. | D08 returns no fee, while the specified fee for $250 with $10,000 balance is $0.25. |
| F-DEP-07 | D11 | Non-student high-amount balance cutoff uses `accountBalance > 5000.00` instead of `> 2500.00`. | D11 has a $3,000 balance and should use the higher-balance fee rule, but the implementation selects the lower branch and returns $3.00. |
| F-DEP-08 | D12 | Non-student high-amount/high-balance path uses `interestPercentage = 0.01` instead of `0.008`. | D12 charges $6.00 (1%) instead of the required $4.80 (0.8%). |

**Additional source-code discrepancies not separately isolated by these failures:** The student high-amount balance cutoff is coded as `> 1000.00` rather than the specified `> 500.00`. The student high-amount/lower-balance rate is coded as `0.005` rather than `0.0025`, and the non-student high-amount/lower-balance rate as `0.005` rather than `0.004`. Further targeted tests would be needed to isolate each of those conditions independently.

### 3.4 Transfer fee calculation

All entries refer to `src/main/java/bank/utils/FeesCalculator.java`, method `calculateTransferFee`. For concise fault localization, `F6a`–`F6d` distinguish **different fee assignments** that were collectively labeled F6 in the team's first draft. Affected cases can appear in multiple rows because incorrect branch conditions and incorrect percentages interact.

| Fault ID | Failing case(s) | Suspected condition or assignment | One-sentence justification |
|---|---|---|---|
| F1 | DT1, DT2, DT4 | Student `if (amount < 100.00)` instead of `< 200.00` | Student transfers from $100 through $199 are routed into the $200-or-more branch. |
| F2 | DT1, DT2, DT5, DT6 | Student `if (fromAccountBalance < 1000.00)` instead of `< 2000.00` in both amount paths | Student source balances from $1,000 to $1,999 are treated as belonging to the high-balance tier. |
| F3 | DT9, DT10 | Non-student small-transfer source cutoff `< 1000.00` rather than `< 4000.00` | Source balances of $3,500 and $3,750 follow the wrong source-balance tier. |
| F4 | DT9, DT11 | Non-student small-transfer destination cutoff `< 1000.00` rather than `< 2000.00` | Destination balances of $1,700 and $1,750 are misclassified, contributing to incorrect fees. |
| F5 | DT13 | Non-student transfer of at least $100 uses source cutoff `< 1000.00` rather than `< 2000.00` | A source balance of $1,250 is incorrectly treated as high. |
| F6a | DT7; also DT1 and DT5 after incorrect branching | Student at-least-$100/high-source/low-destination assignment `0.025` rather than the corresponding specified `0.0025` for the $200-or-more high-source case | DT7 charges $5.00 instead of $0.50; DT1 and DT5 also hit this assignment after being routed incorrectly. |
| F6b | DT8; also DT2, DT4, DT6 after incorrect branching | Student at-least-$100/high-source/high-destination assignment `0.0125` rather than `0.00125` for the specified $200-or-more high-source case | DT8 charges $2.625 instead of $0.2625; other cases additionally suffer from bad thresholds. |
| F6c | DT12; also DT9, DT10, DT11 after incorrect branching | Non-student under-$100/high-source/high-destination assignment `0.05` rather than `0.005` | DT12 is charged $1.75 instead of $0.175; the other cases also follow wrong balance tiers. |
| F6d | DT15; also DT13 after incorrect branching | Non-student at-least-$100/high-source/low-destination assignment `0.05` rather than `0.005` | DT15 is charged $5.50 instead of $0.55, while DT13 additionally has a misclassified source balance. |
| F7 | DT3 | Student under-$100/high-source/low-destination assignment `0.05` rather than `0.0005` | The code charges $2.50 rather than $0.025 by applying 5% instead of 0.05%. |
| F8 | DT14 | Non-student at-least-$100/low-source/high-destination assignment `0.005` rather than `0.001` | The code charges $0.505 rather than $0.101 by applying 0.5% instead of 0.1%. |
| F9 | DT16 | Non-student at-least-$100/high-source/high-destination assignment `0.055` rather than `0.0025` | The code charges $11.00 instead of $0.50 by applying 5.5% instead of 0.25%. |

**Limit of localization:** The differing source thresholds and rates above were analyzed against the test results, but not all underlying branches were individually isolated. The final `phase:tests` PR should keep the failing tests and record the suspected faults, rather than changing production code.

## 4. Evidence, limitations, and handoff

- **Test code:** The four JUnit 5 classes listed in the opening table belong under `src/test/java`. Their filenames should match the actual committed repository.
- **Test execution:** The per-area tables preserve the submitted test results. The **combined `dev` branch** must be checked against the final JUnit/CI run; this document does not assert an unprovided final CI status.
- **Deposit failure log:** The separately maintained `deposit-failure-log.md` records the eight grouped deposit fault IDs. The tester reported a **shared 120 minutes** for test design/setup/execution before seeing the failures and a **shared 45 minutes** investigating them (total **165 minutes**), rather than invented per-fault durations. Preserve it for Assignment 3.
- **Other failure logs:** Each teammate should preserve their own failure IDs, test mappings, and approximate discovery/diagnosis times for Assignment 3; those records were not supplied with this document.
- **Known coverage limitations:** Deposit invalid-input equivalence classes are identified but not executed; the withdrawal-fee suite fixes the withdrawal amount at $100; and the Session cases do not demonstrate exhaustive worst-case combinations. These limitations should be disclosed rather than described as complete coverage.
- **PR and submission:** Submit a single `dev` → `main` PR labeled **`phase:tests`**. The PR description must explain the techniques and specification traceability and include fault localization. Verify `mvn -B checkstyle:check`, the phase-appropriate CI test run, and the informational coverage report; obtain the required teammate approvals, merge, and submit the repository URL and merged PR URL on OWL. Existing production faults are not fixed in this phase.

