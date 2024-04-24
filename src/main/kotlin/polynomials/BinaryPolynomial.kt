package polynomials

import model.BinaryVector
import model.field.GaloisFieldMod2
import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.max

open class BinaryPolynomial {

    val coefficients: List<Boolean>
        get() {
            return field
        }

    constructor(coefficients: List<Boolean>) {
        this.coefficients = trimZeros(coefficients)
    }

    constructor(coefficients: BooleanArray) : this(coefficients.toList())

    //    constructor(coefficients: List<Int>) : this(coefficients.map { it != 0 }.toList())
    constructor(coefficients: IntArray) : this(coefficients.map { it != 0 }.toList())

    constructor(vector: BinaryVector) : this(vector.toBooleanArray())

    companion object {
        val ZERO: BinaryPolynomial = BinaryPolynomial(emptyList())
        val ONE: BinaryPolynomial = BinaryPolynomial(intArrayOf(1))
        val X: BinaryPolynomial = BinaryPolynomial(intArrayOf(0, 1))

        private fun trimZeros(coefficients: List<Boolean>): List<Boolean> {
            if (!coefficients.contains(true)) {
                return emptyList()
            }
            val lastIndex = coefficients.indexOfLast { it }
            return when (lastIndex) {
                -1 -> {
                    emptyList()
                }

                else -> {
                    coefficients.subList(0, lastIndex + 1).toList()
                }
            }
        }

        private fun trimZeros(polynomial: BinaryPolynomial): BinaryPolynomial {
            return BinaryPolynomial(trimZeros(polynomial.coefficients))
        }
    }

    operator fun plus(other: BinaryPolynomial): BinaryPolynomial {
        val newCoefficients = mutableListOf<Boolean>()
        val maxDegree = max(other.degree, this.degree)
        for (i in 0 until maxDegree + 1) {
            val thisCoeff = if (i < this.coefficients.size) this.coefficients[i] else false
            val otherCoeff = if (i < other.coefficients.size) other.coefficients[i] else false
            newCoefficients.add(thisCoeff xor otherCoeff)
        }
        return BinaryPolynomial(newCoefficients)
    }

    operator fun minus(other: BinaryPolynomial): BinaryPolynomial {
        return trimZeros(this.plus(other))
    }

    operator fun times(other: BinaryPolynomial): BinaryPolynomial {
        if (other.isZero() || this.isZero()) {
            return BinaryPolynomial(emptyList())
        }
        if (other.isOne()) {
            return BinaryPolynomial(this.coefficients)
        }
        if (this.isOne()) {
            return BinaryPolynomial(other.coefficients)
        }

        val newCoefficients = MutableList(this.degree + other.degree + 1) { false }
        for (i in other.coefficients.indices) {
            for (j in this.coefficients.indices) {
                newCoefficients[i + j] = newCoefficients[i + j] xor (this.coefficients[j] and other.coefficients[i])
            }
        }
        return BinaryPolynomial(newCoefficients)
    }

    operator fun rem(other: BinaryPolynomial): BinaryPolynomial {
        return (this / other).remainder
    }

    operator fun div(other: BinaryPolynomial): ResultOfDivision {
        if (other.isZero()) {
            throw IllegalArgumentException("Деление на 0 невозможно.")
        }
        if (this.degree < other.degree) {
            return ResultOfDivision(BinaryPolynomial(emptyList()), this)
        }
        if (this.degree == other.degree) {
            return ResultOfDivision(BinaryPolynomial(booleanArrayOf(true)), this - other)
        }

        var dividend = BinaryPolynomial(this.coefficients)
        var quotient = BinaryPolynomial(emptyList())

        // TODO: think about degree = 0
        while (dividend.degree >= other.degree) {
            val termCoefficients = MutableList(abs(dividend.degree - other.degree)) { false }
            termCoefficients.addLast(true)
            val term = BinaryPolynomial(termCoefficients)
            val leadingTerm = term * other
            dividend -= leadingTerm
            quotient += term
        }
        return ResultOfDivision(trimZeros(quotient), trimZeros(dividend))
    }

    fun pow(degree: Int): BinaryPolynomial {
        if (degree == 0) {
            return BinaryPolynomial(intArrayOf(1))
        }
        if (degree == 1) {
            return BinaryPolynomial(this.coefficients)
        }

        var result = BinaryPolynomial(this.coefficients)
        repeat(degree - 1) {
            result *= this
        }
        return trimZeros(result)
    }

    val degree: Int
        get() {
            if (coefficients.isEmpty()) {
                return 0
            }
            return coefficients.size - 1
        }

    fun isZero(): Boolean {
        return coefficients.isEmpty()
    }

    fun isOne(): Boolean {
        return coefficients.isNotEmpty() && (degree == 0) && coefficients[0]
    }

    override fun toString(): String {
        val builder = StringBuilder()
        if (coefficients.isEmpty()) {
            builder.append("0")
            return builder.toString()
        }
        for (i in coefficients.indices.reversed()) {
            if (coefficients[i]) {
                if (i != coefficients.size - 1)
                    builder.append(" + ")
                if (i != 0) {
                    builder.append("x")
                    if (i > 1) {
                        builder.append("^${i}")
                    }
                } else {
                    builder.append("1")
                }
            }
        }
        return builder.toString()
    }


    // TODO: bad semantic
    fun castToFieldElements(field: GaloisFieldMod2): BinaryPolynomial {
        var newCoefficientsTakenModulo = coefficients.toMutableList()
        for (i in newCoefficientsTakenModulo.indices.reversed()) {
            val newIndex = i % (field.fieldSize)
            if ((newIndex != i) and newCoefficientsTakenModulo[i]) {
                newCoefficientsTakenModulo[i] = false
                newCoefficientsTakenModulo[newIndex] =
                    newCoefficientsTakenModulo[newIndex] xor true
            } else if (newIndex == i) {
                break
            }
        }
        newCoefficientsTakenModulo = trimZeros(newCoefficientsTakenModulo.toMutableList()).toMutableList()
        var polynomialInField = BinaryPolynomial(emptyList())
        for (i in newCoefficientsTakenModulo.indices) {
            if (newCoefficientsTakenModulo[i])
                polynomialInField += field[i]
        }
        return polynomialInField
    }

    fun isIrreducible(): Boolean {
        var i = BigInteger.TWO
        val limit = BinaryVector(BigInteger.TWO.pow(degree + 1), degree + 2).toBigInteger()
        while (i < limit) {
            val div = BinaryPolynomial(BinaryVector(i, degree + 1).toBooleanArray())
            if (this % div == ZERO && this != div) {
                return false
            }
            i++
        }
        return true
    }

    fun substituteArgument(newArgument: BinaryPolynomial): BinaryPolynomial {
        var newPolynomial = BinaryPolynomial(booleanArrayOf(coefficients[0]))
        for (i in 1 until coefficients.size) {
            if (coefficients[i]) {
                // TODO: if there is bad performance, you can optimize it
                newPolynomial += newArgument.pow(i)
            }
        }
        return newPolynomial
    }

    fun toBinaryVector(): BinaryVector {
        return BinaryVector(coefficients.toTypedArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BinaryPolynomial) return false
        return trimZeros(this.coefficients) == trimZeros(other.coefficients)
    }

    override fun hashCode(): Int {
        return coefficients.hashCode()
    }
}