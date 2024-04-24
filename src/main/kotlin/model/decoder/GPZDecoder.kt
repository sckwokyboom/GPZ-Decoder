package model.decoder

import model.BinaryVector
import model.code.BCHCode
import polynomials.BinaryPolynomial
import polynomials.BinaryPolynomialMatrix
import polynomials.MultiBinaryPolynomial

class GPZDecoder(private val bchCode: BCHCode) : Decoder {
    override fun decode(vector: BinaryVector): BinaryVector? {
        val vectorPolynomial = BinaryPolynomial(vector)
        val countOfErrorsLimit: Int = (bchCode.delta - 1) / 2
        val vectorPolynomials = ArrayList<BinaryPolynomial>(countOfErrorsLimit)
        for (j in 1 until 2 * countOfErrorsLimit + 1) {
            // TODO: abstract primitive element instead intArrayOf(0, 1)
            val polynomialWithSubstituteArgument =
                vectorPolynomial.substituteArgument(BinaryPolynomial.X.pow(j))
            val polInField = polynomialWithSubstituteArgument.castToFieldElements(bchCode.gf)
            vectorPolynomials.add(
                polInField
            )
        }

        var countOfErrors = 0
        var dMatrix: BinaryPolynomialMatrix
        for (i in countOfErrorsLimit downTo 1) {
            dMatrix = createDMatrix(vectorPolynomials, i)
            if (!dMatrix.determinant().castToFieldElements(bchCode.gf).isZero()) {
                countOfErrors = i
                break
            }
        }
        if (countOfErrors == 0) {
            println("Полученный вектор не содержит ошибок.")
            return vector
        }
        val system = createSystemForLocatorCoefficients(vectorPolynomials, countOfErrors)
        val solutions = system.solveSystem()
        solutions.addFirst(BinaryPolynomial(intArrayOf(1)))
        val polynomialLocator = MultiBinaryPolynomial(solutions)
        chienSearchErrorsIndices(polynomialLocator)
        val errorsIndices1 = bruteForceSearchErrorsIndices(polynomialLocator)
        val errorsIndices2 = chienSearchErrorsIndices(polynomialLocator)
        val errorsIndices3 = hornerSchemeSearchErrorsIndices(polynomialLocator)
        if (errorsIndices1.isEmpty() || errorsIndices2.isEmpty() || errorsIndices3.isEmpty()) {
            return null
        }
        println("Индексы ошибок полным перебором: $errorsIndices1.")
        println("Индексы ошибок методом поиска Чиена: $errorsIndices2.")
        println("Индексы ошибок схемой Горнера: $errorsIndices3.")
        return vector + createVectorOfErrors(errorsIndices1)
    }


    private fun createVectorOfErrors(errorsIndices: List<Int>): BinaryVector {
        val result = BinaryVector(bchCode.length)
        for (i in 0 until bchCode.length) {
            if (errorsIndices.contains(i)) {
                result[i] = result[i] xor true
            }
        }
        return result
    }

    private fun createDMatrix(polynomials: ArrayList<BinaryPolynomial>, matrixSize: Int): BinaryPolynomialMatrix {
        val matrixRows = ArrayList<Array<BinaryPolynomial>>()
        for (i in matrixSize downTo 1) {
            val row = ArrayList<BinaryPolynomial>()
            for (j in 0 until matrixSize) {
                row.add(polynomials[i + j - 1])
            }
            matrixRows.add(row.toTypedArray())
        }
        return BinaryPolynomialMatrix(matrixRows.toTypedArray(), bchCode.gf)
    }

    private fun bruteForceSearchErrorsIndices(polynomialLocator: MultiBinaryPolynomial): List<Int> {
        val roots = ArrayList<BinaryPolynomial>()
        for (entry in bchCode.gf.primitiveElementDegreeAndFieldElement) {
            if (polynomialLocator
                    .substituteArgumentAndCast(entry.value)
                    .castToFieldElements(bchCode.gf)
                    .isZero()
            ) {
                roots.add(entry.value)
            }
        }
        val errorsIndices = ArrayList<Int>()
        for (root in roots) {
            val degreeOfPrimitiveElement = bchCode.gf.findDegreeByFieldElement(root)
            if (degreeOfPrimitiveElement != 0) {
                errorsIndices.add(bchCode.gf.fieldSize - degreeOfPrimitiveElement)
            } else {
                errorsIndices.add(0)
            }
        }
        return errorsIndices
    }

    private fun chienSearchErrorsIndices(polynomialLocator: MultiBinaryPolynomial): List<Int> {
        val errorsIndices = ArrayList<Int>(polynomialLocator.degree)
        var i = 0
        val qList = ArrayList<BinaryPolynomial>(polynomialLocator.degree + 1)
        for (j in 0 until polynomialLocator.degree + 1) {
            qList.add(BinaryPolynomial(intArrayOf(1)))
        }
        var sum: BinaryPolynomial
        while (i < bchCode.gf.fieldSize) {
            sum = qList[0]
            for (j in 1 until polynomialLocator.degree + 1) {
                sum += polynomialLocator.coefficients[j] * qList[j]
            }
            if (sum.castToFieldElements(bchCode.gf).isZero()) {
                if (i != 0) {
                    errorsIndices.add(bchCode.gf.fieldSize - i)
                } else {
                    errorsIndices.add(0)
                }

            }
            for (j in 1 until polynomialLocator.degree + 1) {
                qList[j] *= polynomials.BinaryPolynomial(intArrayOf(0, 1)).pow(j).castToFieldElements(bchCode.gf)
            }
            i++
        }
        return errorsIndices
    }

    private fun hornerSchemeSearchErrorsIndices(polynomialLocator: MultiBinaryPolynomial): List<Int> {
        if (polynomialLocator.coefficients.isEmpty()) {
            throw IllegalArgumentException("Нахождение корней схемой Горнера невозможно.")
        }
        val roots = ArrayList<BinaryPolynomial>()
        for (entry in bchCode.gf.primitiveElementDegreeAndFieldElement) {
            var eval = polynomialLocator.coefficients.reversed()[0]
            for (i in 1 until polynomialLocator.coefficients.size) {
                eval = eval * entry.value + polynomialLocator.coefficients.reversed()[i]
            }
            if (eval.castToFieldElements(bchCode.gf).isZero()) {
                roots.add(entry.value)
            }
        }
        val errorsIndices = ArrayList<Int>()
        for (root in roots) {
            val degreeOfPrimitiveElement = bchCode.gf.findDegreeByFieldElement(root)
            if (degreeOfPrimitiveElement != 0) {
                errorsIndices.add(bchCode.gf.fieldSize - degreeOfPrimitiveElement)
            } else {
                errorsIndices.add(0)
            }
        }
        return errorsIndices
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
        return BinaryPolynomialMatrix(matrixRows.toTypedArray(), bchCode.gf)
    }
}