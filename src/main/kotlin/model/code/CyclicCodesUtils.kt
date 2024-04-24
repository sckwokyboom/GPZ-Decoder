package model.code

import model.binarycanonicalconverter.BinaryMatrix
import polynomials.MultiBinaryPolynomial
import java.security.InvalidParameterException

class CyclicCodesUtils {
    companion object {
        fun getGeneratorMatrixFromGeneratorPolynomial(
            generatorPolynomial: MultiBinaryPolynomial,
            codeLength: Int,
        ): BinaryMatrix {
            if (codeLength <= 0) {
                throw InvalidParameterException("Длина кода должна быть положительным числом.")
            }
            if (generatorPolynomial.degree == 0) {
                throw InvalidParameterException("Порождающий многочлен не должен быть вырожденным.")
            }
            if (generatorPolynomial.degree > codeLength) {
                throw InvalidParameterException("Длина кода должна быть больше, чем степень порождающего многочлена. Получено: ${generatorPolynomial.degree} > $codeLength.")
            }
            for (coefficient in generatorPolynomial.coefficients) {
                if (!coefficient.isOne() && !coefficient.isZero()) {
                    throw InvalidParameterException("Порождающий многочлен построен неправильно и содержит неприемлемые коэффициенты.")
                }
            }
            val generatorPolynomialCoefficients = generatorPolynomial.coefficients.map { it.isOne() }.toMutableList()
            val numberOfCyclicShifts = codeLength - generatorPolynomial.degree
            val generatorMatrixSource = ArrayList<Array<Boolean>>()
            for (i in 0 until numberOfCyclicShifts) {
                val generatorMatrixRow = generatorPolynomialCoefficients.toMutableList()
                for (j in 0 until i) {
                    generatorMatrixRow.addFirst(false)
                }
                for (j in i until numberOfCyclicShifts - 1) {
                    generatorMatrixRow.addLast(false)
                }
                generatorMatrixSource.add(generatorMatrixRow.toTypedArray())
            }
            return BinaryMatrix(generatorMatrixSource.toTypedArray())
        }
    }
}