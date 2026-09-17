package dev.apn7.shunya.feature.home.search.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculatorTest {

    private fun calc(input: String): String? = Calculator.calculate(input)

    @Test
    fun basicArithmeticWithPrecedence() {
        assertEquals("84", calc("12*(3+4)"))
        assertEquals("14", calc("2+3*4"))
        assertEquals("20", calc("(2+3)*4"))
        assertEquals("2.5", calc("5/2"))
        assertEquals("-1", calc("2-3"))
        assertEquals("6", calc("1 + 2 + 3"))
        assertEquals("0", calc("5-5"))
    }

    @Test
    fun leftAssociativeSubtractionAndDivision() {
        assertEquals("5", calc("10-3-2"))
        assertEquals("2", calc("16/4/2"))
    }

    @Test
    fun powerIsRightAssociativeAndBindsTighterThanUnaryMinus() {
        assertEquals("512", calc("2^3^2"))
        assertEquals("-4", calc("-2^2"))
        assertEquals("4", calc("(-2)^2"))
        assertEquals("0.5", calc("2^-1"))
        assertEquals("1024", calc("2^10"))
    }

    @Test
    fun unaryMinusAndPlus() {
        assertEquals("-3", calc("-1-2"))
        assertEquals("1", calc("-(-1)*1"))
        assertEquals("6", calc("+3*2"))
        assertEquals("5", calc("2--3"))
    }

    @Test
    fun unicodeOperatorsAndBanglaDigits() {
        assertEquals("12", calc("3×4"))
        assertEquals("2", calc("8÷4"))
        assertEquals("3", calc("5−2"))
        assertEquals("84", calc("১২*(৩+৪)"))
    }

    @Test
    fun decimals() {
        assertEquals("0.3", calc("0.1+0.2"))
        assertEquals("1", calc(".5+.5"))
        assertEquals("7.5", calc("2.5*3"))
        assertEquals("0.333333333333", calc("1/3"))
        assertEquals("0.666666666667", calc("2/3"))
    }

    @Test
    fun percent() {
        assertEquals("0.5", calc("50%"))
        assertEquals("220", calc("200+10%"))
        assertEquals("180", calc("200-10%"))
        assertEquals("20", calc("200*10%"))
        assertEquals("1", calc("10%3"))
        assertEquals("0.5", calc("(40+10)%"))
    }

    @Test
    fun implicitMultiplicationBeforeParentheses() {
        assertEquals("14", calc("2(3+4)"))
        assertEquals("21", calc("(1+2)(3+4)"))
    }

    @Test
    fun missingClosingParenthesesAtTheEndAreForgiven() {
        assertEquals("84", calc("12*(3+4"))
        assertEquals("9", calc("((1+2)*3"))
    }

    @Test
    fun notACalculation() {
        assertNull(calc(""))
        assertNull(calc("42"))
        assertNull(calc("-5"))
        assertNull(calc("(5)"))
        assertNull(calc("youtube"))
        assertNull(calc("12*"))
        assertNull(calc("*3"))
        assertNull(calc("1..2+1"))
        assertNull(calc("2+3)"))
        assertNull(calc("()"))
        assertNull(calc("1e3+1"))
    }

    @Test
    fun divisionByZeroAndInfinityGiveNoResult() {
        assertNull(calc("1/0"))
        assertNull(calc("5%0"))
        assertNull(calc("0^-1"))
        assertNull(calc("10^400"))
        assertNull(calc("(-8)^0.5"))
    }

    @Test
    fun formatting() {
        assertEquals("84", Calculator.format(84.0))
        assertEquals("100", Calculator.format(100.0))
        assertEquals("-10", Calculator.format(-10.0))
        assertEquals("0", Calculator.format(-0.0))
        assertEquals("1234567", Calculator.format(1234567.0))
        assertEquals("1E+20", Calculator.format(1e20))
        assertEquals("1.5E-10", Calculator.format(1.5e-10))
        assertEquals("0.001", Calculator.format(0.001))
    }
}
