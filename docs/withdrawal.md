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