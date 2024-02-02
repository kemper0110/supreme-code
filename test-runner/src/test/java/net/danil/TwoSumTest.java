package net.danil;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TwoSumTest {
    final TwoSum twoSum = new TwoSum();
    @Test
    void simpleTest() {
        assertEquals(3, twoSum.solve(1, 2));
        assertEquals(4, twoSum.solve(2, 2));
        assertEquals(5, twoSum.solve(1, 4));
    }
    @Test
    void calcTest() {
        assertEquals(1 + 2, twoSum.solve(1, 2));
        assertEquals(2 + 2, twoSum.solve(2, 2));
        assertEquals(1 + 4, twoSum.solve(1, 4));
    }

    @Test
    void randomTest() {
        final var v = new Random().nextInt(1, 15);
        assertEquals(v + 1, twoSum.solve(v, 1));
    }

    @Test
    void failTest() {
        System.out.println("Cringe");
//        throw new RuntimeException("aboba");
    }
}