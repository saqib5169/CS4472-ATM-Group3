package bank.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TransferFeesTest {
    FeesCalculator calculator = new FeesCalculator();

    @ParameterizedTest(name = "{0}")
    @CsvSource ({
        "DT1, 100, 1000, 500, true, 0.10",
        "DT2, 150, 1500, 1100, true, 0.075",
        "DT3, 50, 2200, 900, true, 0.025",
        "DT4, 175, 2500, 1300, true, 0.04375",
        "DT5, 250, 1500, 500, true, 0.125",
        "DT6, 300, 1700, 1100, true, 0.075",
        "DT7, 200, 2500, 700, true, 0.5",
        "DT8, 210, 3000, 1100, true, 0.2625",
        "DT9, 50, 3500, 1700, false, 0.1",
        "DT10, 75, 3750, 2600, false, 0.075",
        "DT11, 10, 4200, 1750, false, 0.1",
        "DT12, 35, 4500, 2700, false, 0.175",
        "DT13, 155, 1250, 750, false, 0.31",
        "DT14, 101, 150, 1555, false, 0.101",
        "DT15, 110, 2150, 555, false, 0.55",
        "DT16, 200, 3000, 1200, false, 0.5",
    })
    void transferTest(String id, double amt, double fromBalance, double toBalance, boolean student, double expected){
        assertEquals(expected, calculator.calculateTransferFee(amt, fromBalance, toBalance, student), 0.0001);
    }

}