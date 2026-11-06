# Assignment 1 — Deposit fee testing

**Target:** `bank.utils.FeesCalculator.calculateDepositInterest(double amount, double accountBalance, boolean student)`  
**Technique:** Weak Robust Equivalence Class Analysis (ECA)  
**Execution:** `mvn -B -Dtest=DepositFeeTest test`  
**Executed on:** November 4, 2026, completed 15:03:02 EST (from Maven log)  
**Observed outcome:** 12 tests executed, 11 assertion failures, 1 pass, 0 errors, 0 skipped. Checkstyle reported 0 violations.

## 1. Test design, process and assumptions

I generated the expected results from the assignment's written deposit fee specification, independently of the existing Java implementation. The three inputs are student status, amount deposited, and the account balance **before** the deposit. The expected fee is `amount × fee rate`; for example, 0.5% is a rate of `0.005`. The current Java method is named `calculateDepositInterest`, but the assignment calls for fee checking, so the expected result follows the specification rather than the method name.

For weak equivalence-class coverage, the valid input space was divided by student status and the amount and balance thresholds relevant to that student status. The resulting eight distinct fee-rule classes each have one representative (D01–D08). D09–D12 are extra valid representatives chosen to help isolate differences in conditions and fee rates. Expected amounts are compared using a tolerance of `0.000001` to avoid floating-point false positives.

**Assumptions:** Dollar inputs are expressed as `double`; the student flag is true for a student. Thresholds are interpreted literally (`>` excludes equality; `≤` includes it). Valid deposit amounts are positive. The assignment does not define input-validation behavior for the deposit fee calculator.

### Valid equivalence classes

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

### Robust / invalid input classes — unresolved test oracle

Weak *robust* ECA also calls for invalid classes. Potential candidates are zero deposit, negative deposit, and possibly negative or nonfinite balances. These are not assigned pass/fail outcomes because the supplied deposit-fee specification does not say whether `FeesCalculator` should reject them, throw an exception, or return a fee. The ATM Session `InvalidAmountException` rule is withdrawal-specific and should not be assumed to apply here. These classes should be confirmed with the instructor/TA or supported by an explicit, approved assumption; **the executed 12-case suite currently covers valid classes only**. Do not present that as full robust coverage.

## 2. Test case table and actual JUnit results

The table below contains outcomes from the actual Maven run, not predicted values.

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

**Execution notes:** The Maven output reports successful compilation and `0 Checkstyle violations` before JUnit runs. `BUILD FAILURE` was caused by eleven JUnit assertion mismatches, not by a compilation error. A Maven repository metadata `PKIX path building failed` warning also appeared, but it did not prevent the tests from running. JaCoCo's `prepare-agent` step ran; coverage report publication is not demonstrated by this test output alone.

## 3. Fault localization — suspected causes

Every entry below refers to `bank.utils.FeesCalculator.calculateDepositInterest`. A failing case can be affected by **more than one** incorrect condition or fee rate; these are grouped by suspected source statement rather than treated as eleven independent defects. Evidence comes from comparing the assignment rules with the implementation and the executed results.

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

**Additional code-review observations, not independently isolated by these failing cases:** In the student high-amount branch, `accountBalance > 1000.00` differs from the specified `> 500.00`, and the lower-rate assignment `0.005` differs from `0.0025` where the balance is ≤$500. In the non-student high-amount lower-balance branch, `0.005` differs from the required `0.004` for balances ≤$2,500. Since multiple wrong conditions/rates interact, extra targeted cases would help isolate these without ambiguity. Do not claim that a particular failing case proves all these individual defects by itself.

## 4. Failure-log and submission notes

The separate `deposit-failure-log.md` lists the grouped suspected defects and failed test IDs. **Fill in your actual minutes spent discovering and diagnosing each fault**; the Maven log does not contain these times. Keep the Maven/Surefire evidence for Assignment 3. Copy these test cases, results and analysis into the group's combined `docs/assignment1.md`, and transfer the grouped fault entries into the graded PR template.

**Do not fix** `src/main/java/bank/utils/FeesCalculator.java` for Assignment 1. Preserve the failing tests. Run `mvn -B checkstyle:check`, commit your own test class and documentation to `feature/deposit-tests`, then open a review PR into `dev`.

## Transfer fees: Decision Table Analysis

### 1. Process and assumptions
- The spec has 4 conditions, so the table has 2⁴ = 16 rules, with one test per rule. No rules were merged, because every condition affects the fee in every branch.
- The cutoffs depend on the branch: the amount cutoff is $200 for students and $100 for non-students. From/to balance cutoffs are $2,000/$1,000, except for non-students under $100, which use $4,000/$2,000.
- "Otherwise" in the spec is read as the to-balance not being below its cutoff, within the same from-balance branch.
- "Less than" is treated as true and "or more" as false, so a value exactly at a cutoff falls on the "or more" side. DT7 uses exactly $200, so it falls under the $200-or-more student rules.
- Test values were chosen inside each rule rather than on the cutoffs, since boundaries are covered by boundary value analysis.
- All inputs are assumed valid (positive amounts and non-negative balances), since the spec does not define behaviour for invalid inputs.
- The expected fee is the amount multiplied by the rate, in dollars. Results are compared with a tolerance of 0.0001 to account for floating-point rounding.

### 2. Decision table
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

**Cutoffs by rule:**
| Rules | Amount | From-balance | To-balance |
|---|---|---|---|
| R1–R8 (student) | $200 | $2,000 | $1,000 |
| R9–R12 (non-student, amount < $100) | $100 | $4,000 | $2,000 |
| R13–R16 (non-student, amount ≥ $100) | $100 | $2,000 | $1,000 |

### 3. Test cases
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

### 4. Results
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

16 tests run, 16 failed.

### 5. Fault localization
| Fault | Class / method | Condition or statement | Test cases | Justification |
|---|---|---|---|---|
| F1 | `FeesCalculator.calculateTransferFee` | Student branch: `if (amount < 100.00)` should be `amount < 200.00` | DT1, DT2, DT4 | Student transfers of $100–$199 are sent to the $200-or-more branch because the student amount cutoff is $100 instead of $200. |
| F2 | `FeesCalculator.calculateTransferFee` | Student branch: `if (fromAccountBalance < 1000.00)` (both amount sub-branches) should be `< 2000.00` | DT1, DT2, DT5, DT6 | Students with a from-balance of $1,000–$1,999 are treated as high-balance because the cutoff is $1,000 instead of $2,000. |
| F3 | `FeesCalculator.calculateTransferFee` | Non-student, amount < $100: `if (fromAccountBalance < 1000.00)` should be `< 4000.00` | DT9, DT10 | Non-students under $100 with a from-balance of $1,000–$3,999 take the high-balance path because the cutoff is $1,000 instead of $4,000. |
| F4 | `FeesCalculator.calculateTransferFee` | Non-student, amount < $100: `if (toAccountBalance < 1000.00)` should be `< 2000.00` | DT9, DT11 | Non-students under $100 with a to-balance of $1,000–$1,999 take the high-balance path because the cutoff is $1,000 instead of $2,000. |
| F5 | `FeesCalculator.calculateTransferFee` | Non-student, amount ≥ $100: `if (fromAccountBalance < 1000.00)` should be `< 2000.00` | DT13 | A non-student with a from-balance of $1,250 is treated as high-balance because the cutoff is $1,000 instead of $2,000. |
| F6 | `FeesCalculator.calculateTransferFee` | Rates 10× too large: `feePercentage = 0.025` / `0.0125` (student ≥ amount, high from), `0.05` (non-student < $100, high from, high to), `0.05` (non-student ≥ $100, high from, low to) | DT1, DT2, DT4, DT5, DT6, DT7, DT8, DT9, DT10, DT11, DT12, DT13, DT15 | These rates have the decimal one place off (e.g. 0.25% coded as 0.025 instead of 0.0025), so every fee on these lines is 10× the specified amount. |
| F7 | `FeesCalculator.calculateTransferFee` | Student, amount < cutoff, high from, low to: `feePercentage = 0.05` should be `0.0005` | DT3 | The spec's 0.05% is coded as 0.05 (5%), so the fee is 100× too large. |
| F8 | `FeesCalculator.calculateTransferFee` | Non-student, amount ≥ $100, low from, high to: `feePercentage = 0.005` should be `0.001` | DT14 | The spec's 0.1% is coded as 0.005 (0.5%), so the fee is 5× too large. |
| F9 | `FeesCalculator.calculateTransferFee` | Non-student, amount ≥ $100, high from, high to: `feePercentage = 0.055` should be `0.0025` | DT16 | The spec's 0.25% is coded as 0.055 (5.5%), which matches no rate in the spec. |

### Withdrawal Fees - Robust Worst Case Boundary Value Analysis

#### 1. Process & Assumptions
- Booleans (`isStudent`, `isWeekday`) only have True/False boundaries. Calendar.MONDAY is used for weekdays, Calendar.SATURDAY for weekends.
- The balance thresholds are $1,000.00 and $5,000.00. The boundaries tested are $999.99, $1000.00, $1000.01 and $4999.99, $5000.00, $5000.01. 
- For the "Robust" aspect, a negative balance (-$0.01) is included as an invalid input.
- A static withdrawal amount of $100.00 is used across all tests to isolate the fee rate calculation.

#### 2. Test Cases & Results

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

#### 3. Fault Localization

**Fault 1: Flipped Weekday/Weekend Logic (Exposed by W-01, W-02)**
* **Class & Method:** `bank.utils.FeesCalculator.calculateWithdrawalFee`
* **Statement:** `if ((dayOfWeek == Calendar.SATURDAY) || (dayOfWeek == Calendar.SUNDAY)) { feePercentage = 0.0; }`
* **Justification:** The logic incorrectly assigns a 0% fee on weekends and a 0.1% fee on weekdays, which is the exact opposite of the business specification.

**Fault 2: Incorrect Fee for Balances Under $1,000 (Exposed by W-03, W-09)**
* **Class & Method:** `bank.utils.FeesCalculator.calculateWithdrawalFee`
* **Statement:** `if (accountBalance < 1000.00) { feePercentage = 0.002; }`
* **Justification:** The code applies a 0.2% fee (`0.002`) instead of the required 0.3% fee for non-students with balances below $1,000.

**Fault 3: Incorrect Upper Boundary for $5,000 Limit (Exposed by W-07, W-08)**
* **Class & Method:** `bank.utils.FeesCalculator.calculateWithdrawalFee`
* **Statement:** `else if (accountBalance < 10000.00) { feePercentage = 0.001; }`
* **Justification:** The code incorrectly sets the upper boundary for the 0.1% fee tier at $10,000 instead of $5,000, causing balances of $5,000 and above to be charged when they should have no fee.
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
