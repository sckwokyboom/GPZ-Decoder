package model

import BinaryPolynomial
import GaloisField
import kotlin.math.abs
import kotlin.math.max

class MultiBinaryPolynomial(
    coefficients: List<BinaryPolynomial>,
) {
    val coefficients: List<BinaryPolynomial> = trimZeros(coefficients)
        get() = field

    companion object {
        private fun trimZeros(coefficients: List<BinaryPolynomial>): List<BinaryPolynomial> {
            if (!coefficients.any { !it.isZero() }) {
                return emptyList()
            }
            val lastIndex = coefficients.indexOfLast { !it.isZero() }
            return if (lastIndex == -1) {
                coefficients
            } else {
                coefficients.subList(0, lastIndex + 1).toList()
            }
        }

        private fun trimZeros(polynomial: MultiBinaryPolynomial): MultiBinaryPolynomial {
            return MultiBinaryPolynomial(
                trimZeros(polynomial.coefficients)
            )
        }
    }

    operator fun plus(other: MultiBinaryPolynomial): MultiBinaryPolynomial {
        val newCoefficients = mutableListOf<BinaryPolynomial>()
        val maxDegree = max(other.degree, this.degree)
        for (i in 0 until maxDegree + 1) {
            val thisCoeff = if (i < this.coefficients.size) this.coefficients[i] else BinaryPolynomial(emptyList())
            val otherCoeff = if (i < other.coefficients.size) other.coefficients[i] else BinaryPolynomial(emptyList())
            newCoefficients.add(thisCoeff + otherCoeff)
        }
        return MultiBinaryPolynomial(newCoefficients)
    }

    operator fun minus(other: MultiBinaryPolynomial): MultiBinaryPolynomial {
        return trimZeros(this.plus(other))
    }

    fun isZero(): Boolean {
        return coefficients.isEmpty()
    }

    operator fun times(other: MultiBinaryPolynomial): MultiBinaryPolynomial {
        val newCoefficients = MutableList(this.degree + other.degree + 1) { BinaryPolynomial(emptyList()) }
        for (i in other.coefficients.indices) {
            for (j in this.coefficients.indices) {
                newCoefficients[i + j] = newCoefficients[i + j] + (this.coefficients[j] * other.coefficients[i])
            }
        }
        return MultiBinaryPolynomial(newCoefficients)
    }

    operator fun rem(other: MultiBinaryPolynomial): MultiBinaryPolynomial {
        return (this / other).remainder
    }

    operator fun div(other: MultiBinaryPolynomial): ResultOfDivisionForMultiPolynomial {
        if (this.degree < other.degree) {
            return ResultOfDivisionForMultiPolynomial(MultiBinaryPolynomial(emptyList()), this)
        }
        if (this.degree == other.degree) {
            return ResultOfDivisionForMultiPolynomial(
                MultiBinaryPolynomial(listOf(BinaryPolynomial(intArrayOf(1)))),
                this - other
            )
        }

        var dividend = MultiBinaryPolynomial(this.coefficients)
        var quotient = MultiBinaryPolynomial(emptyList())
        while (dividend.degree >= other.degree) {
            val termCoefficients = MutableList(abs(dividend.degree - other.degree)) { BinaryPolynomial(emptyList()) }
            termCoefficients.addLast(BinaryPolynomial(intArrayOf(1)))
            val term = MultiBinaryPolynomial(termCoefficients)
            val leadingTerm = term * other
            dividend -= leadingTerm
            quotient += term
        }
        return ResultOfDivisionForMultiPolynomial(trimZeros(quotient), trimZeros(dividend))
    }

    fun pow(degree: Int): MultiBinaryPolynomial {
        if (degree == 0) {
            return MultiBinaryPolynomial(listOf(BinaryPolynomial(intArrayOf(1))))
        }
        if (degree == 1) {
            return MultiBinaryPolynomial(this.coefficients)
        }

        var result = MultiBinaryPolynomial(this.coefficients)
        repeat(degree - 1) {
            result *= this
        }
        return trimZeros(result)
    }

    val degree: Int
        get() = coefficients.size - 1

    override fun toString(): String {
        val builder = StringBuilder()
        if (coefficients.isEmpty()) {
            builder.append("0")
            return builder.toString()
        }
        for (i in coefficients.indices.reversed()) {
            if (!coefficients[i].isZero()) {
                if (i != coefficients.size - 1)
                    builder.append(" + ")
                if (i != 0) {
                    builder.append("y")
                    if (i > 1) {
                        builder.append("^${i}")
                    }
                    if (!coefficients[i].isOne()) {
                        builder.append("(${coefficients[i]})")
                    }
                } else {
                    builder.append(coefficients[i])
                }
            }
        }
        return builder.toString()
    }

    fun castCoefficientsToFieldElements(field: GaloisField): MultiBinaryPolynomial {
        val newCoefficients = ArrayList<BinaryPolynomial>()
        for (coefficientsPolynomial in coefficients) {
            newCoefficients.add(coefficientsPolynomial.castToFieldElements(field))
        }
        return MultiBinaryPolynomial(newCoefficients)
    }

    fun substituteArgument(newArgument: BinaryPolynomial): MultiBinaryPolynomial {
        // TODO: if there is bad performance, you can optimize it
        var newPolynomial = MultiBinaryPolynomial(listOf(coefficients[0]))
        for (i in 1 until coefficients.size) {
            if (!coefficients[i].isZero()) {
                val substitution = newArgument.pow(i)
                val polynomialCoefficients = ArrayList<BinaryPolynomial>()
                for (j in substitution.coefficients.indices) {
                    if (substitution.coefficients[j]) {
                        polynomialCoefficients.add(coefficients[i])
                        continue;
                    }
                    polynomialCoefficients.add(BinaryPolynomial(emptyList()))
                }
                newPolynomial += MultiBinaryPolynomial(polynomialCoefficients)
            }
        }
        return newPolynomial
    }
}