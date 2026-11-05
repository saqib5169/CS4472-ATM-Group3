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
