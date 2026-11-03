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