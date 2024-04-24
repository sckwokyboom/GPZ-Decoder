package model

import BinaryPolynomial
import Decoder
import GaloisField

class GPZDecoder : Decoder {

    override fun decode(vector: BinaryVector): BinaryVector {
        val vectorPolynomial = BinaryPolynomial(vector)
        val t = 3
        val vectorPolynomials = ArrayList<BinaryPolynomial>(t)
        for (j in 1 until 2 * t + 1) {
            // TODO: abstract primitive element instead intArrayOf(0, 1)
            val polynomialWithSubstituteArgument =
                vectorPolynomial.substituteArgument(BinaryPolynomial(intArrayOf(0, 1)).pow(j))
            val polInField = polynomialWithSubstituteArgument.castToFieldElements(GaloisField)
            println(polInField)
            vectorPolynomials.add(
                polInField
            )
        }
        println(vectorPolynomials)
        var countOfErrors = 0
        var dMatrix: BinaryPolynomialMatrix
        for (i in t downTo 1) {
            dMatrix = createDMatrix(vectorPolynomials, i)
            if (!dMatrix.determinant().castToFieldElements(GaloisField).isZero()) {
                countOfErrors = i
                break
            }
        }
        if (countOfErrors == 0) {
            return vector
        }
        val system = createSystemForLocatorCoefficients(vectorPolynomials, countOfErrors)
        val solutions = system.solveSystem()
//        solutions.addFirst(BinaryPolynomial(intArrayOf(1)))
        val polynomialLocator = MultiBinaryPolynomial(solutions)
        val roots = ArrayList<BinaryPolynomial>()
        for (entry in GaloisField.primitiveElementDegreeAndFieldElement) {
            if (polynomialLocator
                    .substituteArgument(entry.value)
                    .castCoefficientsToFieldElements(GaloisField)
                    .isZero()
            ) {
                println("Root: ${entry.value}")
                roots.add(entry.value)
            }
        }
        if (roots.isEmpty()) {
            println("Декодирование невозможно. Произошло слишком много ошибок.")
        }
        return BinaryVector(0)
    }

    private fun createDMatrix(polynomials: ArrayList<BinaryPolynomial>, matrixSize: Int): BinaryPolynomialMatrix {
        val matrixRows = ArrayList<Array<BinaryPolynomial>>()
        for (i in matrixSize downTo 1) {
            val row = ArrayList<BinaryPolynomial>()
            for (j in 0 until matrixSize) {
                row.add(polynomials[i + j])
            }
            matrixRows.add(row.toTypedArray())
        }
        return BinaryPolynomialMatrix(matrixRows.toTypedArray())
    }

    private fun createSystemForLocatorCoefficients(
        polynomials: ArrayList<BinaryPolynomial>,
        matrixHeight: Int,
    ): BinaryPolynomialMatrix {
        val matrixRows = ArrayList<Array<BinaryPolynomial>>()
        for (i in matrixHeight - 1 until 2 * matrixHeight - 1) {
            val row = ArrayList<BinaryPolynomial>()
            for (j in 0 until matrixHeight) {
                row.add(polynomials[i - j])
            }
            row.add(polynomials[i + 1])
            matrixRows.add(row.toTypedArray())
        }
        return BinaryPolynomialMatrix(matrixRows.toTypedArray())
    }
}