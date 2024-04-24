package model.code

import BinaryPolynomial
import GaloisField
import model.BinaryVector
import model.CyclicCodesUtils
import model.CyclotomicClassesFinder
import model.MultiBinaryPolynomial
import model.binarycanonicalconverter.BinaryCanonicalConverter
import model.binarycanonicalconverter.BinaryMatrix
import java.util.*
import kotlin.math.pow

class BCHCode(
    val length: Int,
) {
    val parityCheckLength: Int
    val dimension: Int
    val generatorMatrix: BinaryMatrix
    val parityCheckMatrix: BinaryMatrix

    private val generativePolynomial: MultiBinaryPolynomial

    init {
        val cyclotomicClasses = CyclotomicClassesFinder.findAllCyclotomicClasses(511, 2)
        val cyclotomicClass1 = cyclotomicClasses.getValue(1)
        val cyclotomicClass3 = cyclotomicClasses.getValue(3)
        val cyclotomicClass5 = cyclotomicClasses.getValue(5)
        var minPolynomial1 = MultiBinaryPolynomial(
            listOf(BinaryPolynomial(intArrayOf(1)))
        )
        for (representative in cyclotomicClass1.representatives) {
            minPolynomial1 *=
                MultiBinaryPolynomial(
                    listOf(
                        BinaryPolynomial(intArrayOf(0, 1)).pow(representative),
                        BinaryPolynomial(intArrayOf(1))
                    )
                )
        }
        var minPolynomial3 = MultiBinaryPolynomial(
            listOf(BinaryPolynomial(intArrayOf(1)))
        )
        for (representative in cyclotomicClass3.representatives) {
            minPolynomial3 *=
                MultiBinaryPolynomial(
                    listOf(
                        BinaryPolynomial(intArrayOf(0, 1)).pow(representative),
                        BinaryPolynomial(intArrayOf(1))
                    )
                )
        }
        var minPolynomial5 = MultiBinaryPolynomial(
            listOf(BinaryPolynomial(intArrayOf(1)))
        )
        for (representative in cyclotomicClass5.representatives) {
            minPolynomial5 *=
                MultiBinaryPolynomial(
                    listOf(
                        BinaryPolynomial(intArrayOf(0, 1)).pow(representative),
                        BinaryPolynomial(intArrayOf(1))
                    )
                )
        }
        val gf = GaloisField
        val minPolyInField1 = minPolynomial1.castCoefficientsToFieldElements(gf)
        val minPolyInField3 = minPolynomial3.castCoefficientsToFieldElements(gf)
        val minPolyInField5 = minPolynomial5.castCoefficientsToFieldElements(gf)
        generativePolynomial =
            minPolyInField1 *
                    minPolyInField3 *
                    minPolyInField5
        println(generativePolynomial.castCoefficientsToFieldElements(gf))
        generatorMatrix = CyclicCodesUtils.getGeneratorMatrixFromGeneratorPolynomial(generativePolynomial, 511)
        parityCheckMatrix = BinaryCanonicalConverter().toParityCheck(generatorMatrix)
        parityCheckLength = parityCheckMatrix.rows
        dimension = generatorMatrix.rows
    }

    fun getRandomCodeword(): BinaryVector {
        val countOfCodewords = (2.0.pow(dimension) - 1).toInt()
        val randomCoefficients = Random().nextInt(countOfCodewords)

        val basicVectors = BitSet(dimension)
        for (j in 0 until dimension) {
            if ((randomCoefficients and (1 shl j)) != 0) {
                basicVectors.set(j)
            }
        }

        var codeWord = BinaryVector(length)
        basicVectors.stream().forEach { ind ->
            run {
                codeWord += BinaryVector(generatorMatrix.row(ind))
            }
        }
        return codeWord
    }

}