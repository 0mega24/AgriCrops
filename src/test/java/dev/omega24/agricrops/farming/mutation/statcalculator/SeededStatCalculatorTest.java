package dev.omega24.agricrops.farming.mutation.statcalculator;

import java.util.Random;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class SeededStatCalculatorTest {

    @Test
    public void normalCalculatorIsRepeatableForAFixedSeed() {
        ExposedNormal calculator = new ExposedNormal();
        assertArrayEquals(rollNormal(calculator, 8675309L), rollNormal(calculator, 8675309L));
    }

    @Test
    public void hardcoreCalculatorIsRepeatableForAFixedSeed() {
        ExposedHardcore calculator = new ExposedHardcore();
        assertArrayEquals(rollHardcore(calculator, 424242L), rollHardcore(calculator, 424242L));
    }

    private static int[] rollNormal(ExposedNormal calculator, long seed) {
        Random random = new Random(seed);
        int[] result = new int[32];
        for (int i = 0; i < result.length; i++) {
            result[i] = calculator.roll(10, 4, 1, random);
        }
        return result;
    }

    private static int[] rollHardcore(ExposedHardcore calculator, long seed) {
        Random random = new Random(seed);
        int[] result = new int[32];
        for (int i = 0; i < result.length; i++) {
            result[i] = calculator.roll(10, 4, 1, random);
        }
        return result;
    }

    private static final class ExposedNormal extends StatCalculatorNormal {
        private int roll(int input, int neighbours, int divisor, Random random) {
            return calculateStat(input, neighbours, divisor, random);
        }
    }

    private static final class ExposedHardcore extends StatCalculatorHardcore {
        private int roll(int input, int neighbours, int divisor, Random random) {
            return calculateStat(input, neighbours, divisor, random);
        }
    }
}
