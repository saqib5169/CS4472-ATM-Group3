# Deposit testing — failure log (retain for Assignment 3)

**Test class:** `bank.utils.DepositFeeTest`  
**Test execution:** November 4, 2026 (Maven run completed at 15:03:02 EST)  
**Command:** `mvn -B -Dtest=DepositFeeTest test`  
**JUnit summary:** 12 tests run; 11 assertion failures; 1 passed; 0 errors; 0 skipped.  
**Method under investigation:** `bank.utils.FeesCalculator.calculateDepositInterest`

## Actual approximate effort recorded

| Activity | Approximate time | How it was recorded |
|---|---:|---|
| Test-case design, setup, implementation, and execution before observing the failures | **120 minutes** | One shared preparation/testing period, based on the tester's estimate |
| Investigating JUnit failures and diagnosing suspected source-code faults | **45 minutes** | One shared investigation period covering all the failures, based on the tester's estimate |
| **Total testing and diagnosis work** | **165 minutes (2 h 45 min)** | Combined estimate; not a sum of eight separate fault investigations |

**Discovery method:** All 11 failing test cases were identified together when the 12-case JUnit suite was executed. The resulting failures were then investigated together. Individual first-discovery timestamps and per-fault diagnosis durations were not recorded; the table below intentionally does not invent them. The Maven completion time records when the run ended, **not** when each fault was separately discovered.

## Grouped suspected faults

Each row records a suspected *source-level cause*, which may affect several failed test cases. The timing columns refer to **the same shared 120-minute test preparation/execution period and shared 45-minute diagnosis period**, not a fresh allocation for each row. **Do not add durations across rows.**

| Failure ID | Affected failed cases | Suspected cause | Discovery evidence | Testing before discovery | Diagnosis effort | Notes / evidence |
|---|---|---|---|---|---|---|
| F-DEP-01 | D01, D02, D09 | Student amount cutoff `>100` vs required `>50` | Same JUnit run, Nov. 4; exact individual time not recorded | Shared 120 min total | Shared 45 min total | D01: $0.50 vs $0.20; D02: $0.25 vs $0.20; D09: $0.375 vs $0.150 |
| F-DEP-02 | D04 | Student low-amount/low-balance rate `0.002` vs required `0.0` | Same JUnit run, Nov. 4; exact individual time not recorded | Shared 120 min total | Shared 45 min total | D04: expected $0.00, actual $0.10 |
| F-DEP-03 | D10 | Student high-amount/high-balance rate `0.01` vs required `0.005` | Same JUnit run, Nov. 4; exact individual time not recorded | Shared 120 min total | Shared 45 min total | D10: expected $0.75, actual $1.50 |
| F-DEP-04 | D05, D06 | Non-student amount cutoff `>500` vs required `>250` | Same JUnit run, Nov. 4; exact individual time not recorded | Shared 120 min total | Shared 45 min total | D05: $2.40 vs $0.00; D06: $1.20 vs $0.00 |
| F-DEP-05 | D07 | Non-student low-amount/high-balance rate `0.005` vs required `0.0` | Same JUnit run, Nov. 4; exact individual time not recorded | Shared 120 min total | Shared 45 min total | D07: expected $0.00, actual $1.25 |
| F-DEP-06 | D08 | Non-student low-amount/low-balance rate `0.0` vs required `0.001` | Same JUnit run, Nov. 4; exact individual time not recorded | Shared 120 min total | Shared 45 min total | D08: expected $0.25, actual $0.00 |
| F-DEP-07 | D11 | Non-student high-amount balance cutoff `>5000` vs required `>2500` | Same JUnit run, Nov. 4; exact individual time not recorded | Shared 120 min total | Shared 45 min total | D11: expected $4.80, actual $3.00; another wrong rate may also contribute |
| F-DEP-08 | D12 | Non-student high-amount/high-balance rate `0.01` vs required `0.008` | Same JUnit run, Nov. 4; exact individual time not recorded | Shared 120 min total | Shared 45 min total | D12: expected $4.80, actual $6.00 |

## Scope and interpretation

- **Time is shared, not per-fault:** The 120 minutes covers preparing and running the deposit test suite before seeing the failures. The 45 minutes covers the subsequent investigation of all failures. It would be inaccurate to report 120 + 45 minutes for each of the eight fault IDs.
- **Faults are suspected, not separately proven:** Some incorrect thresholds and percentages interact. Multiple failing tests can share a cause, and a single failing test may reflect more than one problem. The IDs and mappings match the corresponding `deposit.md` analysis.
- **One passing case:** D03 passed and is therefore not entered as a failure.
- **Other code discrepancies:** The original `deposit.md` also notes some conditions/rates not individually isolated by these twelve cases. Do not record them as separately discovered test failures without an additional test.
- **Evidence to retain:** The Maven output and Surefire XML report under `target/surefire-reports/`, or the corresponding CI run reference. The observed Maven build failure results from assertion failures; Checkstyle reported zero violations.
- **Assignment 1 rule:** Leave the production `FeesCalculator.java` implementation unchanged. This log is intended to support later analysis in Assignment 3.
