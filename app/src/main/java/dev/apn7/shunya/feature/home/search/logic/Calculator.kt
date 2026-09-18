package dev.apn7.shunya.feature.home.search.logic

import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs
import kotlin.math.pow

/**
 * The inline calculator of search (PRD 3.2): `12*(3+4)` → "84".
 *
 * Grammar (recursive descent, usual precedence):
 * ```
 * expression := term (('+' | '-') term)*
 * term       := unary (('*' | '/' | '%') unary | unary-in-parentheses)*   "2(3+4)" multiplies
 * unary      := ('+' | '-') unary | power                                  "-2^2" = -4
 * power      := percent ('^' unary)?                                       right-assoc: 2^3^2 = 512
 * percent    := primary '%'?
 * primary    := number | '(' expression ')'                                a missing ')' at the end is fine
 * ```
 * `%` right after a number is percent ("50%" = 0.5, and like phone calculators "200+10%" = 220);
 * between two numbers it is the remainder ("10%3" = 1). Accepts × ÷ − and Bangla digits.
 * Input that is not a complete calculation (no operator, a trailing operator, letters, division
 * by zero, an infinite result) gives no result, so search simply shows no calculator row.
 */
object Calculator {

    /** The formatted result of [input], or null when it is not a complete calculation. */
    fun calculate(input: String): String? = evaluate(input)?.let { format(it) }

    /** The value of [input], or null (see the class comment). */
    fun evaluate(input: String): Double? {
        val tokens = tokenize(input) ?: return null
        if (tokens.isEmpty()) return null
        return try {
            val parser = Parser(tokens)
            val value = parser.parseAll()
            if (parser.operations == 0 || !value.isFinite()) null else value
        } catch (e: CalculationException) {
            null
        }
    }

    /**
     * Friendly number text: no ".0" on whole numbers, at most 12 significant digits (so
     * 0.1+0.2 shows "0.3"), scientific notation only for huge or tiny values.
     */
    fun format(value: Double): String {
        if (value == 0.0 || !value.isFinite()) return "0"
        val rounded = BigDecimal(value).round(MathContext(SIGNIFICANT_DIGITS)).stripTrailingZeros()
        val magnitude = abs(value)
        return if (magnitude >= 1e15 || magnitude < 1e-9) rounded.toString() else rounded.toPlainString()
    }

    private const val SIGNIFICANT_DIGITS = 12
    private const val BANGLA_ZERO = '০'
    private const val BANGLA_NINE = '৯'

    private fun tokenize(input: String): List<Token>? {
        val tokens = ArrayList<Token>()
        var i = 0
        while (i < input.length) {
            val c = normalize(input[i])
            when {
                c.isWhitespace() -> i++
                c.isAsciiDigit() || c == '.' -> {
                    val number = StringBuilder()
                    while (i < input.length) {
                        val d = normalize(input[i])
                        if (!d.isAsciiDigit() && d != '.') break
                        number.append(d)
                        i++
                    }
                    tokens.add(Token.Number(number.toString().toDoubleOrNull() ?: return null))
                }
                c in SYMBOLS -> {
                    tokens.add(Token.Symbol(c))
                    i++
                }
                else -> return null
            }
        }
        return tokens
    }

    private fun normalize(c: Char): Char = when (c) {
        '×', '·' -> '*'
        '÷', '∕' -> '/'
        '−', '–' -> '-'
        in BANGLA_ZERO..BANGLA_NINE -> '0' + (c - BANGLA_ZERO)
        else -> c
    }

    private fun Char.isAsciiDigit(): Boolean = this in '0'..'9'

    private const val SYMBOLS = "+-*/^%()"

    private sealed interface Token {
        data class Number(val value: Double) : Token
        data class Symbol(val char: Char) : Token
    }

    /** A value and whether it was written as a percentage (for "200+10%"). */
    private class Operand(val value: Double, val isPercent: Boolean = false)

    private class CalculationException : Exception()

    private class Parser(private val tokens: List<Token>) {
        private var pos = 0

        /** Binary operators and percent signs applied; 0 means the input was just a number. */
        var operations = 0
            private set

        fun parseAll(): Double {
            val result = expression()
            if (pos != tokens.size) throw CalculationException()
            return result.value
        }

        private fun expression(): Operand {
            var result = term()
            while (true) {
                val op = peekSymbol()
                if (op != '+' && op != '-') return result
                pos++
                operations++
                val right = term()
                val amount = if (right.isPercent) result.value * right.value else right.value
                result = Operand(if (op == '+') result.value + amount else result.value - amount)
            }
        }

        private fun term(): Operand {
            var result = unary()
            while (true) {
                val op = peekSymbol()
                if (op == '*' || op == '/' || op == '%') {
                    pos++
                    operations++
                    result = Operand(apply(result.value, op, unary().value))
                } else if (op == '(') {
                    operations++
                    result = Operand(result.value * unary().value)
                } else {
                    return result
                }
            }
        }

        private fun unary(): Operand {
            val op = peekSymbol()
            if (op != '-' && op != '+') return power()
            pos++
            val inner = unary()
            return if (op == '-') Operand(-inner.value, inner.isPercent) else inner
        }

        private fun power(): Operand {
            val base = percent()
            if (peekSymbol() != '^') return base
            pos++
            operations++
            return Operand(base.value.pow(unary().value))
        }

        private fun percent(): Operand {
            val value = primary()
            if (peekSymbol() == '%' && !isOperandAt(pos + 1)) {
                pos++
                operations++
                return Operand(value / 100.0, isPercent = true)
            }
            return Operand(value)
        }

        private fun primary(): Double {
            val token = tokens.getOrNull(pos) ?: throw CalculationException()
            if (token is Token.Number) {
                pos++
                return token.value
            }
            if (token is Token.Symbol && token.char == '(') {
                pos++
                val inner = expression()
                if (pos < tokens.size) {
                    if (peekSymbol() != ')') throw CalculationException()
                    pos++
                }
                return inner.value
            }
            throw CalculationException()
        }

        private fun apply(left: Double, op: Char, right: Double): Double {
            if (op == '*') return left * right
            if (right == 0.0) throw CalculationException()
            return if (op == '/') left / right else left % right
        }

        private fun isOperandAt(index: Int): Boolean {
            val token = tokens.getOrNull(index)
            return token is Token.Number || (token is Token.Symbol && token.char == '(')
        }

        private fun peekSymbol(): Char? = (tokens.getOrNull(pos) as? Token.Symbol)?.char
    }
}
