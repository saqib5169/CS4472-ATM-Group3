package bank.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;


/**
 * Black-box tests for deposit fee rules in Assignment 1.
 * Expected values come from the assignment specification, not FeesCalculator.
 */
class DepositFeeTest {

    private static final double TOLERANCE = 0.000001;
    private final FeesCalculator calculator = new FeesCalculator();

    private void checkFee(String caseId, double deposit, double balance,
            boolean student, double expectedFee) {
        double actualFee = calculator.calculateDepositInterest(deposit, balance, student);
        assertEquals(expectedFee, actualFee, TOLERANCE, caseId);
    }

    @Test
    void d01StudentOver50BalanceOver500() {
        checkFee("D01", 100.0, 600.0, true, 0.50);
    }

    @Test
    void d02StudentOver50BalanceAt500() {
        checkFee("D02", 100.0, 500.0, true, 0.25);
    }

    @Test
    void d03StudentAt50BalanceOver5000() {
        checkFee("D03", 50.0, 6000.0, true, 0.25);
    }

    @Test
    void d04StudentAt50BalanceAt5000() {
        checkFee("D04", 50.0, 5000.0, true, 0.00);
    }

    @Test
    void d05NonStudentOver250BalanceOver2500() {
        checkFee("D05", 300.0, 3000.0, false, 2.40);
    }

    @Test
    void d06NonStudentOver250BalanceAt2500() {
        checkFee("D06", 300.0, 2500.0, false, 1.20);
    }

    @Test
    void d07NonStudentAt250BalanceOver10000() {
        checkFee("D07", 250.0, 11000.0, false, 0.00);
    }

    @Test
    void d08NonStudentAt250BalanceAt10000() {
        checkFee("D08", 250.0, 10000.0, false, 0.25);
    }

    // Additional representatives help distinguish incorrect thresholds from rates.
    @Test
    void d09StudentBetween50And100() {
        checkFee("D09", 75.0, 750.0, true, 0.375);
    }

    @Test
    void d10StudentOver100BalanceOver1000() {
        checkFee("D10", 150.0, 1200.0, true, 0.75);
    }

    @Test
    void d11NonStudentOver500BalanceBetween2500And5000() {
        checkFee("D11", 600.0, 3000.0, false, 4.80);
    }

    @Test
    void d12NonStudentOver500BalanceOver5000() {
        checkFee("D12", 600.0, 6000.0, false, 4.80);
    }
}
