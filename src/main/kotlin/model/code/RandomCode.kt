package model.code

import allBinaryVectorPermutations
import model.canonicalconverter.CanonicalConverter
import model.canonicalconverter.Matrix
import java.math.BigInteger

class RandomCode(override val length: Int) : Code() {
    override val dimension: Int = (0.9 * length).toInt()
    override val parityCheckLength: Int = length - dimension
    override val parityCheckMatrix: Matrix = generateParityCheckMatrix()
    override val generatorMatrix: Matrix = CanonicalConverter().toGenerative(parityCheckMatrix)

    private fun generateParityCheckMatrix(): Matrix {
        try {
            val parityCheckMatrix = Matrix(parityCheckLength, length)
            val permutations = allBinaryVectorPermutations(5, parityCheckLength)
            val chosenPermutations = ArrayList<BigInteger>(length)
            for (i in 0 until length) {
                chosenPermutations.add(permutations.shuffled().take(1).elementAt(0))
            }
            for (i in 0 until length) {
                for (j in 0 until parityCheckLength) {
                    parityCheckMatrix[j, i] =
                        ((chosenPermutations[i] and (BigInteger.ONE shl j)) != BigInteger.ZERO).compareTo(false)
                }
            }
            return parityCheckMatrix
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Некорректная длина кода. Для данного кода требуется длина как минимум 60.")
        }
    }
}