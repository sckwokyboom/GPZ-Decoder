package model.code

import polynomials.BinaryPolynomial
import model.field.GaloisFieldMod2
import model.*
import model.binarycanonicalconverter.BinaryCanonicalConverter
import model.binarycanonicalconverter.BinaryMatrix
import model.cyclotomicclasses.CyclotomicClass
import model.cyclotomicclasses.CyclotomicClassesFinder
import polynomials.MultiBinaryPolynomial
import java.util.*
import kotlin.math.pow

class BCHCode(
    val length: Int,
    val b: Int,
    val delta: Int,
    private val irreduciblePolynomial: BinaryPolynomial,
    private val primitiveElement: BinaryPolynomial,
) {
    val parityCheckLength: Int
    val dimension: Int
    val generatorMatrix: BinaryMatrix
    val parityCheckMatrix: BinaryMatrix
    val gf = GaloisFieldMod2(irreduciblePolynomial, primitiveElement, length)

    private var generativePolynomial: MultiBinaryPolynomial = MultiBinaryPolynomial(listOf(BinaryPolynomial.ONE))

    init {
        val allCyclotomicClasses = CyclotomicClassesFinder.findAllCyclotomicClasses(length, 2)
        val uniqueCyclotomicClasses = ArrayList<CyclotomicClass>()
        for (entry in allCyclotomicClasses) {
            if (b <= entry.key && entry.key <= b + delta - 2) {
                if (!uniqueCyclotomicClasses.contains(entry.value)) {
                    uniqueCyclotomicClasses.add(entry.value)
                }
            }
        }
        val minPolynomials = ArrayList<MultiBinaryPolynomial>()
        for (cyclotomicClass in uniqueCyclotomicClasses) {
            var minPolynomial = MultiBinaryPolynomial(
                listOf(BinaryPolynomial(intArrayOf(1)))
            )
            for (representative in cyclotomicClass.representatives) {
                minPolynomial *=
                    MultiBinaryPolynomial(
                        listOf(
                            BinaryPolynomial(intArrayOf(0, 1)).pow(representative),
                            BinaryPolynomial(intArrayOf(1))
                        )
                    )
            }
            minPolynomials.add(minPolynomial.castCoefficientsToFieldElements(gf))
        }

        for (minPolynomial in minPolynomials) {
            generativePolynomial *= minPolynomial
        }
        generativePolynomial = generativePolynomial.castCoefficientsToFieldElements(gf)
        generatorMatrix = CyclicCodesUtils.getGeneratorMatrixFromGeneratorPolynomial(generativePolynomial, length)
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