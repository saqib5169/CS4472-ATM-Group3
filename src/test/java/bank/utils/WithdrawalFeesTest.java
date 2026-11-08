package bank.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Calendar;

public class WithdrawalFeesTest {

    FeesCalculator calculator = new FeesCalculator();

    // W-01: Student + Weekday = 0 fee
    @Test
    public void testStudentWeekday() {
        double fee = calculator.calculateWithdrawalFee(100.00, 500.00, true, Calendar.MONDAY);
        assertEquals(0.00, fee, 0.001);
    }

    // W-02: Student + Weekend = 0.1% fee
    @Test
    public void testStudentWeekend() {
        double fee = calculator.calculateWithdrawalFee(100.00, 500.00, true, Calendar.SATURDAY);
        assertEquals(0.10, fee, 0.001);
    }

    // W-03: Non-student, Balance 999.99 = 0.3% fee
    @Test
    public void testNonStudentBalanceBelow1000() {
        double fee = calculator.calculateWithdrawalFee(100.00, 999.99, false, Calendar.MONDAY);
        assertEquals(0.30, fee, 0.001);
    }

    // W-04: Non-student, Balance 1000.00 = 0.1% fee
    @Test
    public void testNonStudentBalanceExactly1000() {
        double fee = calculator.calculateWithdrawalFee(100.00, 1000.00, false, Calendar.MONDAY);
        assertEquals(0.10, fee, 0.001);
    }

    // W-05: Non-student, Balance 1000.01 = 0.1% fee
    @Test
    public void testNonStudentBalanceJustAbove1000() {
        double fee = calculator.calculateWithdrawalFee(100.00, 1000.01, false, Calendar.MONDAY);
        assertEquals(0.10, fee, 0.001);
    }

    // W-06: Non-student, Balance 4999.99 = 0.1% fee
    @Test
    public void testNonStudentBalanceBelow5000() {
        double fee = calculator.calculateWithdrawalFee(100.00, 4999.99, false, Calendar.MONDAY);
        assertEquals(0.10, fee, 0.001);
    }

    // W-07: Non-student, Balance 5000.00 = 0 fee
    @Test
    public void testNonStudentBalanceExactly5000() {
        double fee = calculator.calculateWithdrawalFee(100.00, 5000.00, false, Calendar.MONDAY);
        assertEquals(0.00, fee, 0.001);
    }

    // W-08: Non-student, Balance 5000.01 = 0 fee
    @Test
    public void testNonStudentBalanceJustAbove5000() {
        double fee = calculator.calculateWithdrawalFee(100.00, 5000.01, false, Calendar.MONDAY);
        assertEquals(0.00, fee, 0.001);
    }

    // W-09: Robust (Invalid/Negative Balance)
    @Test
    public void testNonStudentNegativeBalance() {
        double fee = calculator.calculateWithdrawalFee(100.00, -0.01, false, Calendar.MONDAY);
        assertEquals(0.30, fee, 0.001);
    }
}
